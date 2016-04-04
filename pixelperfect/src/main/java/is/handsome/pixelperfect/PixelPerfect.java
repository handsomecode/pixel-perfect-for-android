package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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

        public String getOverlayImageAssetsPath() {
            return overlayImageAssetsPath;
        }

        public String getOverlayInitialImageName() {
            return overlayInitialImageName;
        }

        public float getOverlayScaleFactor() {
            return overlayScaleFactor;
        }

        public static class Builder {

            private String overlayImageAssetsPath;
            private String overlayInitialImageName;
            private float overlayScaleFactor = 1;

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

    private static Overlay overlay;

    private static AppLifeCycleObserver.Listener foregroundListener = new AppLifeCycleObserver.Listener() {
        @Override
        public void onBecameForeground() {
            overlay.show();
        }

        @Override
        public void onBecameBackground() {
            overlay.hide();
        }
    };

    /**
     * shows pixel perfect tool that
     * displays screen overlay and settings view
     *
     * @param activity
     * @return
     */
    public static void show(Activity activity) {
        showOverlay(activity, null);
    }

    //TODO: should we check activity to null ?
    public static void show(Activity activity, PixelPerfect.Config config) {
        showOverlay(activity, config);
    }

    public static boolean isShown() {
        return overlay != null && overlay.isShown();
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
            Log.w(PixelPerfect.class.getSimpleName(), "Error during listener removing", exception);
        }

        if (overlay != null) {
            overlay.destroy();
            overlay = null;
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

    private static void showOverlay(Activity activity, PixelPerfect.Config config) {
        if (overlay != null) {
            overlay.show();
            return;
        }

        overlay = config == null ? new Overlay(activity) :
                new Overlay(activity, config);
        AppLifeCycleObserver.get(activity.getApplicationContext()).addListener(foregroundListener);
    }
}
