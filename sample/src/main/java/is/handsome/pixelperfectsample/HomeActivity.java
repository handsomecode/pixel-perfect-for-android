package is.handsome.pixelperfectsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import is.handsome.pixelperfect.PixelPerfect;

public class HomeActivity extends AppCompatActivity {

    private ImageView imageView;
    private CheckBox pixelPerfectCheckBox;
    private View permissionLinearLayout;

    private int[] portraitDimens = {1440, 1080, 720, 480};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final String overlayImagesAssetsPath = constructOverlayImagesAssetsPath();
        final String coverImageAssetsPath = constructCoverImageAssetsPath(overlayImagesAssetsPath);

        imageView = (ImageView) findViewById(R.id.home_image_view);
        Bitmap bitmap = SampleUtils.getBitmapFromAssets(this, coverImageAssetsPath);
        imageView.setImageBitmap(bitmap);

        pixelPerfectCheckBox = (CheckBox) findViewById(R.id.pixel_perfect_checkbox);
        if (PixelPerfect.isShown()) {
            pixelPerfectCheckBox.setChecked(true);
        }
        pixelPerfectCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PixelPerfect.Config config = new PixelPerfect.Config.Builder()
                            .overlayImagesAssetsPath(overlayImagesAssetsPath)
                            .build();
                    PixelPerfect.show(HomeActivity.this, config);
                } else {
                    PixelPerfect.hide();
                }
            }
        });

        permissionLinearLayout = findViewById(R.id.pixel_perfect_permission_linear_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PixelPerfect.hasPermission(this)) {
            pixelPerfectCheckBox.setVisibility(View.VISIBLE);
            permissionLinearLayout.setVisibility(View.GONE);
        } else {
            pixelPerfectCheckBox.setVisibility(View.GONE);
            permissionLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void openPermissionSettings(View view) {
        PixelPerfect.askForPermission(this);
    }


    private String constructOverlayImagesAssetsPath() {
        int screenMinDimension = Math.min(SampleUtils.getWindowWidth(this), SampleUtils.getWindowHeight(this));
        for (int width : portraitDimens) {
            if (width <= screenMinDimension) {
                return "overlays-" + String.valueOf(width);
            }
        }
        return "overlays-" + 480;
    }

    private String constructCoverImageAssetsPath(String overlayImagesAssetsPath) {
        int screenMinDimension = Math.min(SampleUtils.getWindowWidth(this), SampleUtils.getWindowHeight(this));
        return screenMinDimension == SampleUtils.getWindowWidth(this)
                ? overlayImagesAssetsPath + "/portrait.png" : overlayImagesAssetsPath + "/landscape.png";
    }
}
