package is.handsome.pixelperfect;

import android.app.Activity;

import java.util.List;

public class PixelPerfectConfig {

    private static PixelPerfectConfig instance = null;

    public boolean useVolumeButtons;
    public List<MockupImage> userImages;

    public Activity topActivity;

    public boolean useVolumeButtons() {
        return useVolumeButtons;
    }

    public Activity getTopActivity() {
        return topActivity;
    }

    public static PixelPerfectConfig get() {
        if (instance == null) {
            instance = new PixelPerfectConfig();
        }
        return instance;
    }

    private PixelPerfectConfig () {}
}
