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

    public static void updatePosition() {
        PixelPerfectBuilder.updateFloatingViewPositionAfterRotation();
    }

    public static void innerHide() {
        PixelPerfectBuilder.innerHide();
    }

    public static void innerShow() {
        PixelPerfectBuilder.innerShow();
    }
}
