package helper.roverslounge.com;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

import static android.content.Context.POWER_SERVICE;
import static android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;


/**
 * This class echoes a string called from JavaScript.
 */
public class RoversLoungeHelper extends CordovaPlugin {
    private PowerManager.WakeLock wakeLock;
    private AudioManager audioManager;


    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Context context = webView.getContext();
        audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        super.initialize(cordova, webView);
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("receivedCall")) {
            this.moveToForeground();
        }else if(action.equals("wakeUp")){
            this.addWindowFlags(cordova.getActivity());
            this.unlock();
            this.wakeup();
        }else if(action.equals("sleepAgain")){
            this.clearFlags();
            this.releaseWakeLock();
        }else if(action.equals("loudSpeaker")){
            this.loudSpeaker();
        }else if(action.equals("earPiece")){
            this.earPiece();
        }else if(action.equals("restoreAudio")){
            this.normalSound();
        }
        return false;
    }


    /**
     * provides loundspeaker during call
     */
    private void loudSpeaker(){
        audioManager.setMode(AudioManager.MODE_RINGTONE);
        audioManager.setSpeakerphoneOn(true);
        Log.d("mic","microphone");
    }


    /**
     * provide earpiece during call
     */

    private void earPiece(){
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * restore to normal functionality
     */

    private void normalSound(){
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(false);
    }
    /**
     * Move app to foreground.
     */
    private void moveToForeground() {
        Activity app = getApp();
        Intent intent = getLaunchIntent();
        Log.d("connect","call endpoint called");

        intent.addFlags(
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);

        app.startActivity(intent);
    }


    /**
     * The activity referenced by cordova.
     *
     * @return The main activity of the app.
     */
    Activity getApp() {
        return this.cordova.getActivity();
    }


    /**
     * Wakes up the device if the screen isn't still on.
     */
    private void wakeup() {
        try {
            acquireWakeLock();
        } catch (Exception e) {
            releaseWakeLock();
        }
    }

    /**
     * Get the requested system service by name.
     *
     * @param name The name of the service.
     *
     * @return The service instance.
     */
    private Object getService(String name) {
        return getApp().getSystemService(name);
    }



    /**
     * Add required flags to the window to unlock/wakeup the device.
     */
    static void addWindowFlags(Activity app) {
        final Window window = app.getWindow();

        app.runOnUiThread(new Runnable() {
            public void run() {
                window.addFlags(
                        FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                                FLAG_SHOW_WHEN_LOCKED |
                                FLAG_TURN_SCREEN_ON |
                                FLAG_DISMISS_KEYGUARD
                );
            }
        });
    }


    /**
     * clear flags added before sleeping again
     */

    private void clearFlags(){
        getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cordova.getActivity().getWindow().clearFlags(
                        FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                                FLAG_SHOW_WHEN_LOCKED |
                                FLAG_TURN_SCREEN_ON |
                                FLAG_DISMISS_KEYGUARD
                );
            }
        });
    }

    /**
     * Invoke the callback with information if the screen is on.
     *
     * @param callback The callback to invoke.
     */
    @SuppressWarnings("deprecation")
    private void isDimmed(CallbackContext callback) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, isDimmed());
        callback.sendPluginResult(result);
    }

    /**
     * If the screen is active.
     */
    @SuppressWarnings("deprecation")
    private boolean isDimmed() {
        PowerManager pm = (PowerManager) getService(POWER_SERVICE);

        if (Build.VERSION.SDK_INT < 20) {
            return !pm.isScreenOn();
        }

        return !pm.isInteractive();
    }
    /**
     * Acquire a wake lock to wake up the device.
     */
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getService(POWER_SERVICE);

        releaseWakeLock();

        if (!isDimmed()) {
            return;
        }

        int level = PowerManager.SCREEN_DIM_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP;

        wakeLock = pm.newWakeLock(level, "BackgroundModeExt");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(1000);
    }
    /**
     * Releases the previously acquire wake lock.
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }


    /**
     * Unlocks the device even with password protection.
     */
    private void unlock() {
        Intent intent  = getLaunchIntent();
        getApp().startActivity(intent);
    }

    /**
     * The launch intent for the main activity.
     */
    private Intent getLaunchIntent() {
        Context app    = getApp().getApplicationContext();
        String pkgName = app.getPackageName();

        return app.getPackageManager().getLaunchIntentForPackage(pkgName);
    }
}
