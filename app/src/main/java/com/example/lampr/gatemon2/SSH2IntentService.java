package com.example.lampr.gatemon2;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class SSH2IntentService extends IntentService {

    public static final String ACTION_GET_INPUTS = "com.example.android.picntlservice.action.GET_INPUTS";
    public static final String ACTION_SET_OUT1 = "com.example.android.picntlservice.action.SET_OUT1";
    public static final String ACTION_SET_OUT2 = "com.example.android.picntlservice.action.SET_OUT2";
    public static final String ACTION_SET_OUT3 = "com.example.android.picntlservice.action.SET_OUT3";
    public static final String ACTION_CHANGE_HOST = "com.example.android.picntlservice.action.CHANGE_HOST";
    public static final String ACTION_SHUTDOWN = "com.example.android.picntlservice.action.SHUTDOWN";

    private static final String EXTRA_PARAM1 = "com.example.android.picntlservice.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.android.picntlservice.extra.PARAM2";

    public static final String BUNDLE_SSH_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_STR";
    public static final String BUNDLE_SSH_I2C_ADC1_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_I2C_ADC1_STR";
    public static final String BUNDLE_SSH_I2C_ADC2_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_I2C_ADC2_STR";
    public static final String BUNDLE_SSH_I2C_ADC3_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_I2C_ADC3_STR";
    public static final String BUNDLE_SSH_STATUS_STR = "com.example.android.picntlservice.extra.BUNDLE_SSH_STATUS_STR";

    //instantiate SSHObject for use by this service
    private static SSHObject aSSH;
    //i2c default handle
    static String mAdc1Handle = "0";
    static String mAdc2Handle = "1";
    static String mAdc3Handle = "2";
    //i2c device addresses
    final static String ADC1 = "0x4d";
    final static String ADC2 = "0x55";
    final static String ADC3 = "0x5a";

    boolean gpio19 = true;
    boolean adcInit = false;

    //main activity messenger
    private Messenger mActivityMessenger;

    public class StopReceiver extends BroadcastReceiver {

        public static final String ACTION_STOP = "stop";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SSH2 :", "StopReceiver");
            //continueGet = false;
        }
    }


    //  A constructor is required, and must call the super <code><a href="/reference/android/app/
    //  IntentService.html#IntentService(java.lang.String)">IntentService(String)</a></code>
    //  constructor with a name for the worker thread.
    public SSH2IntentService() {
        super("SSH2IntentService");
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
        super.onCreate();
        aSSH = new SSHObject("lampiespi","pi", "192.168.1.125", 22);
        Log.i("SSH2 :", "onCreate()");

    }


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // Main thread helper method
    public static void startSSH2Service(Context context, String action, Parcelable param1, String param2) {
        Log.i("SSH2 :", "startSSH2Service");
        Intent intent = new Intent(context, SSH2IntentService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_GET_INPUTS.equals(action)) {
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                Log.i("SSH2 :", "on HandleIntent - GET INPUTs");
                handleActionGetInputs(param2);
            } else if (ACTION_SET_OUT1.equals(action)) {
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                Log.i("SSH2 :", "on HandleIntent - SET OUT1");
                handleActionSetOut1(param2);
            } else if (ACTION_SET_OUT2.equals(action)) {
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                Log.i("SSH2 :", "on HandleIntent - SET OUT2");
                handleActionSetOut2(param2);
            } else if (ACTION_SHUTDOWN.equals(action)) {
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                Log.i("SSH2 :", "on HandleIntent - SET OUT3");
                handleActionSetOut3();
            } else if (ACTION_CHANGE_HOST.equals(action)) {
                //string contains new host ip
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                mActivityMessenger = intent.getParcelableExtra(EXTRA_PARAM1);
                Log.i("SSH2 :", "on HandleIntent - Change host");
                changeHost( param2 );
            }
            //unregisterReceiver(receiver);
        }
    }

    private void handleActionGetInputs(String param2) {

        String aStr;
        String[] retStr = new String[4];
        boolean sshOK = true;

        Log.i("SSH2 :", "handleAction Get Inputs");
        try {
            if (gpio19) {
                aStr = aSSH.GetSSHStr("pigs w 19 1 br1 " +
                        " i2crd " + mAdc1Handle + " 2" +
                        " i2crd " + mAdc2Handle + " 2" +
                        " i2crd " + mAdc3Handle + " 2");
            } else {
                aStr = aSSH.GetSSHStr("pigs w 19 0 br1 " +
                        " i2crd " + mAdc1Handle + " 2" +
                        " i2crd " + mAdc2Handle + " 2" +
                        " i2crd " + mAdc3Handle + " 2");
            }
            gpio19 = !gpio19;
        } catch (Exception e) {
            aStr = "00000000/n";
            sshOK = false;
        }
        retStr = parceSSHStr(aStr);
        checkADCInit(retStr);
        Bundle aB = new Bundle();
        aB.putString(BUNDLE_SSH_STR, retStr[0]);
        aB.putString(BUNDLE_SSH_I2C_ADC1_STR, retStr[1]);
        aB.putString(BUNDLE_SSH_I2C_ADC2_STR, retStr[2]);
        aB.putString(BUNDLE_SSH_I2C_ADC3_STR, retStr[3]);
        if (sshOK) {
            aB.putString(BUNDLE_SSH_STATUS_STR, "SSH2 OK 1");
        }
        else {
            aB.putString(BUNDLE_SSH_STATUS_STR, "SSH2 NOT OK 1");
        }
        Message aM = new Message();
        aM.setData(aB);
        try {
            mActivityMessenger.send(aM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String[] parceSSHStr( String sshStr) {
        int a;
        int b;
        String[] retStr = new String[4];

        a=0;
        b = sshStr.indexOf("\n", a) + 1;
        retStr[0] = sshStr.substring(a, b);

        a = b;
        b = sshStr.indexOf("\n", a) + 1;
        retStr[1] = sshStr.substring(a, b);

        a = b;
        b = sshStr.indexOf("\n", a) + 1;
        retStr[2] = sshStr.substring(a, b);

        a = b;
        b = sshStr.indexOf("\n", a) + 1;
        retStr[3] = sshStr.substring(a, b);

        return retStr;
    }


    private void checkADCInit( String[] sshStr) {

        String i2cResp = "";

        try {
            if ( sshStr[1].contains("-") ) {
                //TODO verify first init of i2c bus
                i2cResp = aSSH.GetSSHStr( "pigs i2co 1 " + ADC1 + " 0");
                if (i2cResp.contains("\n")) {
                        mAdc1Handle = i2cResp.substring(0, 1);
                }
            }
            if ( sshStr[2].contains("-") ) {
                //TODO verify first init of i2c bus
                i2cResp = aSSH.GetSSHStr( "pigs i2co 1 " + ADC2 + " 0");
                if (i2cResp.contains("\n")) {
                    mAdc2Handle = i2cResp.substring(0, 1);
                }
            }
            if ( sshStr[3].contains("-") ) {
                //TODO verify first init of i2c bus
                i2cResp = aSSH.GetSSHStr( "pigs i2co 1 " + ADC3 + " 0");
                if (i2cResp.contains("\n")) {
                    mAdc3Handle = i2cResp.substring(0, 1);
                }
            }
        } catch ( Exception e) {
            //ssh failed make return string safe to continue
            i2cResp = "2 0 0\n";
        }
        //return i2cResp;
    }


    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOut1(String par2) {
        String aStr;
        boolean sshOK = true;

        Log.i("SSH2 :", "handleAction Set Out1");
        try {
            aStr = aSSH.GetSSHStr("pigs w 16 " + par2 );
        } catch (Exception e) {
            aStr = "00000000/n";
            sshOK = false;
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOut2(String par2) {
        boolean sshOK = true;
        String aStr;

        Log.i("SSH2 :", "handleAction Set Out2");
        try {
            aStr = aSSH.GetSSHStr("pigs w 17 " + par2 );
        } catch (Exception e) {
            aStr = "00000000/n";
            sshOK = false;
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetOut3() {
        boolean sshOK = true;
        String aStr;

        Log.i("SSH2 :", "handleAction Set Out3");
        try {
            aStr = aSSH.GetSSHStr("sudo shutdown -h now");
        } catch (Exception e) {
            aStr = "00000000/n";
            sshOK = false;
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void changeHost( String newHostIP ) {
        aSSH.SetHost(newHostIP);
    }

    private void notifySSHIssue( String ssh_status) {

        //manageNotificationList("SSH communication not ok");

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

}
