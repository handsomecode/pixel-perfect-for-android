package is.handsome.pixelperfectsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import is.handsome.pixelperfect.PixelPerfect;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private Button pixelPerfectButton;
    private View permissionLinearLayout;

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
            pixelPerfectButton.setVisibility(View.VISIBLE);
            permissionLinearLayout.setVisibility(View.GONE);
        } else {
            pixelPerfectButton.setVisibility(View.GONE);
            permissionLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        pixelPerfectButton = (Button) findViewById(R.id.pixel_perfect_button);
        permissionLinearLayout = findViewById(R.id.pixel_perfect_permission_linear_layout);

        if (PixelPerfect.isShown()) {
            pixelPerfectButton.setText(R.string.button_hide);
        }

        pixelPerfectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PixelPerfect.isShown()) {
                    PixelPerfect.hide();
                    pixelPerfectButton.setText(R.string.button_show);
                } else {
                    PixelPerfect.Config config = new PixelPerfect.Config.Builder()
                            .overlayImagesAssetsPath("overlays")
                            .overlayInitialImageName("im_cat.png")
                            //.overlayScaleFactor(scaleFactor)
                            .build();
                    PixelPerfect.show(HomeActivity.this, config);
                    pixelPerfectButton.setText(R.string.button_hide);
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
}
