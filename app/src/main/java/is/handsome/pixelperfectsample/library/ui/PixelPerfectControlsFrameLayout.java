package is.handsome.pixelperfectsample.library.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import is.handsome.pixelperfectsample.R;
import is.handsome.pixelperfectsample.library.PixelPerfectCallbacks;
import is.handsome.pixelperfectsample.library.PixelPerfectHelper;
import is.handsome.pixelperfectsample.library.ScreensNamesAdapter;

public class PixelPerfectControlsFrameLayout extends FrameLayout implements View.OnLongClickListener {

    private ToggleButton actionsFloatingToggleButton;
    private FrameLayout pixelPerfectOpacityFrameLayout;
    private FrameLayout pixelPerfectModelsFrameLayout;
    private PixelPerfectActionsView pixelPerfectActionsView;

    private PixelPerfectCallbacks.ControlsListener controlsListener;

    private final OnTouchListener emptyTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent rawEvent) {
            return false;
        }
    };

    private final OnTouchListener floatingButtonTouchListener = new OnTouchListener() {
        int pointX, pointY;
        int offsetX, offsetY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pointX = (int) event.getX();
                    pointY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (pointX == 0 && pointY == 0) {
                        pointX = (int) event.getX();
                        pointY = (int) event.getY();
                    }
                    offsetX = (int) event.getRawX() - pointX;
                    offsetY = (int) event.getRawY() - pointY;
                    actionsFloatingToggleButton.setX(offsetX);
                    actionsFloatingToggleButton.setY(offsetY);
                    break;
                case MotionEvent.ACTION_UP:
                    actionsFloatingToggleButton.setOnTouchListener(emptyTouchListener);
                    break;
            }
            return true;
        }
    };

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

    public void setControlsListener(PixelPerfectCallbacks.ControlsListener controlsListener) {
        this.controlsListener = controlsListener;
    }

    private void init() {
        inflate(getContext(), R.layout.layout_pixel_perfect_controls_layer, this);

        initFloatingToggleButton();

        pixelPerfectActionsView = new PixelPerfectActionsView(getContext());
        addView(pixelPerfectActionsView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addActionsListeners();

        initOpacityWidget();
        initModelsWidget();
    }

    private void initFloatingToggleButton() {
        actionsFloatingToggleButton = (ToggleButton) findViewById(R.id.pixel_perfect_toolbar_switch_context_toggle_button);
        actionsFloatingToggleButton.setOnLongClickListener(this);
        actionsFloatingToggleButton.setOnClickListener(createClickAndDoubleClickListener());
    }

    private OnClickListener createClickAndDoubleClickListener() {
        return new OnClickListener() {
            int clickCounter = 0;

            @Override
            public void onClick(View v) {
                clickCounter++;
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (clickCounter == 1) {
                            showOptionsView();
                            hideToggleButton();
                        }
                        clickCounter = 0;
                    }
                };
                if (clickCounter == 1) {
                    actionsFloatingToggleButton.setChecked(!actionsFloatingToggleButton.isChecked());
                    handler.postDelayed(runnable, 250);
                } else if (clickCounter >= 2) {
                    if (controlsListener != null) {
                        controlsListener.onChangePixelPerfectContext();
                    }
                }
            }
        };
    }

    private void addActionsListeners() {
        pixelPerfectActionsView.setActionsListener(new PixelPerfectCallbacks.ActionsListener() {

            @Override
            public void onOpacityClicked(boolean isSelected) {
                if (isSelected) {
                    pixelPerfectModelsFrameLayout.setVisibility(INVISIBLE);
                    showOpacityView();
                } else {
                    hideOpacityView();
                }
            }

            @Override
            public void onModelsClicked(boolean isSelected) {
                if (isSelected) {
                    pixelPerfectOpacityFrameLayout.setVisibility(INVISIBLE);
                    showModelsView();
                } else {
                    hideModelsView();
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
                hideModelsView();
                showToggleButton();
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
        pixelPerfectOpacityFrameLayout.setVisibility(INVISIBLE);
    }

    private void initModelsWidget() {
        pixelPerfectModelsFrameLayout = (FrameLayout) findViewById(R.id.pixel_perfect_models_frame_layout);
        Spinner spinner = (Spinner) pixelPerfectModelsFrameLayout.findViewById(R.id.pixel_perfect_models_spinner);
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

    @Override
    public boolean onLongClick(View view) {
        actionsFloatingToggleButton.setOnTouchListener(floatingButtonTouchListener);
        return true;
    }

    private PixelPerfectActionsView.Corner getCorner(float size) {
        if (actionsFloatingToggleButton.getY() + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2 - getHeight() * 0.05 < size) {
            return PixelPerfectActionsView.Corner.TOP;
        } else if (actionsFloatingToggleButton.getY() + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2 + size > getHeight() * 0.95) {
            return PixelPerfectActionsView.Corner.BOTTOM;
        }
        return null;
    }

    private void showOptionsView() {
        pixelPerfectActionsView.setX(actionsFloatingToggleButton.getX() - getResources().getDimension(R.dimen.pixel_perfect_options_view_width) / 2 + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2);
        pixelPerfectActionsView.setY(actionsFloatingToggleButton.getY() - getResources().getDimension(R.dimen.pixel_perfect_options_view_height) / 2 + getResources().getDimension(R.dimen.pixel_perfect_action_button_size) / 2);
        pixelPerfectActionsView.animate(actionsFloatingToggleButton.getX() < getWidth() / 2, getCorner(getResources().getDimension(R.dimen.pixel_perfect_options_radius_size)));
        pixelPerfectActionsView.setVisibility(VISIBLE);
    }

    private void hideToggleButton() {
        actionsFloatingToggleButton.setAlpha(0f);
        actionsFloatingToggleButton.setScaleX(0.1f);
        actionsFloatingToggleButton.setScaleY(0.1f);
        actionsFloatingToggleButton.setVisibility(View.INVISIBLE);
    }

    private void showToggleButton() {
        actionsFloatingToggleButton.setVisibility(VISIBLE);
        actionsFloatingToggleButton.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(100);
    }

    private void showModelsView() {
        pixelPerfectModelsFrameLayout.setAlpha(0.1f);
        pixelPerfectModelsFrameLayout.setVisibility(VISIBLE);
        pixelPerfectModelsFrameLayout.animate().alpha(1f).setDuration(200);
    }

    private void hideModelsView() {
        if (pixelPerfectModelsFrameLayout.getVisibility() == VISIBLE) {
            pixelPerfectModelsFrameLayout.animate().alpha(0.1f).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    pixelPerfectModelsFrameLayout.setVisibility(View.INVISIBLE);
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

    public boolean inBounds(int x, int y) {
        return actionsFloatingToggleButton.getVisibility() == VISIBLE && PixelPerfectHelper.inViewBounds(actionsFloatingToggleButton, x, y)
                || pixelPerfectActionsView.getVisibility() == VISIBLE && pixelPerfectActionsView.inBounds(x, y)
                || pixelPerfectOpacityFrameLayout.getVisibility() == VISIBLE && PixelPerfectHelper.inViewBounds(pixelPerfectOpacityFrameLayout, x, y)
                || pixelPerfectModelsFrameLayout.getVisibility() == VISIBLE && PixelPerfectHelper.inViewBounds(pixelPerfectModelsFrameLayout, x, y);

    }
}
