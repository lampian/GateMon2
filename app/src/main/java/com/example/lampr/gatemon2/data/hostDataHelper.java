package com.example.lampr.gatemon2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.lampr.gatemon2.data.hosts.HostData.SQL_CREATE_ENTRIES;
import static com.example.lampr.gatemon2.data.hosts.HostData.SQL_DELETE_ENTRIES;
import static com.example.lampr.gatemon2.data.hosts.HostPrefData.SQL_CREATE_PREF_ENTRIES;
import static com.example.lampr.gatemon2.data.hosts.HostPrefData.SQL_DELETE_PREF_ENTRIES;

public class hostDataHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PIZWHostData.db";

    public hostDataHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_PREF_ENTRIES);
        Log.i("SSH DB:", "onCreate :" + SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_PREF_ENTRIES);
        onCreate(db);
    }

    public static void deleteEntries(SQLiteDatabase db){
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_PREF_ENTRIES);
    }

    public static void updateRecord(SQLiteDatabase db, String aTable, String aCol, String aRow, String val){
        String updateStr = "UPDATE " + aTable +
                " SET " + aCol + " = " + val +
                " WHERE _ID = " + aRow;
        db.execSQL( updateStr);
        Log.i("SSHDB ", "updateRecord :" + updateStr);

    }
    public static void deleteRecord(SQLiteDatabase db, String aTable, String ID){
        String deleteStr = "DELETE FROM " + aTable + " WHERE _ID = " + ID;
        db.execSQL( deleteStr);
        Log.i("SSHDB ", "DeleteRecord :" + "ID = " + ID + " result = " );
    }
}
