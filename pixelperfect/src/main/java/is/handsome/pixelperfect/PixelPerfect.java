package is.handsome.pixelperfect;

public class PixelPerfect {

    public static PixelPerfectBuilder create() {
        return new PixelPerfectBuilder();
    }

    public static void hide() {
        PixelPerfectBuilder.hide();
    }

    public static boolean isShown() {
        return PixelPerfectBuilder.isShown();
    }
}
