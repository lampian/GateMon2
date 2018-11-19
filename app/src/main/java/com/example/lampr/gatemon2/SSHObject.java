package com.example.lampr.gatemon2;

import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import static android.os.SystemClock.sleep;
import static android.support.constraint.Constraints.TAG;

//import java.io.InputStream;

class SSHObject {


    private String mPassWord;
    private String mUserName;
    private String mHost;
    private int mPort;
//    private Session msession;
//    private ChannelExec mChannelssh;
//    private ByteArrayOutputStream mBaos;

    SSHObject(String password, String username, String host, int port) {

        mPassWord = password;
        mUserName = username;
        mHost=host;
        mPort=port;
    }

    void SetHost ( String aStr) {
        mHost = aStr;
    }

    String GetSSHStr(String cmdStr) throws Exception {

        String outStr;
        //InputStream aStr;
        JSch jsch = new JSch();
        //Session session = jsch.getSession("pi", "192.168.1.108", 22);
        Session session = jsch.getSession(mUserName,mHost,mPort);
        session.setPassword(mPassWord);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();
        } catch (Exception e) {
            Log.i(TAG, "SSH0 connection failure");
        }
        //session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        // reads bits! boas has hex value returned  channelssh.setCommand("pigs br1");
        // channelssh.setCommand("pigs w 22 1 mils 3000 w 22 0 w 10 1 mils 3000 w 10 0 br1");
        channelssh.setCommand(cmdStr);
        channelssh.connect();
        //changed from 500 to 100 to 50
        sleep(600);

        channelssh.disconnect();

        //this stops thread - must it be stopped???
        session.disconnect();

        outStr= baos.toString();
        Log.i("SSH0 :", "GetSSHStr : " + outStr);

        //sleep(60000);
        return outStr;

    }
}
