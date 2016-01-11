package is.handsome.pixelperfect.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import is.handsome.pixelperfect.PixelPerfectCallbacks;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class PixelPerfectActionsView extends FrameLayout implements View.OnClickListener {

    public enum Corner {
        BOTTOM, TOP
    }

    private View moveModeView;
    private View opacityImageView;
    private View mockupsImageView;
    private View cancelView;
    private PixelPerfectCallbacks.ActionsListener actionsListener;

    public PixelPerfectActionsView(Context context) {
        super(context);
        inflate(getContext(), R.layout.view_pixel_perfect_actions_menu, this);

        moveModeView = findViewById(R.id.pixel_perfect_options_move_mode_text_view);

        opacityImageView = findViewById(R.id.pixel_perfect_options_opacity_image_view);
        opacityImageView.setOnClickListener(this);

        mockupsImageView = findViewById(R.id.pixel_perfect_options_mockups_image_view);
        mockupsImageView.setOnClickListener(this);

        cancelView = findViewById(R.id.pixel_perfect_cancel_frame_layout);
        cancelView.setOnClickListener(this);

        setVisibility(GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int) getResources().getDimension(R.dimen.pixel_perfect_options_view_width), (int) getResources().getDimension(R.dimen.pixel_perfect_options_view_height));
    }

    @Override
    public void onClick(View view) {
        if (view == mockupsImageView) {
            view.setSelected(!view.isSelected());
            if (view.isSelected() && opacityImageView.isSelected()) {
                opacityImageView.setSelected(false);
            }
            if (actionsListener != null) {
                actionsListener.onMockupsClicked(view.isSelected());
            }

        } else if (view == opacityImageView) {
            view.setSelected(!view.isSelected());
            if (view.isSelected() && mockupsImageView.isSelected()) {
                mockupsImageView.setSelected(false);
            }
            if (actionsListener != null) {
                actionsListener.onOpacityClicked(view.isSelected());
            }

        } else if (view == cancelView) {
            mockupsImageView.setSelected(false);
            opacityImageView.setSelected(false);
            setVisibility(GONE);
            if (actionsListener != null) {
                actionsListener.onCancelClicked();
            }

        }
    }

    public boolean inBounds(int x, int y) {
        return (PixelPerfectUtils.inViewBounds(opacityImageView, x, y)
                || PixelPerfectUtils.inViewBounds(mockupsImageView, x, y)
                || PixelPerfectUtils.inViewBounds(cancelView, x, y));
    }

    public void animate(final boolean toRight, final Corner corner) {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int actionViewSize = (int) getResources().getDimension(R.dimen.pixel_perfect_action_button_size);
                int cancelSize = (int) getResources().getDimension(R.dimen.pixel_perfect_options_cancel_size);
                int radiusSize = (int) getResources().getDimension(R.dimen.pixel_perfect_options_radius_size);
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int offsetX = toRight ? cancelSize / 2 : actionViewSize - cancelSize / 2;

                int fromX = centerX - actionViewSize / 2;
                int fromY = centerY - actionViewSize / 2;

                if (corner == null) {
                    animate(moveModeView, fromX, centerX - offsetX, fromY, centerY - radiusSize);
                    animate(opacityImageView, fromX, toRight ? centerX + radiusSize - actionViewSize : centerX - radiusSize, fromY, fromY);
                    animate(mockupsImageView, fromX, centerX - offsetX, fromY, centerY + radiusSize - actionViewSize);
                } else if (corner == Corner.BOTTOM) {
                    animate(moveModeView, fromX, centerX - offsetX, fromY, (getHeight() - getWidth()) / 2);
                    animate(opacityImageView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, 0);
                    animate(mockupsImageView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, fromY - actionViewSize / 2 + cancelSize / 2);
                } else {
                    animate(moveModeView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, fromY + actionViewSize / 2 - cancelSize / 2);
                    animate(opacityImageView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, getHeight() - actionViewSize);
                    animate(mockupsImageView, fromX, centerX - offsetX, fromY, getHeight() - actionViewSize);
                }
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void animate(View actionButton, int fromX, int toX, int fromY, int toY) {
        actionButton.setVisibility(VISIBLE);
        ObjectAnimator.ofFloat(actionButton, "translationX", fromX, toX).setDuration(200).start();
        ObjectAnimator.ofFloat(actionButton, "translationY", fromY, toY).setDuration(200).start();
        ObjectAnimator.ofFloat(actionButton, "scaleX", 0.1f, 1).setDuration(200).start();
        ObjectAnimator.ofFloat(actionButton, "scaleY", 0.1f, 1).setDuration(200).start();
        ObjectAnimator.ofFloat(actionButton, "alpha", 0f, 1).setDuration(150).start();
    }

    public void setActionsListener(PixelPerfectCallbacks.ActionsListener actionsListener) {
        this.actionsListener = actionsListener;
    }
}
