package com.example.lampr.gatemon2;

import android.annotation.SuppressLint;
import android.app.IntentService;
//import android.app.NotificationChannel;
//import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Message;
//import android.os.Messenger;
//import android.os.Parcelable;
//import android.os.RemoteException;
//import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
//import android.app.Notification;
//import android.app.NotificationManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//import static java.lang.Thread.sleep;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * The IntentService class does the following:
 * - It creates a default worker thread that executes all of the intents that are delivered to
 *   onStartCommand(), separate from your application's main thread.
 * - Creates a work queue that passes one intent at a time to your onHandleIntent()
 *   implementation, so you never have to worry about multi-threading.
 * - Stops the service after all of the start requests are handled, so you never have to call stopSelf().
 * - Provides a default implementation of onBind() that returns null.
 * - Provides a default implementation of onStartCommand() that sends the intent to the work queue
 *   and then to your onHandleIntent() implementation.
 *
 * helper methods.
 */
public class SSHIntentService extends IntentService {


    private static final String ACTION_Monitor_IO = "com.example.android.picntlservice.action.MONITOR_IO";
    private static final String EXTRA_PARAM1 = "com.example.android.picntlservice.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.android.picntlservice.extra.PARAM2";

    final static int RQS_STOP_SERVICE = 1;

    //instantiate SSHObject for use by this service
    private static SSHObject aSSH;

    NotifyServiceReceiver notifyServiceReceiver;
    private static final int MY_NOTIFICATION_ID=1;

    private NotificationManagerCompat notificationManager;
    //private Notification myNotification;
    private NotificationCompat.Builder mBuilder;

    //private final String myBlog = "http://android-er.blogspot.com/";
    private boolean continueGet = true;
    private String prevSW5Value = "";
    protected ArrayList notificationList = new ArrayList();

    final static String ACTION = "NotifyServiceAction";



    public class NotifyServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int rqs = arg1.getIntExtra("RQS", 0);
            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
                Log.i("SSH1 :", "RQS STOP SERVICE");
            }
        }
    }

    //  A constructor is required, and must call the super <code><a href="/reference/android/app/
    //  IntentService.html#IntentService(java.lang.String)">IntentService(String)</a></code>
    //  constructor with a name for the worker thread.
    public SSHIntentService() {
        super("SSHIntentService");
    }


    //The system invokes this method by calling startService() when another component
    // (such as an activity) requests that the service be started. When this method executes,
    // the service is started and can run in the background indefinitely. If you implement this,
    // it is your responsibility to stop the service when its work is complete by calling
    // stopSelf() or stopService(). If you only want to provide binding, you don't need to
    // implement this method.
    //public void onStartCommand()

    @Override
    public void onDestroy() {
        unregisterReceiver(notifyServiceReceiver);
        super.onDestroy();
    }

    //system invokes this method to perform one-time setup procedures when the service is
    // initially created (before it calls either onStartCommand() or onBind()).
    // If the service is already running, this method is not called.
    @Override
    public void onCreate(){
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();
        SetupNotification();
        aSSH = new SSHObject("lampiespi","pi", "192.168.1.125", 22);
        Log.i("SSH1 :", "onCreate()");
    }

    /**
     * Starts this service to perform action Monitor IO with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // Main thread helper method
    public static void startActionMonitorIO(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SSHIntentService.class);
        intent.setAction(ACTION_Monitor_IO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
        Log.i("SSH1 :", "startActionMonitorIO()");
    }


    //  The IntentService calls this method from the default worker thread with
    //  the intent that started the service. When this method returns, IntentService
    //  stops the service, as appropriate.
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("SSH1 :", "onHandleIntent()");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_Monitor_IO.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionMonitorIO(param2);
            }
        }
    }

    /**
     * Handle action getinputs provided background thread with the provided
     * parameters. Keep thread running.
     * After the first loop executed, wait for xxx seconds before executing get inputs actions.
     *
     */
    private void handleActionMonitorIO(String param2) {

        String sw5Value;
        String sw5Str;
        String ssh_status = "not set";
        boolean go = true;
        long tnow;
        long tElapsed;
        long tStart = System.currentTimeMillis();
        long twait = 0;

        while (true) {

            tnow = System.currentTimeMillis();
            if ( tnow >= tStart) {
                tElapsed = tnow -tStart;
            }
            else {
                tElapsed = tStart - tnow;
            }
            if ( tElapsed > twait ) {
                tStart = System.currentTimeMillis();
                Log.i("SSH1 :", "handleAction MonIO - tElapsed : " + tElapsed);
                try {
                    sw5Str = aSSH.GetSSHStr("pigs r 14");
                    if ( prevSW5Value.isEmpty() ) {
                        prevSW5Value = sw5Str;
                    }
                    else {
                        if ( !prevSW5Value.equalsIgnoreCase(sw5Str)) {
                            prevSW5Value = sw5Str;
                            if (sw5Str.contains("\n") ) {
                                sw5Value = sw5Str.substring( 0, 1);
                                aSSH.GetSSHStr("pigs w 18 " + sw5Str);
                            }
                            else {
                                sw5Value = "Unknown";
                            }

                            notifySW5Change(sw5Value);
                            Log.i("SSH1 :", "handleAction MonIO - SW5 :" + sw5Value);

                        }
                    }
                    //ssh_status = "SSH ok 1a";
                } catch (Exception e) {
                    //aStr = "00000000/n";
                    //ssh_status = "SSH not ok 1";
                    //notifySSHIssue( ssh_status );
                    Log.i("SSH1 :", "handleAction MonIO - exception");
                }
                // wait x sec from now on - it seems android measures in 1/10 of msec!
                twait = 2500;
            }

            //go = continueGet;

        }
    }

    //api issue for notification.builder call
    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void SetupNotification () {

        // Prepare intent which is triggered if the notification is selected
        mBuilder = new NotificationCompat.Builder(this, "1");
        mBuilder.setContentTitle("PI Notifications")
                .setContentText("Service started")
                //.setContentIntent(pI)
                .setSmallIcon(R.drawable.ic_stat_lock_open);
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(MY_NOTIFICATION_ID, mBuilder.build() );
    }

    private void notifySW5Change(String valStr) {
        manageNotificationList("SW 14 state changed to " + valStr );
    }


    private void manageNotificationList( String aStr ) {

        String newStr;
        NotificationCompat.InboxStyle aStyle;

        //build string item
        Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd MM yyyy");
        String currentDateandTime = sdf.format( currentTime );
        newStr = currentDateandTime + " : " + aStr;

        //remove head and add to end, only allowing 5 items
        if (notificationList.size() >= 5 ) {
            notificationList.remove(0);
        }
        notificationList.add( newStr);

        //build notification style
        aStyle = new NotificationCompat.InboxStyle();
        for ( int i = 0; i < notificationList.size(); i++ ){
            aStyle.addLine( (String) notificationList.get(i) );
        }

        //mBuilder.setContentText("SSH communication not ok " + currentDateandTime);
        mBuilder.setStyle(  aStyle );
        notificationManager.notify(MY_NOTIFICATION_ID, mBuilder.build());

    }
}

