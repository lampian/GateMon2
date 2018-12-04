package com.example.lampr.gatemon2;

import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import static android.os.SystemClock.sleep;

//import java.io.InputStream;

class SSHObject {


    public String mPassWord;
    public String mUserName;
    public String mHost;
    public int mPort;

    // https://epaul.github.io/jsch-documentation/javadoc/com/jcraft/jsch/Session.html
    // A Session represents a connection to a SSH server.
    // One session can contain multiple Channels of various types,
    // created with openChannel(java.lang.String).
    // A session is opened with connect() and closed with disconnect().
    // connect() opens the connection, using the timeout set with setTimeout(int).
    // connect(int connectTimeout) opens the connection, using the specified timeout.
    // disconnect() closes the connection to the server.
    // getTimeout() retrieves the current timeout setting.
    // setConfig(Properties newconf) sets several configuration options at once.
    // openChannel Opens a new channel of some type over this connection.
    //      Parameters:
    //          type - a string identifying the channel type. For now, the available types are these:
    //                  shell - ChannelShell
    //                  exec - ChannelExec
    //                  direct-tcpip - ChannelDirectTCPIP
    //                  sftp - ChannelSftp
    //                  subsystem - ChannelSubsystem
    //      This method then returns a channel object of the linked Channel subclass.
    //      Returns: a fresh channel of the right type, already initialized, but not yet connected.
    String GetSSHStr(SSHObject sshInfo, String cmdStr) throws Exception {

        String outStr;
        //InputStream aStr;
        JSch jsch = new JSch();
        //Session session = jsch.getSession("pi", "192.168.1.108", 22);
        Session session = jsch.getSession(sshInfo.mUserName, sshInfo.mHost, sshInfo.mPort);
        session.setPassword(sshInfo.mPassWord);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        String aIP = session.getHost();
        int aPort = session.getPort();
        String aUser = session.getUserName();
        try {
            //session.connect();
            session.connect(10000);
            Log.i("SSH0", "SSH connection ok " + aUser +  " " + aIP + " " + aPort + " "  );
        } catch (Exception e) {
            Log.i("SSH0", "SSH connection failure " + aUser +  " " + aIP + " " + aPort + " "  );
        }
        int timeout = session.getTimeout();
        Log.i("SSH0", "GetSSHStr: timeout = " + timeout);

        // SSH Channel
        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        // reads bits! boas has hex value returned  channelssh.setCommand("pigs br1");
        // channelssh.setCommand("pigs w 22 1 mils 3000 w 22 0 w 10 1 mils 3000 w 10 0 br1");
        channelssh.setCommand(cmdStr);
        channelssh.connect();
        //test
//        OutputStream aOS;
//        aOS = channelssh.getOutputStream();
//        Log.i("SSH0", "GetSSHStr: " + aOS);

        //changed from 500 to 100 to 50
        sleep(1000);

        channelssh.disconnect();

        //this stops thread - must it be stopped???
        session.disconnect();

        outStr = baos.toString();
        Log.i("SSH0", "GetSSHStr : " + outStr);

        //sleep(60000);
        return outStr;

    }

}