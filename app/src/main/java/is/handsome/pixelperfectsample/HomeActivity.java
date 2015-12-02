package is.handsome.pixelperfectsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ToggleButton;

import is.handsome.pixelperfectsample.library.PixelPerfect;
import is.handsome.pixelperfectsample.util.VisualWarnTree;
import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {

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

    public void startSecondActivity(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void setPixelPerfectToolVisibility(boolean enabled) {
        if (enabled) {
            PixelPerfect.create().show(getApplicationContext());
        } else {
            PixelPerfect.hide(getApplicationContext());
        }
    }
}
