package is.handsome.pixelperfect;

public class PixelPerfect {

    public static PixelPerfectBuilder create() {
        return new PixelPerfectBuilder();
    }

    public static boolean isCreated() {
        return PixelPerfectBuilder.isCreated();
    }

    public static boolean isShown() {
        if (isCreated()) {
            return PixelPerfectBuilder.isShown();
        }
        return false;
    }

    public static void show() {
        PixelPerfectBuilder.show();
    }

    public static void hide() {
        PixelPerfectBuilder.hide();
    }

    public static void destroy() {
        PixelPerfectBuilder.destroy();
    }
}
