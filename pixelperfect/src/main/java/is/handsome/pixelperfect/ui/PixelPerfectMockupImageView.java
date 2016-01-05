package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PixelPerfectMockupImageView extends ImageView {
    public PixelPerfectMockupImageView(Context context) {
        super(context);
    }

    public PixelPerfectMockupImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PixelPerfectMockupImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PixelPerfectMockupImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (getDrawable() == null) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
        }
        setMeasuredDimension(width, height);
    }

    private void init() {
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        requestLayout();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        requestLayout();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        requestLayout();
    }

    @Override
    public void setImageIcon(Icon icon) {
        super.setImageIcon(icon);
        requestLayout();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        requestLayout();
    }
}
