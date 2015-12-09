package is.handsome.pixelperfectsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import is.handsome.pixelperfect.PixelPerfect;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private boolean pixelPerfectOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new VisualWarnTree(this));
        }
    }

    public void changePixelPerfectState(View view) {
        setPixelPerfectToolVisibility(((ToggleButton) view).isChecked());
    }

    public void onImageClick(View view) {
        pixelPerfectOpened = !pixelPerfectOpened;
        setPixelPerfectToolVisibility(pixelPerfectOpened);
    }

    private void setPixelPerfectToolVisibility(boolean enabled) {
        if (enabled) {
            PixelPerfect.create().show(getApplicationContext());
        } else {
            PixelPerfect.hide();
        }
    }
}
