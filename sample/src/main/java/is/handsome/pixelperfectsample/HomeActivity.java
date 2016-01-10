package is.handsome.pixelperfectsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import is.handsome.pixelperfect.PixelPerfect;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

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
    }

    public void onImageClick(View view) {
        if (!PixelPerfect.isShown()) {
            PixelPerfect.create().useVolumeButtons(true).show(this);
        } else {
            PixelPerfect.hide();
        }
    }

    public void onButton1Click(View view) {
        Toast.makeText(this, "Button 1 clicked", Toast.LENGTH_SHORT).show();
    }

    public void onButton2Click(View view) {
        Toast.makeText(this, "Button 2 clicked", Toast.LENGTH_SHORT).show();
    }
}
