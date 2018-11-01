package com.example.lampr.gatemon2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import static com.example.lampr.gatemon2.SSHIntentService.BUNDLE_SSH_I2C_ADC1_STR;
import static com.example.lampr.gatemon2.SSHIntentService.BUNDLE_SSH_I2C_ADC2_STR;
import static com.example.lampr.gatemon2.SSHIntentService.BUNDLE_SSH_I2C_ADC3_STR;
import static com.example.lampr.gatemon2.SSHIntentService.BUNDLE_SSH_STATUS_STR;
import static com.example.lampr.gatemon2.SSHIntentService.BUNDLE_SSH_STR;


public class MainActivity extends AppCompatActivity {

    private ComponentName mServiceComponent;

    // Handler for incoming messages from the service.
    private IncomingMessageHandler mHandler;
    private SSHIntentService mIntent;

    int adc1_val = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RadioButton mRB1 = (RadioButton) findViewById(R.id.ip1); // initiate a radio button
        RadioButton mRB2 = (RadioButton) findViewById(R.id.ip2); // initiate a radio button
        mRB2.setChecked(true);

        mServiceComponent = new ComponentName(this, SSHIntentService.class);
        mHandler = new IncomingMessageHandler(this);
        scheduleJob();

    }

    /**
     *
     */
    public void scheduleJob() {

        int mJobId = 1234;
        JobInfo.Builder builder = new JobInfo.Builder(mJobId, mServiceComponent);

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        // Schedule job
        Log.i("SSH :", "ScheduleJob");
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert tm != null;
        tm.schedule(builder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        Messenger messengerIncoming = new Messenger(mHandler);
        mIntent = new SSHIntentService();
        mIntent.startActionGetInputs( this, messengerIncoming, "" );

    }


    @Override
    protected void onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(new Intent(this, SSHIntentService.class));
        super.onStop();
    }

    public void setLed16(View view) {

        Intent sIntent = new Intent();
        sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        Messenger messengerIncoming = new Messenger(mHandler);
        mIntent.startActionSetOut1( this, messengerIncoming, "" );
        Log.i("SSH :", "ScheduleJob LED 16");

        //continue with reads
        mIntent.startActionGetInputs( this, messengerIncoming, "" );

    }

    public void setLed17(View view) {

        Intent sIntent = new Intent();
        sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        Messenger messengerIncoming = new Messenger(mHandler);
        mIntent.startActionSetOut2( this, messengerIncoming, "" );
        Log.i("SSH :", "ScheduleJob LED 17");

        //continue with reads
        mIntent.startActionGetInputs( this, messengerIncoming, "" );

    }

    public void changeHost(View view) {

        String newHostIP = "192.168.1.108";
        Intent sIntent = new Intent();
        sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        RadioButton mRB1 = (RadioButton) findViewById(R.id.ip1); // initiate a radio button
        RadioButton mRB2 = (RadioButton) findViewById(R.id.ip2); // initiate a radio button
        Boolean mRB2Set = mRB2.isChecked(); // check current state of a radio button (true or false).
        Boolean mRB1Set = mRB1.isChecked(); // check current state of a radio button (true or false).
        if (mRB2Set) {
            newHostIP = "192.168.1.125";
        }
        else if (mRB1Set) {
            newHostIP = "192.168.1.108";
        }

        Messenger messengerIncoming = new Messenger(mHandler);
        mIntent.changeHost( this, messengerIncoming , newHostIP );
        Log.i("SSH :", "ScheduleJob change host");

    }

    public void shutdownDevice(View view) {
        Intent sIntent = new Intent();
        sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        Messenger messengerIncoming = new Messenger(mHandler);
        mIntent.startActionSetOut3( this, messengerIncoming, "" );
        Log.i("SSH :", "ScheduleJob Shutdown");

        //continue with reads
        mIntent.startActionGetInputs( this, messengerIncoming, "" );

    }


    /**
     * A {@link Handler} allows you to send messages associated with a thread. A {@link Messenger}
     * uses this handler to communicate from {@link SSHIntentService}. It's also used to make
     * the start and stop views blink for a short period of time.
     */
    class IncomingMessageHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super(/* default looper */);
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            CharSequence aCharSeq;
            String aStr;
            String i2cStr;
            String ssh_status;
            Long aLong;
            Integer adc1Val = 0;
            Integer adc2Val = 0;
            Integer adc3Val = 0;

            int indexA;
            int indexB;

            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                // Activity is no longer available, exit.
                return;
            }
            Bundle aB = new Bundle( msg.getData() );
            aCharSeq = aB.getCharSequence( BUNDLE_SSH_STATUS_STR );
            assert aCharSeq != null;
            try {
                aStr = aCharSeq.toString();
            } catch (Exception e) {
                aStr = "SSH status unknown";
                Log.i("SSH :", "Status read string bad");
            }
            ssh_status = aStr;

            aCharSeq = aB.getCharSequence( BUNDLE_SSH_STR );
            assert aCharSeq != null;
            try {
                aStr = aCharSeq.toString();
            } catch (Exception e) {
                aStr = "0000000\n";
                Log.i("SSH :", "GPIO read string bad");
            }
            aLong = StringToLong( aStr );

            //string returned from i2c bus -> 2 2 18/n  or 2  c 128/n
            aCharSeq = aB.getCharSequence( BUNDLE_SSH_I2C_ADC1_STR );
            adc1Val = ParseI2CString(aCharSeq,2);
            //scale to a value between 0 and 100
            adc1Val = (adc1Val * 100) / 787;

            aCharSeq = aB.getCharSequence( BUNDLE_SSH_I2C_ADC2_STR );
            adc2Val = ParseI2CString(aCharSeq,2);
            adc2Val = (adc2Val * 100)/ 1024;

            aCharSeq = aB.getCharSequence( BUNDLE_SSH_I2C_ADC3_STR );
            adc3Val = ParseI2CString(aCharSeq,2);
            adc3Val = (adc3Val * 100 )/ 1024;

            updateUi( aLong,  ssh_status, adc1Val, adc2Val, adc3Val );
//            onStop();
        }

        int ParseI2CString( CharSequence aCS, int shrCnt ) {

            String i2cStr;
            String aStr;
            assert aCS != null;
            int indexA;
            int indexB;
            int aInt;
            try {
                i2cStr = aCS.toString();
            } catch (Exception e) {
                i2cStr = "2 12 32\n";
                Log.i("SSH :", "I2C string bad");
            }
            aStr = i2cStr.substring(0,1);
            try {
                if (Integer.parseInt(aStr) == 2) {
                    //example string in format of 2 x y\n, where x=[0 255] y=[0.255]
                    indexB = i2cStr.indexOf(" ", 2);
                    aStr = i2cStr.substring(2, indexB);
                    aInt = Integer.parseInt(aStr);
                    aInt = aInt << 8;
                    indexB = indexB + 1;
                    indexA = i2cStr.indexOf('\n', indexB);
                    aStr = i2cStr.substring(indexB, indexA);
                    Integer bInt = Integer.parseInt(aStr);
                    //bInt = bInt >> 2;
                    aInt = aInt | bInt;
                    aInt = aInt >> shrCnt;
                }
                else {
                    //bytes returned by i2c bus <> 2
                    aInt = 0;
                }
            } catch (Exception e) {
                aInt = 0;
            }

            return aInt;
        }

        Long StringToLong(String aStr) {

            String sshResult = "";
            String bStr;
            Long aLong;

            if (aStr.contains("\n") ) {
                bStr = aStr.substring( 0, 8);
                sshResult = bStr;
            }
            try {
                //radix 16 gives hexadecimal base
                aLong = Long.valueOf(sshResult, 16);
            } catch (Exception e){
                aLong = (long) 0;
            }

            bStr = Long.toHexString(aLong);
            //Log.i("SSH :", "StringToLong  " + bStr + " Long : " + aLong );

            return  aLong;

        }


        private void updateUi(Long aLong, String aSSHStatus, Integer... aInt) {


            String swStatus;

            String aStr = Long.toHexString(aLong);
            //Log.i("SSH :", "UpdateUi :" +  aStr + " Long : " + aLong );
            //Log.i("SSH return string :", "update ui " + aLong );

            //gpio 12
            swStatus = "SW12 <0>";
            if ( (aLong & 0x001000) > 0 ) {
                swStatus = "SW12  <1>";
            }
            TextView tv1 = findViewById(R.id.sw12);
            tv1.setText( swStatus );

            //gpio 13
            swStatus = "SW13 <0>";
            if ( (aLong & 0x002000) > 0 ) swStatus = "SW13  <1>";
            TextView tv2 = findViewById(R.id.sw13);
            tv2.setText( swStatus );

            //gpio 14
            swStatus = "SW14 <0>";
            if ( (aLong & 0x004000) > 0 ) swStatus = "SW14  <1>";
            TextView tv3 = findViewById(R.id.sw14);
            tv3.setText( swStatus );

            //gpio 15
            swStatus = "SW15 <0>";
            if ( (aLong & 0x008000) > 0 ) swStatus = "SW15  <1>";
            TextView tv4 = findViewById(R.id.sw15);
            tv4.setText( swStatus );

            ProgressBar pb1 = findViewById(R.id.adc1);
            pb1.setProgress(aInt[0], true);

            ProgressBar pb2 = findViewById(R.id.adc2);
            pb2.setProgress(aInt[1], true);

            ProgressBar pb3 = findViewById(R.id.adc3);
            pb3.setProgress(aInt[2], true);

            //pb.setIndeterminate(false);

            TextView tv5 = findViewById(R.id.ssh_status);
            tv5.setText( aSSHStatus );


        }
    }


}

