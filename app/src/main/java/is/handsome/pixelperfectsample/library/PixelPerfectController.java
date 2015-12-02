package is.handsome.pixelperfectsample.library;

import android.app.Application;
import android.app.Service;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import is.handsome.pixelperfectsample.R;
import is.handsome.pixelperfectsample.library.ui.PPToggleButton;
import is.handsome.pixelperfectsample.library.ui.PixelPerfectLayout;

public class PixelPerfectController {

    private final WindowManager windowManager;
    private PixelPerfectConfig pixelPerfectConfig;
    private PixelPerfectLayout pixelPerfectLayout;
    private PPToggleButton ppToggleButton;

    public PixelPerfectController(Application context, PixelPerfectConfig config) {

        pixelPerfectConfig = config;

        pixelPerfectLayout = new PixelPerfectLayout(context);
        ppToggleButton = new PPToggleButton(context);
        ((ToggleButton) ppToggleButton.findViewById(R.id.pixel_perfect_toggle_button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pixelPerfectLayout.setPixelPerfectContext();
            }
        });
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        addViewToWindow(pixelPerfectLayout, ppToggleButton);
    }

    private void addViewToWindow(View view1, View view2) {

        WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        // add view to the window
        windowManager.addView(view1, paramsF);

        WindowManager.LayoutParams paramsF2 = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // add view to the window
        windowManager.addView(view2, paramsF2);

        show();
    }

    public void show() {
        pixelPerfectLayout.setImageVisible(true);
        pixelPerfectLayout.setControlsLayerVisible(true);
        pixelPerfectLayout.setVisibility(View.VISIBLE);
        ppToggleButton.setVisibility(View.VISIBLE);
    }

    public void hide() {
        pixelPerfectLayout.setImageVisible(false);
        pixelPerfectLayout.setControlsLayerVisible(false);
        pixelPerfectLayout.setVisibility(View.GONE);
        ppToggleButton.setVisibility(View.GONE);
    }

    public void destroy() {
        windowManager.removeView(pixelPerfectLayout);
        windowManager.removeView(ppToggleButton);
    }
}
