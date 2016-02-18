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

class AppLifeCycleObserver implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = AppLifeCycleObserver.class.getSimpleName();
    public static final long CHECK_DELAY = 500;

    public interface Listener {

        void onBecameForeground();
        void onBecameBackground();
    }

    private static AppLifeCycleObserver instance;

    private boolean foreground = true, paused = true;
    private Handler handler = new Handler();
    private List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Runnable checkRunnable;

    public static AppLifeCycleObserver get(Context context){
        if (instance == null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext instanceof Application) {
                return init((Application) applicationContext);
            }
            throw new IllegalStateException (
                    "Foreground is not initialised and " +
                            "cannot obtain the Application object");
        }
        return instance;
    }

    public static AppLifeCycleObserver get(){
        if (instance == null) {
            throw new IllegalStateException (
                    "Foreground is not initialized - invoke " +
                            "at least once with parameterized init/get");
        }
        return instance;
    }

    private static AppLifeCycleObserver init(Application application){
        if (instance == null) {
            instance = new AppLifeCycleObserver();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
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

        handler.postDelayed(checkRunnable = new Runnable(){
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
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}