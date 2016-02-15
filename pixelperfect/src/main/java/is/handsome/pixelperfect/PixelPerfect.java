package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class PixelPerfect {

    public static class Config {

        private String overlayImageAssetsPath;
        private String overlayInitialImageName;
        private float overlayScaleFactor;

        private Config(Config.Builder configBuilder) {
            this.overlayImageAssetsPath = configBuilder.overlayImageAssetsPath;
            this.overlayInitialImageName = configBuilder.overlayInitialImageName;
            this.overlayScaleFactor = configBuilder.overlayScaleFactor;
        }

        public static class Builder {

            private String overlayImageAssetsPath;
            private String overlayInitialImageName;
            private float overlayScaleFactor;

            public Builder overlayImagesAssetsPath(String overlayImagesAssetsPath) {
                this.overlayImageAssetsPath = overlayImagesAssetsPath;
                return this;
            }
            public Builder overlayInitialImageName(String overlayInitialImageName) {
                this.overlayInitialImageName = overlayInitialImageName;
                return this;
            }
            public Builder overlayScaleFactor(float overlayScaleFactor) {
                this.overlayScaleFactor = overlayScaleFactor;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

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

    /**
     * shows pixel perfect tool that
     * displays screen overlay and settings view
     *
     * @param context
     * @return
     */
    public static void show(Context context) {
        if (pixelPerfectController != null) {
            pixelPerfectController.show();
            return;
        }

        pixelPerfectController = new PixelPerfectController(context);
        AppLifeCycleObserver.get(context).addListener(foregroundListener);
    }

    public static void show(Context context, PixelPerfect.Config config) {
        show(context);
        if (!TextUtils.isEmpty(config.overlayInitialImageName)) {
            pixelPerfectController.setImage(config.overlayInitialImageName);
        }
    }

    public static boolean isShown() {
        return pixelPerfectController != null && pixelPerfectController.isShown();
    }

    /**
     * stops foreground listener
     * removes views from the window
     * nulls all static variables
     */
    public static void hide() {
        try {
            AppLifeCycleObserver.get().removeListener(foregroundListener);
        } catch (IllegalStateException exception) {
            Log.w("PixelPerfect", "Error during listener removing", exception);
        }

        if (pixelPerfectController != null) {
            pixelPerfectController.destroy();
            pixelPerfectController = null;
        }
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
