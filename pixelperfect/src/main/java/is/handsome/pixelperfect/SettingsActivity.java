package is.handsome.pixelperfect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity {

    private FrameLayout pixelPerfectOpacityFrameLayout;
    private FrameLayout pixelPerfectMockupsFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initOpacityWidget();
        initMockupsWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PixelPerfect.innerHide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PixelPerfect.innerShow();
    }

    public void updateOpacityProgress(float currentAlpha) {
        ((SeekBar) pixelPerfectOpacityFrameLayout.findViewById(R.id.pixel_perfect_opacity_seek_bar)).
                setProgress((int) (currentAlpha * 100));
    }

    private void initOpacityWidget() {
        pixelPerfectOpacityFrameLayout = (FrameLayout) findViewById(R.id.settings_opacity_frame_layout);
        ((SeekBar) pixelPerfectOpacityFrameLayout.findViewById(R.id.settings_opacity_seek_bar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*if (controlsListener != null) {
                    controlsListener.onSetImageAlpha(progress / 100.0f);
                }*/
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initMockupsWidget() {
        pixelPerfectMockupsFrameLayout = (FrameLayout) findViewById(R.id.settings_mockups_frame_layout);
        Spinner spinner = (Spinner) pixelPerfectMockupsFrameLayout.findViewById(R.id.settings_mockups_spinner);
        spinner.setAdapter(new ScreensNamesAdapter(this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullName = (String) parent.getItemAtPosition(position);
                /*if (controlsListener != null) {
                    controlsListener.onUpdateImage(fullName);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(1, true);
    }
}
