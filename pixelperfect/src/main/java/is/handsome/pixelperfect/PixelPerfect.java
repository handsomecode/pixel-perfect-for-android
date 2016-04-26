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
     * shows pixel perfect tool that
     * displays screen overlay and settings view
     *
     * @param context
     * @return
     */
    public static void show(@NonNull Context context) {
        showOverlay(context, null);
    }

    public static void show(@NonNull Context context, PixelPerfect.Config config) {
        long beginingTime = System.currentTimeMillis();
        showOverlay(context, config);
        long performanceTime = System.currentTimeMillis() - beginingTime;
        Log.v("PP", "Performance : " + performanceTime + "ms");
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
            ActivityLifeCycleObserver.get().removeListener(foregroundListener);
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
