package is.handsome.pixelperfect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

class OverlayStateStore {

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
    private int positionX;
    private int positionY;
    private int width;
    private int height;
    private int fixedOffsetX;
    private int fixedOffsetY;
    private int orientation;
    private boolean isSettingsOpened;

    public OverlayStateStore(Context context) {
        preferences = context.getSharedPreferences(OverlayStateStore.class.getSimpleName(), Context.MODE_PRIVATE);
        isPixelPerfectActive = preferences.getBoolean("isActive", false);
        imageName = preferences.getString("image", null);
        assetsFolderName = preferences.getString("assetsName", "pixelperfect");
        isInverse = preferences.getBoolean("inverse", false);
        opacity = preferences.getFloat("getOpacity", 0.5f);
        positionX = preferences.getInt("positionX", 0);
        positionY = preferences.getInt("positionY", 0);
        width = preferences.getInt("width", 0);
        height = preferences.getInt("height", 0);
        fixedOffsetX = preferences.getInt("fixedOffsetX", 0);
        fixedOffsetY = preferences.getInt("fixedOffsetY", 0);
        orientation = preferences.getInt("orientation", Configuration.ORIENTATION_UNDEFINED);
        isSettingsOpened = preferences.getBoolean("settingsOpened", false);
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

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void savePosition(int x, int y) {
        this.positionX = x;
        this.positionY = y;
        preferences.edit().putInt("positionX", positionX).apply();
        preferences.edit().putInt("positionY", positionY).apply();
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

    public int getFixedOffsetX() {
        return fixedOffsetX;
    }

    public int getFixedOffsetY() {
        return fixedOffsetY;
    }

    public void saveFixedOffset(int x, int y) {
        this.fixedOffsetX = x;
        this.fixedOffsetY = y;
        preferences.edit().putInt("fixedOffsetX", fixedOffsetX).apply();
        preferences.edit().putInt("fixedOffsetY", fixedOffsetY).apply();
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

    public int getOrientation() {
        return orientation;
    }

    public void saveOrientation(int orientation) {
        this.orientation = orientation;
        preferences.edit().putInt("orientation", orientation).apply();
    }

    public boolean isSettingsOpened() {
        return isSettingsOpened;
    }

    public void saveSettingsOpened(boolean isSettingsOpened) {
        this.isSettingsOpened = isSettingsOpened;
        preferences.edit().putBoolean("settingsOpened", isSettingsOpened).apply();
    }

    public void reset() {
        isPixelPerfectActive = false;
        imageName = null;
        assetsFolderName = "pixelperfect";
        isInverse = false;
        opacity = 0.5f;
        positionX = 0;
        positionY = 0;
        width = 0;
        height = 0;
        fixedOffsetX = 0;
        fixedOffsetY = 0;
        isSettingsOpened = false;
        orientation = Configuration.ORIENTATION_UNDEFINED;
        preferences.edit().clear().apply();
    }
}
