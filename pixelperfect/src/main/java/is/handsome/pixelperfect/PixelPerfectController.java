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

import is.handsome.pixelperfect.ui.PixelPerfectLayout;

public class PixelPerfectController implements View.OnLongClickListener {

    public interface LayoutListener {

        void onCloseActionsView();
        void onClosePixelPerfect();
        void onMockupOverlayMoveX(int dx);
        void onMockupOverlayMoveY(int dy);
    }

    private final WindowManager windowManager;
    private Context context;

    private PixelPerfectLayout pixelPerfectLayout;
    private FrameLayout floatingFrameLayout;
    private View switchContextButton;
    private PixelPerfectCallbacks.ControllerListener listener;

    private WindowManager.LayoutParams floatingButtonParams;
    private WindowManager.LayoutParams overlayParams;

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
                    windowManager.updateViewLayout(floatingFrameLayout, floatingButtonParams);
                    break;
                case MotionEvent.ACTION_UP:
                    floatingFrameLayout.findViewById(R.id.switch_context_image_button).setOnTouchListener(emptyTouchListener);
                    downTouchX = -1;
                    downTouchY = -1;
                    break;
            }
            return true;
        }
    };

    public PixelPerfectController(Application context) {
        this.context = context;

        pixelPerfectLayout = new PixelPerfectLayout(context);
        floatingFrameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_pixel_perfect_floating_button, null);

        switchContextButton = floatingFrameLayout.findViewById(R.id.switch_context_image_button);
        switchContextButton.setSelected(true);

        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        addViewsToWindow();
    }

    public void setListener(PixelPerfectCallbacks.ControllerListener listener) {
        this.listener = listener;
    }

    public void updateFloatingViewPositionAfterRotation() {
        if (floatingButtonParams != null) {
            int xPosition = floatingButtonParams.x;
            floatingButtonParams.x = floatingButtonParams.y;
            floatingButtonParams.y = xPosition;
            windowManager.updateViewLayout(floatingFrameLayout, floatingButtonParams);
        }
    }

    private void addViewsToWindow() {
        addOverlayMockup();
        addFloatingFrameLayout();
        show();
    }

    private void addOverlayMockup() {
        overlayParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(pixelPerfectLayout, overlayParams);

        pixelPerfectLayout.setLayoutListener(new LayoutListener() {

            @Override
            public void onCloseActionsView() {
                showToggleButton();
            }

            @Override
            public void onClosePixelPerfect() {
                if (listener != null) {
                    listener.onClosePixelPerfect();
                }
            }

            @Override
            public void onMockupOverlayMoveX(int dx) {
                overlayParams.x = overlayParams.x + dx;
                windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);
            }

            @Override
            public void onMockupOverlayMoveY(int dy) {
                overlayParams.y = overlayParams.y + dy;
                windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);
            }
        });
    }

    private void addFloatingFrameLayout() {
        floatingButtonParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        floatingButtonParams.gravity = Gravity.TOP | Gravity.START;
        floatingButtonParams.x = PixelPerfectUtils.getWindowWidth(windowManager)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_action_button_size)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_floating_button_margin);
        floatingButtonParams.y = PixelPerfectUtils.getWindowHeight(windowManager)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_action_button_size)
                - (int) context.getResources().getDimension(R.dimen.pixel_perfect_floating_button_margin);

        windowManager.addView(floatingFrameLayout, floatingButtonParams);

        switchContextButton.setOnLongClickListener(this);
        switchContextButton.setOnClickListener(createClickAndDoubleClickListener());
    }

    private void updateDisplayedPixelPerfectContext(boolean isPixelPerfectContext) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) pixelPerfectLayout.getLayoutParams();
        layoutParams.flags = isPixelPerfectContext ? WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        windowManager.updateViewLayout(pixelPerfectLayout, layoutParams);
    }

    public void show() {
        pixelPerfectLayout.setImageVisible(true);
        pixelPerfectLayout.setControlsLayerVisible(true);
        pixelPerfectLayout.setVisibility(View.VISIBLE);
        floatingFrameLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        pixelPerfectLayout.setImageVisible(false);
        pixelPerfectLayout.setControlsLayerVisible(false);
        pixelPerfectLayout.setVisibility(View.GONE);
        floatingFrameLayout.setVisibility(View.GONE);
    }

    public void destroy() {
        windowManager.removeView(pixelPerfectLayout);
        windowManager.removeView(floatingFrameLayout);
    }

    @Override
    public boolean onLongClick(View v) {
        switchContextButton.setOnTouchListener(floatingButtonTouchListener);
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
                    handler.postDelayed(runnable, 250);
                } else if (clickCounter >= 2) {
                    switchContextButton.setSelected(!switchContextButton.isSelected());
                    pixelPerfectLayout.setPixelPerfectContext(switchContextButton.isSelected());
                    updateDisplayedPixelPerfectContext(switchContextButton.isSelected());
                }
            }
        };
    }

    private void hideToggleButton() {
        floatingFrameLayout.setAlpha(0f);
        floatingFrameLayout.setScaleX(0.1f);
        floatingFrameLayout.setScaleY(0.1f);
        floatingFrameLayout.setVisibility(View.INVISIBLE);
    }

    private void showToggleButton() {
        floatingFrameLayout.setVisibility(View.VISIBLE);
        floatingFrameLayout.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(100);
    }
}
