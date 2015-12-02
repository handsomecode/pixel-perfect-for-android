package is.handsome.pixelperfectsample.library;

import android.content.Context;

public class PixelPerfect {

    public static PixelPerfectBuilder create() {
        return new PixelPerfectBuilder();
    }

    public static void hide(Context context) {
        PixelPerfectBuilder.hide(context);
    }
}
