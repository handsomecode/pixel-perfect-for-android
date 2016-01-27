package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import is.handsome.pixelperfect.PixelPerfectController;

public class FastActionsOverlayView extends FrameLayout {

    public static final int LIFE_TIMEOUT = 750;

    private class FastActionsGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            setVisibility(INVISIBLE);
            shouldDie = false;
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            listener.onInverseChange();
            setVisibility(INVISIBLE);
            shouldDie = false;
            return true;
        }
    }

    private PixelPerfectController.FastActionsOverlayListener listener;
    private GestureDetector gestureDetector;
    private boolean shouldDie = true;

    public FastActionsOverlayView(Context context) {
        super(context);
        init();
    }

    public FastActionsOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FastActionsOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FastActionsOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }

    public void setListener(PixelPerfectController.FastActionsOverlayListener listener){
        this.listener = listener;
    }

    public void startVisibilityTimer() {
        shouldDie = true;
        postDelayed(new Runnable() {

            @Override
            public void run() {
                if (shouldDie) {
                    setVisibility(View.INVISIBLE);
                }
            }
        }, LIFE_TIMEOUT);
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), new FastActionsGestureListener());
    }
}
