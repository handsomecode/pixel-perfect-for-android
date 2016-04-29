package is.handsome.pixelperfect;

import android.content.Context;
import android.content.SharedPreferences;

class OverlayStateStore {

    public enum SettingsState {
        CLOSED,
        OPENED_MAIN,
        OPENED_IMAGES;

        public static SettingsState fromInteger(int x) {
            switch (x) {
                case 0:
                    return CLOSED;
                case 1:
                    return OPENED_MAIN;
                case 2:
                    return OPENED_IMAGES;
            }
            return CLOSED;
        }
    }

    private static OverlayStateStore mInstance = null;

    public static OverlayStateStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OverlayStateStore(context);
        }
        return mInstance;
    }

    private final SharedPreferences preferences;

    private boolean isPixelPerfectActive;

    private String imageName;
    private String assetsFolderName;
    private boolean isInverse;
    private float opacity;
    private int width;
    private int height;
    private SettingsState settingsState;

    public OverlayStateStore(Context context) {
        preferences = context.getSharedPreferences(OverlayStateStore.class.getSimpleName(), Context.MODE_PRIVATE);
        isPixelPerfectActive = preferences.getBoolean("isActive", false);
        imageName = preferences.getString("image", null);
        assetsFolderName = preferences.getString("assetsName", "pixelperfect");
        isInverse = preferences.getBoolean("inverse", false);
        opacity = preferences.getFloat("getOpacity", 0.5f);
        width = preferences.getInt("width", 0);
        height = preferences.getInt("height", 0);
        settingsState = SettingsState.fromInteger(preferences.getInt("settingsState", 0));
    }

    public boolean isInverse() {
        return isInverse;
    }

    public void saveInverse(boolean isInverse) {
        this.isInverse = isInverse;
        preferences.edit().putBoolean("inverse", isInverse).apply();
    }

    public boolean isPixelPerfectActive() {
        return isPixelPerfectActive;
    }

    public void savePixelPerfectActive(boolean isPixelPerfectActive) {
        this.isPixelPerfectActive = isPixelPerfectActive;
        preferences.edit().putBoolean("isActive", isPixelPerfectActive).apply();
    }

    public float getOpacity() {
        return opacity;
    }

    public void saveOpacity(float opacity) {
        this.opacity = opacity;
        preferences.edit().putFloat("getOpacity", opacity).apply();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void saveSize(int width, int height) {
        this.width = width;
        this.height = height;
        preferences.edit().putInt("width", width).apply();
        preferences.edit().putInt("height", height).apply();
    }

    public String getImageName() {
        return imageName;
    }

    public void saveImageName(String imageName) {
        this.imageName = imageName;
        preferences.edit().putString("image", imageName).apply();
    }

    public String getAssetsFolderName() {
        return assetsFolderName;
    }

    public void saveAssetsFolderName(String assetsFolderName) {
        this.assetsFolderName = assetsFolderName;
        preferences.edit().putString("assetsName", assetsFolderName).apply();
    }

    public SettingsState getSettingsState() {
        return settingsState;
    }

    public void saveSettingsState(SettingsState settingsState) {
        this.settingsState = settingsState;
        preferences.edit().putInt("settingsState", settingsState.ordinal()).apply();
    }

    public void reset() {
        isPixelPerfectActive = false;
        imageName = null;
        assetsFolderName = "pixelperfect";
        isInverse = false;
        opacity = 0.5f;
        width = 0;
        height = 0;
        settingsState = SettingsState.CLOSED;
        preferences.edit().clear().apply();
    }
}
