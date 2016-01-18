package is.handsome.pixelperfect;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import is.handsome.pixelperfect.ui.PixelPerfectLayout;
import is.handsome.pixelperfect.ui.SettingsView;

public class PixelPerfectController {

    public interface LayoutListener {

        void onBackPressed();

        void onMockupOverlayMoveX(int dx);
        void onMockupOverlayMoveY(int dy);

        void onOffsetViewMoveX(int dx);
        void onOffsetViewMoveY(int dy);

        void hideOffsetView();
        void showOffsetView(int xPos, int yPos);
        void openSettings();
    }

    public interface SettingsListener {
        void onSetImageAlpha(float alpha);
        void onUpdateImage(Bitmap bitmap);
        void onFixOffset();
    }

    private final WindowManager windowManager;

    private SettingsView settingsView;
    private PixelPerfectLayout pixelPerfectLayout;
    private ViewGroup offsetPixelsView;
    private TextView offsetXTextView;
    private TextView offsetYTextView;
    private PixelPerfectCallbacks.ControllerListener listener;

    private WindowManager.LayoutParams offsetPixelsViewParams;
    private WindowManager.LayoutParams overlayParams;

    private int fixedOffsetX = 0;
    private int fixedOffsetY = 0;
    private boolean settingsOpened = false;

    public PixelPerfectController(Context context) {
        Context applicationContext = context.getApplicationContext();
        pixelPerfectLayout = new PixelPerfectLayout(applicationContext);
        settingsView = new SettingsView(applicationContext);
        offsetPixelsView = (ViewGroup) LayoutInflater.from(applicationContext).inflate(R.layout.view_offset_pixels, null);
        offsetXTextView = (TextView) offsetPixelsView.findViewById(R.id.offset_x_text_view);
        offsetYTextView = (TextView) offsetPixelsView.findViewById(R.id.offset_y_text_view);

        windowManager = (WindowManager) applicationContext.getSystemService(Service.WINDOW_SERVICE);
        addViewsToWindow(context);
    }

    public void setListener(PixelPerfectCallbacks.ControllerListener listener) {
        this.listener = listener;
    }

    private void addViewsToWindow(Context context) {
        addOverlayMockup(context);
        addOffsetPixelsView();
        addSettingsView();
        show();
    }

    private void addOverlayMockup(Context context) {
        int overlayWidth = PixelPerfectUtils.getWindowWidth(windowManager)
                + 2 * (int) context.getResources().getDimension(R.dimen.overlay_border_size);

        overlayParams = new WindowManager.LayoutParams(
                overlayWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        overlayParams.gravity = Gravity.TOP;

        overlayParams.y = -1 * (int) context.getResources().getDimension(R.dimen.overlay_border_size);
        fixedOffsetY = Math.abs(overlayParams.y);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && PixelPerfectUtils.isTranslucentStatusBar(context)) {
            overlayParams.y -= (int) context.getResources().getDimension(R.dimen.android_status_bar_height);
            fixedOffsetY = Math.abs(overlayParams.y);
        }
        windowManager.addView(pixelPerfectLayout, overlayParams);

        pixelPerfectLayout.setLayoutListener(new LayoutListener() {

            @Override
            public void onBackPressed() {
                if (settingsView.getVisibility() == View.VISIBLE) {
                    settingsView.onBack();
                } else {
                    if (listener != null) {
                        listener.onClosePixelPerfect();
                    }
                }
            }

            @Override
            public void onMockupOverlayMoveX(int dx) {
                overlayParams.x += dx;
                windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);

                offsetXTextView.setText(fixedOffsetX + overlayParams.x + " px");
            }

            @Override
            public void onMockupOverlayMoveY(int dy) {
                overlayParams.y += dy;
                windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);

                offsetYTextView.setText(fixedOffsetY + overlayParams.y + " px");
            }

            @Override
            public void onOffsetViewMoveX(int dx) {
                offsetPixelsViewParams.x += dx;
                windowManager.updateViewLayout(offsetPixelsView, offsetPixelsViewParams);
            }

            @Override
            public void onOffsetViewMoveY(int dy) {
                offsetPixelsViewParams.y += dy;
                windowManager.updateViewLayout(offsetPixelsView, offsetPixelsViewParams);
            }

            @Override
            public void hideOffsetView() {
                offsetPixelsView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void showOffsetView(int xPos, int yPos) {
                offsetPixelsView.setVisibility(View.VISIBLE);
                offsetPixelsViewParams.x = xPos - offsetPixelsView.getWidth() / 2;
                offsetPixelsViewParams.y = yPos - offsetPixelsView.getHeight() * 3;
                windowManager.updateViewLayout(offsetPixelsView, offsetPixelsViewParams);
            }

            @Override
            public void openSettings() {
                settingsView.updateOpacityProgress(pixelPerfectLayout.getImageAlpha());
                settingsView.updateOffset(fixedOffsetX + overlayParams.x, fixedOffsetY + overlayParams.y);
                settingsView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addSettingsView() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(settingsView, params);

        settingsView.setVisibility(View.GONE);
        settingsView.setListener(new SettingsListener() {
            @Override
            public void onSetImageAlpha(float alpha) {
                pixelPerfectLayout.setImageAlpha(alpha);
            }

            @Override
            public void onUpdateImage(Bitmap bitmap) {
                pixelPerfectLayout.updateImage(bitmap);
            }

            @Override
            public void onFixOffset() {
                fixedOffsetX = -1 * overlayParams.x;
                fixedOffsetY = -1 * overlayParams.y;

                offsetXTextView.setText(fixedOffsetX + overlayParams.x + " px");
                offsetYTextView.setText(fixedOffsetY + overlayParams.y + " px");
            }
        });
        settingsView.setImageOverlay(1);
    }

    private void addOffsetPixelsView() {
        offsetPixelsViewParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        offsetPixelsViewParams.gravity = Gravity.TOP | Gravity.START;
        offsetPixelsView.setVisibility(View.GONE);
        windowManager.addView(offsetPixelsView, offsetPixelsViewParams);
    }

    public void show() {
        pixelPerfectLayout.setImageVisible(true);
        //pixelPerfectLayout.setControlsLayerVisible(true);
        pixelPerfectLayout.setVisibility(View.VISIBLE);
        if (settingsOpened) {
            settingsView.setVisibility(View.VISIBLE);
            settingsOpened = false;
        }
    }

    public void hide() {
        pixelPerfectLayout.setImageVisible(false);
        //pixelPerfectLayout.setControlsLayerVisible(false);
        pixelPerfectLayout.setVisibility(View.GONE);
        offsetPixelsView.setVisibility(View.GONE);
        if (settingsView.getVisibility() == View.VISIBLE) {
            settingsOpened = true;
        }
        settingsView.setVisibility(View.GONE);
    }

    public void destroy() {
        windowManager.removeView(pixelPerfectLayout);
        windowManager.removeView(offsetPixelsView);
        windowManager.removeView(settingsView);
    }
}
