package is.handsome.pixelperfectsample.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import timber.log.Timber;

public class VisualWarnTree extends Timber.Tree {

    private final Context context;
    private boolean firstToast;

    public VisualWarnTree(Context context) {
        this.context = context.getApplicationContext();
        this.firstToast = true;
    }

    @Override
    public void v(String message, Object... args) {
        // do nothing
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        // do nothing
    }

    @Override
    public void d(String s, Object... objects) {
        // do nothing
    }

    @Override
    public void d(Throwable throwable, String s, Object... objects) {
        // do nothing
    }

    @Override
    public void i(String s, Object... objects) {
        // do nothing
    }

    @Override
    public void i(Throwable throwable, String s, Object... objects) {
        // do nothing
    }

    @Override
    public void w(String s, Object... objects) {
        final String message = String.format(s, objects);
        showToast("W: " + message);
    }

    @Override
    public void w(Throwable throwable, String s, Object... objects) {
        final String message = String.format(s, objects);
        showToast("W: " + message);
    }

    @Override
    public void e(String s, Object... objects) {
        final String message = String.format(s, objects);
        showToast("E: " + message);
    }

    @Override
    public void e(Throwable throwable, String s, Object... objects) {
        final String message = String.format(s, objects);
        showToast("E: " + message);
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        // do nothing
    }

    private void showToast(final String message) {
        final Runnable showToast = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                if (firstToast) {
                    Toast.makeText(context, "See LogCat for more details", Toast.LENGTH_LONG).show();
                    firstToast = false;
                }
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()) {
            showToast.run();
        } else {
            new Handler(Looper.getMainLooper()).post(showToast);
        }
    }
}