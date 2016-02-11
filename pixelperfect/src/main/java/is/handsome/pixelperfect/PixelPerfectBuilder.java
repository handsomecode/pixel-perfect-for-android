package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

public class PixelPerfectBuilder {

    private static PixelPerfectController pixelPerfectController;
    private static AppLifeCycleObserver.Listener foregroundListener = new AppLifeCycleObserver.Listener() {
        @Override
        public void onBecameForeground() {
            pixelPerfectController.show();
        }

        @Override
        public void onBecameBackground() {
            pixelPerfectController.hide();
        }
    };

    protected PixelPerfectBuilder() {}

    /**
     * stops foreground listener
     * removes views from the window
     * nulls all static variables
     */
    protected static void destroy() {
        try {
            AppLifeCycleObserver.get().removeListener(foregroundListener);
        } catch (IllegalStateException exception) {
            Log.w("PixelPerfectBuilder", "Error during listener removing", exception);
        }

        if (pixelPerfectController != null) {
            pixelPerfectController.destroy();
            pixelPerfectController = null;
        }
    }

    /**
     * shows pixel perfect tool that
     * displays screen overlay and settings view
     *
     * @param activity
     * @return
     */
    public boolean show(Activity activity) {
        if (pixelPerfectController != null) {
            pixelPerfectController.show();
            return true;
        }

        pixelPerfectController = new PixelPerfectController(activity);
        AppLifeCycleObserver.get(activity).addListener(foregroundListener);
        return true;
    }

    public PixelPerfectBuilder withImages(List<PixelPerfectImage> images) {
        PixelPerfectConfig.get().userImages = images;
        return this;
    }

    public static void setImage(String imageName) {
        if (isCreated()) {
            pixelPerfectController.setImage(imageName);
        }
    }

    /**
     * checks if Pixel Perfect Tool
     * is displayed on the screen now
     */
    public static boolean isCreated() {
        return pixelPerfectController != null;
    }

    public static void show() {
        pixelPerfectController.show();
    }

    public static void hide() {
        pixelPerfectController.hide();
    }

    public static boolean isShown() {
        return pixelPerfectController.isShown();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void askForPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
