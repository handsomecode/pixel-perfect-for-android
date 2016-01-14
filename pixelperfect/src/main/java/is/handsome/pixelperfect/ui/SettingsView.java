package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import is.handsome.pixelperfect.PixelPerfectController;
import is.handsome.pixelperfect.R;
import is.handsome.pixelperfect.ScreensNamesAdapter;

public class SettingsView extends FrameLayout {

    private View exitButton;
    private PixelPerfectController.SettingsListener settingsListener;

    private FrameLayout pixelPerfectMockupsFrameLayout;
    private SeekBar opacitySeekBar;

    public SettingsView(Context context) {
        super(context);
        init();
    }

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setListener(PixelPerfectController.SettingsListener listener) {
        this.settingsListener = listener;
    }

    private void init() {
        inflate(getContext(), R.layout.layout_settings, this);
        exitButton = findViewById(R.id.settings_exit_button);
        OnClickListener exitButtonListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                settingsListener.closeSettings();
            }
        };
        exitButton.setOnClickListener(exitButtonListener);
        findViewById(R.id.settings_opacity_confirm_image_view).setOnClickListener(exitButtonListener);

        initOpacityWidget();
        initMockupsWidget();
    }

    public void updateOpacityProgress(float currentAlpha) {
        opacitySeekBar.setProgress((int) (currentAlpha * 100));
    }

    private void initOpacityWidget() {
        opacitySeekBar = (SeekBar) findViewById(R.id.settings_opacity_seek_bar);
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (settingsListener != null) {
                    settingsListener.onSetImageAlpha(progress / 100.0f);
                }
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
        spinner.setAdapter(new ScreensNamesAdapter(getContext()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullName = (String) parent.getItemAtPosition(position);
                if (settingsListener != null) {
                    settingsListener.onUpdateImage(fullName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(1, true);
    }
}
