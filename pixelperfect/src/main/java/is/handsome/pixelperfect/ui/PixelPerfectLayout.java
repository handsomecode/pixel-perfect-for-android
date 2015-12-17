package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Calendar;

import is.handsome.pixelperfect.PixelPerfectCallbacks;
import is.handsome.pixelperfect.PixelPerfectConfig;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class PixelPerfectLayout extends FrameLayout {

    private static final int LONG_CLICK_DURATION = 500;

    public enum MoveMode {
        VERTICAL, HORIZONTAL, ALL_DIRECTIONS
    }

    private ImageView pixelPerfectOverlayImageView;
    private PixelPerfectControlsFrameLayout pixelPerfectControlsFrameLayout;
    private PixelPerfectCallbacks.LayoutListener layoutListener;
    private FrameLayout magnifierFrameLayout;
    private MagnifierView magnifierView;
    private MoveMode moveMode = MoveMode.ALL_DIRECTIONS;
    private boolean pixelPerfectContext = true;

    private MotionEvent lastMotionEvent;
    private int touchSlop;
    private boolean justClick;
    private boolean wasClick;
    private boolean longClickActive;
    private long startClickTime;

    boolean wasMagnifierClick;

    public PixelPerfectLayout(Context context) {
        super(context);
        init();
    }

    public PixelPerfectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelPerfectLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PixelPerfectLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setLayoutListener(PixelPerfectCallbacks.LayoutListener listener) {
        layoutListener = listener;
    }

    private void initOverlay() {
        pixelPerfectOverlayImageView = new PixelPerfectModelImageView(getContext());
        pixelPerfectOverlayImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        pixelPerfectOverlayImageView.setAdjustViewBounds(true);
        pixelPerfectOverlayImageView.setVisibility(INVISIBLE);
        pixelPerfectOverlayImageView.setAlpha(0.5f);
        addView(pixelPerfectOverlayImageView);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int newIndex = -1;
        if (child != pixelPerfectOverlayImageView && child != pixelPerfectControlsFrameLayout) {
            newIndex = index == -1 ? getChildCount() - 2 : index;
        }
        super.addView(child, newIndex, params);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if (pixelPerfectControlsFrameLayout.inBounds(x, y) || !pixelPerfectContext || magnifierFrameLayout.getVisibility() == VISIBLE) {
            return false;
        } else {
            return pixelPerfectOverlayImageView.getVisibility() == VISIBLE || super.onInterceptTouchEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pixelPerfectOverlayImageView.getVisibility() == VISIBLE && pixelPerfectContext) {
            return handleTouch(event);
        }
        wasClick = false;
        justClick = false;
        return false;
    }

    private boolean handleTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            wasClick = true;
            justClick = true;
            if (!longClickActive) {
                longClickActive = true;
                startClickTime = Calendar.getInstance().getTimeInMillis();
            }
            lastMotionEvent = MotionEvent.obtain(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && wasClick && magnifierFrameLayout.getVisibility() != VISIBLE) {
            if (justClick
                    && Math.abs(event.getY() - lastMotionEvent.getY()) < touchSlop
                    && Math.abs(event.getX() - lastMotionEvent.getX()) < touchSlop) {
                if (longClickActive) {
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    if (clickDuration >= LONG_CLICK_DURATION) {
                        longClickActive = false;
                        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(40);
                        wasMagnifierClick = true;
                        lastMotionEventX = MotionEvent.obtain(event);
                        lastMotionEventY = MotionEvent.obtain(event);
                        showMagnifierMode((int) event.getX(), (int) event.getY());
                    }
                }
                return true;
            }
            justClick = false;
            longClickActive = false;
            if (moveMode != MoveMode.VERTICAL) {
                pixelPerfectOverlayImageView.setTranslationX(pixelPerfectOverlayImageView.getTranslationX() + (event.getX() - lastMotionEvent.getX()));
            }
            if (moveMode != MoveMode.HORIZONTAL) {
                pixelPerfectOverlayImageView.setTranslationY(pixelPerfectOverlayImageView.getTranslationY() + (event.getY() - lastMotionEvent.getY()));
            }
            lastMotionEvent = MotionEvent.obtain(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && magnifierFrameLayout.getVisibility() == VISIBLE) {
            return handleMagnifierMove(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP && justClick) {
            wasClick = false;
            justClick = false;
            longClickActive = false;
            if (lastMotionEvent.getX() < getWidth() / 5 || lastMotionEvent.getX() > getWidth() * 4 / 5) {
                pixelPerfectOverlayImageView.setTranslationX(pixelPerfectOverlayImageView.getTranslationX()
                        + (lastMotionEvent.getX() < getWidth() / 5 ? -1 : 1));
            } else {
                pixelPerfectOverlayImageView.setTranslationY(pixelPerfectOverlayImageView.getTranslationY()
                        + (lastMotionEvent.getY() > getHeight() / 2 ? 1 : -1));
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            wasClick = false;
            justClick = false;
            longClickActive = false;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            wasClick = false;
            justClick = false;
            longClickActive = false;
            return true;
        }
        wasClick = false;
        justClick = false;
        longClickActive = false;
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
            layoutListener.onClosePixelPerfect();
        }

        if (PixelPerfectConfig.get().useVolumeButtons()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (action == KeyEvent.ACTION_DOWN) {
                        pixelPerfectOverlayImageView.setAlpha(Math.min(1, pixelPerfectOverlayImageView.getAlpha() + 0.05f));
                        pixelPerfectControlsFrameLayout.updateOpacityProgress(pixelPerfectOverlayImageView.getAlpha());
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (action == KeyEvent.ACTION_DOWN) {
                        pixelPerfectOverlayImageView.setAlpha(Math.max(0, pixelPerfectOverlayImageView.getAlpha() - 0.05f));
                        pixelPerfectControlsFrameLayout.updateOpacityProgress(pixelPerfectOverlayImageView.getAlpha());
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setImageVisible(boolean visible) {
        if (visible && pixelPerfectOverlayImageView.getDrawable() == null) {
            updateImage(null);
        }
        pixelPerfectOverlayImageView.setVisibility(visible ? VISIBLE : GONE);
        pixelPerfectControlsFrameLayout.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setControlsLayerVisible(boolean visible) {
        pixelPerfectControlsFrameLayout.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setImageAlpha(float alpha) {
        pixelPerfectOverlayImageView.setAlpha(alpha);
    }

    public void updateImage(String fullName) {
        pixelPerfectOverlayImageView.setTranslationY(0);
        if (TextUtils.isEmpty(fullName)) {
            pixelPerfectOverlayImageView.setImageDrawable(null);
        } else {
            pixelPerfectOverlayImageView.setImageBitmap(PixelPerfectUtils.getBitmapFromAssets(getContext(), fullName));
        }
    }

    public void setPixelPerfectContext(boolean isPixelPerfectContext) {
        pixelPerfectContext = isPixelPerfectContext;
    }

    public boolean isPixelPerfectContext() {
        return pixelPerfectContext;
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        initOverlay();

        pixelPerfectControlsFrameLayout = new PixelPerfectControlsFrameLayout(getContext());
        addView(pixelPerfectControlsFrameLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        pixelPerfectControlsFrameLayout.setControlsListener(new PixelPerfectCallbacks.ControlsListener() {

            @Override
            public void onSetImageAlpha(float alpha) {
                setImageAlpha(alpha);
            }

            @Override
            public void onUpdateImage(String fullName) {
                updateImage(fullName);
            }

            @Override
            public void onCloseActionsView() {
                if (layoutListener != null) {
                    layoutListener.onCloseActionsView();
                }
            }

            @Override
            public void onChangeMoveMode(MoveMode changedMoveMode) {
                moveMode = changedMoveMode;
            }
        });

        magnifierFrameLayout = (FrameLayout) pixelPerfectControlsFrameLayout.findViewById(R.id.controls_magnifier_frame_layout);
        magnifierFrameLayout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (PixelPerfectUtils.inViewBounds(magnifierView, x, y)) {
                        wasMagnifierClick = true;
                        lastMotionEventX = MotionEvent.obtain(event);
                        lastMotionEventY = MotionEvent.obtain(event);
                    } else {
                        hideMagnifierMode();
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE && wasMagnifierClick) {
                    return handleMagnifierMove(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    wasMagnifierClick = false;
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    wasMagnifierClick = false;
                    return true;
                }
                wasMagnifierClick = false;
                return false;
            }
        });
        magnifierView = (MagnifierView) pixelPerfectControlsFrameLayout.findViewById(R.id.controls_magnifier_view);
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
    }

    private MotionEvent lastMotionEventX;
    private MotionEvent lastMotionEventY;

    private boolean handleMagnifierMove(MotionEvent event) {
        float deltaTranslationX = event.getX() - lastMotionEventX.getX();
        float deltaTranslationY = event.getY() - lastMotionEventY.getY();
        float bitmapX = magnifierView.getTranslationX() + deltaTranslationX;
        float bitmapY = magnifierView.getTranslationY() + deltaTranslationY;
        if (magnifierView.getTranslationX() + deltaTranslationX < 0) {
            bitmapX = deltaTranslationX;
        } else if (magnifierView.getTranslationX() + deltaTranslationX > getWidth() - 300) {
            bitmapX = getWidth() - 300 + deltaTranslationX;
        }
        if (magnifierView.getTranslationY() + deltaTranslationY < 0) {
            bitmapY = deltaTranslationY;
        } else if (magnifierView.getTranslationY() + deltaTranslationY > getHeight() - 300) {
            bitmapY = getHeight() - 300 + deltaTranslationY;
        }
        magnifierView.updateScaledImageBitmap((int) bitmapX, (int) bitmapY);
        if (bitmapX == magnifierView.getTranslationX() + deltaTranslationX) {
            magnifierView.setTranslationX(magnifierView.getTranslationX() + deltaTranslationX);
            lastMotionEventX = MotionEvent.obtain(event);
        }
        if (bitmapY == magnifierView.getTranslationY() + deltaTranslationY) {
            magnifierView.setTranslationY(magnifierView.getTranslationY() + deltaTranslationY);
            lastMotionEventY = MotionEvent.obtain(event);
        }
        return true;
    }

    public void showActionsView(int poinX, int pointY) {
        pixelPerfectControlsFrameLayout.showActionsView(poinX, pointY);
    }

    private void showMagnifierMode(int x, int y) {
        magnifierFrameLayout.setVisibility(VISIBLE);
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
        magnifierView.updateSrcBitmap(PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView));
        pixelPerfectControlsFrameLayout.setVisibility(VISIBLE);
        setMagnifierViewPosition(x, y);
    }

    private void setMagnifierViewPosition(int x, int y) {
        magnifierView.setTranslationX(x - 150);
        magnifierView.setTranslationY(y - 150);
        int validX = x;
        int validY = y;
        if (x - 150 < 0) {
            magnifierView.setTranslationX(0);
            validX = 150;
        }
        if (y - 150 < 0) {
            magnifierView.setTranslationY(0);
            validY = 150;
        }
        if (x - 150 > getWidth() - 300) {
            magnifierView.setTranslationX(getWidth() - 300);
            validX = getWidth() - 150;
        }
        if (y - 150 > getHeight() - 300) {
            magnifierView.setTranslationY(getHeight() - 300);
            validY = getHeight() - 150;
        }
        magnifierView.setScaledImageBitmap(validX, validY);
    }

    private void hideMagnifierMode() {
        magnifierFrameLayout.setVisibility(GONE);
    }
}
