package is.handsome.pixelperfect;

import android.graphics.Bitmap;

public class PixelPerfectCallbacks {

    public interface ControlsListener {

        void onSetImageAlpha(float alpha);

        void onUpdateImage(Bitmap bitmap);
    }

    public interface ActionsListener {

        void onOpacityClicked(boolean isSelected);

        void onMockupsClicked(boolean isSelected);

        void onCancelClicked();
    }

    public interface ControllerListener {

        void onClosePixelPerfect();
    }
}
