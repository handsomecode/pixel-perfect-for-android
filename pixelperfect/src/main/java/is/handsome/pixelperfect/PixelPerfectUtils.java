package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isTranslucentStatusBar(Context context) {
        Window window = ((Activity)context).getWindow();
        int flags = window.getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    }

    static Bitmap takeActivityScreenshot() {
        View rootView = ((ViewGroup) PixelPerfectConfig.get().getTopActivity()
                .findViewById(android.R.id.content)).getChildAt(0).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public static Bitmap takeOverlayScreenshot(View view) {
        View rootView = view.getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public static Bitmap combineBitmaps(View overlayView) {
        Bitmap activityBitmap = takeActivityScreenshot();
        Bitmap overlayBitmap = takeOverlayScreenshot(overlayView);
        int width = activityBitmap.getWidth();
        int height = activityBitmap.getHeight();
        Bitmap comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(comboBitmap);
        Rect src = new Rect(0, 0, activityBitmap.getWidth(), activityBitmap.getHeight());
        Rect dest = new Rect(0, 0, activityBitmap.getWidth(), activityBitmap.getHeight());
        comboImage.drawBitmap(activityBitmap, src, dest, null);
        comboImage.drawBitmap(overlayBitmap, 0, 0, null);
        return comboBitmap;
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

    public static int getMaxTextureSize() {
        // approach adopted from: http://stackoverflow.com/questions/26985858/gles10-glgetintegerv-returns-0-in-lollipop-only
        EGL10 egl = (EGL10) EGLContext.getEGL();

        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        egl.eglInitialize(dpy, vers);

        int[] configAttr = {
                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_LEVEL, 0,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];

        int[] numConfig = new int[1];

        egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);

        if (numConfig[0] == 0) {
            // what to do here?
        }

        EGLConfig config = configs[0];

        int[] surfAttr = {
                EGL10.EGL_WIDTH, 64,
                EGL10.EGL_HEIGHT, 64,
                EGL10.EGL_NONE
        };

        EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);

        final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;  // missing in EGL10

        int[] ctxAttrib = {
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
        };

        EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);

        egl.eglMakeCurrent(dpy, surf, surf, ctx);

        int[] maxSize = new int[1];

        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surf);
        egl.eglDestroyContext(dpy, ctx);
        egl.eglTerminate(dpy);

        return maxSize[0];
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

    public static ArrayList<Bitmap> splitLargeBitmap(Bitmap bitmap) {
        int maxTextureSize = getMaxTextureSize();
        int arraySize = bitmap.getHeight() / maxTextureSize;
        if (bitmap.getHeight() % maxTextureSize > 0) {
            arraySize++;
        }
        ArrayList<Bitmap> bitmaps = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            int height = (i != arraySize - 1) ? maxTextureSize : bitmap.getHeight() - maxTextureSize * i;
            Bitmap splitBitmap = Bitmap.createBitmap(bitmap.getWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(splitBitmap);

            int srcHeight = (i != arraySize - 1) ? maxTextureSize * (i + 1) : bitmap.getHeight();
            Rect src = new Rect(0, maxTextureSize * i, bitmap.getWidth(), srcHeight);
            Rect dest = new Rect(0, 0, splitBitmap.getWidth(), splitBitmap.getHeight());
            canvas.drawBitmap(bitmap, src, dest, null);
            bitmaps.add(splitBitmap);
        }
        return bitmaps;
    }

    private PixelPerfectUtils() {
        throw new AssertionError("no instances");
    }
}
