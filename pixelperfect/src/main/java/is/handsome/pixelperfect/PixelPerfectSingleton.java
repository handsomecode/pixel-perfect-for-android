package is.handsome.pixelperfect;

import android.app.Activity;

public class PixelPerfectSingleton {

    private static PixelPerfectSingleton instance = null;

    public Activity topActivity;

    public Activity getTopActivity() {
        return topActivity;
    }

    public static PixelPerfectSingleton get() {
        if (instance == null) {
            instance = new PixelPerfectSingleton();
        }
        return instance;
    }

    private PixelPerfectSingleton() {}
}
