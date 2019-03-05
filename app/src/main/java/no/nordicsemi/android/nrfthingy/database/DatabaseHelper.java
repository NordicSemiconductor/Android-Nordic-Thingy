/*
 * Copyright (c) 2010 - 2017, Nordic Semiconductor ASA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form, except as embedded into a Nordic
 *    Semiconductor ASA integrated circuit in a product or a software update for
 *    such product, must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. Neither the name of Nordic Semiconductor ASA nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. This software, with or without modification, must only be used with a
 *    Nordic Semiconductor ASA integrated circuit.
 *
 * 5. Any software provided in binary form under this license must not be reverse
 *    engineered, decompiled, modified and/or disassembled.
 *
 * THIS SOFTWARE IS PROVIDED BY NORDIC SEMICONDUCTOR ASA "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY, NONINFRINGEMENT, AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NORDIC SEMICONDUCTOR ASA OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfthingy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import no.nordicsemi.android.nrfthingy.database.DatabaseContract.CloudDbColumns;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract.ThingyDbColumns;
import no.nordicsemi.android.nrfthingy.thingy.Thingy;

public class DatabaseHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String BOOLEAN_TYPE = " INTEGER";
    private static final String NOT_NULL = " NOT NULL";
    private static final String UNIQUE = " UNIQUE";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_THINGY_DB_COLUMN_ENTRIES = "CREATE TABLE " + ThingyDbColumns.TABLE_NAME + " (" + ThingyDbColumns._ID + " INTEGER PRIMARY KEY," +
            ThingyDbColumns.COLUMN_ADDRESS + TEXT_TYPE + NOT_NULL + UNIQUE + COMMA_SEP +
            ThingyDbColumns.COLUMN_DEVICE_NAME + TEXT_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_LAST_SELECTED + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_LOCATION + TEXT_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_LATITUDE + TEXT_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_LONGITUDE + TEXT_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_MARKER_TITLE + TEXT_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_MARKER_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_COLOR + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_BUTTON + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_EULER + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_HEADING + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA + BOOLEAN_TYPE + COMMA_SEP +
            ThingyDbColumns.COLUMN_NOTIFICATION_TAP + BOOLEAN_TYPE + ")";

    private static final String SQL_CREATE_CLOUD_DB_COLUMN_ENTRIES = "CREATE TABLE " + DatabaseContract.CloudDbColumns.TABLE_NAME + " (" + CloudDbColumns._ID + " INTEGER PRIMARY KEY," +
            CloudDbColumns.COLUMN_ADDRESS + TEXT_TYPE + NOT_NULL + UNIQUE + COMMA_SEP +
            CloudDbColumns.COLUMN_TEMPERATURE_UPLOAD + BOOLEAN_TYPE + COMMA_SEP +
            CloudDbColumns.COLUMN_PRESSURE_UPLOAD + BOOLEAN_TYPE + COMMA_SEP +
            CloudDbColumns.COLUMN_BUTTON_STATE_UPLOAD + BOOLEAN_TYPE + ")";

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ThingyDbColumns.db";

    private static final String[] THINGY_DEVICES = new String[]{ThingyDbColumns._ID, ThingyDbColumns.COLUMN_ADDRESS,
            ThingyDbColumns.COLUMN_DEVICE_NAME, ThingyDbColumns.COLUMN_LAST_SELECTED, ThingyDbColumns.COLUMN_LOCATION,
            ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE, ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE,
            ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY, ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY,
            ThingyDbColumns.COLUMN_NOTIFICATION_COLOR, ThingyDbColumns.COLUMN_NOTIFICATION_BUTTON,
            ThingyDbColumns.COLUMN_NOTIFICATION_EULER, ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR,
            ThingyDbColumns.COLUMN_NOTIFICATION_HEADING, ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION,
            ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER, ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION,
            ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA, ThingyDbColumns.COLUMN_NOTIFICATION_TAP};

    private static SqliteHelper mSqliteHelper;
    private static SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(final Context context) {
        if (mSqliteHelper == null) {
            mSqliteHelper = new SqliteHelper(context);
            sqLiteDatabase = mSqliteHelper.getWritableDatabase();
        }
    }

    public class SqliteHelper extends SQLiteOpenHelper {

        public SqliteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Creating the thingy entries table
            db.execSQL(SQL_CREATE_THINGY_DB_COLUMN_ENTRIES);
            //Creating the cloud entries table
            db.execSQL(SQL_CREATE_CLOUD_DB_COLUMN_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1:
                    //Updgrading data base version from 1 to 2
                    //Creating the cloud entries table
                    db.execSQL(SQL_CREATE_CLOUD_DB_COLUMN_ENTRIES);
                    break;
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void insertDevice(final String macAddress, final String name) {
        final ContentValues content = new ContentValues();
        content.put(ThingyDbColumns.COLUMN_ADDRESS, macAddress);
        content.put(ThingyDbColumns.COLUMN_DEVICE_NAME, name);
        sqLiteDatabase.insert(ThingyDbColumns.TABLE_NAME, null, content);
    }

    public boolean isExist(final String macAddress) {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, new String[]{ThingyDbColumns.COLUMN_ADDRESS}, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{macAddress}, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public Thingy getSavedDevice(final String address) {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, DatabaseHelper.THINGY_DEVICES, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return new Thingy(cursor.getString(1), cursor.getString(2));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public ArrayList<Thingy> getSavedDevices() {
        final ArrayList<Thingy> devices = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, DatabaseHelper.THINGY_DEVICES, null, null, null, null, null);
        while (cursor.moveToNext()) {
            devices.add(new Thingy(cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();

        return devices;
    }

    public void setLastSelected(final String address, final boolean state) {
        final ContentValues content = new ContentValues();
        content.put(ThingyDbColumns.COLUMN_LAST_SELECTED, state ? 1 : 0);
        sqLiteDatabase.update(ThingyDbColumns.TABLE_NAME, content, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address});
    }

    public boolean getLastSelected(final String address) {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, new String[]{ThingyDbColumns.COLUMN_LAST_SELECTED}, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            cursor.close();
        }
        return true;
    }

    public Thingy getLastSelected() {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, DatabaseHelper.THINGY_DEVICES, null, null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                //4th Column has boolean for the last selected device;
                if (cursor.getInt(3) > 0) {
                    return new Thingy(cursor.getString(1), cursor.getString(2));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public boolean getNotificationsState(final String address, final String columnName) {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, new String[]{columnName}, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            cursor.close();
        }
        return true;
    }

    public void updateNotificationsState(final String address, final boolean flag, final String columnName) {
        final ContentValues content = new ContentValues();
        content.put(columnName, flag ? 1 : 0);
        sqLiteDatabase.update(ThingyDbColumns.TABLE_NAME, content, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address});
    }

    public void updateDeviceName(final String address, final String deviceName) {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, new String[]{ThingyDbColumns.COLUMN_DEVICE_NAME}, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        if (cursor.getCount() > 0) {
            final ContentValues content = new ContentValues();
            content.put(ThingyDbColumns.COLUMN_DEVICE_NAME, deviceName);
            sqLiteDatabase.update(ThingyDbColumns.TABLE_NAME, content, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{String.valueOf(address)});
        }
    }

    public String getDeviceName(final String address) {
        Cursor cursor = sqLiteDatabase.query(ThingyDbColumns.TABLE_NAME, new String[]{ThingyDbColumns.COLUMN_DEVICE_NAME}, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return "";
    }

    public void removeDevice(final String address) {
        sqLiteDatabase.delete(ThingyDbColumns.TABLE_NAME, ThingyDbColumns.COLUMN_ADDRESS + "=?", new String[]{address});
    }

    public boolean getTemperatureUploadState(final String address) {
        final Cursor cursor = sqLiteDatabase.query(CloudDbColumns.TABLE_NAME, new String[]{CloudDbColumns.COLUMN_TEMPERATURE_UPLOAD}, CloudDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    public boolean getPressureUploadState(final String address) {
        final Cursor cursor = sqLiteDatabase.query(CloudDbColumns.TABLE_NAME, new String[]{CloudDbColumns.COLUMN_PRESSURE_UPLOAD}, CloudDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    public boolean getButtonUploadState(final String address) {
        final Cursor cursor = sqLiteDatabase.query(CloudDbColumns.TABLE_NAME, new String[]{CloudDbColumns.COLUMN_BUTTON_STATE_UPLOAD}, CloudDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    public void enableCloudNotifications(final String address, final boolean flag, final String columnName) {
        final ContentValues content = new ContentValues();
        content.put(columnName, flag ? 1 : 0);
        if (sqLiteDatabase.update(CloudDbColumns.TABLE_NAME, content, CloudDbColumns.COLUMN_ADDRESS + "=?", new String[]{address}) == 0) {
            insertDeviceRecordToCloudUploadTable(address, flag, columnName);
        }
    }

    public void insertDeviceRecordToCloudUploadTable(final String macAddress, final boolean flag, final String columnName) {
        final ContentValues content = new ContentValues();
        content.put(CloudDbColumns.COLUMN_ADDRESS, macAddress);
        content.put(columnName, flag);
        sqLiteDatabase.insert(CloudDbColumns.TABLE_NAME, null, content);
    }
}
