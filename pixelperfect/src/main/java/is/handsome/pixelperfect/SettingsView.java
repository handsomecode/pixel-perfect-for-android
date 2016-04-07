package is.handsome.pixelperfect;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SettingsView extends FrameLayout {

    public interface AdapterListener {
        void onItemSelected(int position);
    }

    private Overlay.SettingsListener settingsListener;
    private List<Image> images = new ArrayList<>();
    private String overlayImageAssetsPath = "pixelperfect";

    private SeekBar opacitySeekBar;
    private TextView opacityTitleTextView;
    private LinearLayout firstScreenSettingsLinearLayout;
    private View secondScreenSettingsView;
    private ImageView exitButton;
    private TextView imageNameTextView;
    private String imageName;
    private TextView offsetTextView;
    private CheckBox inverseCheckbox;

    private RecyclerView recyclerView;
    private TextView emptyListTextView;

    private View backgroundLayout;
    private View mainLayout;
    private View toolbarView;
    private View opacityLayout;

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

    public void setListener(Overlay.SettingsListener listener) {
        this.settingsListener = listener;
    }

    public void setInverseMode() {
        inverseCheckbox.setChecked(!inverseCheckbox.isChecked());
    }

    public boolean isInverse() {
        return inverseCheckbox.isChecked();
    }

    public void setImageAssetsPath(String overlayImageAssetsPath) {
        this.overlayImageAssetsPath = overlayImageAssetsPath;
        updateImagesContent();
        updateAdapter();
    }

    public void setImageOverlay(int position) {
        if (position >= 0 && position < images.size()) {
            settingsListener.onUpdateImage(images.get(position).getBitmap());
            imageNameTextView.setText(images.get(position).getName());
            ((ImagesAdapter) recyclerView.getAdapter()).setSelectedPosition(position);
        }
    }

    public void setImageOverlay(String imageName) {
        int position = indexOfImage(imageName);
        this.imageName = imageName;
        setImageOverlay(position);
    }

    public int indexOfImage(String imageName) {
        for (Image image : images) {
            if (image.getName().equalsIgnoreCase(imageName)) {
                return images.indexOf(image);
            }
        }
        return -1;
    }

    public void updateOpacityProgress(float currentAlpha) {
        opacitySeekBar.setProgress((int) (currentAlpha * 100));
    }

    public void onBack() {
        if (secondScreenSettingsView.getVisibility() == VISIBLE) {
            secondScreenSettingsView.setVisibility(GONE);
            firstScreenSettingsLinearLayout.setVisibility(VISIBLE);
            exitButton.setImageResource(R.drawable.ic_settings_cancel);
        } else {
            setVisibility(GONE);
        }
    }

    public void openImagesSettingsScreen() {
        secondScreenSettingsView.setVisibility(VISIBLE);
        exitButton.setImageResource(R.drawable.ic_back);
    }

    public void updateOffset(int x, int y) {
        offsetTextView.setText(String.format(getResources().getString(R.string.settings_offset_text), x, y));
    }

    public String currentImageName() {
        return imageName;
    }

    public String getOverlayImageAssetsPath() {
        return overlayImageAssetsPath;
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

        backgroundLayout = findViewById(R.id.settings_background_frame_layout);
        mainLayout = findViewById(R.id.settings_main_linear_layout);
        opacityLayout = findViewById(R.id.settings_opacity_linear_layout);
        toolbarView = findViewById(R.id.settings_toolbar_linear_layout);

        initOpacityWidget();
        initImagesRecyclerView();

        findViewById(R.id.settings_images_option_linear_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.settings_first_screen_linear_layout).setVisibility(GONE);
                secondScreenSettingsView.setVisibility(VISIBLE);
                exitButton.setImageResource(R.drawable.ic_back);
            }
        });

        findViewById(R.id.settings_offset_option_linear_layout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsListener.onFixOffset();
                offsetTextView.setText(String.format(getResources().getString(R.string.settings_offset_text), 0, 0));
            }
        });

        firstScreenSettingsLinearLayout = (LinearLayout) findViewById(R.id.settings_first_screen_linear_layout);
        secondScreenSettingsView = findViewById(R.id.settings_second_screen_frame_layout);
        imageNameTextView = (TextView) findViewById(R.id.settings_image_name);
        offsetTextView = (TextView) findViewById(R.id.settings_offset_text_view);
        inverseCheckbox = (CheckBox) findViewById(R.id.inverse_checkbox);
        inverseCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsListener.onInverseChecked(!isChecked);
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
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideNonOpacityElements();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                showNonOpacityElements();
            }
        });
        opacityTitleTextView = (TextView) findViewById(R.id.opacity_title_text_view);
    }

    private void hideNonOpacityElements() {
        backgroundLayout.setBackgroundColor(Color.TRANSPARENT);
        mainLayout.setBackgroundColor(Color.TRANSPARENT);
        toolbarView.setVisibility(INVISIBLE);
        for (int i = 0; i < firstScreenSettingsLinearLayout.getChildCount(); i++) {
            firstScreenSettingsLinearLayout.getChildAt(i).setVisibility(INVISIBLE);
        }
        opacityLayout.setVisibility(VISIBLE);
        opacityLayout.setBackgroundResource(R.color.black_50_alpha);
        opacityTitleTextView.setTextColor(Color.WHITE);
    }

    private void showNonOpacityElements() {
        backgroundLayout.setBackgroundResource(R.color.black_30_alpha);
        mainLayout.setBackgroundResource(R.drawable.bg_settings_view_border);
        toolbarView.setVisibility(VISIBLE);
        for (int i = 0; i < firstScreenSettingsLinearLayout.getChildCount(); i++) {
            firstScreenSettingsLinearLayout.getChildAt(i).setVisibility(VISIBLE);
        }
        opacityLayout.setBackgroundColor(Color.TRANSPARENT);
        opacityTitleTextView.setTextColor(Color.BLACK);
    }

    private void initImagesRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.settings_images_recycler_view);
        emptyListTextView = (TextView) findViewById(R.id.settings_empty_list_text_view);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        updateImagesContent();
        updateAdapter();
    }

    private void updateAdapter() {
        if (images.isEmpty()) {
            recyclerView.setVisibility(GONE);
            emptyListTextView.setVisibility(VISIBLE);
            emptyListTextView.setText(String.format(getResources().getString(R.string.settings_empty_list_message),
                    overlayImageAssetsPath));
        } else {
            recyclerView.setVisibility(VISIBLE);
            emptyListTextView.setVisibility(GONE);
            recyclerView.setAdapter(new ImagesAdapter(getContext(), images, new AdapterListener() {

                @Override
                public void onItemSelected(int position) {
                    if (settingsListener != null) {
                        settingsListener.onUpdateImage(images.get(position).getBitmap());
                        if (inverseCheckbox.isChecked()) {
                            settingsListener.onInverseChecked(true);
                        }
                        imageNameTextView.setText(images.get(position).getName());
                        imageName = images.get(position).getName();
                        exitSettingsView();
                    }
                }
            }));
        }
    }

    private void updateImagesContent() {
        String[] filenames;

        try {
            filenames = getContext().getAssets().list(overlayImageAssetsPath);
        } catch (IOException e) {
            filenames = new String[0];
        }
        images.clear();
        for (int i = 0; i < filenames.length; i++) {
            //TODO: add bitmap decoding max size
            Bitmap bitmap = Utils.getBitmapFromAssets(getContext(), overlayImageAssetsPath + "/" + filenames[i]);
            final Image image = new Image(filenames[i], bitmap);
            images.add(image);
        }
    }

    private void exitSettingsView() {
        firstScreenSettingsLinearLayout.setVisibility(VISIBLE);
        secondScreenSettingsView.setVisibility(GONE);
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
