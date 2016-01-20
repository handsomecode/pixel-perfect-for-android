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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
                Timber.plant(new VisualWarnTree(this));
            }
            Fabric.with(this, new Crashlytics());
            PixelPerfect.create().useVolumeButtons(true).show(this);
        }
        init();
    }

    public void init() {
        pixelPerfectButton = (Button) findViewById(R.id.pixel_perfect_button);
        if (!PixelPerfect.isShown()) {
            pixelPerfectButton.setText(R.string.button_show);
        }
        pixelPerfectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PixelPerfect.isShown()) {
                    PixelPerfect.hide();
                    pixelPerfectButton.setText(R.string.button_show);
                } else {
                    PixelPerfect.show();
                    pixelPerfectButton.setText(R.string.button_hide);
                }
            }
        });
    }
}
