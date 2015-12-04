package is.handsome.pixelperfect;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import is.handsome.pixelperfect.ui.PixelPerfectLayout;

public class PixelPerfectController implements View.OnLongClickListener {

    private final WindowManager windowManager;
    private Context context;

    private PixelPerfectConfig pixelPerfectConfig;
    private PixelPerfectLayout pixelPerfectLayout;
    private FrameLayout floatingButton;

    private WindowManager.LayoutParams floatingButtonParams;

    private final View.OnTouchListener emptyTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent rawEvent) {
            return false;
        }
    };

    private final View.OnTouchListener floatingButtonTouchListener = new View.OnTouchListener() {
        int initialX, initialY;
        int downTouchX = -1, downTouchY = -1;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (downTouchX == -1) {
                        initialX = floatingButtonParams.x;
                        initialY = floatingButtonParams.y;
                        downTouchX = (int) event.getRawX();
                        downTouchY = (int) event.getRawY();
                    }
                    floatingButtonParams.x = initialX + (int) event.getRawX() - downTouchX;
                    floatingButtonParams.y = initialY + (int) event.getRawY() - downTouchY;
                    windowManager.updateViewLayout(floatingButton, floatingButtonParams);
                    break;
                case MotionEvent.ACTION_UP:
                    floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button).setOnTouchListener(emptyTouchListener);
                    break;
            }
            return true;
        }
    };

    public PixelPerfectController(Application context, PixelPerfectConfig config) {

        this.context = context;
        this.pixelPerfectConfig = config;

        pixelPerfectLayout = new PixelPerfectLayout(context);
        floatingButton = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_pixel_perfect_floating_button, null);

        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        addViewsToWindow();
    }

    private void addViewsToWindow() {
        addOverlayModel();
        addFloatingToggleButton();
        show();
    }

    private void addOverlayModel() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(pixelPerfectLayout, params);

        pixelPerfectLayout.setLayoutListener(new PixelPerfectCallbacks.LayoutListener() {

            @Override
            public void onCloseActionsView() {
                showToggleButton();
            }
        });
    }

    private void addFloatingToggleButton() {
        floatingButtonParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        floatingButtonParams.gravity = Gravity.TOP | Gravity.START;
        floatingButtonParams.x = PixelPerfectHelper.getWindowWidth(windowManager)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_action_button_size)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_floating_button_margin);
        floatingButtonParams.y = PixelPerfectHelper.getWindowHeight(windowManager)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_action_button_size)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_floating_button_margin) - 72;

        windowManager.addView(floatingButton, floatingButtonParams);

        floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button).setOnLongClickListener(this);
        floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button).setOnClickListener(createClickAndDoubleClickListener());
    }

    private void updateDisplayedPixelPerfectContext(boolean isPixelPerfectContext) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) pixelPerfectLayout.getLayoutParams();
        layoutParams.flags = isPixelPerfectContext ? WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(pixelPerfectLayout, layoutParams);
    }

    public void show() {
        pixelPerfectLayout.setImageVisible(true);
        pixelPerfectLayout.setControlsLayerVisible(true);
        pixelPerfectLayout.setVisibility(View.VISIBLE);
        floatingButton.setVisibility(View.VISIBLE);
    }

    public void hide() {
        pixelPerfectLayout.setImageVisible(false);
        pixelPerfectLayout.setControlsLayerVisible(false);
        pixelPerfectLayout.setVisibility(View.GONE);
        floatingButton.setVisibility(View.GONE);
    }

    public void destroy() {
        windowManager.removeView(pixelPerfectLayout);
        windowManager.removeView(floatingButton);
    }

    @Override
    public boolean onLongClick(View v) {
        floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button).setOnTouchListener(floatingButtonTouchListener);
        return true;
    }

    private View.OnClickListener createClickAndDoubleClickListener() {
        return new View.OnClickListener() {
            int clickCounter = 0;

            @Override
            public void onClick(View v) {
                clickCounter++;
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (clickCounter == 1) {
                            if (pixelPerfectLayout.isPixelPerfectContext()) {
                                pixelPerfectLayout.showActionsView(floatingButtonParams.x, floatingButtonParams.y);
                                hideToggleButton();
                            }
                        }
                        clickCounter = 0;
                    }
                };
                if (clickCounter == 1) {
                    ((ToggleButton)floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button))
                            .setChecked(!((ToggleButton)floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button)).isChecked());
                    handler.postDelayed(runnable, 250);
                } else if (clickCounter >= 2) {
                    pixelPerfectLayout.setPixelPerfectContext(((ToggleButton)floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button)).isChecked());
                    updateDisplayedPixelPerfectContext(((ToggleButton)floatingButton.findViewById(R.id.pixel_perfect_switch_context_toggle_button)).isChecked());
                }
            }
        };
    }

    private void hideToggleButton() {
        floatingButton.setAlpha(0f);
        floatingButton.setScaleX(0.1f);
        floatingButton.setScaleY(0.1f);
        floatingButton.setVisibility(View.INVISIBLE);
    }

    private void showToggleButton() {
        floatingButton.setVisibility(View.VISIBLE);
        floatingButton.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(100);
    }
}
