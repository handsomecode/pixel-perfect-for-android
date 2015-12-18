package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class MagnifierView extends ImageView {

    private static int ZOOM_FACTOR = 3;

    private Bitmap srcBitmap;

    public MagnifierView(Context context) {
        super(context);
    }

    public MagnifierView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MagnifierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MagnifierView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSrcBitmap(Bitmap bitmap) {
        this.srcBitmap = bitmap;
    }

    public void setScaledImageBitmap(final int centerX, final int centerY) {
        if (getWidth() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setImageBitmap(centerX, centerY);
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            setImageBitmap(centerX, centerY);
        }
    }

    public void updateScaledImageBitmap(int x, int y) {
        int viewWidth = getWidth();
        int padding = viewWidth / ZOOM_FACTOR * (ZOOM_FACTOR - 1) / 2;
        int left = x + padding >= 0 ? x + padding : 0;
        int top = y + padding >= 0 ? y + padding : 0;

        if (left + viewWidth / ZOOM_FACTOR > srcBitmap.getWidth()) {
            left = srcBitmap.getWidth() - viewWidth / ZOOM_FACTOR;
        }
        if (top + viewWidth / ZOOM_FACTOR > srcBitmap.getHeight()) {
            top = srcBitmap.getHeight() - viewWidth / ZOOM_FACTOR;
        }

        Bitmap croppedBitmap = Bitmap.createBitmap(srcBitmap, left, top, viewWidth / ZOOM_FACTOR,
                viewWidth / ZOOM_FACTOR);
        setImageBitmap(croppedBitmap);
    }

    private void setImageBitmap(int centerX, int centerY) {
        int viewWidth = getWidth();
        int padding = viewWidth / ZOOM_FACTOR * (ZOOM_FACTOR - 1) / 2;
        Bitmap croppedBitmap = Bitmap.createBitmap(srcBitmap, centerX + padding - viewWidth / 2, centerY + padding - viewWidth / 2,
                viewWidth / ZOOM_FACTOR, viewWidth / ZOOM_FACTOR);
        setImageBitmap(croppedBitmap);
    }
}
