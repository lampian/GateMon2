package com.example.lampr.gatemon2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;

import static com.example.lampr.gatemon2.SSH2IntentService.ACTION_CHANGE_HOST;
import static com.example.lampr.gatemon2.SSH2IntentService.ACTION_GET_INPUTS;
import static com.example.lampr.gatemon2.SSH2IntentService.ACTION_SET_OUT1;
import static com.example.lampr.gatemon2.SSH2IntentService.ACTION_SET_OUT2;
import static com.example.lampr.gatemon2.SSH2IntentService.BUNDLE_SSH_I2C_ADC1_STR;
import static com.example.lampr.gatemon2.SSH2IntentService.BUNDLE_SSH_I2C_ADC2_STR;
import static com.example.lampr.gatemon2.SSH2IntentService.BUNDLE_SSH_I2C_ADC3_STR;
import static com.example.lampr.gatemon2.SSH2IntentService.BUNDLE_SSH_STATUS_STR;
import static com.example.lampr.gatemon2.SSH2IntentService.BUNDLE_SSH_STR;


public class MainActivity extends AppCompatActivity {

    //private ComponentName mServiceComponent;

    // Handler for incoming messages from the service.
    private IncomingMessageHandler mHandler;
    //private SSHIntentService mIntent;
    //private SSH2IntentService m2Intent;

    //int adc1_val = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //RadioButton mRB1 = (RadioButton) findViewById(R.id.ip1); // initiate a radio button
        //RadioButton mRB2 = findViewById(R.id.ip2); // initiate a radio button
        //mRB2.setChecked(true);

        //mServiceComponent = new ComponentName(this, SSHIntentService.class);
        mHandler = new IncomingMessageHandler(this);

        //use job scheduler to handle ssh monitoring function which must stay alive
        scheduleJob();


    }

    /**
     *
     */
    public void scheduleJob() {

        int mJobId = 1234;
        ComponentName mServiceComponent = new ComponentName(this, SSHIntentService.class);
        JobInfo.Builder builder = new JobInfo.Builder(mJobId, mServiceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        // Schedule job
        Log.i("SSH1 :", "ScheduleJob");
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert tm != null;
        tm.schedule(builder.build());

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        //Messenger messengerIncoming = new Messenger(mHandler);
        SSHIntentService.startActionMonitorIO( this, "", "" );
    }

//================================================
    Handler h = new Handler();
    int delay = 5*1000; //1 second=1000 milisecond, 15*1000=15seconds
    Runnable runnable;

    @Override
    protected void onResume() {
        //start handler as activity become visible
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                Log.i("SSH :", "Handler for post delay");
                Messenger messengerIncoming = new Messenger(mHandler);
                //mIntent.startActionSetOut1( this, messengerIncoming, "" );
                SSH2IntentService.startSSH2Service(getApplicationContext(), ACTION_GET_INPUTS, messengerIncoming, "");
                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
//==============================
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

        String tbTxt;
        Intent sIntent = new Intent();
        //sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        Messenger messengerIncoming = new Messenger(mHandler);
        //mIntent.startActionSetOut1( this, messengerIncoming, "" );
        ToggleButton tb = findViewById(R.id.led16);

        if ( tb.isChecked() ) {
            tbTxt = "1";
        } else {
            tbTxt = "0";
        }
        SSH2IntentService.startSSH2Service(this, ACTION_SET_OUT1, messengerIncoming, tbTxt);
        Log.i("SSH2 :", "ScheduleJob LED 16");

        //continue with reads
        //mIntent.startActionGetInputs( this, messengerIncoming, "" );
        //SSH2IntentService.startSSH2Service(this, ACTION_GET_INPUTS, messengerIncoming, "");
    }

    public void setLed17(View view) {

        String tbTxt;
        Intent sIntent = new Intent();
        //sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        Messenger messengerIncoming = new Messenger(mHandler);
        //mIntent.startActionSetOut2( this, messengerIncoming, "" );
        ToggleButton tb = findViewById(R.id.led17);

        if ( tb.isChecked() ) {
            tbTxt = "1";
        } else {
            tbTxt = "0";
        }

        SSH2IntentService.startSSH2Service(this, ACTION_SET_OUT2, messengerIncoming, tbTxt);
        Log.i("SSH2 :", "ScheduleJob LED 17");

        //continue with reads
        //mIntent.startActionGetInputs( this, messengerIncoming, "" );
        //SSH2IntentService.startSSH2Service(this, ACTION_GET_INPUTS, messengerIncoming, "");

    }



    public void shutdownDevice(View view) {
        Intent sIntent = new Intent();
        //sIntent.setAction(SSHIntentService.StopReceiver.ACTION_STOP);
        sendBroadcast(sIntent);

        Messenger messengerIncoming = new Messenger(mHandler);
        //mIntent.startActionSetOut3( this, messengerIncoming, "" );
        SSH2IntentService.startSSH2Service(this, ACTION_CHANGE_HOST, messengerIncoming, "");
        Log.i("SSH :", "ScheduleJob Shutdown");

        //continue with reads
        //mIntent.startActionGetInputs( this, messengerIncoming, "" );

    }

    public void setLed18(View view) {
    }

    public void setLed19(View view) {
    }

    public void changeIP(View view) {

        // launch a new activity to display
        // the dialog fragment with selected text.
        // That is: if this is a single-pane (e.g., portrait mode on a
        // phone) then fire DetailsActivity to display the details
        // fragment

        // Create an intent for starting the DetailsActivity
        Intent intent = new Intent();

        // explicitly set the activity context and class
        // associated with the intent (context, class)
//        intent.setClass(getActivity(), GetHostActivity.class);
        intent.setClass( this, GetHostActivity.class);

        // pass the current position
//        intent.putExtra("index", 1);

        startActivity(intent);


    }

//    public void changeHost(View view) {
//        String newHostIP = "192.168.1.108";
//
//        RadioButton mRB2 = findViewById(R.id.ip2); // initiate a radio button
//        Boolean mRB2Set = mRB2.isChecked(); // check current state of a radio button (true or false).
//        if (mRB2Set) {
//            newHostIP = "192.168.1.125";
//        }
//        Messenger messengerIncoming = new Messenger(mHandler);
//        SSH2IntentService.startSSH2Service(this, ACTION_CHANGE_HOST, messengerIncoming, newHostIP);
//    }


    /**
     * A {@link Handler} allows you to send messages associated with a thread. A {@link Messenger}
     * uses this handler to communicate from {@link SSHIntentService}.
     */
    class IncomingMessageHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> mActivity;

        IncomingMessageHandler(MainActivity activity) {
            super(/* default looper */);
            this.mActivity = new WeakReference<>(activity);
            //this.postDelayed(mGetInputs,1000 );
        }


        @Override
        public void handleMessage(Message msg) {

            CharSequence aCharSeq;
            String aStr;
            String ssh_status;
            Long aLong;
            Integer adc1Val;
            Integer adc2Val;
            Integer adc3Val;

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
                Log.i("SSH3 :", "Status read string bad");
            }
            ssh_status = aStr;

            aCharSeq = aB.getCharSequence( BUNDLE_SSH_STR );
            assert aCharSeq != null;
            try {
                aStr = aCharSeq.toString();
            } catch (Exception e) {
                aStr = "0000000\n";
                Log.i("SSH3 :", "GPIO read string bad");
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
            int indexA;
            int indexB;
            int aInt;
            if (aCS == null) {
                throw new AssertionError();
            }
            try {
                i2cStr = aCS.toString();
            } catch (Exception e) {
                i2cStr = "2 0 0\n";
                Log.i("SSH3 :", "I2C string bad");
            }
            if (i2cStr.length() > 5) {
                aStr = i2cStr.substring(0, 1);
            } else {
                aStr = "2 0 0\n";
            }
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
                    aInt |= bInt;
                    aInt >>= shrCnt;
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

            //bStr = Long.toHexString(aLong);
            //Log.i("SSH :", "StringToLong  " + bStr + " Long : " + aLong );

            return  aLong;

        }


        private void updateUi(Long aLong, String aSSHStatus, Integer... aInt) {


            String swStatus;
            boolean tbSame;
            boolean tbXOR;

            //String aStr = Long.toHexString(aLong);
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

            TextView tv5 = findViewById(R.id.ssh_status);
            tv5.setText( aSSHStatus );

            //make toggle button red if the shown and actual state of the bit differs,
            //make toglle button green of the shown and actual state are the same, ie Off and 0
            ToggleButton tb1 = findViewById(R.id.led16);
            if ((aLong & 0x10000) > 0)
                tbXOR = true;
            else
                tbXOR = false;
            tbXOR ^= tb1.isChecked();
            if (!tbXOR) {
                tb1.setBackgroundColor(Color.GREEN);
            } else {
                tb1.setBackgroundColor(Color.RED);
            }

            //make toggle button red if the shown and actual state of the bit differs,
            //make toglle button green of the shown and actual state are the same, ie Off and 0
            ToggleButton tb2 = findViewById(R.id.led17);
            if ((aLong & 0x20000) > 0)
                tbXOR = true;
            else
                tbXOR = false;
            tbXOR ^= tb2.isChecked();
            if (!tbXOR) {
                tb2.setBackgroundColor(Color.GREEN);
            } else {
                tb2.setBackgroundColor(Color.RED);
            }

        }
    }



    // This is a secondary activity, to show what the user has selected when the
    // screen is not large enough to show it all in one activity.

    public static class GetHostActivity extends FragmentActivity {

        private static final String TAG = "GetHostACTIVITY";

        public GetHostActivity() {
            super();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Log.d(TAG, " onCreate()");

            Toast.makeText(this, "GetHostActivity", Toast.LENGTH_SHORT).show();

            setContentView(R.layout.fragment_get_host);


//            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                // If the screen is now in landscape mode, we can show the
//                // dialog in-line with the list so we don't need this activity.
//                finish();
//                return;
//            }

            if (savedInstanceState == null) {
                // During initial setup, plug in the details fragment.

                // create fragment
//                GetHostFragment aGetHostFragment = new GetHostFragment();

                // get and set the position input by user (i.e., "index")
                // which is the construction arguments for this fragment
//                aGetHostFragment.setArguments(getIntent().getExtras());

                //
//                getFragmentManager().beginTransaction()
//                        .add(, aGetHostFragment).commit();

//                GetHostFragment getHostFragment = new GetHostFragment();
//                getSupportFragmentManager().beginTransaction().add(R.id.container, getHostFragment).commit();
    //                setContentView(R.layout.fragment_get_host);

                GetHostFragment getHostFragment = new GetHostFragment();
                //container -> contents???
                getSupportFragmentManager().beginTransaction().add(R.id.container, getHostFragment).commit();
//                setContentView(R.layout.fragment_get_host);
            }
        }

        // GetHostActivity Lifecycle

        @Override
        protected void onStart() {
            //setContentView(R.layout.fragment_get_host);
            super.onStart();
            Log.d(TAG, " onStart()");
        }

        @Override
        protected void onResume() {
            super.onResume();
            Log.d(TAG, " onResume()");
        }

        @Override
        protected void onPause() {
            super.onPause();
            Log.d(TAG, " onPause()");
        }

        @Override
        protected void onStop() {
            super.onStop();
            Log.d(TAG, " onStop()");
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            Log.d(TAG, " onDestroy()");
        }

        @Override
        protected void onRestart() {
            super.onRestart();
            Log.d(TAG, " onRestart()");
        }


    }


}

