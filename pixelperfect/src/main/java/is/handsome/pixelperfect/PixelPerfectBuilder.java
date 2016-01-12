package is.handsome.pixelperfect;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

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
    protected static void hide() {
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
     * displays floating button and options menu
     *
     * @param activity
     */
    public void show(Activity activity) {

        if (overlayPermRequest(activity)) {
            //once permission is granted then you must call show() again
            return;
        }

        if (pixelPerfectController != null) {
            pixelPerfectController.show();
            return;
        }

        pixelPerfectController = new PixelPerfectController((Application) activity.getApplicationContext());
        pixelPerfectController.setListener(new PixelPerfectCallbacks.ControllerListener() {
            @Override
            public void onClosePixelPerfect() {
                hide();
            }
        });
        AppLifeCycleObserver.get(activity).addListener(foregroundListener);
    }

    /**
     * checks if Pixel Perfect Tool
     * is displayed on the screen now
     */
    public static boolean isShown() {
        return pixelPerfectController != null;
    }

    public static void innerHide() {
        if (pixelPerfectController != null) {
            pixelPerfectController.hide();
        }
    }

    public static void innerShow() {
        if (pixelPerfectController != null) {
            pixelPerfectController.show();
        }
    }

    public static void updateFloatingViewPositionAfterRotation() {
        if (isShown()) {
            pixelPerfectController.updateFloatingViewPositionAfterRotation();
        }
    }

    /**
     * asks if volume buttons
     * can be used for Opacity widget
     *
     * @param use
     */
    public PixelPerfectBuilder useVolumeButtons(boolean use) {
        PixelPerfectConfig.get().useVolumeButtons = use;
        return this;
    }

    /**
     * request overlay permission when api 23 and above
     *
     * @param context
     * @return
     */
    private boolean overlayPermRequest(Context context) {
        boolean permNeeded = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                permNeeded = true;
            }
        }
        return permNeeded;
    }
}
