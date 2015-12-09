package is.handsome.pixelperfect;

import is.handsome.pixelperfect.ui.PixelPerfectLayout;

public class PixelPerfectCallbacks {

    public interface ControlsListener {

        void onSetImageAlpha(float alpha);

        void onUpdateImage(String fullName);

        void onCloseActionsView();

        void onChangeMoveMode(PixelPerfectLayout.MoveMode moveMode);
    }

    public interface ActionsListener {

        void onOpacityClicked(boolean isSelected);

        void onModelsClicked(boolean isSelected);

        void onMoveModeClicked(PixelPerfectLayout.MoveMode moveMode);

        void onCancelClicked();
    }

    public interface LayoutListener {

        void onCloseActionsView();

        void onClosePixelPerfect();
    }

    public interface ControllerListener {

        void onClosePixelPerfect();
    }
}
