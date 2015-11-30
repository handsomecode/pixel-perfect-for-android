package is.handsome.pixelperfectsample.library;

import is.handsome.pixelperfectsample.library.ui.PixelPerfectLayout;

public class PixelPerfectCallbacks {

    public interface ControlsListener {

        void onSetImageAlpha(float alpha);

        void onUpdateImage(String fullName);

        void onChangePixelPerfectContext();

        void onChangeMoveMode(PixelPerfectLayout.MoveMode moveMode);
    }

    public interface ActionsListener {

        void onOpacityClicked(boolean isSelected);
        void onModelsClicked(boolean isSelected);
        void onMoveModeClicked(PixelPerfectLayout.MoveMode moveMode);
        void onCancelClicked();
    }
}
