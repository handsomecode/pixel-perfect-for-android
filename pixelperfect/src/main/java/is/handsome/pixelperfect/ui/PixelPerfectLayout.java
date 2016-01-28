package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import is.handsome.pixelperfect.PixelPerfectController;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class PixelPerfectLayout extends FrameLayout {

    private static int MICRO_OFFSET = 8;
    private static int LONG_PRESS_TIMEOUT = 500;

    public enum MoveMode {
        VERTICAL, HORIZONTAL, UNDEFINED
    }

    private ImageView pixelPerfectOverlayImageView;
    private PixelPerfectController.LayoutListener layoutListener;
    private MagnifierContainerFrameLayout magnifierFrameLayout;
    private MoveMode moveMode = MoveMode.UNDEFINED;
    private int touchSlop;

    private GestureDetector gestureDetector;
    private MotionEvent lastMotionEvent;
    private MotionEvent doubleTapEvent;
    private boolean justClick;
    private boolean wasActionDown;
    private boolean wasDoubleAction;

    private float microOffsetDx;
    private float microOffsetDy;

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
    public boolean onTouchEvent(MotionEvent event) {
        if (pixelPerfectOverlayImageView.getVisibility() == VISIBLE) {
            gestureDetector.onTouchEvent(event);
            return handleTouch(event);
        }
        clearTouchData();
        return false;
    }

    public void setLayoutListener(PixelPerfectController.LayoutListener listener) {
        layoutListener = listener;
    }

    public void setImageVisible(boolean visible) {
        if (visible && pixelPerfectOverlayImageView.getDrawable() == null) {
            updateImage("");
        }
        pixelPerfectOverlayImageView.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setImageAlpha(float alpha) {
        pixelPerfectOverlayImageView.setAlpha(alpha);
    }

    public float getImageAlpha() {
        return pixelPerfectOverlayImageView.getAlpha();
    }

    public void updateImage(Bitmap bitmap) {
        if (bitmap != null) {
            ViewGroup.LayoutParams layoutParams = pixelPerfectOverlayImageView.getLayoutParams();
            layoutParams.width = bitmap.getWidth();
            layoutParams.height = bitmap.getHeight();
            pixelPerfectOverlayImageView.setLayoutParams(layoutParams);
            pixelPerfectOverlayImageView.setImageBitmap(bitmap);

            ViewGroup.LayoutParams containerParams = getLayoutParams();
            containerParams.width = layoutParams.width + 2 * (int) getResources().getDimension(R.dimen.overlay_border_size);
            containerParams.height = layoutParams.height + 2 * (int) getResources().getDimension(R.dimen.overlay_border_size);
            setLayoutParams(containerParams);

            layoutListener.onOverlayUpdate();
        }
    }

    public void updateImage(String fullName) {
        if (!TextUtils.isEmpty(fullName)) {
            Bitmap bitmap = PixelPerfectUtils.getBitmapFromAssets(getContext(), fullName);
            updateImage(bitmap);
        }
    }

    public void invertImageBitmap(boolean enabled) {
        if (enabled) {
            setImageAlpha(0.5f);
        }
        Bitmap srcBitmap = ((BitmapDrawable) pixelPerfectOverlayImageView.getDrawable()).getBitmap();
        Bitmap invertedBitmap = PixelPerfectUtils.invertBitmap(srcBitmap);
        pixelPerfectOverlayImageView.setImageBitmap(invertedBitmap);
    }

    private void initOverlay() {
        pixelPerfectOverlayImageView = new ImageView(getContext());
        FrameLayout.LayoutParams layoutParams = new LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        int margin = (int) getResources().getDimension(R.dimen.overlay_border_size);
        layoutParams.setMargins(margin, margin, margin, margin);
        pixelPerfectOverlayImageView.setLayoutParams(layoutParams);
        pixelPerfectOverlayImageView.setVisibility(INVISIBLE);
        pixelPerfectOverlayImageView.setAlpha(0.5f);
        addView(pixelPerfectOverlayImageView);
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setBackgroundResource(R.drawable.bg_overlay);
        initOverlay();

        gestureDetector = new GestureDetector(getContext(), new PixelPerfectLayoutGestureListener());

        magnifierFrameLayout = new MagnifierContainerFrameLayout(getContext());

        //Feature is turned off temporary
        /*addView(magnifierFrameLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));*/
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

                magnifierFrameLayout.setVisibility(INVISIBLE);
                Bitmap bitmap = PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView);
                magnifierFrameLayout.setVisibility(VISIBLE);
                magnifierFrameLayout.setMagnifierSrc(bitmap, true);
            }
        });
        magnifierFrameLayout.setVisibility(INVISIBLE);
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
            microOffsetDx = 0;
            microOffsetDy = 0;
            lastMotionEvent = MotionEvent.obtain(event);
        } else {
            if (moveMode == MoveMode.HORIZONTAL) {
                if (layoutListener != null) {
                    float dx = event.getRawX() - lastMotionEvent.getRawX();
                    if (Math.abs(dx) < MICRO_OFFSET) {
                        microOffsetDx += dx / (MICRO_OFFSET * 2);
                        dx = Math.round(microOffsetDx);
                        if (dx != 0) {
                            microOffsetDx = 0;
                        }
                    } else {
                        microOffsetDx = 0;
                    }
                    layoutListener.onOverlayMoveX((int) dx);
                }
            } else {
                if (layoutListener != null) {
                    float dy = event.getRawY() - lastMotionEvent.getRawY();
                    if (Math.abs(dy) < MICRO_OFFSET) {
                        microOffsetDy += dy / (MICRO_OFFSET * 2);
                        dy = Math.round(microOffsetDy);
                        if (dy != 0) {
                            microOffsetDy = 0;
                        }
                    } else {
                        microOffsetDy = 0;
                    }
                    layoutListener.onOverlayMoveY((int) dy);
                }
            }
            float dx = event.getRawX() - lastMotionEvent.getRawX();
            if (Math.abs(dx) >= 3) {
                layoutListener.onOffsetViewMoveX((int) dx);
            }
            float dy = event.getRawY() - lastMotionEvent.getRawY();
            if (Math.abs(dy) >= 3) {
                layoutListener.onOffsetViewMoveY((int) dy);
            }
            lastMotionEvent = MotionEvent.obtain(event);
        }
    }

    private void showMagnifierMode(int x, int y) {
        magnifierFrameLayout.setVisibility(INVISIBLE);
        Bitmap bitmap = PixelPerfectUtils.combineBitmaps(pixelPerfectOverlayImageView);
        magnifierFrameLayout.setVisibility(VISIBLE);
        magnifierFrameLayout.setMagnifierSrc(bitmap, false);
        magnifierFrameLayout.setMagnifierViewPosition(x, y);
    }

    private class PixelPerfectLayoutGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(final MotionEvent event) {
            wasDoubleAction = false;
            doubleTapEvent = MotionEvent.obtain(event);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!wasDoubleAction
                            && Math.abs(event.getY() - doubleTapEvent.getY()) < touchSlop
                            && Math.abs(event.getX() - doubleTapEvent.getX()) < touchSlop) {
                        layoutListener.onFixOffset();
                    }
                    wasDoubleAction = true;
                }
            }, LONG_PRESS_TIMEOUT);
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && !wasDoubleAction) {
                layoutListener.setInverseMode();
                wasDoubleAction = true;
                return true;
            }
            return super.onDoubleTapEvent(event);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            layoutListener.openSettings();
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
            if (layoutListener != null) {
                layoutListener.showOffsetView((int) event.getRawX(), (int) event.getRawY());
            }
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
