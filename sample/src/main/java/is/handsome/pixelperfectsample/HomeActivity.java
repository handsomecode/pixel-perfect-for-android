package is.handsome.pixelperfectsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import is.handsome.pixelperfect.PixelPerfect;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private CheckBox pixelPerfectCheckBox;
    private View permissionLinearLayout;
    private ImageView imageView;

    private int[] portraitDimens = {1440, 1080, 720, 480};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
                Timber.plant(new VisualWarnTree(this));
            } else {
                Fabric.with(this, new Crashlytics());
            }
        }
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

    private void init() {
        pixelPerfectCheckBox = (CheckBox) findViewById(R.id.pixel_perfect_checkbox);
        permissionLinearLayout = findViewById(R.id.pixel_perfect_permission_linear_layout);
        imageView = (ImageView) findViewById(R.id.home_image_view);

        int screenMinDimension = Math.min(PixelPerfectUtils.getWindowWidth(this), PixelPerfectUtils.getWindowHeight(this));
        final String assetsFolderName = getPreferredFolderName(screenMinDimension);
        Bitmap bitmap = PixelPerfectUtils.getBitmapFromAssets(this, screenMinDimension == PixelPerfectUtils.getWindowWidth(this)
                ? assetsFolderName + "/portrait.png" : assetsFolderName + "/landscape.png");
        imageView.setImageBitmap(bitmap);

        if (PixelPerfect.isShown()) {
            pixelPerfectCheckBox.setChecked(true);
        }

        pixelPerfectCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PixelPerfect.Config config = new PixelPerfect.Config.Builder()
                            .overlayImagesAssetsPath(assetsFolderName)
                            .build();
                    PixelPerfect.show(HomeActivity.this, config);
                } else {
                    PixelPerfect.hide();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (PixelPerfect.isShown()) {
            PixelPerfect.hide();
        }
    }

    public void openPermissionSettings(View view) {
        PixelPerfect.askForPermission(this);
    }

    private String getPreferredFolderName(int screenMinDimension) {
        for (int width: portraitDimens) {
            if (width <= screenMinDimension) {
                return "overlays-" + String.valueOf(width);
            }
        }
        return "overlays-" + 480;
    }
}
