package com.example.lampr.gatemon2.data;

import android.provider.BaseColumns;

public final class hosts {

    private hosts() {}

    public static final class HostData implements BaseColumns {

        public final static String TABLE_NAME = "hostData";

        public final static String _ID = BaseColumns._ID;
        public final static String HOST_NAME = "hostName";
        public final static String HOST_USER_NAME = "hostUserName";
        public final static String HOST_IP = "hostIP";
        public final static String HOST_PORT = "hostPort";
        public final static String HOST_PW = "hostPW";

        public final static String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        HOST_NAME + " TEXT" + "," +
                        HOST_USER_NAME + " TEXT" + "," +
                        HOST_PW + " TEXT" + "," +
                        HOST_IP + " TEXT" + "," +
                        HOST_PORT + " TEXT" + ");";
        public final static String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public final static class HostPrefData implements BaseColumns {

        public final static String TABLE_NAME = "hostPrefData";

        public final static String _ID = BaseColumns._ID;
        public final static String HOST_NAME = "hostName";
        public final static String HOST_PREF = "hostPref";


        public final static String SQL_CREATE_PREF_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        HOST_NAME + " TEXT" + "," +
                        HOST_PREF + " INT" + ");";
        public final static String SQL_DELETE_PREF_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
