package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

class OverlayView extends FrameLayout {

    private static int MICRO_OFFSET = 8;
    private static int LONG_PRESS_TIMEOUT = 500;
    private static int OFFSET_VIEW_TIMEOUT = 200;

    public enum MoveMode {
        VERTICAL, HORIZONTAL, UNDEFINED
    }

    private ImageView pixelPerfectOverlayImageView;
    private TextView noOverlayImageTextView;
    private Overlay.LayoutListener layoutListener;
    private MoveMode moveMode = MoveMode.UNDEFINED;
    private int touchSlop;
    private OverlayStateStore overlayStateStore;

    private GestureDetector gestureDetector;
    private MotionEvent lastMotionEvent;
    private MotionEvent doubleTapEvent;
    private boolean justClick;
    private boolean wasActionDown;
    private boolean wasDoubleAction;
    private boolean letFastActions;
    private long savedTime;

    private float microOffsetDx;
    private float microOffsetDy;
    private float offsetViewDx;
    private float offsetViewDy;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    public void updateNoImageTextViewSize() {
        if (noOverlayImageTextView.getVisibility() == View.VISIBLE) {
            ViewGroup.LayoutParams containerParams = getLayoutParams();
            containerParams.width = overlayStateStore.getWidth() + 2 * (int) getResources().getDimension(R.dimen.overlay_border_size);
            containerParams.height = overlayStateStore.getHeight() + 2 * (int) getResources().getDimension(R.dimen.overlay_border_size);
            setLayoutParams(containerParams);
        }
    }

    public void setLayoutListener(Overlay.LayoutListener listener) {
        layoutListener = listener;
    }

    public void setImageVisible(boolean visible) {
        pixelPerfectOverlayImageView.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setImageAlpha(float alpha) {
        pixelPerfectOverlayImageView.setAlpha(alpha);
    }

    public float getImageAlpha() {
        return pixelPerfectOverlayImageView.getAlpha();
    }

    public void updateImage(Bitmap bitmap, float overlayScaleFactor) {
        if (bitmap != null) {
            if (noOverlayImageTextView.getVisibility() == VISIBLE) {
                noOverlayImageTextView.setVisibility(GONE);
                letFastActions = true;
            }
            ViewGroup.LayoutParams layoutParams = pixelPerfectOverlayImageView.getLayoutParams();
            layoutParams.width = (int) (bitmap.getWidth() * overlayScaleFactor);
            layoutParams.height = (int) (bitmap.getHeight() * overlayScaleFactor);
            pixelPerfectOverlayImageView.setLayoutParams(layoutParams);
            pixelPerfectOverlayImageView.setImageBitmap(bitmap);

            ViewGroup.LayoutParams containerParams = getLayoutParams();
            containerParams.width = layoutParams.width + 2 * (int) getResources().getDimension(R.dimen.overlay_border_size);
            containerParams.height = layoutParams.height + 2 * (int) getResources().getDimension(R.dimen.overlay_border_size);
            setLayoutParams(containerParams);

            layoutListener.onOverlayUpdate(layoutParams.width, layoutParams.height);
        }
    }

    public boolean invertImageBitmap(boolean saveOpacity) {
        if (!saveOpacity) {
            setImageAlpha(0.5f);
        }
        if (pixelPerfectOverlayImageView.getDrawable() != null) {
            Bitmap srcBitmap = ((BitmapDrawable) pixelPerfectOverlayImageView.getDrawable()).getBitmap();
            Bitmap invertedBitmap = Utils.invertBitmap(srcBitmap);
            pixelPerfectOverlayImageView.setImageBitmap(invertedBitmap);
            return true;
        }
        return false;
    }

    public boolean isNoImageOverlay() {
        return noOverlayImageTextView.getVisibility() == VISIBLE;
    }

    private void initOverlay() {
        pixelPerfectOverlayImageView = new ImageView(getContext());
        final FrameLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.overlay_border_size);
        layoutParams.setMargins(margin, margin, margin, margin);
        pixelPerfectOverlayImageView.setLayoutParams(layoutParams);
        pixelPerfectOverlayImageView.setVisibility(INVISIBLE);
        pixelPerfectOverlayImageView.setAlpha(0.5f);
        pixelPerfectOverlayImageView.setImageDrawable(null);
        addView(pixelPerfectOverlayImageView);
        initNoOverlayTextView();
    }

    private void initNoOverlayTextView() {
        int marginPortion = (int) getContext().getResources().getDimension(R.dimen.stub_overlay_margin_portion);
        int overlayWidth = overlayStateStore.getWidth() == 0 ?
                Utils.getWindowWidth(getContext()) - marginPortion * 2 : overlayStateStore.getWidth();
        int overlayHeight = overlayStateStore.getHeight() == 0 ?
                Utils.getWindowHeight(getContext()) - marginPortion * 3 : overlayStateStore.getHeight();

        final FrameLayout.LayoutParams noOverlayParams = new LayoutParams(overlayWidth, overlayHeight);
        int margin = (int) getResources().getDimension(R.dimen.overlay_border_size);
        noOverlayParams.setMargins(margin, margin, margin, margin);

        noOverlayImageTextView = new TextView(getContext());
        noOverlayImageTextView.setBackgroundResource(R.color.black_50_alpha);
        noOverlayImageTextView.setText(R.string.no_overlay_image_text);
        noOverlayImageTextView.setTextSize(22);
        noOverlayImageTextView.setGravity(Gravity.CENTER);
        noOverlayImageTextView.setTextColor(Color.WHITE);
        addView(noOverlayImageTextView, noOverlayParams);
    }

    private void init() {
        overlayStateStore = OverlayStateStore.getInstance(getContext());
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        setBackgroundResource(R.drawable.bg_overlay);
        initOverlay();

        gestureDetector = new GestureDetector(getContext(), new PixelPerfectLayoutGestureListener());
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
            if (letFastActions) {
                layoutListener.showOffsetView((int) event.getRawX(), (int) event.getRawY());
            }
        } else {
            if (moveMode == MoveMode.HORIZONTAL) {
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
            } else {
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
            if (letFastActions) {
                if ((int) (event.getRawX() - lastMotionEvent.getRawX() + offsetViewDx) == 0) {
                    offsetViewDx += event.getRawX() - lastMotionEvent.getRawX();
                } else {
                    layoutListener.onOffsetViewMoveX((int) (event.getRawX() - lastMotionEvent.getRawX() + offsetViewDx));
                    offsetViewDx = (event.getRawX() - lastMotionEvent.getRawX() + offsetViewDx) % 1;
                }

                if ((int) (event.getRawY() - lastMotionEvent.getRawY() + offsetViewDy) == 0) {
                    offsetViewDy += event.getRawY() - lastMotionEvent.getRawY();
                } else {
                    layoutListener.onOffsetViewMoveY((int) (event.getRawY() - lastMotionEvent.getRawY() + offsetViewDy));
                    offsetViewDy = (event.getRawY() - lastMotionEvent.getRawY() + offsetViewDy) % 1;
                }
            }
            lastMotionEvent = MotionEvent.obtain(event);
        }
    }

    private boolean handleTouch(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            justClick = true;
            wasActionDown = true;
            lastMotionEvent = MotionEvent.obtain(event);
            savedTime = System.currentTimeMillis();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && wasActionDown) {
            if (letFastActions && justClick && System.currentTimeMillis() - savedTime > OFFSET_VIEW_TIMEOUT) {
                layoutListener.showOffsetView((int) event.getRawX(), (int) event.getRawY());
            }
            moveMockupOverlay(event);
            return true;
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
                    if (letFastActions && !wasDoubleAction
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
            if (letFastActions && event.getAction() == MotionEvent.ACTION_UP && !wasDoubleAction) {
                layoutListener.setInverseMode();
                wasDoubleAction = true;
                return true;
            }
            return super.onDoubleTapEvent(event);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            layoutListener.openSettings(noOverlayImageTextView.getVisibility() == VISIBLE);
            return true;
        }
    }
}
