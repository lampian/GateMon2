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
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
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
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_GET_INPUTS = "com.example.android.picntlservice.action.GET_INPUTS";
    private static final String ACTION_SET_OUT1 = "com.example.android.picntlservice.action.SET_OUT1";
    private static final String ACTION_SET_OUT2 = "com.example.android.picntlservice.action.SET_OUT2";
    private static final String ACTION_SET_OUT3 = "com.example.android.picntlservice.action.SET_OUT3";
    private static final String ACTION_CHANGE_HOST = "com.example.android.picntlservice.action.CHANGE_HOST";


    private static final String EXTRA_PARAM1 = "com.example.android.picntlservice.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.android.picntlservice.extra.PARAM2";

    final static int RQS_STOP_SERVICE = 1;

    static SSHObject aSSH = new SSHObject("lampiespi","pi", "192.168.1.125", 22);
    //i2c handle
    static String mAdc1Handle = "0";
    static String mAdc2Handle = "1";
    static String mAdc3Handle = "2";
    //i2c device addresses
    final static String ADC1 = "0x4d";
    final static String ADC2 = "0x55";
    final static String ADC3 = "0x5a";

    //main activity messenger
    private Messenger mActivityMessenger;

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

    public static final String BUNDLE_SSH_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_STR";
    public static final String BUNDLE_SSH_I2C_ADC1_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_I2C_ADC1_STR";
    public static final String BUNDLE_SSH_I2C_ADC2_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_I2C_ADC2_STR";
    public static final String BUNDLE_SSH_I2C_ADC3_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_I2C_ADC3_STR";
    public static final String BUNDLE_SSH_STATUS_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_STATUS_STR";


    public class StopReceiver extends BroadcastReceiver {

        public static final String ACTION_STOP = "stop";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SSH :", "Set continueGet false by StopReceiver");
            continueGet = false;
        }
    }

    public class NotifyServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int rqs = arg1.getIntExtra("RQS", 0);
            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
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

    //system invokes this method to perform one-time setup procedures when the service is
    // initially created (before it calls either onStartCommand() or onBind()).
    // If the service is already running, this method is not called.
    @Override
    public void onCreate(){
        notifyServiceReceiver = new NotifyServiceReceiver();
        super.onCreate();
        SetupNotification();
    }


    /**
     * Starts this service to perform action GetInputs with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // Main thread helper method
    public static void startActionGetInputs(Context context, Parcelable param1, String param2) {
        Intent intent = new Intent(context, SSHIntentService.class);
        intent.setAction(ACTION_GET_INPUTS);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // Main thread helper method
    public static void startActionSetOut1(Context context, Parcelable param1, String param2) {
        Intent intent = new Intent(context, SSHIntentService.class);
        intent.setAction(ACTION_SET_OUT1);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // Main thread helper method
    public static void startActionSetOut2(Context context, Parcelable param1, String param2) {
        Intent intent = new Intent(context, SSHIntentService.class);
        intent.setAction(ACTION_SET_OUT2);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    // Main thread helper method
    public static void startActionSetOut3(Context context, Parcelable param1, String param2) {
        Intent intent = new Intent(context, SSHIntentService.class);
        intent.setAction(ACTION_SET_OUT3);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    // Main thread helper method
    public static void changeHost(Context context, Parcelable param1, String param2) {
        Intent intent = new Intent(context, SSHIntentService.class);
        intent.setAction(ACTION_CHANGE_HOST);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    //  The IntentService calls this method from the default worker thread with
    //  the intent that started the service. When this method returns, IntentService
    //  stops the service, as appropriate.
    @Override
    protected void onHandleIntent(Intent intent) {

        IntentFilter filter = new IntentFilter(StopReceiver.ACTION_STOP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        StopReceiver receiver = new StopReceiver();
        registerReceiver(receiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        //SetupNotification();

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_INPUTS.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                continueGet = true;
                Log.i("SSH :", "Set continueGet true by onHandleIntent");
                handleActionGetInputs(param2);


            } else if (ACTION_SET_OUT1.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                handleActionSetOut1();
            } else if (ACTION_SET_OUT2.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                handleActionSetOut2();
            } else if (ACTION_SET_OUT3.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                handleActionSetOut3();
            } else if (ACTION_CHANGE_HOST.equals(action)) {
                //string contains new host ip
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                changeHost( param2 );
            }

        }
        unregisterReceiver(receiver);

    }

    /**
     * Handle action getinputs provided background thread with the provided
     * parameters.
     * keep thread running until continueGet is set to false to give other task chance to
     * execute.
     * control will on enter into this routine, immediately execute the get inputs actions.
     * After the first loop executed, wait for xxx seconds before executing get inputs actions.
     *
     */
    private void handleActionGetInputs(String param2) {

        String aStr;
        String i2cResp1 = "0 0\n";
        String i2cResp2 = "0 0\n";
        String i2cResp3 = "0 0\n";
        String sw5Value;
        String sw5Str;
        String ssh_status = "not set";
        boolean go = true;
        long tnow;
        long tElapsed;
        long tStart = System.currentTimeMillis();
        long twait = 0;

        while (go) {

            tnow = System.currentTimeMillis();
            if ( tnow >= tStart) {
                tElapsed = tnow -tStart;
            }
            else {
                tElapsed = tStart - tnow;
            }
            if ( tElapsed > twait ) {
                tStart = System.currentTimeMillis();
                Log.i("SSH :", "handleAction Get Inputs - tElapsed : " + tElapsed);
                Log.i("SSH :", "handleAction Get Inputs - continueGet :" + continueGet);
                try {
                    aStr = aSSH.GetSSHStr("pigs br1");
                    i2cResp1 = getADCValue( ADC1);
                    i2cResp2 = getADCValue( ADC2);
                    i2cResp3 = getADCValue( ADC3);
                    sw5Str = aSSH.GetSSHStr("pigs r 14");
                    if ( prevSW5Value.isEmpty() ) {
                        prevSW5Value = sw5Str;
                    }
                    else {
                        if ( !prevSW5Value.equalsIgnoreCase(sw5Str)) {
                            prevSW5Value = sw5Str;
                            if (sw5Str.contains("\n") ) {
                                sw5Value = sw5Str.substring( 0, 1);

                            }
                            else {
                                sw5Value = "Unknown";
                            }

                            notifySW5Change(sw5Value);
                        }
                    }
                    ssh_status = "SSH ok 1a";
                } catch (Exception e) {
                    aStr = "00000000/n";
                    ssh_status = "SSH not ok 1";
                    notifySSHIssue( ssh_status );
                }
                Bundle aB = new Bundle();
                aB.putString(BUNDLE_SSH_STR, aStr);
                aB.putString(BUNDLE_SSH_I2C_ADC1_STR, i2cResp1);
                aB.putString(BUNDLE_SSH_I2C_ADC2_STR, i2cResp2);
                aB.putString(BUNDLE_SSH_I2C_ADC3_STR, i2cResp3);
                aB.putString(BUNDLE_SSH_STATUS_STR, ssh_status);
                Message aM = new Message();
                aM.setData(aB);
                try {
                    mActivityMessenger.send(aM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                // wait x sec from now on - it seems android measures in 1/10 of msec!
                twait = 2500*10;
            }

            go = continueGet;

        }
    }

    private String getADCValue( String i2cAddr) {

        String i2cResp = "";

        try {
            if ( i2cAddr == ADC1) {
                // do a read, if it fails (=neg value) initialise device addr
                i2cResp = aSSH.GetSSHStr( "pigs i2crd " + mAdc1Handle + " 2" );
                if ( i2cResp.contains("-") ) {
                    //TODO verify first init of i2c bus
                    i2cResp = aSSH.GetSSHStr( "pigs i2co 1 " + ADC1 + " 0");
                    if (i2cResp.contains("\n")) {
                        mAdc1Handle = i2cResp.substring(0, 1);
                        i2cResp = aSSH.GetSSHStr( "pigs i2crd " + mAdc1Handle + " 2" );
                    }
                }
            }
            else if (i2cAddr == ADC2) {
                // do a write to reg 0 and read 2 bytes, if it fails (=neg value) initialise device addr
                i2cResp = aSSH.GetSSHStr( "pigs i2cwd " + mAdc2Handle + " 0" +
                        " i2crd " + mAdc2Handle + " 2");
                if ( i2cResp.contains("-") ) {
                    //TODO verify first init of i2c bus
                    i2cResp = aSSH.GetSSHStr( "pigs i2co 1 " + ADC2 + " 0");
                    if (i2cResp.contains("\n")) {
                        mAdc2Handle = i2cResp.substring(0, 1);
                        //try reading again with a valid handle
                        i2cResp = aSSH.GetSSHStr( "pigs i2cwd " + mAdc2Handle + " 0" +
                                " i2crd " + mAdc2Handle + " 2");
                    }
                }
            }
            else if (i2cAddr == ADC3) {
                // do a write to reg 0 and read 2 bytes, if it fails (=neg value) initialise device addr
                i2cResp = aSSH.GetSSHStr( "pigs i2cwd " + mAdc3Handle + " 0" +
                        " i2crd " + mAdc3Handle + " 2");
                if ( i2cResp.contains("-") ) {
                    //TODO verify first init of i2c bus
                    i2cResp = aSSH.GetSSHStr( "pigs i2co 1 " + ADC3 + " 0");
                    if (i2cResp.contains("\n")) {
                        mAdc3Handle = i2cResp.substring(0, 1);
                        //try reading again with a valid handle
                        i2cResp = aSSH.GetSSHStr( "pigs i2cwd " + mAdc3Handle + " 0" +
                                " i2crd " + mAdc3Handle + " 2");
                    }
                }

            }
        } catch ( Exception e) {
            //ssh failed make return string safe to continue
            i2cResp = "0\n";
        }
        return i2cResp;
    }


    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOut1() {
        String aStr;

        Log.i("SSH :", "handleAction Set Out1");
        try {
            aStr = aSSH.GetSSHStr("pigs w 16 1 w 17 0 br1");
        } catch (Exception e) {
            aStr = "00000000/n";
            notifySSHIssue( "SSH not ok 2");
        }
        Bundle aB = new Bundle();
        aB.putString(BUNDLE_SSH_STR, aStr);
        aB.putString(BUNDLE_SSH_STATUS_STR, "SSH OK 2");
        Message aM = new Message();
        aM.setData(aB);
        try {
            mActivityMessenger.send(aM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        try {
//            sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOut2() {
        String aStr;

        Log.i("SSH :", "handleAction Set Out2");
        try {
            aStr = aSSH.GetSSHStr("pigs w 16 0 w 17 1 br1");
        } catch (Exception e) {
            aStr = "00000000/n";
            notifySSHIssue( "SSH not ok 3");
        }
        Bundle aB = new Bundle();
        aB.putString(BUNDLE_SSH_STR, aStr);
        aB.putString(BUNDLE_SSH_STATUS_STR, "SSH OK 3");
        Message aM = new Message();
        aM.setData(aB);
        try {
            mActivityMessenger.send(aM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
//        try {
//            sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOut3() {
        String aStr;

        Log.i("SSH :", "handleAction Set Out3");
        try {
            aStr = aSSH.GetSSHStr("sudo shutdown -h");
        } catch (Exception e) {
            aStr = "00000000/n";
            notifySSHIssue( "SSH not ok 4");
        }
        Bundle aB = new Bundle();
        aB.putString(BUNDLE_SSH_STR, aStr);
        aB.putString(BUNDLE_SSH_STATUS_STR, "SSH OK 3");
        Message aM = new Message();
        aM.setData(aB);
        try {
            mActivityMessenger.send(aM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void changeHost( String newHostIP ) {
        aSSH.SetHost(newHostIP);
    }

    //api issue for notification.builder call
    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void SetupNotification () {

        // Prepare intent which is triggered if the notification is selected
//        Context context = getApplicationContext();
//        Intent mI = new Intent(Intent.ACTION_VIEW);
//        //Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myBlog));
//        PendingIntent pI = PendingIntent.getActivity(   getBaseContext(),
//                (int) System.currentTimeMillis(),
//                mI,
//                0);
        //build notification
//        myNotification = new Notification.Builder(this, "1")
        mBuilder = new NotificationCompat.Builder(this, "1");
        mBuilder.setContentTitle("PI Notifications")
                .setContentText("Service started")
                //.setContentIntent(pI)
                .setSmallIcon(R.drawable.ic_stat_lock_open);
        //.addAction(R.drawable.ic_stat_lock_open,"qwerty", pI)
        //.setAutoCancel(true)
        //.build();


        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(MY_NOTIFICATION_ID, mBuilder.build() );
    }

    private void notifySW5Change(String valStr) {
        manageNotificationList("SW 14 state changed to " + valStr );
    }

    private void notifySSHIssue( String ssh_status) {

        manageNotificationList("SSH communication not ok");

        Bundle aB = new Bundle();
        aB.putString(BUNDLE_SSH_STATUS_STR, ssh_status );
        Message aM = new Message();
        aM.setData(aB);
        try {
            mActivityMessenger.send(aM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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

