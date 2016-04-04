package is.handsome.pixelperfect;

import android.graphics.Bitmap;
import android.text.TextUtils;

final class Image {

    private final String name;
    private final Bitmap bitmap;

    public Image(String name, Bitmap bitmap) {
        this.name = !TextUtils.isEmpty(name) ? name : "No name";
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getName() {
        return name;
    }
}
