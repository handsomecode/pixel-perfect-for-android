package is.handsome.pixelperfect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class PixelPerfectUtils {

    private static Rect outRect = new Rect();
    private static int[] location = new int[2];

    public static boolean inViewBounds(View view, int x, int y) {
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

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