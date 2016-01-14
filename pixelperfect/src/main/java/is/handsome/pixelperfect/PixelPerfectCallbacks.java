package is.handsome.pixelperfect;

public class PixelPerfectCallbacks {

    public interface ControlsListener {

        void onSetImageAlpha(float alpha);

        void onUpdateImage(String fullName);
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
