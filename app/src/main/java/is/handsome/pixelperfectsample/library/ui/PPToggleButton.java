package is.handsome.pixelperfectsample.library.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import is.handsome.pixelperfectsample.R;

public class PPToggleButton extends FrameLayout {
    public PPToggleButton(Context context) {
        super(context);
        init();
    }

    public PPToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PPToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PPToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.pp_toggle_button, this);
    }
}
