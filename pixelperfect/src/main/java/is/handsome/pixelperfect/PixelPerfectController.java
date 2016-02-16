package is.handsome.pixelperfect;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.text.TextUtils;
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
        void onOverlayMoveX(int dx);

        void onOverlayMoveY(int dy);

        void onOverlayUpdate(int imageWidth, int imageHeight);

        void onOffsetViewMoveX(int dx);

        void onOffsetViewMoveY(int dy);

        void hideOffsetView();

        void showOffsetView(int xPos, int yPos);

        void openSettings(boolean goToImages);

        void setInverseMode();

        void onFixOffset();
    }

    public interface SettingsListener {
        void onSetImageAlpha(float alpha);

        void onUpdateImage(Bitmap bitmap);

        void onFixOffset();

        boolean onInverseChecked(boolean saveOpacity);
    }

    private WindowManager windowManager;

    private SettingsView settingsView;
    private PixelPerfectLayout pixelPerfectLayout;
    private ViewGroup offsetPixelsView;
    private TextView offsetXTextView;
    private TextView offsetYTextView;

    private WindowManager.LayoutParams offsetPixelsViewParams;
    private WindowManager.LayoutParams overlayParams;
    private WindowManager.LayoutParams settingsParams;

    private int fixedOffsetX = 0;
    private int fixedOffsetY = 0;
    private boolean settingsOpened = false;

    private int overlayBorderSize;
    private int statusBarHeight;
    private String offsetTextTemplate;
    private float overlayScaleFactor;

    public PixelPerfectController(Activity activity) {
        initController(activity);
    }

    public PixelPerfectController(Activity activity, PixelPerfect.Config config) {
        overlayScaleFactor = config.getOverlayScaleFactor();
        initController(activity);

        if (!TextUtils.isEmpty(config.getOverlayImageAssetsPath())) {
            settingsView.setImageAssetsPath(config.getOverlayImageAssetsPath());
        }
        if (!TextUtils.isEmpty(config.getOverlayInitialImageName())) {
            settingsView.setImageOverlay(config.getOverlayInitialImageName());
        }
    }

    private void initController(Activity activity) {
        Context applicationContext = activity.getApplicationContext();
        pixelPerfectLayout = new PixelPerfectLayout(applicationContext);
        settingsView = new SettingsView(applicationContext);
        offsetPixelsView = (ViewGroup) LayoutInflater.from(applicationContext).inflate(R.layout.view_offset_pixels, null);
        offsetXTextView = (TextView) offsetPixelsView.findViewById(R.id.offset_x_text_view);
        offsetYTextView = (TextView) offsetPixelsView.findViewById(R.id.offset_y_text_view);

        overlayBorderSize = (int) applicationContext.getResources().getDimension(R.dimen.overlay_border_size);
        statusBarHeight = PixelPerfectUtils.getStatusBarHeight(activity);
        offsetTextTemplate = applicationContext.getString(R.string.offset_text);

        windowManager = (WindowManager) applicationContext.getSystemService(Service.WINDOW_SERVICE);
        addViewsToWindow(applicationContext);
    }

    private void addViewsToWindow(Context context) {
        addOverlayMockup(context);
        addOffsetPixelsView();
        addSettingsView();
        show();
    }

    private void addOverlayMockup(final Context context) {
        overlayParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        setInitialOverlayPosition();
        windowManager.addView(pixelPerfectLayout, overlayParams);

        final int overlayMinimumVisibleSize = (int) context.getResources().getDimension(R.dimen.overlay_minimum_visible_size);

        pixelPerfectLayout.setLayoutListener(new LayoutListener() {

            @Override
            public void onOverlayMoveX(int dx) {
                if (overlayParams.x + dx + pixelPerfectLayout.getWidth() >= overlayMinimumVisibleSize
                        && PixelPerfectUtils.getWindowWidth(windowManager) - overlayParams.x - dx >= overlayMinimumVisibleSize) {
                    overlayParams.x += dx;
                    windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);

                    offsetXTextView.setText(String.format(offsetTextTemplate, fixedOffsetX + overlayParams.x));
                }
            }

            @Override
            public void onOverlayMoveY(int dy) {
                if (overlayParams.y + dy + pixelPerfectLayout.getHeight() >= overlayMinimumVisibleSize
                        && PixelPerfectUtils.getWindowHeight(windowManager) - statusBarHeight - overlayParams.y - dy >= overlayMinimumVisibleSize) {
                    overlayParams.y += dy;
                    windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);

                    offsetYTextView.setText(String.format(offsetTextTemplate, fixedOffsetY + overlayParams.y));
                }
            }

            @Override
            public void onOverlayUpdate(int width, int height) {
                setInitialOverlayPosition(width, height);
                windowManager.updateViewLayout(pixelPerfectLayout, overlayParams);
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
                if (offsetPixelsView.getVisibility() != View.VISIBLE) {
                    offsetPixelsViewParams.x = xPos - offsetPixelsView.getWidth() / 2;
                    offsetPixelsViewParams.y = yPos - offsetPixelsView.getHeight() * 3;
                    windowManager.updateViewLayout(offsetPixelsView, offsetPixelsViewParams);
                    offsetXTextView.setText(String.format(offsetTextTemplate, fixedOffsetX + overlayParams.x));
                    offsetYTextView.setText(String.format(offsetTextTemplate, fixedOffsetY + overlayParams.y));
                    offsetPixelsView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void openSettings(boolean showImages) {
                displaySettingsView();
                if (showImages) {
                    settingsView.openImagesSettingsScreen();
                }
            }

            @Override
            public void setInverseMode() {
                settingsView.setInverseMode();
            }

            @Override
            public void onFixOffset() {
                fixOffset();
            }
        });
    }

    private void setInitialOverlayPosition(int imageWidth, int imageHeight) {
        overlayParams.gravity = Gravity.TOP | Gravity.START;

        int screenWidth = PixelPerfectUtils.getWindowWidth(windowManager);
        int screenHeight = PixelPerfectUtils.getWindowHeight(windowManager);

        int marginHorizontal = (screenWidth - imageWidth) / 2;
        int marginVertical = (screenHeight - imageHeight) / 2;

        overlayParams.x = -1 * overlayBorderSize + (marginHorizontal > 0 ? marginHorizontal : 0);
        fixedOffsetX = -1 * overlayParams.x;

        overlayParams.y = -1 * overlayBorderSize - statusBarHeight + (marginVertical > 0 ? marginVertical : 0);
        fixedOffsetY = -1 * overlayParams.y;

        windowManager.removeView(pixelPerfectLayout);
        windowManager.removeView(settingsView);
        windowManager.addView(pixelPerfectLayout, overlayParams);
        windowManager.addView(settingsView, settingsParams);
    }

    private void setInitialOverlayPosition() {
        overlayParams.gravity = Gravity.TOP | Gravity.START;

        overlayParams.x = -1 * overlayBorderSize + statusBarHeight;
        fixedOffsetX = -1 * overlayParams.x;

        overlayParams.y = -1 * overlayBorderSize + statusBarHeight;
        fixedOffsetY = -1 * overlayParams.y;
    }

    private void displaySettingsView() {
        settingsView.updateOpacityProgress(pixelPerfectLayout.getImageAlpha());
        settingsView.updateOffset(fixedOffsetX + overlayParams.x, fixedOffsetY + overlayParams.y);
        settingsView.setVisibility(View.VISIBLE);
    }

    private void addSettingsView() {
        settingsParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        settingsParams.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(settingsView, settingsParams);

        settingsView.setVisibility(View.GONE);
        settingsView.setListener(new SettingsListener() {
            @Override
            public void onSetImageAlpha(float alpha) {
                pixelPerfectLayout.setImageAlpha(alpha);
            }

            @Override
            public void onUpdateImage(Bitmap bitmap) {
                pixelPerfectLayout.updateImage(bitmap, overlayScaleFactor);
            }

            @Override
            public void onFixOffset() {
                fixOffset();
            }

            @Override
            public boolean onInverseChecked(boolean saveOpacity) {
                boolean inverted = pixelPerfectLayout.invertImageBitmap(saveOpacity);
                settingsView.updateOpacityProgress(pixelPerfectLayout.getImageAlpha());
                return inverted;
            }
        });
    }

    private void fixOffset() {
        fixedOffsetX = -1 * overlayParams.x;
        fixedOffsetY = -1 * overlayParams.y;

        offsetXTextView.setText(String.format(offsetTextTemplate, fixedOffsetX + overlayParams.x));
        offsetYTextView.setText(String.format(offsetTextTemplate, fixedOffsetY + overlayParams.y));
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
        pixelPerfectLayout.setVisibility(View.VISIBLE);
        if (settingsOpened) {
            settingsView.setVisibility(View.VISIBLE);
            settingsOpened = false;
        }
    }

    public boolean isShown() {
        return pixelPerfectLayout.getVisibility() == View.VISIBLE;
    }

    public void hide() {
        pixelPerfectLayout.setImageVisible(false);
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
