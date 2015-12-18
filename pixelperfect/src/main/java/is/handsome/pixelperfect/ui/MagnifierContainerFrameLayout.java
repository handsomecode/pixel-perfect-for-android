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

    private MagnifierView magnifierView;
    private int magnifierWidth;

    private MotionEvent lastMotionEventMagnifierX;
    private MotionEvent lastMotionEventMagnifierY;
    boolean wasMagnifierClick;

    public MagnifierContainerFrameLayout(Context context) {
        super(context);
    }

    public MagnifierContainerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MagnifierContainerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MagnifierContainerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                lastMotionEventMagnifierX = MotionEvent.obtain(event);
                lastMotionEventMagnifierY = MotionEvent.obtain(event);
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

    public void setMagnifierSrc(Bitmap bitmap) {
        magnifierView.setSrcBitmap(bitmap);
    }

    public void updateTouchData(MotionEvent event) {
        wasMagnifierClick = true;
        lastMotionEventMagnifierX = MotionEvent.obtain(event);
        lastMotionEventMagnifierY = MotionEvent.obtain(event);
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
        float deltaTranslationX = event.getX() - lastMotionEventMagnifierX.getX();
        float deltaTranslationY = event.getY() - lastMotionEventMagnifierY.getY();
        float bitmapX = magnifierView.getTranslationX() + deltaTranslationX;
        float bitmapY = magnifierView.getTranslationY() + deltaTranslationY;
        if (magnifierView.getTranslationX() + deltaTranslationX < 0) {
            bitmapX = deltaTranslationX;
        } else if (magnifierView.getTranslationX() + deltaTranslationX > getWidth() - magnifierWidth) {
            bitmapX = getWidth() - magnifierWidth + deltaTranslationX;
        }
        if (magnifierView.getTranslationY() + deltaTranslationY < 0) {
            bitmapY = deltaTranslationY;
        } else if (magnifierView.getTranslationY() + deltaTranslationY > getHeight() - magnifierWidth) {
            bitmapY = getHeight() - magnifierWidth + deltaTranslationY;
        }
        magnifierView.updateScaledImageBitmap((int) bitmapX, (int) bitmapY);
        if (bitmapX == magnifierView.getTranslationX() + deltaTranslationX) {
            magnifierView.setTranslationX(magnifierView.getTranslationX() + deltaTranslationX);
            lastMotionEventMagnifierX = MotionEvent.obtain(event);
        }
        if (bitmapY == magnifierView.getTranslationY() + deltaTranslationY) {
            magnifierView.setTranslationY(magnifierView.getTranslationY() + deltaTranslationY);
            lastMotionEventMagnifierY = MotionEvent.obtain(event);
        }
        return true;
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
    }

    private void hideMagnifierMode() {
        this.setVisibility(GONE);
    }
}
