package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class MagnifierView extends ImageView {

    private static int ZOOM_FACTOR = 2;
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

    public void setScaledImageBitmap() {
        if (getWidth() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateScaledImageBitmap();
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            updateScaledImageBitmap();
        }
    }

    public void updateScaledImageBitmap() {
        int padding = WIDTH / ZOOM_FACTOR * (ZOOM_FACTOR - 1) / 2;
        Bitmap croppedBitmap = Bitmap.createBitmap(srcBitmap, (int) getX() + padding, (int) getY() + padding,
                WIDTH / ZOOM_FACTOR, WIDTH / ZOOM_FACTOR);
        setImageBitmap(croppedBitmap);
    }
}
