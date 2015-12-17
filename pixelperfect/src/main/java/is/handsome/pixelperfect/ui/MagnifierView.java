package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MagnifierView extends ImageView {

    private static int ZOOM_FACTOR = 3;
    private static int WIDTH = 300;

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

    public void updateSrcBitmap(Bitmap bitmap) {
        this.srcBitmap = bitmap;
    }

    public void setScaledImageBitmap(int centerX, int centerY) {
        int padding = WIDTH / ZOOM_FACTOR * (ZOOM_FACTOR - 1) / 2;
        Bitmap croppedBitmap = Bitmap.createBitmap(srcBitmap, centerX + padding - WIDTH / 2, centerY + padding - WIDTH / 2,
                WIDTH / ZOOM_FACTOR, WIDTH / ZOOM_FACTOR);
        setImageBitmap(croppedBitmap);
    }

    public void updateScaledImageBitmapAccordingViewPosition() {
        int padding = WIDTH / ZOOM_FACTOR * (ZOOM_FACTOR - 1) / 2;
        int left = (int) getX() + padding >= 0 ? (int) getX() + padding : 0;
        int top = (int) getY() + padding >= 0 ? (int) getY() + padding : 0;
        Bitmap croppedBitmap = Bitmap.createBitmap(srcBitmap, left, top,
                WIDTH / ZOOM_FACTOR, WIDTH / ZOOM_FACTOR);
        setImageBitmap(croppedBitmap);
    }

    public void updateScaledImageBitmap(int x, int y) {
        int padding = WIDTH / ZOOM_FACTOR * (ZOOM_FACTOR - 1) / 2;
        int left = x + padding >= 0 ? x + padding : 0;
        int top = y + padding >= 0 ? y + padding : 0;

        if (left + WIDTH / ZOOM_FACTOR > srcBitmap.getWidth()) {
            left = srcBitmap.getWidth() - WIDTH / ZOOM_FACTOR;
        }
        if (top + WIDTH / ZOOM_FACTOR > srcBitmap.getHeight()) {
            top = srcBitmap.getHeight() - WIDTH / ZOOM_FACTOR;
        }

        Bitmap croppedBitmap = Bitmap.createBitmap(srcBitmap, left, top, WIDTH / ZOOM_FACTOR, WIDTH / ZOOM_FACTOR);
        setImageBitmap(croppedBitmap);
    }
}
