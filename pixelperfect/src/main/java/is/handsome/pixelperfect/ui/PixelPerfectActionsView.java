package is.handsome.pixelperfect.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import is.handsome.pixelperfect.PixelPerfectCallbacks;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class PixelPerfectActionsView extends FrameLayout implements View.OnClickListener {

    public enum Corner {
        BOTTOM, TOP
    }

    private PixelPerfectMoveModeView moveModeView;
    private TextView opacityTextView;
    private TextView modelsTextView;
    private View cancelView;
    private PixelPerfectCallbacks.ActionsListener actionsListener;

    public PixelPerfectActionsView(Context context) {
        super(context);
        inflate(getContext(), R.layout.view_pixel_perfect_actions_menu, this);

        moveModeView = (PixelPerfectMoveModeView) findViewById(R.id.pixel_perfect_options_move_mode_text_view);
        moveModeView.setOnClickListener(this);

        opacityTextView = (TextView) findViewById(R.id.pixel_perfect_options_opacity_text_view);
        opacityTextView.setOnClickListener(this);

        modelsTextView = (TextView) findViewById(R.id.pixel_perfect_options_models_text_view);
        modelsTextView.setOnClickListener(this);

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
        if (view == modelsTextView) {
            view.setSelected(!view.isSelected());
            if (view.isSelected() && opacityTextView.isSelected()) {
                opacityTextView.setSelected(false);
            }
            if (actionsListener != null) {
                actionsListener.onModelsClicked(view.isSelected());
            }

        } else if (view == opacityTextView) {
            view.setSelected(!view.isSelected());
            if (view.isSelected() && modelsTextView.isSelected()) {
                modelsTextView.setSelected(false);
            }
            if (actionsListener != null) {
                actionsListener.onOpacityClicked(view.isSelected());
            }

        } else if (view == moveModeView) {
            moveModeView.toNextState();
            if (actionsListener != null) {
                actionsListener.onMoveModeClicked(moveModeView.getState());
            }

        } else if (view == cancelView) {
            modelsTextView.setSelected(false);
            opacityTextView.setSelected(false);
            setVisibility(GONE);
            if (actionsListener != null) {
                actionsListener.onCancelClicked();
            }

        }
    }

    public boolean inBounds(int x, int y) {
        return (PixelPerfectUtils.inViewBounds(moveModeView, x, y)
                || PixelPerfectUtils.inViewBounds(opacityTextView, x, y)
                || PixelPerfectUtils.inViewBounds(modelsTextView, x, y)
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
                    animate(opacityTextView, fromX, toRight ? centerX + radiusSize - actionViewSize : centerX - radiusSize, fromY, fromY);
                    animate(modelsTextView, fromX, centerX - offsetX, fromY, centerY + radiusSize - actionViewSize);
                } else if (corner == Corner.BOTTOM) {
                    animate(moveModeView, fromX, centerX - offsetX, fromY, (getHeight() - getWidth()) / 2);
                    animate(opacityTextView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, 0);
                    animate(modelsTextView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, fromY - actionViewSize / 2 + cancelSize / 2);
                } else {
                    animate(moveModeView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, fromY + actionViewSize / 2 - cancelSize / 2);
                    animate(opacityTextView, fromX, toRight ? getWidth() - actionViewSize : 0, fromY, getHeight() - actionViewSize);
                    animate(modelsTextView, fromX, centerX - offsetX, fromY, getHeight() - actionViewSize);
                }

                if (Build.VERSION.SDK_INT < 16) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
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
