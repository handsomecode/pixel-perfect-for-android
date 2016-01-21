package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class MagnifierContainerFrameLayout extends FrameLayout implements View.OnTouchListener {

    public interface MagnifierListener {
        void onMockupDown(MotionEvent event);

        void onMockupMove(MotionEvent event);
    }

    private MagnifierListener listener;
    private MagnifierView magnifierView;

    private boolean wasMagnifierClick;
    private boolean wasPPClick;
    private boolean justClick;

    private int magnifierWidth;
    private int lastBitmapPositionX;
    private int lastBitmapPositionY;

    private MotionEvent lastMotionEventMagnifierX;
    private MotionEvent lastMotionEventMagnifierY;


    public MagnifierContainerFrameLayout(Context context) {
        super(context);
        init();
    }

    public MagnifierContainerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MagnifierContainerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MagnifierContainerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnTouchListener(this);
        magnifierView = (MagnifierView) findViewById(R.id.controls_magnifier_view);
        magnifierWidth = (int) getResources().getDimension(R.dimen.pixel_perfect_magnifier_view_width);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (PixelPerfectUtils.inViewBounds(magnifierView, x, y)) {
                wasMagnifierClick = true;
                wasPPClick = false;
                if (magnifierView.getTranslationX() >= 0 && magnifierView.getTranslationX() <= getWidth() - magnifierWidth) {
                    lastMotionEventMagnifierX = MotionEvent.obtain(event);
                }
                if (magnifierView.getTranslationY() >= 0 && magnifierView.getTranslationY() <= getHeight() - magnifierWidth) {
                    lastMotionEventMagnifierY = MotionEvent.obtain(event);
                }
            } else {
                wasMagnifierClick = false;
                justClick = true;
                wasPPClick = true;
                if (listener != null) {
                    listener.onMockupDown(event);
                }
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            justClick = false;
            if (wasMagnifierClick) {
                return handleMagnifierMove(event);
            }
            if (wasPPClick) {
                if (listener != null) {
                    listener.onMockupMove(event);
                }
                return true;
            }
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (justClick) {
                hideMagnifierMode();
            }
            wasMagnifierClick = false;
            wasPPClick = false;
            justClick = false;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            wasMagnifierClick = false;
            wasPPClick = false;
            justClick = false;
            return true;
        }
        wasMagnifierClick = false;
        wasPPClick = false;
        justClick = false;
        return false;
    }

    public void setListener(MagnifierListener listener) {
        this.listener = listener;
    }

    public void setMagnifierSrc(Bitmap bitmap, boolean updateImage) {
        if (updateImage) {
            magnifierView.setSrcBitmap(bitmap, lastBitmapPositionX, lastBitmapPositionY);
        } else {
            magnifierView.setSrcBitmap(bitmap);
        }
    }

    public void updateTouchData(MotionEvent event) {
        wasMagnifierClick = true;
        if (magnifierView.getTranslationX() > 0 && magnifierView.getTranslationX() < getWidth() - magnifierWidth) {
            lastMotionEventMagnifierX = MotionEvent.obtain(event);
        }
        if (magnifierView.getTranslationY() > 0 && magnifierView.getTranslationY() < getHeight() - magnifierWidth) {
            lastMotionEventMagnifierY = MotionEvent.obtain(event);
        }
    }

    public void setMagnifierViewPosition(final int x, final int y) {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMagnifierPosition(x, y);
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public boolean handleMagnifierMove(MotionEvent event) {
        float deltaTranslationX = event.getX() - (lastMotionEventMagnifierX != null ? lastMotionEventMagnifierX.getX() : 0f);
        float deltaTranslationY = event.getY() - (lastMotionEventMagnifierY != null ? lastMotionEventMagnifierY.getY() : 0f);
        float bitmapX = calculateEdgeBitmapX(deltaTranslationX);
        float bitmapY = calculateEdgeBitmapY(deltaTranslationY);

        if (Float.isNaN(bitmapX)) {
            bitmapX = magnifierView.getTranslationX() + deltaTranslationX;
            magnifierView.setTranslationX(magnifierView.getTranslationX() + deltaTranslationX);
            lastMotionEventMagnifierX = MotionEvent.obtain(event);
        }
        if (Float.isNaN(bitmapY)) {
            bitmapY = magnifierView.getTranslationY() + deltaTranslationY;
            magnifierView.setTranslationY(magnifierView.getTranslationY() + deltaTranslationY);
            lastMotionEventMagnifierY = MotionEvent.obtain(event);
        }
        magnifierView.updateScaledImageBitmap((int) bitmapX, (int) bitmapY);
        lastBitmapPositionX = (int) bitmapX;
        lastBitmapPositionY = (int) bitmapY;
        return true;
    }

    private void init() {
        inflate(getContext(), R.layout.layout_magnifier_overlay, this);
    }

    private float calculateEdgeBitmapX(float deltaTranslationX) {
        float bitmapX = Float.NaN;
        if (magnifierView.getTranslationX() + deltaTranslationX < 0) {
            bitmapX = deltaTranslationX;
            if (magnifierView.getTranslationX() > 0) {
                magnifierView.setTranslationX(0);
            }
        } else if (getWidth() > 0 && magnifierView.getTranslationX() + deltaTranslationX > getWidth() - magnifierWidth) {
            bitmapX = getWidth() - magnifierWidth + deltaTranslationX;
            if (magnifierView.getTranslationX() < getWidth() - magnifierWidth) {
                magnifierView.setTranslationX(getWidth() - magnifierWidth);
            }
        }
        return bitmapX;
    }

    private float calculateEdgeBitmapY(float deltaTranslationY) {
        float bitmapY = Float.NaN;
        if (magnifierView.getTranslationY() + deltaTranslationY < 0) {
            bitmapY = deltaTranslationY;
            if (magnifierView.getTranslationY() > 0) {
                magnifierView.setTranslationY(0);
            }
        } else if (getHeight() > 0 && magnifierView.getTranslationY() + deltaTranslationY > getHeight() - magnifierWidth) {
            bitmapY = getHeight() - magnifierWidth + deltaTranslationY;
            if (magnifierView.getTranslationY() < getHeight() - magnifierWidth) {
                magnifierView.setTranslationY(getHeight() - magnifierWidth);
            }
        }
        return bitmapY;
    }

    private void setMagnifierPosition(int x, int y) {
        magnifierView.setTranslationX(x - magnifierWidth / 2);
        magnifierView.setTranslationY(y - magnifierWidth / 2);
        int validX = x;
        int validY = y;
        if (x - magnifierWidth / 2 < 0) {
            magnifierView.setTranslationX(0);
            validX = magnifierWidth / 2;
        }
        if (y - magnifierWidth / 2 < 0) {
            magnifierView.setTranslationY(0);
            validY = magnifierWidth / 2;
        }
        if (x - magnifierWidth / 2 > getWidth() - magnifierWidth) {
            magnifierView.setTranslationX(getWidth() - magnifierWidth);
            validX = getWidth() - magnifierWidth / 2;
        }
        if (y - magnifierWidth / 2 > getHeight() - magnifierWidth) {
            magnifierView.setTranslationY(getHeight() - magnifierWidth);
            validY = getHeight() - magnifierWidth / 2;
        }
        magnifierView.setScaledImageBitmap(validX, validY);
        lastBitmapPositionX = validX - magnifierWidth / 2;
        lastBitmapPositionY = validY - magnifierWidth / 2;
    }

    private void hideMagnifierMode() {
        this.setVisibility(GONE);
    }
}
