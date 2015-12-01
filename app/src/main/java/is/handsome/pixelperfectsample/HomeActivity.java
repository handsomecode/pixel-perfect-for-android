package is.handsome.pixelperfectsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import is.handsome.pixelperfectsample.library.ui.PixelPerfectLayout;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

    private PixelPerfectLayout pixelPerfectLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pixelPerfectLayout = (PixelPerfectLayout) findViewById(R.id.home_pixel_perfect_layout);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new VisualWarnTree(this));
        }
    }

    public void changePixelPerfectState(View view) {
        setPixelPerfectToolVisibility(((ToggleButton) view).isChecked());
    }

    public void startSecondActivity(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void setPixelPerfectToolVisibility(boolean enabled) {
        pixelPerfectLayout.setImageVisible(enabled);
        pixelPerfectLayout.setControlsLayerVisible(enabled);
    }
}
