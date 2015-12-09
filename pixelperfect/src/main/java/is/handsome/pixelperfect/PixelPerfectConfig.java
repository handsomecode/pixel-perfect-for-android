package is.handsome.pixelperfect;

public class PixelPerfectConfig {

    private static PixelPerfectConfig instance = null;

    public boolean useVolumeButtons;

    public boolean useVolumeButtons() {
        return useVolumeButtons;
    }

    public static PixelPerfectConfig get() {
        if (instance == null) {
            instance = new PixelPerfectConfig();
        }
        return instance;
    }

    private PixelPerfectConfig () {}
}
