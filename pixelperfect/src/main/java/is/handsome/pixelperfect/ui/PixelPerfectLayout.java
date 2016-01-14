package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import is.handsome.pixelperfect.PixelPerfectCallbacks;
import is.handsome.pixelperfect.PixelPerfectConfig;
import is.handsome.pixelperfect.PixelPerfectController;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class PixelPerfectLayout extends FrameLayout {

    public enum MoveMode {
        VERTICAL, HORIZONTAL, UNDEFINED
    }

    private ImageView pixelPerfectOverlayImageView;
    private PixelPerfectControlsFrameLayout pixelPerfectControlsFrameLayout;
    private PixelPerfectController.LayoutListener layoutListener;
    private MagnifierContainerFrameLayout magnifierFrameLayout;
    private MoveMode moveMode = MoveMode.UNDEFINED;
    private boolean pixelPerfectContext = true;

    private GestureDetector gestureDetector;
    private MotionEvent lastMotionEvent;
    private int touchSlop;
    private boolean justClick;
    private boolean wasActionDown;

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
            gestureDetector.onTouchEvent(event);
            return handleTouch(event);
        }
        clearTouchData();
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
            layoutListener.onBackPressed();
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

    public void setLayoutListener(PixelPerfectController.LayoutListener listener) {
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

    public float getImageAlpha() {
        return pixelPerfectOverlayImageView.getAlpha();
    }

    public void updateImage(String fullName) {
        pixelPerfectOverlayImageView.setTranslationY(0);
        if (TextUtils.isEmpty(fullName)) {
            pixelPerfectOverlayImageView.setImageDrawable(null);
        } else {
            pixelPerfectOverlayImageView.setImageBitmap(PixelPerfectUtils.getBitmapFromAssets(getContext(), fullName));
        }
    }

    private void initOverlay() {
        pixelPerfectOverlayImageView = new PixelPerfectMockupImageView(getContext());
        pixelPerfectOverlayImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        pixelPerfectOverlayImageView.setAdjustViewBounds(true);
        pixelPerfectOverlayImageView.setVisibility(INVISIBLE);
        pixelPerfectOverlayImageView.setAlpha(0.5f);
        addView(pixelPerfectOverlayImageView);
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        initOverlay();

        gestureDetector = new GestureDetector(getContext(), new PixelPerfectLayoutGestureListener());

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
        });

        magnifierFrameLayout = (MagnifierContainerFrameLayout) pixelPerfectControlsFrameLayout
                .findViewById(R.id.controls_magnifier_frame_layout);
        magnifierFrameLayout.setListener(new MagnifierContainerFrameLayout.MagnifierListener() {
            @Override
            public void onMockupDown(MotionEvent event) {
                justClick = true;
                wasActionDown = true;
                lastMotionEvent = MotionEvent.obtain(event);
            }

            @Override
            public void onMockupMove(MotionEvent event) {
                moveMockupOverlay(event);

                pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
                Bitmap bitmap = PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView);
                pixelPerfectControlsFrameLayout.setVisibility(VISIBLE);
                magnifierFrameLayout.setMagnifierSrc(bitmap, true);
            }
        });
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
    }

    private void moveMockupOverlay(MotionEvent event) {
        if (justClick
                && Math.abs(event.getY() - lastMotionEvent.getY()) < touchSlop
                && Math.abs(event.getX() - lastMotionEvent.getX()) < touchSlop) {
            return;
        }
        if (justClick) {
            float dx = event.getRawX() - lastMotionEvent.getRawX();
            float dy = event.getRawY() - lastMotionEvent.getRawY();
            if (Math.abs(dx) >= Math.abs(dy)) {
                moveMode = MoveMode.HORIZONTAL;
            } else {
                moveMode = MoveMode.VERTICAL;
            }
            justClick = false;
            lastMotionEvent = MotionEvent.obtain(event);
            if (layoutListener != null) {
                layoutListener.showOffsetView((int) event.getRawX() - 100, (int) event.getRawY() - 170);
            }
        } else {
            if (moveMode == MoveMode.HORIZONTAL) {
                if (layoutListener != null) {
                    layoutListener.onMockupOverlayMoveX((int) (event.getRawX() - lastMotionEvent.getRawX()));
                }
            } else {
                if (layoutListener != null) {
                    layoutListener.onMockupOverlayMoveY((int) (event.getRawY() - lastMotionEvent.getRawY()));
                }
            }
            if (layoutListener != null) {
                layoutListener.onOffsetViewMoveX((int) (event.getRawX() - lastMotionEvent.getRawX()));
                layoutListener.onOffsetViewMoveY((int) (event.getRawY() - lastMotionEvent.getRawY()));
            }
            lastMotionEvent = MotionEvent.obtain(event);
        }
    }

    private void showMagnifierMode(int x, int y) {
        magnifierFrameLayout.setVisibility(VISIBLE);
        pixelPerfectControlsFrameLayout.setVisibility(INVISIBLE);
        Bitmap bitmap = PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView);
        pixelPerfectControlsFrameLayout.setVisibility(VISIBLE);
        magnifierFrameLayout.setMagnifierSrc(bitmap, false);
        magnifierFrameLayout.setMagnifierViewPosition(x, y);
    }

    private class PixelPerfectLayoutGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            magnifierFrameLayout.updateTouchData(event);
            showMagnifierMode((int) event.getX(), (int) event.getY());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (layoutListener != null) {
                layoutListener.openSettings();
            }
            return true;
        }
    }

    private boolean handleTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            justClick = true;
            wasActionDown = true;
            lastMotionEvent = MotionEvent.obtain(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && wasActionDown && magnifierFrameLayout.getVisibility() != VISIBLE) {
            moveMockupOverlay(event);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE && magnifierFrameLayout.getVisibility() == VISIBLE) {
            clearTouchData();
            return magnifierFrameLayout.handleMagnifierMove(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            clearTouchData();
            return true;
        }
        clearTouchData();
        return false;
    }

    private void clearTouchData() {
        if (layoutListener != null) {
            layoutListener.hideOffsetView();
        }
        moveMode = MoveMode.UNDEFINED;
        wasActionDown = false;
        justClick = false;
    }
}
