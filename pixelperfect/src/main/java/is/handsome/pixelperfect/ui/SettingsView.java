package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import is.handsome.pixelperfect.ImagesAdapter;
import is.handsome.pixelperfect.PixelPerfectImage;
import is.handsome.pixelperfect.PixelPerfectController;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;

public class SettingsView extends FrameLayout {

    public interface AdapterListener {
        void onItemSelected(int position);
    }

    private PixelPerfectController.SettingsListener settingsListener;
    private List<PixelPerfectImage> images = new ArrayList<>();

    private SeekBar opacitySeekBar;
    private View opacityDemoView;
    private View firstScreenOptionsView;
    private ImageView exitButton;
    private TextView imageNameTextView;
    private TextView offsetTextView;
    private CheckBox inverseCheckbox;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

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

    public void setInverseMode() {
        inverseCheckbox.setChecked(!inverseCheckbox.isChecked());
    }

    public void addUserImages(List<PixelPerfectImage> userImages) {
        if (userImages != null) {
            images.addAll(0, userImages);
        }
    }

    public void setImageOverlay(int position) {
        if (position >= 0 && position < images.size()) {
            settingsListener.onUpdateImage(images.get(position).bitmap);
            imageNameTextView.setText(images.get(position).name);
        }
    }

    public void setImageOverlay(String imageName) {
        int position = indexOfImage(imageName);
        setImageOverlay(position);
    }

    public int indexOfImage(String imageName) {
        for (PixelPerfectImage image: images) {
            if (image.name.equalsIgnoreCase(imageName)) {
                return images.indexOf(image);
            }
        }
        return -1;
    }

    public void updateOpacityProgress(float currentAlpha) {
        opacitySeekBar.setProgress((int) (currentAlpha * 100));
    }

    public void onBack() {
        if (recyclerView.getVisibility() == VISIBLE) {
            recyclerView.setVisibility(GONE);
            firstScreenOptionsView.setVisibility(VISIBLE);
            exitButton.setImageResource(R.drawable.ic_settings_cancel);
        } else {
            setVisibility(GONE);
        }
    }

    public void openImagesSettingsScreen() {
        recyclerView.setVisibility(VISIBLE);
        exitButton.setImageResource(R.drawable.ic_back);
    }

    public void updateOffset(int x, int y) {
        offsetTextView.setText("(" + x + "px, " + y + "px)");
    }

    private void init() {
        inflate(getContext(), R.layout.layout_settings, this);
        exitButton = (ImageView) findViewById(R.id.settings_exit_button);
        OnClickListener exitButtonListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBack();
            }
        };
        exitButton.setOnClickListener(exitButtonListener);

        initOpacityWidget();
        initImagesRecyclerView();

        findViewById(R.id.settings_images_option_linear_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.settings_linear_layout).setVisibility(GONE);
                recyclerView.setVisibility(VISIBLE);
                exitButton.setImageResource(R.drawable.ic_back);
            }
        });

        findViewById(R.id.settings_offset_option_linear_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsListener.onFixOffset();
                offsetTextView.setText("(0px, 0px)");
            }
        });

        firstScreenOptionsView = findViewById(R.id.settings_linear_layout);
        imageNameTextView = (TextView) findViewById(R.id.settings_image_name);
        offsetTextView = (TextView) findViewById(R.id.settings_offset_text_view);
        inverseCheckbox = (CheckBox) findViewById(R.id.inverse_checkbox);
        inverseCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsListener.onInverseChecked(isChecked);
            }
        });
    }

    private void initOpacityWidget() {
        opacitySeekBar = (SeekBar) findViewById(R.id.settings_opacity_seek_bar);
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (settingsListener != null) {
                    settingsListener.onSetImageAlpha(progress / 100.0f);
                    opacityDemoView.setAlpha(progress / 100.0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        opacityDemoView = findViewById(R.id.settings_opacity_demo_view);
    }

    private void initImagesRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.settings_images_recycler_view);

        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        addImagesContent();

        recyclerView.setAdapter(new ImagesAdapter(getContext(), images, new AdapterListener() {

            @Override
            public void onItemSelected(int position) {
                if (settingsListener != null) {
                    inverseCheckbox.setChecked(false);
                    settingsListener.onUpdateImage(images.get(position).bitmap);
                    imageNameTextView.setText(images.get(position).name);
                    exitSettingsView();
                }
            }
        }));
    }

    private void addImagesContent() {
        String[] filenames;

        try {
            filenames = getContext().getAssets().list("pixelperfect");
        } catch (IOException e) {
            filenames = new String[0];
        }
        for (int i = 0; i < filenames.length; i++) {
            PixelPerfectImage pixelPerfectImage = new PixelPerfectImage();
            pixelPerfectImage.name = filenames[i];
            //TODO: add bitmap decoding max size
            pixelPerfectImage.bitmap = PixelPerfectUtils.getBitmapFromAssets(getContext(), "pixelperfect" + "/" + filenames[i]);
            images.add(pixelPerfectImage);
        }
    }

    private void exitSettingsView() {
        firstScreenOptionsView.setVisibility(VISIBLE);
        recyclerView.setVisibility(GONE);
        exitButton.setImageResource(R.drawable.ic_settings_cancel);
        setVisibility(GONE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
            onBack();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
