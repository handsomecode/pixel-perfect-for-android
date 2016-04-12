package is.handsome.pixelperfect;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

class Overlay {

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

        void onUpdateImage(Bitmap bitmap, boolean resetPosition);

        void onFixOffset();

        boolean onInverseChecked(boolean saveOpacity);
    }

    private WindowManager windowManager;

    private SettingsView settingsView;
    private OverlayView overlayView;
    private ViewGroup offsetPixelsView;
    private TextView offsetXTextView;
    private TextView offsetYTextView;

    private WindowManager.LayoutParams offsetPixelsViewParams;
    private WindowManager.LayoutParams overlayParams;
    private WindowManager.LayoutParams settingsParams;

    private int fixedOffsetX = 0;
    private int fixedOffsetY = 0;

    private int overlayBorderSize;
    private int statusBarHeight;
    private String offsetTextTemplate;
    private float overlayScaleFactor = 1;

    private OverlayStateStore overlayStateStore;
    private OverlayPositionStore overlayPositionStore;

    public Overlay(Activity activity, boolean restoreState) {
        initOverlay(activity, restoreState);
        show();
    }

    public Overlay(Activity activity, PixelPerfect.Config config) {
        overlayScaleFactor = config.getOverlayScaleFactor();
        initOverlay(activity, false);
        if (!TextUtils.isEmpty(config.getOverlayImageAssetsPath())) {
            settingsView.setImageAssetsPath(config.getOverlayImageAssetsPath());
        }
        if (!TextUtils.isEmpty(config.getOverlayInitialImageName())) {
            settingsView.setImageOverlay(config.getOverlayInitialImageName());
        }
        show();
    }

    public void show() {
        overlayView.setImageVisible(true);
        overlayView.setVisibility(View.VISIBLE);
        if (overlayStateStore.getSettingsState() != OverlayStateStore.SettingsState.CLOSED) {
            displaySettingsView();
            if (overlayStateStore.getSettingsState() == OverlayStateStore.SettingsState.OPENED_IMAGES) {
                settingsView.openImagesSettingsScreen();
            }
        }
    }

    public boolean isShown() {
        return overlayView.getVisibility() == View.VISIBLE;
    }

    public void calculatePositionAfterRotation() {
        if (overlayPositionStore.getPositionX() != 0 && overlayPositionStore.getPositionY() != 0) {
            overlayParams.x = overlayPositionStore.getPositionX();
            overlayParams.y = overlayPositionStore.getPositionY();

            fixedOffsetX = overlayPositionStore.getFixedOffsetX();
            fixedOffsetY = overlayPositionStore.getFixedOffsetY();
        } else {
            calculateFirstRotationPosition();
        }
        overlayView.updateNoImageTextViewSize();
        windowManager.updateViewLayout(overlayView, overlayParams);
    }

    public void resetState() {
        overlayStateStore.reset();
    }

    public void saveState(Activity activity) {
        overlayStateStore.savePixelPerfectActive(true);
        overlayStateStore.saveSize(overlayView.getWidth(), overlayView.getHeight());
        overlayPositionStore.saveOrientation(activity.getResources().getConfiguration().orientation);
        overlayPositionStore.savePosition(overlayParams.x, overlayParams.y);
        overlayPositionStore.saveFixedOffset(fixedOffsetX, fixedOffsetY);
        if (overlayView.isNoImageOverlay()) {
            overlayStateStore.saveImageName("no_image");
        } else {
            overlayStateStore.saveImageName(settingsView.currentImageName());
        }
        overlayStateStore.saveAssetsFolderName(settingsView.getOverlayImageAssetsPath());
        overlayStateStore.saveOpacity(overlayView.getImageAlpha());
        overlayStateStore.saveInverse(settingsView.isInverse());
        overlayStateStore.saveSettingsState(settingsView.getSettingsState());
    }

    public void hide() {
        overlayView.setImageVisible(false);
        overlayView.setVisibility(View.GONE);
        offsetPixelsView.setVisibility(View.GONE);
        settingsView.setVisibility(View.GONE);
    }

    public void destroy() {
        windowManager.removeView(overlayView);
        windowManager.removeView(offsetPixelsView);
        windowManager.removeView(settingsView);
    }

    private void calculateFirstRotationPosition() {
        int width = Utils.getWindowWidth(windowManager);
        int height = Utils.getWindowHeight(windowManager);

        int overlayMinimumVisibleSize = (int) overlayView.getContext().getResources().getDimension(R.dimen.overlay_minimum_visible_size);

        overlayParams.x = (overlayParams.x + overlayStateStore.getWidth() / 2) * width / height
                - overlayStateStore.getWidth() / 2;

        if (overlayParams.x + overlayStateStore.getWidth() < overlayMinimumVisibleSize) {
            overlayParams.x = overlayMinimumVisibleSize - overlayStateStore.getWidth();
        } else if (overlayParams.x > Utils.getWindowWidth(windowManager) - overlayMinimumVisibleSize) {
            overlayParams.x = Utils.getWindowWidth(windowManager) - overlayMinimumVisibleSize;
        }
        fixedOffsetX = -overlayParams.x;

        overlayParams.y = (overlayParams.y + overlayStateStore.getHeight() / 2) * height / width
                - overlayStateStore.getHeight() / 2;

        if (overlayParams.y + overlayStateStore.getHeight() < overlayMinimumVisibleSize) {
            overlayParams.y = overlayMinimumVisibleSize - overlayStateStore.getHeight();
        } else if (overlayParams.y > Utils.getWindowHeight(windowManager) - overlayMinimumVisibleSize - statusBarHeight) {
            overlayParams.y = Utils.getWindowHeight(windowManager) - overlayMinimumVisibleSize - statusBarHeight;
        }
        fixedOffsetY = -overlayParams.y;
    }

    private void restoreState(Activity activity) {
        if (overlayStateStore.getImageName() != null) {
            settingsView.setImageAssetsPath(overlayStateStore.getAssetsFolderName());
            if (!overlayStateStore.getImageName().equalsIgnoreCase("no_image")) {
                settingsView.setImageOverlay(overlayStateStore.getImageName());
            }
            if (overlayStateStore.isInverse()) {
                settingsView.setInverseMode();
            }
            settingsView.updateOpacityProgress(overlayStateStore.getOpacity());
            if (overlayPositionStore.getOrientation() != Configuration.ORIENTATION_UNDEFINED &&
                    activity.getResources().getConfiguration().orientation != overlayPositionStore.getOrientation()) {
                overlayPositionStore.saveOrientation(activity.getResources().getConfiguration().orientation);
                calculatePositionAfterRotation();
            }
        }
    }

    private void initOverlay(final Activity activity, boolean restoreState) {
        Context applicationContext = activity.getApplicationContext();
        overlayStateStore = OverlayStateStore.getInstance(applicationContext);
        overlayPositionStore = OverlayPositionStore.getInstance(applicationContext);
        if (!restoreState) {
            resetState();
        }
        overlayView = new OverlayView(applicationContext);
        settingsView = new SettingsView(applicationContext);
        offsetPixelsView = (ViewGroup) LayoutInflater.from(applicationContext).inflate(R.layout.view_offset_pixels, null);
        offsetXTextView = (TextView) offsetPixelsView.findViewById(R.id.offset_x_text_view);
        offsetYTextView = (TextView) offsetPixelsView.findViewById(R.id.offset_y_text_view);

        overlayBorderSize = (int) applicationContext.getResources().getDimension(R.dimen.overlay_border_size);
        statusBarHeight = Utils.getStatusBarHeight(activity);
        offsetTextTemplate = applicationContext.getString(R.string.offset_text);

        windowManager = (WindowManager) applicationContext.getSystemService(Service.WINDOW_SERVICE);
        addViewsToWindow(applicationContext);
        restoreState(activity);
    }

    private void addViewsToWindow(Context context) {
        addOverlayMockup(context);
        addOffsetPixelsView();
        addSettingsView();
    }

    private void addOverlayMockup(final Context context) {
        overlayParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        updateInitialOverlayPosition();
        windowManager.addView(overlayView, overlayParams);

        final int overlayMinimumVisibleSize = (int) context.getResources().getDimension(R.dimen.overlay_minimum_visible_size);

        overlayView.setLayoutListener(new LayoutListener() {

            @Override
            public void onOverlayMoveX(int dx) {
                if (overlayParams.x + dx + overlayView.getWidth() >= overlayMinimumVisibleSize
                        && Utils.getWindowWidth(windowManager) - overlayParams.x - dx >= overlayMinimumVisibleSize) {
                    overlayParams.x += dx;
                    windowManager.updateViewLayout(overlayView, overlayParams);

                    offsetXTextView.setText(String.format(offsetTextTemplate, fixedOffsetX + overlayParams.x));
                }
            }

            @Override
            public void onOverlayMoveY(int dy) {
                if (overlayParams.y + dy + overlayView.getHeight() >= overlayMinimumVisibleSize
                        && Utils.getWindowHeight(windowManager) - statusBarHeight - overlayParams.y - dy >= overlayMinimumVisibleSize) {
                    overlayParams.y += dy;
                    windowManager.updateViewLayout(overlayView, overlayParams);

                    offsetYTextView.setText(String.format(offsetTextTemplate, fixedOffsetY + overlayParams.y));
                }
            }

            @Override
            public void onOverlayUpdate(int width, int height) {
                updateInitialOverlayPosition(width, height);
                windowManager.updateViewLayout(overlayView, overlayParams);
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

    private void updateInitialOverlayPosition(int imageWidth, int imageHeight) {
        overlayParams.gravity = Gravity.TOP | Gravity.START;

        int screenWidth = Utils.getWindowWidth(windowManager);
        int screenHeight = Utils.getWindowHeight(windowManager);

        int marginHorizontal = (screenWidth - imageWidth) / 2;
        int marginVertical = (screenHeight - imageHeight) / 2;

        if (overlayStateStore.getImageName() != null) {
            overlayParams.x = overlayPositionStore.getPositionX();
            fixedOffsetX = overlayPositionStore.getFixedOffsetX();

            overlayParams.y = overlayPositionStore.getPositionY();
            fixedOffsetY = overlayPositionStore.getFixedOffsetY();
        } else {
            overlayParams.x = -1 * overlayBorderSize + (marginHorizontal > 0 ? marginHorizontal : 0);
            fixedOffsetX = -1 * overlayParams.x;

            overlayParams.y = -1 * overlayBorderSize - statusBarHeight + (marginVertical > 0 ? marginVertical : 0);
            fixedOffsetY = -1 * overlayParams.y;
        }

        windowManager.removeView(overlayView);
        windowManager.removeView(settingsView);
        windowManager.addView(overlayView, overlayParams);
        windowManager.addView(settingsView, settingsParams);
    }

    private void updateInitialOverlayPosition() {
        overlayParams.gravity = Gravity.TOP | Gravity.START;

        if (overlayStateStore.getImageName() != null) {
            overlayParams.x = overlayPositionStore.getPositionX();
            fixedOffsetX = overlayPositionStore.getFixedOffsetX();

            overlayParams.y = overlayPositionStore.getPositionY();
            fixedOffsetY = overlayPositionStore.getFixedOffsetY();
        } else {
            overlayParams.x = -1 * overlayBorderSize + statusBarHeight;
            fixedOffsetX = -1 * overlayParams.x;

            overlayParams.y = -1 * overlayBorderSize + statusBarHeight;
            fixedOffsetY = -1 * overlayParams.y;
        }
    }

    private void displaySettingsView() {
        settingsView.updateOpacityProgress(overlayView.getImageAlpha());
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
                overlayView.setImageAlpha(alpha);
            }

            @Override
            public void onUpdateImage(Bitmap bitmap, boolean resetPosition) {
                overlayView.updateImage(bitmap, overlayScaleFactor);
                if (resetPosition) {
                    overlayPositionStore.reset();
                }
            }

            @Override
            public void onFixOffset() {
                fixOffset();
            }

            @Override
            public boolean onInverseChecked(boolean saveOpacity) {
                boolean inverted = overlayView.invertImageBitmap(saveOpacity);
                settingsView.updateOpacityProgress(overlayView.getImageAlpha());
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
        offsetPixelsView.setVisibility(View.INVISIBLE);
        windowManager.addView(offsetPixelsView, offsetPixelsViewParams);
    }
}
