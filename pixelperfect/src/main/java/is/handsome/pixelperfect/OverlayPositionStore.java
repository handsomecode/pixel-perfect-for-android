package is.handsome.pixelperfect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

class OverlayPositionStore {

    private static OverlayPositionStore mInstance = null;

    public static OverlayPositionStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OverlayPositionStore(context);
        }
        return mInstance;
    }

    private final SharedPreferences preferences;

    private int positionX;
    private int positionY;
    private int positionXLand;
    private int positionYLand;
    private int fixedOffsetX;
    private int fixedOffsetY;
    private int fixedOffsetXLand;
    private int fixedOffsetYLand;
    private int orientation;

    public OverlayPositionStore(Context context) {
        preferences = context.getSharedPreferences(OverlayPositionStore.class.getSimpleName(), Context.MODE_PRIVATE);
        positionX = preferences.getInt("positionX", 0);
        positionY = preferences.getInt("positionY", 0);
        positionXLand = preferences.getInt("positionXLand", 0);
        positionYLand = preferences.getInt("positionYLand", 0);
        fixedOffsetX = preferences.getInt("fixedOffsetX", 0);
        fixedOffsetY = preferences.getInt("fixedOffsetY", 0);
        fixedOffsetXLand = preferences.getInt("fixedOffsetXLand", 0);
        fixedOffsetYLand = preferences.getInt("fixedOffsetYLand", 0);
        orientation = preferences.getInt("orientation", Configuration.ORIENTATION_UNDEFINED);
    }

    public int getPositionX() {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return positionX;
        } else {
            return positionXLand;
        }
    }

    public int getPositionY() {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return positionY;
        } else {
            return positionYLand;
        }
    }

    public void savePosition(int x, int y) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.positionX = x;
            this.positionY = y;
            preferences.edit().putInt("positionX", positionX).apply();
            preferences.edit().putInt("positionY", positionY).apply();
        } else {
            this.positionXLand = x;
            this.positionYLand = y;
            preferences.edit().putInt("positionXLand", positionXLand).apply();
            preferences.edit().putInt("positionYLand", positionYLand).apply();
        }
    }

    public int getFixedOffsetX() {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return fixedOffsetX;
        } else {
            return fixedOffsetXLand;
        }
    }

    public int getFixedOffsetY() {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return fixedOffsetY;
        } else {
            return fixedOffsetYLand;
        }
    }

    public void saveFixedOffset(int x, int y) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.fixedOffsetX = x;
            this.fixedOffsetY = y;
            preferences.edit().putInt("fixedOffsetX", fixedOffsetX).apply();
            preferences.edit().putInt("fixedOffsetY", fixedOffsetY).apply();
        } else {
            this.fixedOffsetXLand = x;
            this.fixedOffsetYLand = y;
            preferences.edit().putInt("fixedOffsetXLand", fixedOffsetXLand).apply();
            preferences.edit().putInt("fixedOffsetYLand", fixedOffsetYLand).apply();
        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void saveOrientation(int orientation) {
        this.orientation = orientation;
        preferences.edit().putInt("orientation", orientation).apply();
    }

    public void reset() {
        positionX = 0;
        positionY = 0;
        positionXLand = 0;
        positionYLand = 0;
        fixedOffsetX = 0;
        fixedOffsetY = 0;
        fixedOffsetXLand = 0;
        fixedOffsetYLand = 0;
        orientation = Configuration.ORIENTATION_UNDEFINED;
    }
}
