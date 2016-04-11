package is.handsome.pixelperfect;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Thanks to https://gist.github.com/steveliles/11116937

class ActivityLifeCycleObserver implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = ActivityLifeCycleObserver.class.getSimpleName();
    public static final long CHECK_DELAY = 500;

    public interface Listener {

        void onBecameForeground();
        void onBecameBackground();
        void onActivityStopped(Activity activity);
    }

    private static ActivityLifeCycleObserver instance;

    private boolean foreground = true, paused = true;
    private Handler handler = new Handler();
    private List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Runnable checkRunnable;

    public static ActivityLifeCycleObserver get(Context context){
        if (instance == null) {
            Context applicationContext = context;
            if (!(applicationContext instanceof Application)) {
                applicationContext = context.getApplicationContext();
            }
            return init((Application) applicationContext);
        }
        return instance;
    }

    public static ActivityLifeCycleObserver get(){
        if (instance == null) {
            throw new IllegalStateException (
                    "Foreground is not initialized - invoke " +
                            "at least once with parameterized init/get");
        }
        return instance;
    }

    public ActivityLifeCycleObserver() {
        checkRunnable = new Runnable(){
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    Log.i(TAG, "went background");
                    for (Listener listener : listeners) {
                        try {
                            listener.onBecameBackground();
                        } catch (Exception exception) {
                            Log.i(TAG, "Listener threw exception!", exception);
                        }
                    }
                } else {
                    Log.i(TAG, "still foreground");
                }
            }
        };
    }

    public boolean isForeground(){
        return foreground;
    }

    public boolean isBackground(){
        return !foreground;
    }

    public void addListener(Listener listener){
        listeners.add(listener);
    }

    public void removeListener(Listener listener){
        listeners.remove(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (checkRunnable != null)
            handler.removeCallbacks(checkRunnable);

        if (wasBackground){
            Log.i(TAG, "went foreground");
            for (Listener listener : listeners) {
                try {
                    listener.onBecameForeground();
                } catch (Exception exception) {
                    Log.i(TAG, "Listener threw exception!", exception);
                }
            }
        } else {
            Log.i(TAG, "still foreground");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;
        if (checkRunnable != null) {
            handler.removeCallbacks(checkRunnable);
        }
        handler.postDelayed(checkRunnable, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        OverlayStateStore overlayStateStore = OverlayStateStore.getInstance(activity);
        if (overlayStateStore.isPixelPerfectActive()) {
            PixelPerfect.show(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        for (Listener listener : listeners) {
            try {
                listener.onActivityStopped(activity);
            } catch (Exception exception) {
                Log.i(TAG, "Listener threw exception!", exception);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    private static ActivityLifeCycleObserver init(Application application){
        if (instance == null) {
            instance = new ActivityLifeCycleObserver();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }
}