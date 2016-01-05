package is.handsome.pixelperfect.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import is.handsome.pixelperfect.R;

public class PixelPerfectMoveModeView extends ImageView {

    PixelPerfectLayout.MoveMode currentMoveMode = PixelPerfectLayout.MoveMode.ANY_DIRECTION;

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
        return currentMoveMode;
    }

    public void setState(PixelPerfectLayout.MoveMode moveMode) {
        switch (moveMode) {
            case HORIZONTAL:
                currentMoveMode = PixelPerfectLayout.MoveMode.HORIZONTAL;
                setImageResource(R.drawable.ic_move_horizontal);
                break;
            case VERTICAL:
                currentMoveMode = PixelPerfectLayout.MoveMode.VERTICAL;
                setImageResource(R.drawable.ic_move_vertical);
                break;
            default:
                currentMoveMode = PixelPerfectLayout.MoveMode.ANY_DIRECTION;
                setImageResource(R.drawable.ic_move_any);
                break;
        }
    }

    public void toNextState() {
        if (getState() == PixelPerfectLayout.MoveMode.ANY_DIRECTION) {
            setState(PixelPerfectLayout.MoveMode.VERTICAL);
        } else if (getState() == PixelPerfectLayout.MoveMode.VERTICAL) {
            setState(PixelPerfectLayout.MoveMode.HORIZONTAL);
        } else {
            setState(PixelPerfectLayout.MoveMode.ANY_DIRECTION);
        }
    }
}