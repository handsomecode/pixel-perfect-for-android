package is.handsome.pixelperfectsample.library.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import is.handsome.pixelperfectsample.library.BitmapUtils;
import is.handsome.pixelperfectsample.library.PixelPerfectCallbacks;
import timber.log.Timber;

public class PixelPerfectLayout extends FrameLayout {

    public enum MoveMode {
        VERTICAL, HORIZONTAL, ALL_DIRECTIONS
    }

    private ImageView pixelPerfectOverlayImageView;
    private PixelPerfectControlsFrameLayout pixelPerfectControlsFrameLayout;

    private MotionEvent lastMotionEvent;
    private int touchSlop;
    private boolean justClick;
    private boolean wasClick;
    private boolean pixelPerfectContext = true;
    private MoveMode moveMode = MoveMode.ALL_DIRECTIONS;

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

    private void initOverlay() {
        pixelPerfectOverlayImageView = new FixedXImageView(getContext());
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
        if (pixelPerfectControlsFrameLayout.inBounds(x, y) || !pixelPerfectContext) {
            return false;
        } else {
            return pixelPerfectOverlayImageView.getVisibility() == VISIBLE || super.onInterceptTouchEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pixelPerfectOverlayImageView.getVisibility() == VISIBLE && pixelPerfectContext) {
            return handleTouchOnePointer(event);
        }
        wasClick = false;
        justClick = false;
        return false;
    }

    private boolean handleTouchOnePointer(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            wasClick = true;
            justClick = true;
            lastMotionEvent = MotionEvent.obtain(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && wasClick) {
            if (justClick
                    && Math.abs(event.getY() - lastMotionEvent.getY()) < 3 * touchSlop
                    && Math.abs(event.getX() - lastMotionEvent.getX()) < 3 * touchSlop) {
                return true;
            }
            justClick = false;
            if (moveMode != MoveMode.VERTICAL) {
                pixelPerfectOverlayImageView.setTranslationX(pixelPerfectOverlayImageView.getTranslationX() + (event.getX() - lastMotionEvent.getX()));
            }
            if (moveMode != MoveMode.HORIZONTAL) {
                pixelPerfectOverlayImageView.setTranslationY(pixelPerfectOverlayImageView.getTranslationY() + (event.getY() - lastMotionEvent.getY()));
            }
            lastMotionEvent = MotionEvent.obtain(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP && justClick) {
            wasClick = false;
            justClick = false;
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
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            wasClick = false;
            justClick = false;
            return true;
        }
        wasClick = false;
        justClick = false;
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    pixelPerfectOverlayImageView.setAlpha(Math.min(1, pixelPerfectOverlayImageView.getAlpha() + 0.05f));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    pixelPerfectOverlayImageView.setAlpha(Math.max(0, pixelPerfectOverlayImageView.getAlpha() - 0.05f));
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_DOWN) {
                    Timber.w("On Back");
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
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
            pixelPerfectOverlayImageView.setImageBitmap(BitmapUtils.getBitmapFromAssets(getContext(), fullName));
        }
    }

    public void setPixelPerfectContext() {
        setPixelPerfectContext(!pixelPerfectContext);
    }

    private void setPixelPerfectContext(boolean newValue) {
        pixelPerfectContext = newValue;
        if (pixelPerfectContext) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) PixelPerfectLayout.this.getLayoutParams();
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            windowManager.updateViewLayout(this, layoutParams);
        } else {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) PixelPerfectLayout.this.getLayoutParams();
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            windowManager.updateViewLayout(this, layoutParams);
        }
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
            public void onChangePixelPerfectContext() {
                pixelPerfectContext = !pixelPerfectContext;
                if (getChildCount() > 0 && getChildAt(0) != null) {
                    getChildAt(0).dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0));
                }
            }

            @Override
            public void onChangeMoveMode(MoveMode changedMoveMode) {
                moveMode = changedMoveMode;
            }
        });
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
    }
}
