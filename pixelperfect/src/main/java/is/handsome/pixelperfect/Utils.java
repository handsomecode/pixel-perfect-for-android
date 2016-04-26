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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

class Utils {

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
        Window window = ((Activity) context).getWindow();
        int flags = window.getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeightDp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25;
        return convertDipsToPixels(context, statusBarHeightDp);
    }

    private static int convertDipsToPixels(Context context, int dips) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, displayMetrics);
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

    public static Bitmap getBitmapFromAssets(Context context, String fullName) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(fullName);
            return BitmapFactory.decodeStream(inputStream);
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

    public static Bitmap getAdoptedBitmapFromAssets(Context context, String fullName,
                                                    int reqWidth, int reqHeight) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(fullName);
            int reqWidthPixels = convertDipsToPixels(context, reqWidth);
            int reqHeightPixels = convertDipsToPixels(context, reqHeight);
            return decodeBitmapFromInputStream(inputStream, reqWidthPixels, reqHeightPixels);
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

    private static Bitmap decodeBitmapFromInputStream(InputStream inputStream, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        try {
            inputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                           int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize * 2;
    }

    private Utils() {
        throw new AssertionError("no instances");
    }
}
