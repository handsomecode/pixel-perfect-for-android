package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import is.handsome.pixelperfect.PixelPerfectCallbacks;
import is.handsome.pixelperfect.PixelPerfectConfig;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class PixelPerfectLayout extends FrameLayout {

    private static final int DOUBLE_CLICK_DURATION = 200;

    public enum MoveMode {
        VERTICAL, HORIZONTAL, ALL_DIRECTIONS
    }

    private ImageView pixelPerfectOverlayImageView;
    private PixelPerfectControlsFrameLayout pixelPerfectControlsFrameLayout;
    private PixelPerfectCallbacks.LayoutListener layoutListener;
    private MagnifierContainerFrameLayout magnifierFrameLayout;
    private MoveMode moveMode = MoveMode.ALL_DIRECTIONS;
    private boolean pixelPerfectContext = true;

    private MotionEvent lastMotionEvent;
    private Handler doubleClickHandler = new Handler();
    private Runnable doubleClickRunnable;
    private int touchSlop;
    private boolean justClick;
    private boolean wasActionDown;
    private int clickCounter;

    private int fixedOffsetX = 0;
    private int fixedOffsetY = 0;

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
        wasActionDown = false;
        justClick = false;
        clickCounter = 0;
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

    public void setLayoutListener(PixelPerfectCallbacks.LayoutListener listener) {
        layoutListener = listener;
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

    public void showActionsView(int poinX, int pointY) {
        pixelPerfectControlsFrameLayout.showActionsView(poinX, pointY);
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

    private boolean handleTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clickCounter++;
            if (clickCounter == 1) {
                wasActionDown = true;
                doubleClickHandler.postDelayed(doubleClickRunnable, DOUBLE_CLICK_DURATION);
                lastMotionEvent = MotionEvent.obtain(event);
            } else if (clickCounter >= 2) {
                wasActionDown = false;
                clickCounter = 0;
                magnifierFrameLayout.updateTouchData(event);
                showMagnifierMode((int) event.getX(), (int) event.getY());
            }
            justClick = false;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && wasActionDown && magnifierFrameLayout.getVisibility() != VISIBLE) {
            if (justClick
                    && Math.abs(event.getY() - lastMotionEvent.getY()) < touchSlop
                    && Math.abs(event.getX() - lastMotionEvent.getX()) < touchSlop) {
                return true;
            }
            justClick = false;
            clickCounter = 0;
            if (moveMode != MoveMode.VERTICAL) {
                pixelPerfectOverlayImageView.setTranslationX(pixelPerfectOverlayImageView.getTranslationX() + (event.getX() - lastMotionEvent.getX()));
                pixelPerfectControlsFrameLayout.updateDiffXPixelsData(fixedOffsetX + (int) pixelPerfectOverlayImageView.getTranslationX());
            }
            if (moveMode != MoveMode.HORIZONTAL) {
                pixelPerfectOverlayImageView.setTranslationY(pixelPerfectOverlayImageView.getTranslationY() + (event.getY() - lastMotionEvent.getY()));
                pixelPerfectControlsFrameLayout.updateDiffYPixelsData(fixedOffsetY + (int) pixelPerfectOverlayImageView.getTranslationY());
            }
            lastMotionEvent = MotionEvent.obtain(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && magnifierFrameLayout.getVisibility() == VISIBLE) {
            wasActionDown = false;
            justClick = false;
            clickCounter = 0;
            return magnifierFrameLayout.handleMagnifierMove(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            wasActionDown = false;
            justClick = false;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            wasActionDown = false;
            justClick = false;
            clickCounter = 0;
            return true;
        }
        wasActionDown = false;
        justClick = false;
        clickCounter = 0;
        return false;
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

        magnifierFrameLayout = (MagnifierContainerFrameLayout) pixelPerfectControlsFrameLayout
                .findViewById(R.id.controls_magnifier_frame_layout);
        magnifierFrameLayout.setListener(new MagnifierContainerFrameLayout.MagnifierListener() {
            @Override
            public void onMockupDown(MotionEvent event) {
                lastMotionEvent = event;
            }

            @Override
            public void onMockupMove(MotionEvent event) {
                if (moveMode != MoveMode.VERTICAL) {
                    pixelPerfectOverlayImageView.setTranslationX(pixelPerfectOverlayImageView.getTranslationX() + (event.getX() - lastMotionEvent.getX()) / 5);
                    pixelPerfectControlsFrameLayout.updateDiffXPixelsData(fixedOffsetX + (int) pixelPerfectOverlayImageView.getTranslationX());
                }
                if (moveMode != MoveMode.HORIZONTAL) {
                    pixelPerfectOverlayImageView.setTranslationY(pixelPerfectOverlayImageView.getTranslationY() + (event.getY() - lastMotionEvent.getY()) / 5);
                    pixelPerfectControlsFrameLayout.updateDiffYPixelsData(fixedOffsetY + (int) pixelPerfectOverlayImageView.getTranslationY());
                }
                lastMotionEvent = MotionEvent.obtain(event);

                pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
                Bitmap bitmap = PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView);
                pixelPerfectControlsFrameLayout.setVisibility(VISIBLE);
                magnifierFrameLayout.setMagnifierSrc(bitmap, true);
            }
        });
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);

        doubleClickRunnable = new Runnable() {
            @Override
            public void run() {
                if (clickCounter == 1) {
                    justClick = true;
                    wasActionDown = false;
                    if (lastMotionEvent.getX() < getWidth() / 3 || lastMotionEvent.getX() > getWidth() * 2 / 3) {
                        pixelPerfectOverlayImageView.setTranslationX(pixelPerfectOverlayImageView.getTranslationX()
                                + (lastMotionEvent.getX() < getWidth() / 5 ? -1 : 1));
                        pixelPerfectControlsFrameLayout.updateDiffXPixelsData(fixedOffsetX + (int) pixelPerfectOverlayImageView.getTranslationX());
                    } else if (lastMotionEvent.getY() < getHeight() / 4 || lastMotionEvent.getY() > getHeight() * 3 / 4) {
                        pixelPerfectOverlayImageView.setTranslationY(pixelPerfectOverlayImageView.getTranslationY()
                                + (lastMotionEvent.getY() > getHeight() / 2 ? 1 : -1));
                        pixelPerfectControlsFrameLayout.updateDiffYPixelsData(fixedOffsetY + (int) pixelPerfectOverlayImageView.getTranslationY());
                    } else {
                        pixelPerfectControlsFrameLayout.setDiffPixelsViewVisibility();
                    }
                }
                justClick = false;
                clickCounter = 0;
            }
        };
    }

    private void showMagnifierMode(int x, int y) {
        magnifierFrameLayout.setVisibility(VISIBLE);
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
        Bitmap bitmap = PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView);
        pixelPerfectControlsFrameLayout.setVisibility(VISIBLE);
        magnifierFrameLayout.setMagnifierSrc(bitmap, false);
        magnifierFrameLayout.setMagnifierViewPosition(x, y);
    }
}
