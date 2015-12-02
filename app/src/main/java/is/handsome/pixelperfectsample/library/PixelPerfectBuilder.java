package is.handsome.pixelperfectsample.library;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class PixelPerfectBuilder {

    private static PixelPerfectConfig pixelPerfectConfig;
    private static PixelPerfectController pixelPerfectController;
    private static Foreground.Listener foregroundListener = new Foreground.Listener() {
        @Override
        public void onBecameForeground() {
            pixelPerfectController.show();
        }

        @Override
        public void onBecameBackground() {
            pixelPerfectController.hide();
        }
    };

    protected PixelPerfectBuilder() {
        pixelPerfectConfig = new PixelPerfectConfig();
    }

    /**
     * stops foreground listener
     * removes views from the window
     * nulls all static variables
     *
     * @param context
     */
    protected static void hide(Context context) {
        Foreground.get(context).removeListener(foregroundListener);

        pixelPerfectController.destroy();

        pixelPerfectController = null;
        pixelPerfectConfig = null;
    }

    /**
     * shows pixel perfect tool that
     * displays floating button and options menu
     *
     * @param context
     */
    public void show(Context context) {

        if (overlayPermRequest(context)) {
            //once permission is granted then you must call show() again
            return;
        }

        if (pixelPerfectController != null) {
            pixelPerfectController.show();
            return;
        }

        pixelPerfectController = new PixelPerfectController((Application) context.getApplicationContext(),
                pixelPerfectConfig);

        Foreground.init((Application) context.getApplicationContext()).addListener(foregroundListener);
    }

    /**
     * asks if volume buttons
     * can be used for Opacity widget
     *
     * @param canUse
     */
    public PixelPerfectBuilder useVolumeButtons(boolean canUse) {
        pixelPerfectConfig.useVolumeButtons = canUse;
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
