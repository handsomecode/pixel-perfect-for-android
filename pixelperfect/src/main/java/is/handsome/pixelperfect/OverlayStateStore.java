package is.handsome.pixelperfect;

import android.content.Context;
import android.content.SharedPreferences;

class OverlayStateStore {

    private static OverlayStateStore mInstance = null;

    public static OverlayStateStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OverlayStateStore(context);
        }
        return mInstance;
    }

    private final SharedPreferences preferences;

    private String imageName;
    private boolean isInverse;
    private float opacity;
    private int positionX;
    private int positionY;
    private int fixedOffsetX;
    private int fixedOffsetY;

    public OverlayStateStore(Context context) {
        preferences = context.getSharedPreferences(OverlayStateStore.class.getSimpleName(), Context.MODE_PRIVATE);
        imageName = preferences.getString("image", null);
        isInverse = preferences.getBoolean("inverse", false);
        opacity = preferences.getFloat("getOpacity", 0.5f);
        positionX = preferences.getInt("positionX", 0);
        positionY = preferences.getInt("positionY", 0);
        fixedOffsetX = preferences.getInt("fixedOffsetX", 0);
        fixedOffsetY = preferences.getInt("fixedOffsetY", 0);
    }

    public boolean isInverse() {
        return isInverse;
    }

    public void saveInverse(boolean isInverse) {
        this.isInverse = isInverse;
        preferences.edit().putBoolean("inverse", isInverse).apply();
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

    public void reset() {
        imageName = null;
        isInverse = false;
        opacity = 0.5f;
        positionX = 0;
        positionY = 0;
        fixedOffsetX = 0;
        fixedOffsetY = 0;
        preferences.edit().clear().apply();
    }
}
