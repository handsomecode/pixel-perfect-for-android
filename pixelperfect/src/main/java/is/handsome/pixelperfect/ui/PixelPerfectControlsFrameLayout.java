package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import is.handsome.pixelperfect.PixelPerfectCallbacks;
import is.handsome.pixelperfect.PixelPerfectUtils;
import is.handsome.pixelperfect.R;
import is.handsome.pixelperfect.ScreensNamesAdapter;


public class PixelPerfectControlsFrameLayout extends FrameLayout {

    private PixelPerfectActionsView pixelPerfectActionsView;
    private FrameLayout pixelPerfectOpacityFrameLayout;
    private FrameLayout pixelPerfectMockupsFrameLayout;
    private FrameLayout pixelPerfectDiffPixelsFrameLayout;

    private PixelPerfectCallbacks.ControlsListener controlsListener;

    public PixelPerfectControlsFrameLayout(Context context) {
        super(context);
        init();
    }

    public PixelPerfectControlsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelPerfectControlsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PixelPerfectControlsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if (inBounds(x, y)) {
            return false;
        } else {
            return super.onInterceptTouchEvent(event);
        }
    }

    public void setControlsListener(PixelPerfectCallbacks.ControlsListener controlsListener) {
        this.controlsListener = controlsListener;
    }

    public void showActionsView(int pointX, int pointY) {
        pixelPerfectActionsView.setX(pointX - getResources().getDimension(R.dimen.pixel_perfect_options_view_width) / 2 + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2);
        pixelPerfectActionsView.setY(pointY - getResources().getDimension(R.dimen.pixel_perfect_options_view_height) / 2 + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2);
        pixelPerfectActionsView.animate(pointX < getWidth() / 2, getCorner(pointY, getResources().getDimension(R.dimen.pixel_perfect_options_radius_size)));
        pixelPerfectActionsView.setVisibility(VISIBLE);
    }

    public boolean inBounds(int x, int y) {
        return pixelPerfectActionsView.getVisibility() == VISIBLE && pixelPerfectActionsView.inBounds(x, y)
                || pixelPerfectOpacityFrameLayout.getVisibility() == VISIBLE && PixelPerfectUtils.inViewBounds(pixelPerfectOpacityFrameLayout, x, y)
                || pixelPerfectMockupsFrameLayout.getVisibility() == VISIBLE && PixelPerfectUtils.inViewBounds(pixelPerfectMockupsFrameLayout, x, y);
    }

    public void updateOpacityProgress(float currentAlpha) {
        ((SeekBar) pixelPerfectOpacityFrameLayout.findViewById(R.id.pixel_perfect_opacity_seek_bar)).
                setProgress((int) (currentAlpha * 100));
    }

    public void setDiffPixelsViewVisibility() {
        if (pixelPerfectDiffPixelsFrameLayout.getVisibility() == VISIBLE) {
            pixelPerfectDiffPixelsFrameLayout.setVisibility(INVISIBLE);
        } else {
            pixelPerfectDiffPixelsFrameLayout.setVisibility(VISIBLE);
        }
    }

    public void updateDiffXPixelsData(int x) {
        ((TextView) pixelPerfectDiffPixelsFrameLayout.findViewById(R.id.diff_pixels_x)).setText(x + " px");
    }

    public void updateDiffYPixelsData(int y) {
        ((TextView) pixelPerfectDiffPixelsFrameLayout.findViewById(R.id.diff_pixels_y)).setText(y + "\npx");
    }

    private void init() {
        inflate(getContext(), R.layout.layout_pixel_perfect_controls_layer, this);

        pixelPerfectActionsView = new PixelPerfectActionsView(getContext());
        addView(pixelPerfectActionsView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addActionsListeners();

        initOpacityWidget();
        initMockupsWidget();

        pixelPerfectDiffPixelsFrameLayout = (FrameLayout) findViewById(R.id.controls_diff_pixels);
    }

    private void addActionsListeners() {
        pixelPerfectActionsView.setActionsListener(new PixelPerfectCallbacks.ActionsListener() {

            @Override
            public void onOpacityClicked(boolean isSelected) {
                if (isSelected) {
                    pixelPerfectMockupsFrameLayout.setVisibility(INVISIBLE);
                    showOpacityView();
                } else {
                    hideOpacityView();
                }
            }

            @Override
            public void onMockupsClicked(boolean isSelected) {
                if (isSelected) {
                    pixelPerfectOpacityFrameLayout.setVisibility(INVISIBLE);
                    showMockupsView();
                } else {
                    hideMockupsView();
                }
            }

            @Override
            public void onMoveModeClicked(PixelPerfectLayout.MoveMode moveMode) {
                if (controlsListener != null) {
                    controlsListener.onChangeMoveMode(moveMode);
                }
            }

            @Override
            public void onCancelClicked() {
                hideOpacityView();
                hideMockupsView();
                if (controlsListener != null) {
                    controlsListener.onCloseActionsView();
                }
            }
        });
    }

    private void initOpacityWidget() {
        pixelPerfectOpacityFrameLayout = (FrameLayout) findViewById(R.id.pixel_perfect_opacity_frame_layout);
        ((SeekBar) pixelPerfectOpacityFrameLayout.findViewById(R.id.pixel_perfect_opacity_seek_bar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (controlsListener != null) {
                    controlsListener.onSetImageAlpha(progress / 100.0f);
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
        pixelPerfectMockupsFrameLayout = (FrameLayout) findViewById(R.id.pixel_perfect_mockups_frame_layout);
        Spinner spinner = (Spinner) pixelPerfectMockupsFrameLayout.findViewById(R.id.pixel_perfect_mockups_spinner);
        spinner.setAdapter(new ScreensNamesAdapter(getContext()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullName = (String) parent.getItemAtPosition(position);
                if (controlsListener != null) {
                    controlsListener.onUpdateImage(fullName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(1, true);
    }

    private PixelPerfectActionsView.Corner getCorner(int pointY, float size) {
        if (pointY + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2 - getHeight() * 0.05 < size) {
            return PixelPerfectActionsView.Corner.TOP;
        } else if (pointY + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2 + size > getHeight() * 0.95) {
            return PixelPerfectActionsView.Corner.BOTTOM;
        }
        return null;
    }

    private void showMockupsView() {
        pixelPerfectMockupsFrameLayout.setAlpha(0.1f);
        pixelPerfectMockupsFrameLayout.setVisibility(VISIBLE);
        pixelPerfectMockupsFrameLayout.animate().alpha(1f).setDuration(200);
    }

    private void hideMockupsView() {
        if (pixelPerfectMockupsFrameLayout.getVisibility() == VISIBLE) {
            pixelPerfectMockupsFrameLayout.animate().alpha(0.1f).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    pixelPerfectMockupsFrameLayout.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void showOpacityView() {
        pixelPerfectOpacityFrameLayout.setAlpha(0.1f);
        pixelPerfectOpacityFrameLayout.setVisibility(VISIBLE);
        pixelPerfectOpacityFrameLayout.animate().alpha(1f).setDuration(200);
    }

    private void hideOpacityView() {
        if (pixelPerfectOpacityFrameLayout.getVisibility() == VISIBLE) {
            pixelPerfectOpacityFrameLayout.animate().alpha(0.1f).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    pixelPerfectOpacityFrameLayout.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
