package ema.androidsvc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;

public class ForegroundService extends Service {
    private static final String TAG = "EMAForegroundService";

    /**
     * Stores a reference to this service if running. Set to null if service never started or not currently running.
     */
    private static ForegroundService instance;

    /** Binder object providing access to this service via the onBind() method call. Created dynamically. */
    private ForegroundService.Binder binder;

    public static final int NOTIFICATION_ID = 1234;
    public static final String NOTIFICATION_CHANNEL_NAME = "service_channel";

    public static boolean isRunning(){
        return (ForegroundService.instance != null);
    }

    public static boolean start() {
        Log.w(TAG, "start: isRunning=" + isRunning());
        if (isRunning())
            // Already started
            return true;

        // Start the service.
        boolean wasStarted = false;
        try {
            TiApplication context = TiApplication.getInstance();
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            wasStarted = true;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to start service.", ex);
        }
        return wasStarted;
    }

    public static void refresh() {
    }

    public static void stop() {
    }

    public static void cancelNotification(int notificationId) {
    }

    /**
     * Called when the Android OS creates this service.
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate() called.");
        ForegroundService.instance = this;

        int notiIcon = android.R.drawable.alert_light_frame;
        int notiTranIcon = android.R.drawable.alert_light_frame;
        try{
            notiIcon = TiRHelper.getApplicationResource("drawable.ic_launcherblue");
            notiTranIcon = TiRHelper.getApplicationResource("drawable.ic_launcher");

        } catch (Exception e) {
            Log.i(TAG, "ignoring error getting icons: " + e.getMessage());
        }

        // Enable this service's foreground state.
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_NAME, "Foreground Service",
                        NotificationManager.IMPORTANCE_LOW);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notificationBuilder;
            notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_NAME);
            notificationBuilder.setGroup(NOTIFICATION_CHANNEL_NAME);
            notificationBuilder.setContentTitle("(ema svc title)");
            notificationBuilder.setContentText("ema svc text)");
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setSmallIcon(notiTranIcon);
                notificationBuilder.setColor(Color.parseColor("#3F51B5"));
            } else {
                notificationBuilder.setSmallIcon(notiIcon);
            }
            // Set up an intent to launch/resume the app when the above notification is tapped on.
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            launchIntent.setPackage(null);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, launchIntent, flags));

            startForeground(NOTIFICATION_ID, notificationBuilder.build());
            Log.i(TAG, "EMA Foreground Service started");
        } catch (Exception ex) {
            Log.e(TAG, "Failed to put service into the foreground.", ex);
        }
    }

    /** Called when the Android OS is about to destroy this service. */
    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy() called.");

        // Remove reference to this service now that it's destroyed. This means static getInstance() will return null.
        ForegroundService.instance = null;

    }
    /**
     * Called when the service has just been started.
     * @param intent The intent used to start this service. Can be null.
     * @param flags Provides additional flags such as START_FLAG_REDELIVERY, START START_FLAG_RETRY, etc.
     * @param startId Unique integer ID to be used by stopSelfResult() method, if needed.
     * @return Returns a "START_*" constant indicating how the Android OS should handle the started service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }

    /**
     * Called when a client of this service wants direct access to this service via a binder object.
     * @param intent The intent that was used to bind this service.
     * @return Returns a binder object used to access this service.
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        if (this.binder == null) {
            this.binder = new ForegroundService.Binder(this);
        }
        return this.binder;
    }

    /** Binder providing external access to the ForegroundService object. */
    private static class Binder extends android.os.Binder
    {
        /** Reference to the service this binder provides access to. */
        private ForegroundService service;

        /**
         * Creates a new binder providing access to the given service.
         * @param service Reference to the service this binder will provide access to.
         */
        public Binder(ForegroundService service)
        {
            this.service = service;
        }

        /**
         * Gets the service this binder provides access to.
         * @return Returns the service this binder provides access to.
         */
        Service getService()
        {
            return this.service;
        }
    }

}