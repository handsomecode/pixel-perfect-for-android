package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

public class PixelPerfect {

    public static class Config {

        private String overlayImageAssetsPath;
        private String overlayActiveImageName;
        private float overlayScaleFactor;

        private Config(Config.Builder configBuilder) {
            this.overlayImageAssetsPath = configBuilder.overlayImageAssetsPath;
            this.overlayActiveImageName = configBuilder.overlayActiveImageName;
            this.overlayScaleFactor = configBuilder.overlayScaleFactor;
        }

        public String getOverlayImageAssetsPath() {
            return overlayImageAssetsPath;
        }

        public String overlayActiveImageName() {
            return overlayActiveImageName;
        }

        public float getOverlayScaleFactor() {
            return overlayScaleFactor;
        }

        public static class Builder {

            private String overlayImageAssetsPath;
            private String overlayActiveImageName;
            private float overlayScaleFactor = 1;

            /**
             * Set custom path in assets folder
             * to overlay images
             *
             * @param overlayImagesAssetsPath
             * @return builder
             */
            public Builder overlayImagesAssetsPath(String overlayImagesAssetsPath) {
                this.overlayImageAssetsPath = overlayImagesAssetsPath;
                return this;
            }

            /**
             * Set active image name for overlay
             * that will be visible right after
             * creating PixelPerfect tool
             *
             * @param overlayActiveImageName
             * @return builder
             */
            public Builder overlayActiveImageName(String overlayActiveImageName) {
                this.overlayActiveImageName = overlayActiveImageName;
                return this;
            }

            /**
             * Add overlay scale factor that
             * is used for all overlay
             *
             * @param overlayScaleFactor
             * @return builder
             */
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

    private static ActivityLifeCycleObserver.Listener foregroundListener = new ActivityLifeCycleObserver.Listener() {
        @Override
        public void onBecameForeground() {
            overlay.show();
        }

        @Override
        public void onBecameBackground() {
            overlay.hide();
        }

        @Override
        public void onActivityStopped(Activity activity) {
            overlay.saveState(activity);
            hide();
        }
    };

    /**
     * Show pixel perfect tool that
     * displays screen overlay and settings view
     *
     * @param context
     */
    public static void show(@NonNull Context context) {
        showOverlay(context, null);
    }

    /**
     * Show pixel perfect tool
     * according to Config settings that
     * displays screen overlay and settings view
     *
     * @param context
     * @param config
     */
    public static void show(@NonNull Context context, PixelPerfect.Config config) {
        showOverlay(context, config);
    }

    /**
     * Check if PixelPerfect overlay is created
     * and is shown
     *
     * @return PixelPerfect overlay is shown or not
     */
    public static boolean isShown() {
        return overlay != null && overlay.isShown();
    }

    /**
     * Stop foreground listener and
     * remove overlay views from the window
     */
    public static void hide() {
        try {
            ActivityLifeCycleObserver.get().removeListener(foregroundListener);
        } catch (IllegalStateException exception) {
            Log.w(PixelPerfect.class.getSimpleName(), "Error during listener removing", exception);
        }

        if (overlay != null) {
            overlay.destroy();
            overlay = null;
        }
    }

    /**
     * Check if app has SYSTEM_ALERT_WINDOW for
     * Android M
     *
     * @param context
     * @return Is app has permission or not
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * Ask for SYSTEM_ALERT_WINDOW permission
     * Android M
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void askForPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private static void showOverlay(Context context, PixelPerfect.Config config) {
        if (overlay != null) {
            overlay.resetState();
            overlay.show();
            return;
        }

        if (!hasPermission(context)) {
            throw new IllegalStateException("permissions are not granted. You have to call askForPermission method before using showOverlay");
        }

        overlay = config == null ? new Overlay(context, false) :
                new Overlay(context, config);
        ActivityLifeCycleObserver.get(context.getApplicationContext()).addListener(foregroundListener);
    }

    static void showOverlayInner(Context context) {
        if (overlay != null) {
            overlay.show();
            return;
        }

        if (!hasPermission(context)) {
            throw new IllegalStateException("permissions are not granted. You have to call askForPermission method before using showOverlay");
        }

        overlay = new Overlay(context, true);
        ActivityLifeCycleObserver.get(context.getApplicationContext()).addListener(foregroundListener);
        overlay.resetState();
    }
}
