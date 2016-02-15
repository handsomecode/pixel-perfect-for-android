package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class PixelPerfectUtils {

    public static int getWindowWidth(WindowManager windowManager) {
        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getSize(displaySize);
        return displaySize.x;
    }

    public static int getWindowHeight(WindowManager windowManager) {
        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getSize(displaySize);
        return displaySize.y;
    }

    public static int getWindowWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        return getWindowWidth(windowManager);
    }

    public static int getWindowHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        return getWindowHeight(windowManager);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isTranslucentStatusBar(Context context) {
        Window window = ((Activity)context).getWindow();
        int flags = window.getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    }

    public static Bitmap getBitmapFromAssets(Context context, String fullName) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(fullName);
            return decodeSampledBitmapFromInputStream(inputStream, screenWidth);
        } catch (IOException e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap invertBitmap(Bitmap src) {
        ColorMatrix invertedColorMatrix =
                new ColorMatrix(new float[]{
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0});

        ColorFilter sepiaColorFilter = new ColorMatrixColorFilter(invertedColorMatrix);
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColorFilter(sepiaColorFilter);
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    private static Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        options.inJustDecodeBounds = false;
        try {
            inputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > reqWidth) {
            final int halfWidth = width / 2;
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private PixelPerfectUtils() {
        throw new AssertionError("no instances");
    }
}
