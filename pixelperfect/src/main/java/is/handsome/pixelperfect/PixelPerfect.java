package is.handsome.pixelperfect;

import android.content.Context;

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

    public static void setImage(String imageName) {
        PixelPerfectBuilder.setImage(imageName);
    }

    public static boolean hasPermission(Context context) {
        return PixelPerfectBuilder.hasPermission(context);
    }

    public static void askForPermission(Context context) {
        PixelPerfectBuilder.askForPermission(context);
    }
}
