package is.handsome.pixelperfectsample.library.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class PixelPerfectMoveModeView extends TextView {

    public PixelPerfectMoveModeView(Context context) {
        super(context);
    }

    public PixelPerfectMoveModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PixelPerfectMoveModeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PixelPerfectMoveModeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PixelPerfectLayout.MoveMode getState() {
        if (getText().equals("V")) {
            return PixelPerfectLayout.MoveMode.VERTICAL;
        } else if (getText().equals("H")) {
            return PixelPerfectLayout.MoveMode.HORIZONTAL;
        }
        return PixelPerfectLayout.MoveMode.ALL_DIRECTIONS;
    }

    public void setState(PixelPerfectLayout.MoveMode moveMode) {
        switch (moveMode) {
            case HORIZONTAL:
                setText("H");
                break;
            case VERTICAL:
                setText("V");
                break;
            default:
                setText("All");
                break;
        }
    }

    public void toNextState() {
        if (getState() == PixelPerfectLayout.MoveMode.ALL_DIRECTIONS) {
            setState(PixelPerfectLayout.MoveMode.VERTICAL);
        } else if (getState() == PixelPerfectLayout.MoveMode.VERTICAL) {
            setState(PixelPerfectLayout.MoveMode.HORIZONTAL);
        } else {
            setState(PixelPerfectLayout.MoveMode.ALL_DIRECTIONS);
        }
    }
}
