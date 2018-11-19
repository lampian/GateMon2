package com.example.lampr.gatemon2;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.lampr.gatemon2.data.hostDataHelper;
import com.example.lampr.gatemon2.data.hosts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

    int mIPSelected = 1;
    //int mIPSelectedNew = 1;
    boolean updateHostInfo = true;
    Bundle prefBundle;
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

        //get host preferences from db
        prefBundle = getHostPrefFromDB();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflowmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_dummy_data:
                insertDummyData();
                return  true;
            case R.id.delete_data:
                clearDB();
                return true;
            case R.id.update_ref_data:
                updateHostPrefInDB();
                clearDB();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData() {
        long rowID;
        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getWritableDatabase();
        //put data into Hostdata table
        ContentValues hostValues = new ContentValues();
        hostValues.put(hosts.HostData.HOST_IP, "192.168.1.125");
        hostValues.put(hosts.HostData.HOST_NAME, "PI");
        hostValues.put(hosts.HostData.HOST_PORT, "22");
        rowID = db.insert(hosts.HostData.TABLE_NAME, null, hostValues);
        Log.i("SSHDB", "insertDummyData: " + rowID);

        //put data into HostPrefData table
        ContentValues hostPrefValues = new ContentValues();
        hostPrefValues.put(hosts.HostPrefData.HOST_NAME, "PIZW001");
        hostPrefValues.put(hosts.HostPrefData.HOST_PREF, 0);
        rowID = db.insert(hosts.HostPrefData.TABLE_NAME, null, hostPrefValues);
        Log.i("SSHDB", "insertDummyPrefData: " + rowID);

    }

    private void    clearDB() {
        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getWritableDatabase();
        hostDataHelper.deleteEntries(db);
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

    // selectIp creates a AlertDialog that enables user to:
    // (1) return from dialog with no changes to ip selection,
    // (2) add a host to the host db
    // (3) select a host from the current db
    // The host db is supported by host.java and hostDataHelper.java
    // the current selected host ip is held by mIPSelected
    //The ip is displayed in ip_used textView
    public void selectIP(View view) {
        Log.i("SSH :", "SelectIP()");
        //final TextView ipUsed = (TextView) findViewById(R.id.ip_used);

        //prepare AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alertdialog_custom_view, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        //------------------list
        final ArrayAdapter aAA = getHostDataFromDB();
        // Set a single choice items list for alert dialog
        builder.setSingleChoiceItems(
            aAA, // Items list
            mIPSelected - 1, // Index of checked item (-1 = no selection)
            new DialogInterface.OnClickListener() // Item click listener
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Get the alert dialog selected item's text
                    //note that the index retuned is that of the list and not the _ID
                    //translate index to _ID by i++ - not ideal but ok for now
                    //mIPSelectedNew = i;
                    // get id of selection
                    String aStr = aAA.getItem(i).toString();
                    int indexA = aStr.indexOf(":", 0);
                    aStr = aStr.substring(0, indexA);
                    int aInt = Integer.parseInt(aStr);
                    mIPSelected = aInt;
                    Log.i("SSHDB", "SingleChoisDialog onClick: " + i + " & _ID: " + mIPSelected);

                }
            }
        );
        //------------------list
        Button btnPos = dialogView.findViewById(R.id.dialog_positive_btn);
        Button btnNeg = dialogView.findViewById(R.id.dialog_negative_btn);
        final Button btnAdd = dialogView.findViewById(R.id.dialog_neutral_btn);
        btnAdd.setEnabled(false);
        Button btnDel = dialogView.findViewById(R.id.dialog_delete_btn);
        final EditText et_name = dialogView.findViewById(R.id.et_name);
        //listner's exclusive task it to enable add button once user has typed in text
        //no checking of fields yet
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btnAdd.setEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        } );

        final EditText et_ip = dialogView.findViewById(R.id.et_ip);
        final EditText et_port = dialogView.findViewById(R.id.et_port);
        final EditText et_user = dialogView.findViewById(R.id.et_user);
        final EditText et_pw = dialogView.findViewById(R.id.et_pw);
        final AlertDialog dialog = builder.create();
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                //use mIPselected selected by user.
                removeItemFromDB(mIPSelected);
            }
        });
        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                updateHostPrefInDB();
                prefBundle = getHostPrefFromDB();
                updateHostInfo = true;
            }
        });
        btnNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO if fields empty dont end dialog, get user to add data
                //TODO add btn should be inactive until text fields are filled by user
                String newName = et_name.getText().toString(); //TODO think about using trim()
                String newIP = et_ip.getText().toString();
                String newPort = et_port.getText().toString();
                String newUser = et_user.getText().toString();
                String newPW = et_pw.getText().toString();
                ContentValues hostValues = new ContentValues();
                hostValues.put(hosts.HostData.HOST_IP, newIP);
                hostValues.put(hosts.HostData.HOST_NAME, newName);
                hostValues.put(hosts.HostData.HOST_PORT, newPort);
                hostValues.put(hosts.HostData.HOST_USER_NAME,newUser);
                hostValues.put(hosts.HostData.HOST_PW,newPW);
                mIPSelected = addHosttoDB( hostValues);
                updateHostPrefInDB();
                prefBundle = getHostPrefFromDB();
                updateHostInfo = true;
                //updateHostPrefInDB();
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void removeItemFromDB(int aID) {
        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getWritableDatabase();
        int noRowsDeleted = db.delete(hosts.HostData.TABLE_NAME,hosts.HostData._ID + " = " + mIPSelected, null);
        Log.i("SSHDB", "removeItemFromDB: no rpws deleted " + noRowsDeleted);
    }

    private int addHosttoDB(ContentValues hostValues) {
        long newRow;
        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getWritableDatabase();
        newRow = db.insert(hosts.HostData.TABLE_NAME, null, hostValues);
        Log.i("SSHDB", "addHosttoDB: " + newRow);
        return ((int) newRow);
    }
    // get all host entries in db and pack into a arrayadapter
    private ArrayAdapter getHostDataFromDB() {
        int noRows;

        String[] projection = {
            hosts.HostData._ID,
            hosts.HostData.HOST_NAME,
            hosts.HostData.HOST_IP,
            hosts.HostData.HOST_PORT,
            hosts.HostData.HOST_PW
        };

        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getReadableDatabase();

        Cursor cursor;
        try {
            cursor = db.query(
                    hosts.HostData.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            Log.i("SSHDB", "getHostDataFromDB: cursur issue ");
            ArrayAdapter aAA = null;
            return aAA;
        }
        noRows = cursor.getCount();
        Log.i("SSHDB", "getHostDataFromDB: nr rows =" + noRows);
        List items = new ArrayList<>();
        while (cursor.moveToNext()) {
            String aID = cursor.getString( cursor.getColumnIndexOrThrow(hosts.HostData._ID));
            String aIP = cursor.getString( cursor.getColumnIndexOrThrow(hosts.HostData.HOST_IP));
            String newStr = aID + ": " + aIP;
            items.add(newStr);
        }
        cursor.close();
        // Initialize a new array adapter instance
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(
                this, // Context
                android.R.layout.simple_list_item_single_choice, // Layout
                items // List
        );

        return arrayAdapter;
    }

    private Bundle getHostPrefFromDB() {
        int noRows;
        int selection;

        String[] projection = {
                hosts.HostPrefData._ID,
                hosts.HostPrefData.HOST_PREF
        };

        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getReadableDatabase();
        Cursor cursor;
        try {
            cursor = db.query(
                    hosts.HostPrefData.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            Log.i("SSHDB", "getHostPrefFromDB: cursur issue");
            mIPSelected = 1;// add record to host db returns 1 on first add???
            updateHostPrefInDB();
            Bundle aB = new Bundle();
            aB.putString(hosts.HostData.HOST_NAME, "no name");
            aB.putString(hosts.HostData.HOST_USER_NAME, "no user");
            aB.putString(hosts.HostData.HOST_IP, "no ip");
            aB.putString(hosts.HostData.HOST_PORT, "no port");
            aB.putString(hosts.HostData.HOST_PW, "no pw");
            aB.putString(hosts.HostData.HOST_NAME, "no host name");
            return aB;

        }

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            selection = cursor.getInt(cursor.getColumnIndex(hosts.HostPrefData.HOST_PREF));
        }
        else {
            selection = 1; //first _id in table seems to be 1 and not 0 after first add of record
        }
        Log.i("SSHDB", "getHostDataFromDB: pref =" + selection);

        //get information from hostdata pointed to by selection in hostprefdata
        String[] projection2 = {
                hosts.HostData._ID,
                hosts.HostData.HOST_NAME,
                hosts.HostData.HOST_USER_NAME,
                hosts.HostData.HOST_IP,
                hosts.HostData.HOST_PORT,
                hosts.HostData.HOST_PW
        };

        cursor = db.query(
            hosts.HostData.TABLE_NAME,
            projection2,
            null,
            null,
            null,
            null,
            null
        );

        Bundle aB = new Bundle();
        if  (cursor.getCount() > 0) {
            String query = "select * from " + hosts.HostData.TABLE_NAME +
                    " where "+ hosts.HostData._ID + " = " + Integer.toString(selection) + " ";
            cursor = db.rawQuery(query, null);
            cursor.moveToNext();
            //de constructed steps to help with debug on first column
            int  col = cursor.getColumnIndexOrThrow(hosts.HostData.HOST_NAME);
            String valStr = cursor.getString(col);
            aB.putString(hosts.HostData.HOST_NAME, valStr);
//            aB.putString(hosts.HostData.HOST_NAME,
//                    cursor.getString(cursor.getColumnIndexOrThrow(hosts.HostData.HOST_NAME)));
            aB.putString(hosts.HostData.HOST_USER_NAME,
                    cursor.getString(cursor.getColumnIndexOrThrow(hosts.HostData.HOST_USER_NAME)));
            aB.putString(hosts.HostData.HOST_IP,
                    cursor.getString(cursor.getColumnIndexOrThrow(hosts.HostData.HOST_IP)));
            aB.putString(hosts.HostData.HOST_PORT,
                    cursor.getString(cursor.getColumnIndexOrThrow(hosts.HostData.HOST_PORT)));
            aB.putString(hosts.HostData.HOST_PW,
                    cursor.getString(cursor.getColumnIndexOrThrow(hosts.HostData.HOST_PW)));
            aB.putString(hosts.HostData.HOST_NAME,
                    cursor.getString(cursor.getColumnIndexOrThrow(hosts.HostData.HOST_NAME)));
        }
        else {
            aB.putString(hosts.HostData.HOST_NAME, "no name");
            aB.putString(hosts.HostData.HOST_USER_NAME, "no user");
            aB.putString(hosts.HostData.HOST_IP, "no ip");
            aB.putString(hosts.HostData.HOST_PORT, "no port");
            aB.putString(hosts.HostData.HOST_PW, "no pw");
            aB.putString(hosts.HostData.HOST_NAME, "no host name");
        }
        Log.i("SSHDB", " getHostPrefDataFromDB: row nr =" + selection);

        cursor.close();
        mIPSelected = selection;

        return aB;
    }

    private void updateHostPrefInDB() {

        hostDataHelper mhostDataHelper  = new hostDataHelper(this);
        SQLiteDatabase db = mhostDataHelper.getWritableDatabase();
        String[] projection = {
                hosts.HostPrefData._ID,
                hosts.HostPrefData.HOST_PREF
        };
        Cursor cursor = db.query(
                hosts.HostPrefData.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.getCount() > 0) {
            //at least one recors in table - update
            hostDataHelper.updateRecord(db, hosts.HostPrefData.TABLE_NAME,
                    hosts.HostPrefData.HOST_PREF,
                    "1", //TODO check if 0  should not be a 1
                    Integer.toString(mIPSelected));
        } else {
            //no records in table - add a record
            ContentValues hostPrefValues = new ContentValues();
            hostPrefValues.put(hosts.HostPrefData.HOST_NAME, "PIZW001");
            hostPrefValues.put(hosts.HostPrefData.HOST_PREF, mIPSelected);
            long newRow = db.insert(hosts.HostPrefData.TABLE_NAME, null, hostPrefValues);
            Log.i("SSHDB", "addHosttoDB: " + newRow);
        }
    }

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

            TextView ipUsed = (TextView) findViewById(R.id.ip_used);
            if (updateHostInfo) {
                String aStr = prefBundle.getString(hosts.HostData.HOST_USER_NAME) + "@" +
                        prefBundle.getString(hosts.HostData.HOST_IP) + "\nPort " +
                        prefBundle.getString(hosts.HostData.HOST_PORT);
                ipUsed.setText( aStr );
                updateHostInfo = false;
            }

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


}

