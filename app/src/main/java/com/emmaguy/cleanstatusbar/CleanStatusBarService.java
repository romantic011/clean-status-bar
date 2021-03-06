package com.emmaguy.cleanstatusbar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;

import com.emmaguy.cleanstatusbar.util.StatusBarConfig;
import com.emmaguy.cleanstatusbar.widgets.StatusBarView;

public class CleanStatusBarService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static boolean sIsRunning = false;

    private StatusBarConfig mStatusBarConfig;
    private StatusBarView mStatusBarView;
    private NotificationManager mNotificationManager;
    private WindowManager mWindowManager;

    public CleanStatusBarService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIsRunning = true;

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mStatusBarView = new StatusBarView(this);
        mStatusBarConfig = new StatusBarConfig(getAPIValue(), getResources(), getAssets());

        mStatusBarView.setStatusBarConfig(mStatusBarConfig, getBackgroundColour(), getClockTime());

        mWindowManager.addView(mStatusBarView, getWindowManagerParams());
        showNotification();
    }

    private WindowManager.LayoutParams getWindowManagerParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT); // must be translucent to support KitKat gradient
        params.gravity = Gravity.TOP;
        params.height = mStatusBarConfig.getStatusBarHeight();
        return params;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsRunning = false;
        
        if (mStatusBarView != null) {
            mWindowManager.removeView(mStatusBarView);
        }
        removeNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public String getClockTime() {
        return getSharedPrefs().getString(MainActivity.PREFS_KEY_CLOCK_TIME, "12:00");
    }

    public int getBackgroundColour() {
        return getSharedPrefs().getInt(MainActivity.PREFS_KEY_BACKGROUND_COLOUR, 0);
    }

    public int getAPIValue() {
        String apiValue = getSharedPrefs().getString(MainActivity.PREFS_KEY_API_VALUE, "");
        if(!TextUtils.isEmpty(apiValue)) {
            return Integer.valueOf(apiValue);
        }

        return MainActivity.VERSION_CODE_L;
    }

    private SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void showNotification() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setWhen(0)
                .setContentTitle(getString(R.string.clean_status_bar_is_running))
                .setContentText(getString(R.string.touch_to_configure))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT));
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
    public static boolean isRunning() {
    	return sIsRunning;
    }
}
