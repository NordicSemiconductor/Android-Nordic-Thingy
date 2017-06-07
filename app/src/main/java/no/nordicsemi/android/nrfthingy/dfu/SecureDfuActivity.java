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

package no.nordicsemi.android.nrfthingy.dfu;

import android.Manifest;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.FileBrowserAppsAdapter;
import no.nordicsemi.android.nrfthingy.common.PermissionRationaleDialogFragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.thingy.ThingyService;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.dfu.DfuService;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class SecureDfuActivity extends AppCompatActivity implements
        ThingySdkManager.ServiceConnectionListener,
        PermissionRationaleDialogFragment.PermissionDialogListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        ScannerFragmentListener,
        DfuRationaleDialogFragment.DfuRationaleFragmentListener {

    private static final int SCAN_DURATION = 15000;
    private TextView mNordicFirmware;
    private TextView mCustomFirmware;
    private TextView mFileNameView;
    private TextView mFileSizeView;
    private TextView mDfuTargetNameView;
    private TextView mDfuSpeed;
    private TextView mDfuSpeedUnit;
    private TextView mEnableBootloaderMsg;
    private TextView mInitializingDfuMsg;
    private TextView mUploadingFwMsg;
    private TextView mDfuCompletedMsg;
    private ImageView mEnableBootloaderView;
    private ImageView mInitializingDfuView;
    private ImageView mUploadingFwView;
    private ImageView mDfuCompletedView;
    private FloatingActionButton mFabStartStop;

    private ProgressBar mProgressBar;
    private ProgressDialog mProgressDialog;

    private String mFileName;
    private String mFileSize;
    private String mFilePath;
    private String mTargetName;

    private float mEnabledBootloaderAlpha = 0.20f;
    private float mInitDfuAlpha = 0.20f;
    private float mUploadingFwAlpha = 0.20f;
    private float mDfuCompletedAlpha = 0.20f;

    private Uri mFileStreamUri;
    private Uri mInitFileStreamUri;

    private String mInitFilePath;

    private int mFileType;
    private int mFileTypeTemp; // This value is being used when user is selecting a file not to overwrite the old value (in case he/she will cancel selecting file)

    private boolean mStatusOk;
    private boolean mStartDfu;
    private boolean mIsNordicFw = true;

    private Handler mScanHandler = new Handler();

    private ThingySdkManager mThingySdkManager;
    private BluetoothDevice mDevice;
    private boolean mDeviceWasConnected;
    private String mNewAddress;
    private boolean mIsScanning;
    private boolean mOnDfuCompleted = false;
    private DatabaseHelper mDatabaseHelper;
    private ThingyService.ThingyBinder mBinder;

    private ThingyListener mThingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            if (mStartDfu) {
                String targetName = mDatabaseHelper.getDeviceName(device.getAddress());
                if (targetName.isEmpty()) {
                    targetName = device.getName();
                }
                mTargetName = targetName;
                mDfuTargetNameView.setText(targetName);
            } else {
                mDeviceWasConnected = false;
            }
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            if (mOnDfuCompleted) {
                hideProgressDialog();
                mOnDfuCompleted = false;
                final String mFragmentTag = mBinder.getLastVisibleFragment();
                if (mFragmentTag.equals(Utils.MOTION_FRAGMENT)) {
                    enableMotionNotifications();
                } else if (mFragmentTag.equals(Utils.UI_FRAGMENT)) {
                    enableUiNotifications();
                } else if (mFragmentTag.equals(Utils.SOUND_FRAGMENT)) {
                    enableSoundNotifications(device, true);
                } else if (mFragmentTag.equals(Utils.CLOUD_FRAGMENT)) {
                    enableNotificationsForCloudUpload();
                } else {
                    enableEnvironmentNotifications();
                }
            }
        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, final String pressure) {
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, final String humidity) {
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, final int eco2, final int tvoc) {
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, final float red, final float green, final float blue, final float alpha) {
        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, final byte[] data) {

        }
    };

    final BroadcastReceiver mBleStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Utils.showToast(SecureDfuActivity.this, getString(R.string.ble_turned_off));
                        enableBle();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_dfu);
        mDatabaseHelper = new DatabaseHelper(this);
        mThingySdkManager = ThingySdkManager.getInstance();

        final Toolbar dfuToolbar = (Toolbar) findViewById(R.id.dfu_toolbar);
        dfuToolbar.setTitle(R.string.dfu_title);
        setSupportActionBar(dfuToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Toolbar dfuFwInfoToolbar = (Toolbar) findViewById(R.id.dfu_fw_info_toolbar);
        dfuFwInfoToolbar.setLogo(ContextCompat.getDrawable(this, R.drawable.ic_format_list_bulleted));
        dfuFwInfoToolbar.setTitle(R.string.dfu_fw_info);

        final Toolbar dfuTargetInfoToolbar = (Toolbar) findViewById(R.id.dfu_target_toolbar);
        dfuTargetInfoToolbar.setLogo(R.drawable.ic_thingy_gray);
        dfuTargetInfoToolbar.setTitle(R.string.dfu_target);

        mDfuSpeed = (TextView) findViewById(R.id.dfu_upload_speed);
        mDfuSpeedUnit = (TextView) findViewById(R.id.dfu_speed_unit);
        final Toolbar mDfuStatusToolbar = (Toolbar) findViewById(R.id.dfu_status_toolbar);
        mDfuStatusToolbar.setLogo(ContextCompat.getDrawable(this, R.drawable.ic_dfu_gray));
        mDfuStatusToolbar.setTitle(R.string.dfu_status);

        mFabStartStop = (FloatingActionButton) findViewById(R.id.dfu_fab);
        mFabStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsNordicFw || mFileStreamUri != null) {
                    if (!mThingySdkManager.isDfuServiceRunning(getBaseContext())) {
                        resetUi();
                        mStartDfu = true;
                        checkBootLoaderMode();
                    } else {
                        DfuRationaleDialogFragment fragment = DfuRationaleDialogFragment.newInstance();
                        fragment.show(getSupportFragmentManager(), null);
                    }
                } else {
                    if (!mIsNordicFw) {
                        Utils.showToast(SecureDfuActivity.this, getString(R.string.dfu_alert_no_file_selected));
                    }
                }
            }
        });

        mNordicFirmware = (TextView) findViewById(R.id.nordic_fw);
        mCustomFirmware = (TextView) findViewById(R.id.custom_fw);
        mFileNameView = (TextView) findViewById(R.id.file_name);
        mFileSizeView = (TextView) findViewById(R.id.file_size);
        mDfuTargetNameView = (TextView) findViewById(R.id.dfu_target_name);

        mProgressBar = (ProgressBar) findViewById(R.id.dfu_status_bar);

        mEnableBootloaderMsg = (TextView) findViewById(R.id.dfu_step_one);
        mInitializingDfuMsg = (TextView) findViewById(R.id.dfu_step_two);
        mUploadingFwMsg = (TextView) findViewById(R.id.dfu_step_three);
        mDfuCompletedMsg = (TextView) findViewById(R.id.dfu_step_four);

        mEnableBootloaderView = (ImageView) findViewById(R.id.dfu_step_one_img);
        mInitializingDfuView = (ImageView) findViewById(R.id.dfu_step_two_img);
        mUploadingFwView = (ImageView) findViewById(R.id.dfu_step_three_img);
        mDfuCompletedView = (ImageView) findViewById(R.id.dfu_step_four_img);

        mCustomFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsNordicFw = false;
                mCustomFirmware.setBackgroundColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.greyBg));
                mCustomFirmware.setTextColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.textColorWhite));
                mNordicFirmware.setBackgroundColor(Color.WHITE);
                mNordicFirmware.setTextColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.textColorSecondary));
                importCustomFirmwareFiles();
            }
        });

        mNordicFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsNordicFw = true;
                mNordicFirmware.setBackgroundColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.greyBg));
                mNordicFirmware.setTextColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.textColorWhite));
                mCustomFirmware.setTextColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.greyBg));
                mCustomFirmware.setBackgroundColor(Color.WHITE);
                mFileName = getResources().getResourceEntryName(R.raw.thingy_dfu_pkg_app_v1_1_0);
                mFileNameView.setText(mFileName);
                mFileStreamUri = null;
            }
        });

        if (getIntent().getExtras() != null) {
            mDevice = getIntent().getExtras().getParcelable(Utils.EXTRA_DEVICE);
            if (mDevice != null && mThingySdkManager.isConnected(mDevice)) {
                final String name = mDatabaseHelper.getDeviceName(mDevice.getAddress());
                if (!name.isEmpty()) {
                    mTargetName = name;
                } else {
                    mTargetName = mDevice.getName();
                }
            }
        }

        // restore saved state
        mFileType = DfuService.TYPE_AUTO; // Default
        if (savedInstanceState != null) {
            mIsNordicFw = savedInstanceState.getBoolean(Utils.NORDIC_FW);
            mFileType = savedInstanceState.getInt(Utils.DATA_FILE_TYPE);
            mFileTypeTemp = savedInstanceState.getInt(Utils.DATA_FILE_TYPE_TMP);
            mFilePath = savedInstanceState.getString(Utils.DATA_FILE_PATH);
            mFileStreamUri = savedInstanceState.getParcelable(Utils.DATA_FILE_STREAM);
            mInitFilePath = savedInstanceState.getString(Utils.DATA_INIT_FILE_PATH);
            mInitFileStreamUri = savedInstanceState.getParcelable(Utils.DATA_INIT_FILE_STREAM);
            mDevice = savedInstanceState.getParcelable(Utils.EXTRA_DEVICE);
            mStatusOk = mStatusOk || savedInstanceState.getBoolean(Utils.DATA_STATUS);
            mDeviceWasConnected = savedInstanceState.getBoolean(Utils.EXTRA_DEVICE_CONNECTION_STATE);

            mTargetName = savedInstanceState.getString(Utils.EXTRA_DEVICE_NAME);
            mFileName = savedInstanceState.getString(Utils.EXTRA_DATA_FILE_NAME);
            mFileSize = savedInstanceState.getString(Utils.EXTRA_DATA_FILE_SIZE);

            mEnabledBootloaderAlpha = savedInstanceState.getFloat(Utils.EXTRA_DATA_BOOTLOADER_ALPHA);
            mInitDfuAlpha = savedInstanceState.getFloat(Utils.EXTRA_DATA_INIT_DFU_ALPHA);
            mUploadingFwAlpha = savedInstanceState.getFloat(Utils.EXTRA_DATA_UPLOAD_FW_ALPHA, mUploadingFwAlpha);
            mDfuCompletedAlpha = savedInstanceState.getFloat(Utils.EXTRA_DATA_DFU_COMPLETED_ALPHA, mDfuCompletedAlpha);
            mOnDfuCompleted = savedInstanceState.getBoolean(Utils.EXTRA_DEVICE_DFU_COMPLETED, mOnDfuCompleted);
        } else {

            if (TextUtils.isEmpty(mFilePath)) {
                InputStream is = null;
                try {
                    mFileName = getResources().getResourceEntryName(R.raw.thingy_dfu_pkg_app_v1_1_0);
                    is = getResources().openRawResource(R.raw.thingy_dfu_pkg_app_v1_1_0);
                    int size = is.available();
                    mFileSize = String.valueOf(Utils.humanReadableByteCount(size, true));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        setGUI();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    private void setGUI() {
        mFileNameView.setText(mFileName);
        mFileSizeView.setText(mFileSize);
        mDfuTargetNameView.setText(mTargetName);
        mFabStartStop.setEnabled(true);

        if (!mIsNordicFw) {
            mCustomFirmware.setBackgroundColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.greyBg));
            mCustomFirmware.setTextColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.textColorWhite));
            mNordicFirmware.setBackgroundColor(Color.WHITE);
            mNordicFirmware.setTextColor(ContextCompat.getColor(SecureDfuActivity.this, R.color.textColorSecondary));
        }

        if (mThingySdkManager.isDfuServiceRunning(getBaseContext())) {
            mNordicFirmware.setEnabled(false);
            mCustomFirmware.setEnabled(false);
            mFabStartStop.setImageResource(R.drawable.ic_close_white);

            mEnableBootloaderMsg.setAlpha(mEnabledBootloaderAlpha);
            mEnableBootloaderView.setAlpha(mEnabledBootloaderAlpha);

            mInitializingDfuMsg.setAlpha(mInitDfuAlpha);
            mInitializingDfuView.setAlpha(mInitDfuAlpha);

            mUploadingFwMsg.setAlpha(mUploadingFwAlpha);
            mUploadingFwView.setAlpha(mUploadingFwAlpha);
            mDfuSpeed.setAlpha(mUploadingFwAlpha);

            mDfuCompletedMsg.setAlpha(mDfuCompletedAlpha);
            mDfuCompletedView.setAlpha(mDfuCompletedAlpha);

            mStatusOk = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isBleEnabled()) {
            enableBle();
        }
        mThingySdkManager.bindService(this, ThingyService.class);
        checkIfRequiredPermissionsGranted();
        registerReceiver(mBleStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        ThingyListenerHelper.registerThingyListener(this, mThingyListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Utils.NORDIC_FW, mIsNordicFw);
        outState.putInt(Utils.DATA_FILE_TYPE, mFileType);
        outState.putInt(Utils.DATA_FILE_TYPE_TMP, mFileTypeTemp);
        outState.putString(Utils.DATA_FILE_PATH, mFilePath);
        outState.putParcelable(Utils.DATA_FILE_STREAM, mFileStreamUri);
        outState.putString(Utils.DATA_INIT_FILE_PATH, mInitFilePath);
        outState.putParcelable(Utils.DATA_INIT_FILE_STREAM, mInitFileStreamUri);
        outState.putParcelable(Utils.EXTRA_DEVICE, mDevice);
        outState.putBoolean(Utils.DATA_STATUS, mStatusOk);
        outState.putFloat(Utils.EXTRA_DATA_BOOTLOADER_ALPHA, mEnabledBootloaderAlpha);
        outState.putFloat(Utils.EXTRA_DATA_INIT_DFU_ALPHA, mInitDfuAlpha);
        outState.putFloat(Utils.EXTRA_DATA_UPLOAD_FW_ALPHA, mUploadingFwAlpha);
        outState.putFloat(Utils.EXTRA_DATA_DFU_COMPLETED_ALPHA, mDfuCompletedAlpha);
        outState.putString(Utils.EXTRA_DEVICE_NAME, mTargetName);
        outState.putString(Utils.EXTRA_DATA_FILE_NAME, mFileName);
        outState.putString(Utils.EXTRA_DATA_FILE_SIZE, mFileSize);
        outState.putBoolean(Utils.EXTRA_DEVICE_CONNECTION_STATE, mDeviceWasConnected);
        outState.putBoolean(Utils.EXTRA_DEVICE_DFU_COMPLETED, mOnDfuCompleted);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            mScanHandler.removeCallbacks(runnable);
        }
        ThingyListenerHelper.unregisterThingyListener(this, mThingyListener);
        unregisterReceiver(mBleStateChangedReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan(); //Stopping scan on destroy
        mThingySdkManager.unbindService(this);
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
        hideProgressDialog();
    }

    @Override
    public void onBackPressed() {
        if (mThingySdkManager.isDfuServiceRunning(getBaseContext())) {
            DfuRationaleDialogFragment fragment = DfuRationaleDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;

        }
        return true;
    }

    @Override
    public void onDfuAborted() {
        final Intent pauseAction = new Intent(DfuBaseService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.ACTION_ABORT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pauseAction);
        mStartDfu = false;
        mNewAddress = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
            case Utils.SELECT_FILE_REQ: {
                // clear previous data
                mFileType = mFileTypeTemp;
                mFilePath = null;
                mFileStreamUri = null;
                Uri uri;
                // and read new one
                if (data != null) {
                    uri = data.getData();
                    if (uri == null) {
                        Utils.showToast(this, getString(R.string.dfu_alert_no_file_selected));
                        break;
                    }
                } else {
                    Utils.showToast(this, getString(R.string.dfu_alert_no_file_selected));
                    break;
                }
            /*
             * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
			 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
			 */
                if (uri.getScheme().equals("file")) {
                    // the direct path to the file has been returned
                    mFilePath = uri.getPath();

                    //updateFileInfo(file.getName(), file.length(), mFileType);
                } else if (uri.getScheme().equals("content")) {
                    // an Uri has been returned
                    mFileStreamUri = uri;
                    // if application returned Uri for streaming, let's us it. Does it works?
                    // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
                    final Bundle extras = data.getExtras();
                    if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
                        mFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM);

                    // file name and size must be obtained from Content Provider
                    final Bundle bundle = new Bundle();
                    bundle.putParcelable(Utils.EXTRA_URI, uri);
                    getLoaderManager().restartLoader(Utils.SELECT_FILE_REQ, bundle, this);
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onCancellingPermissionRationale() {
        Utils.showToast(this, getString(R.string.requested_permission_not_granted_rationale));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.REQ_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Utils.showToast(this, getString(R.string.rationale_permission_denied));
                } else {
                    openFileChooser();
                }
        }
    }

    /**
     * Checks whether the Bluetooth adapter is enabled.
     */
    private boolean isBleEnabled() {
        final BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        final BluetoothAdapter ba = bm.getAdapter();
        return ba != null && ba.isEnabled();
    }

    /**
     * Tries to start Bluetooth adapter.
     */
    private void enableBle() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, Utils.REQUEST_ENABLE_BT);
    }

    private void importCustomFirmwareFiles() {
        openFileChooser();
    }

    private void openFileChooser() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mFileType == DfuService.TYPE_AUTO ? DfuService.MIME_TYPE_ZIP : DfuService.MIME_TYPE_OCTET_STREAM);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // file browser has been found on the device
            startActivityForResult(intent, Utils.SELECT_FILE_REQ);
        } else {
            // there is no any file browser app, let's try to download one
            final View customView = getLayoutInflater().inflate(R.layout.app_file_browser, null);
            final ListView appsList = (ListView) customView.findViewById(android.R.id.list);
            appsList.setAdapter(new FileBrowserAppsAdapter(this));
            appsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            appsList.setItemChecked(0, true);
            new AlertDialog.Builder(this).setTitle(R.string.dfu_alert_no_filebrowser_title).setView(customView)
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final int pos = appsList.getCheckedItemPosition();
                    if (pos >= 0) {
                        final String query = getResources().getStringArray(R.array.dfu_app_file_browser_action)[pos];
                        final Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
                        startActivity(storeIntent);
                    }
                }
            }).show();
        }
    }

    private void checkBootLoaderMode() {
        if (mStartDfu) {
            if (mDevice != null) {
                mDeviceWasConnected = mThingySdkManager.isConnected(mDevice);
                if (mDeviceWasConnected) {
                    if (!mThingySdkManager.isInBootloaderMode(mDevice)) {
                        mThingySdkManager.triggerBootLoaderMode(mDevice);
                        mNewAddress = Utils.incrementAddress(mDevice.getAddress());
                        startScan();
                    }
                } else {
                    ScannerFragment scannerFragment = ScannerFragment.getInstance(ThingyUtils.SECURE_DFU_SERVICE);
                    scannerFragment.show(getSupportFragmentManager(), null);
                }
            } else {
                ScannerFragment scannerFragment = ScannerFragment.getInstance(ThingyUtils.SECURE_DFU_SERVICE);
                scannerFragment.show(getSupportFragmentManager(), null);
            }
        }
    }

    private void reconnectToDevice(final String deviceAddress) {
        final BluetoothDevice device = getBluetoothDevice(deviceAddress);
        if (mThingySdkManager != null && mDeviceWasConnected) {
            showConnectionProgressDialog();
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mThingySdkManager.connectToThingy(getApplicationContext(), device, ThingyService.class);
                }
            }, 30000 /*allowing some time for the thingy to boot up for the app to start reconnecting as Samsung devices seemed to take a long time to reconnect*/);
        }
    }

    private boolean checkIfRequiredPermissionsGranted() {
        if (Utils.checkIfVersionIsMarshmallowOrAbove()) {
            if (ActivityCompat.checkSelfPermission(SecureDfuActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                final PermissionRationaleDialogFragment dialog = PermissionRationaleDialogFragment.getInstance(Manifest.permission.ACCESS_COARSE_LOCATION, Utils.REQUEST_ACCESS_COARSE_LOCATION, getString(R.string.rationale_message_location));
                dialog.show(getSupportFragmentManager(), null);
                return false;
            }
        } else {
            return true;
        }
    }

    private void startScan() {
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ThingyUtils.PARCEL_SECURE_DFU_SERVICE).build());
        scanner.startScan(filters, settings, scanCallback);
        mIsScanning = true;

        //Hanlder to stop scan after the duration time out
        mScanHandler.postDelayed(mBleScannerTimeoutRunnable, SCAN_DURATION);
    }

    /**
     * Stop scan if user tap Cancel button
     */
    private void stopScan() {
        if (mIsScanning) {
            mScanHandler.removeCallbacks(mBleScannerTimeoutRunnable);
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);
            mIsScanning = false;
        }
    }


    final Runnable mBleScannerTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };


    private boolean isConnected(final BluetoothDevice device) {
        if (mThingySdkManager != null) {
            final List<BluetoothDevice> connectedDevices = mThingySdkManager.getConnectedDevices();
            for (BluetoothDevice dev : connectedDevices) {
                if (device.getAddress().equals(dev.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            // do nothing
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            for (final ScanResult result : results) {
                if (mNewAddress != null && mNewAddress.equals(result.getDevice().getAddress())) {
                    mScanHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            stopScan();
                            initiateDfu(result.getDevice());
                        }
                    });
                }
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };


    private void initiateDfu(final BluetoothDevice device) {
        if (!mIsNordicFw) {
            if (mFilePath == null && mFileStreamUri == null) {
                Utils.showToast(SecureDfuActivity.this, getString(R.string.dfu_alert_no_file_selected));
                return;
            }
        }
        mNordicFirmware.setEnabled(false);
        mCustomFirmware.setEnabled(false);
        mFabStartStop.setImageResource(R.drawable.ic_close_white);
        mStartDfu = true;
        if (mIsNordicFw) {
            mThingySdkManager.startDFUWithNordicFW(this, device, R.raw.thingy_dfu_pkg_app_v1_1_0, mFileType);
        } else {
            mThingySdkManager.startDFUWithCustomFW(this, device, mFileType, mFilePath, mFileStreamUri);
        }
        mEnabledBootloaderAlpha = 1.0f;
        mEnableBootloaderMsg.setAlpha(mEnabledBootloaderAlpha);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = args.getParcelable(Utils.EXTRA_URI);
        /*
		 * Some apps, f.e. Google Drive allow to select file that is not on the device. There is no "_data" column handled by that provider. Let's try to obtain
		 * all columns and than check which columns are present.
		 */
        // final String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
        return new CursorLoader(this, uri, null /* all columns, instead of projection */, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToNext()) {
            // Here we have to check the column indexes by name as we have requested for all. The order may be different.

            String filePath = null;
            int dataIndex = data.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            if (dataIndex != -1)
                filePath = data.getString(dataIndex /* 2 DATA */);
            if (!TextUtils.isEmpty(filePath)) {
                mFilePath = filePath;
            }
            final String fileName = data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
            if (!fileName.isEmpty()) {
                mFileName = fileName;
                mFileNameView.setText(mFileName);
            }
            final String size = data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
            if (!size.isEmpty()) {
                mFileSize = Utils.humanReadableByteCount(Long.parseLong(size), true);
                mFileSizeView.setText(mFileSize);
            }
        } else {
            /*mFilePath = null;
            mFileStreamUri = null;*/
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFilePath = null;
        mFileStreamUri = null;
    }

    @Override
    public void onServiceConnected() {
        mBinder = ((ThingyService.ThingyBinder) mThingySdkManager.getThingyBinder());

    }


    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            //mProgressBar.setText(R.string.dfu_status_connecting);
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            if (mEnableBootloaderView.getAlpha() < 1.0f) {
                mEnabledBootloaderAlpha = 1.0f;
                mEnableBootloaderView.setAlpha(mEnabledBootloaderAlpha);
            }
            mInitDfuAlpha = 1.0f;
            mInitializingDfuMsg.setAlpha(mInitDfuAlpha);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTransferCompleted(deviceAddress);
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 1000); //Increased from 200 to 500 to allow thingy to write all the default settings to nvm
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onUploadCanceled(getString(R.string.dfu_status_aborted));
                    // if this activity is still open and upload procoress was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {

            mDfuSpeed.setText(String.valueOf(Float.valueOf(String.format(Locale.US, "%.2f", avgSpeed))));
            mProgressBar.setIndeterminate(false);
            if (mInitializingDfuView.getAlpha() < 1.0f) {
                mInitDfuAlpha = 1.0f;
                mInitializingDfuView.setAlpha(mInitDfuAlpha);
            }

            if (mUploadingFwMsg.getAlpha() < 1.0f) {
                mUploadingFwMsg.setAlpha(1.0f);
            }

            if (mDfuSpeedUnit.getAlpha() < 1.0f) {
                mDfuSpeed.setAlpha(mUploadingFwAlpha);
                mDfuSpeedUnit.setAlpha(mUploadingFwAlpha);
            }

            final float alpha = (float) (percent) / 100;
            if (mUploadingFwView.getAlpha() <= alpha) {
                mUploadingFwAlpha = alpha;
                mUploadingFwView.setAlpha(alpha);
            }

            mProgressBar.setProgress(percent);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            Log.v(Utils.TAG, "Error: " + message);
            onUploadCanceled(message);
            // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);

                }
            }, 200);
        }
    };

    private void onTransferCompleted(final String deviceAddress) {
        mStartDfu = false;
        mOnDfuCompleted = true;
        mNewAddress = null;
        mNordicFirmware.setEnabled(true);
        mCustomFirmware.setEnabled(true);
        mFabStartStop.setImageResource(R.drawable.ic_action_dfu_white);
        Utils.showToast(this, getString(R.string.dfu_success));
        mProgressBar.setIndeterminate(false);
        mUploadingFwAlpha = 1.0f;
        mDfuCompletedAlpha = 1.0f;
        mDfuCompletedMsg.setText(R.string.dfu_step_completed);
        mDfuCompletedMsg.setAlpha(mDfuCompletedAlpha);
        mDfuCompletedView.setImageResource(R.drawable.ic_done_grey);
        mDfuCompletedView.setAlpha(mDfuCompletedAlpha);
        reconnectToDevice(Utils.decrementAddress(deviceAddress));
    }

    public void onUploadCanceled(final String error) {
        mNordicFirmware.setEnabled(true);
        mCustomFirmware.setEnabled(true);
        mDfuCompletedMsg.setAlpha(1.0f);
        mDfuCompletedMsg.setText(error);
        mDfuCompletedView.setAlpha(1.0f);
        mDfuCompletedView.setImageResource(R.drawable.ic_close_black);
        mFabStartStop.setImageResource(R.drawable.ic_action_dfu_white);
        Utils.showToast(this, getString(R.string.dfu_aborted));
    }

    private void showErrorMessage(final String message) {
        Utils.showToast(this, "Upload failed: " + message);
    }

    private BluetoothDevice getBluetoothDevice(final String thingeeAddress) {
        final BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter ba = bm.getAdapter();
        if (ba != null && ba.isEnabled()) {
            return ba.getRemoteDevice(thingeeAddress);
        }
        return null; //ideally shouldn't go here
    }

    private void showConnectionProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.setTitle(getString(no.nordicsemi.android.thingylib.R.string.please_wait));
        mProgressDialog.setMessage(getString(R.string.dfu_complete_reconnecting));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mScanHandler.postDelayed(runnable, 40000);
        mProgressDialog.show();
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mScanHandler.removeCallbacks(runnable);
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        if (!mIsNordicFw) {
            if (mFilePath == null && mFileStreamUri == null) {
                Utils.showToast(SecureDfuActivity.this, getString(R.string.dfu_alert_no_file_selected));
                return;
            }
        }
        mDevice = device;
        if (name.isEmpty()) {
            name = mDatabaseHelper.getDeviceName(device.getAddress());
        }
        mTargetName = name;
        mDfuTargetNameView.setText(mTargetName);
        resetUi();
        initiateDfu(device);
    }

    @Override
    public void onNothingSelected() {
        if (!mFabStartStop.isEnabled()) {
            mFabStartStop.setEnabled(true);
        }
    }

    private void resetUi() {
        final float initialAlpha = 0.2f;
        mEnabledBootloaderAlpha = initialAlpha;
        mEnableBootloaderView.setAlpha(initialAlpha);
        mEnableBootloaderMsg.setAlpha(initialAlpha);

        mInitDfuAlpha = initialAlpha;
        mInitializingDfuView.setAlpha(initialAlpha);
        mInitializingDfuMsg.setAlpha(initialAlpha);

        mUploadingFwAlpha = initialAlpha;
        mUploadingFwView.setAlpha(initialAlpha);
        mUploadingFwMsg.setAlpha(initialAlpha);
        mDfuSpeed.setAlpha(initialAlpha);
        mDfuSpeedUnit.setAlpha(initialAlpha);

        mDfuCompletedAlpha = initialAlpha;
        mDfuCompletedView.setImageResource(R.drawable.ic_done_grey);
        mDfuCompletedView.setAlpha(initialAlpha);
        mDfuCompletedMsg.setText(R.string.dfu_step_completed);
        mDfuCompletedMsg.setAlpha(initialAlpha);

        mProgressBar.setProgress(0);
    }

    private void enableEnvironmentNotifications() {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE)) {
            mThingySdkManager.enableTemperatureNotifications(mDevice, true);
        } else {
            mThingySdkManager.enableTemperatureNotifications(mDevice, false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE)) {
            mThingySdkManager.enablePressureNotifications(mDevice, true);
        } else {
            mThingySdkManager.enablePressureNotifications(mDevice, false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY)) {
            mThingySdkManager.enableHumidityNotifications(mDevice, true);
        } else {
            mThingySdkManager.enableHumidityNotifications(mDevice, false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY)) {
            mThingySdkManager.enableAirQualityNotifications(mDevice, true);
        } else {
            mThingySdkManager.enableAirQualityNotifications(mDevice, false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_COLOR)) {
            mThingySdkManager.enableColorNotifications(mDevice, true);
        } else {
            mThingySdkManager.enableColorNotifications(mDevice, false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_BUTTON)) {
            mThingySdkManager.enableButtonStateNotification(mDevice, true);
        } else {
            mThingySdkManager.enableColorNotifications(mDevice, false);
        }
    }

    private void enableUiNotifications() {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_BUTTON)) {
            mThingySdkManager.enableButtonStateNotification(mDevice, true);
        } else {
            mThingySdkManager.enableButtonStateNotification(mDevice, false);
        }
    }

    private void enableMotionNotifications() {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_EULER)) {
            enableEulerNotifications(true);
        } else {
            enableEulerNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TAP)) {
            enableTapNotifications(true);
        } else {
            enableTapNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HEADING)) {
            enableHeadingNotifications(true);
        } else {
            enableHeadingNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR)) {
            enableGravityVectorNotifications(true);
        } else {
            enableGravityVectorNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION)) {
            enableOrientationNotifications(true);
        } else {
            enableOrientationNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
            enableQuaternionNotifications(true);
        } else {
            enableQuaternionNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER)) {
            enablePedometerNotifications(true);
        } else {
            enablePedometerNotifications(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA)) {
            enableRawdataNotifications(true);
        } else {
            enableRawdataNotifications(false);
        }
    }

    public void enableSoundNotifications(final BluetoothDevice device, final boolean flag) {
        if (mThingySdkManager != null) {
            mThingySdkManager.enableSpeakerStatusNotifications(device, flag);
        }
    }

    public void enableOrientationNotifications(final boolean flag) {
        mThingySdkManager.enableOrientationNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION);
    }

    public void enableHeadingNotifications(final boolean flag) {
        mThingySdkManager.enableHeadingNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HEADING);
    }

    public void enableTapNotifications(final boolean flag) {
        mThingySdkManager.enableTapNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TAP);
    }

    public void enableQuaternionNotifications(final boolean flag) {
        mThingySdkManager.enableQuaternionNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION);
    }

    public void enablePedometerNotifications(final boolean flag) {
        mThingySdkManager.enablePedometerNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER);
    }

    public void enableGravityVectorNotifications(final boolean flag) {
        mThingySdkManager.enableGravityVectorNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR);
    }

    public void enableRawdataNotifications(final boolean flag) {
        mThingySdkManager.enableRawDataNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA);
    }

    public void enableEulerNotifications(final boolean flag) {
        mThingySdkManager.enableEulerNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_EULER);
    }

    public void enableNotificationsForCloudUpload() {
        mThingySdkManager.enableTemperatureNotifications(mDevice, true);
        mThingySdkManager.enablePressureNotifications(mDevice, true);
        mThingySdkManager.enableButtonStateNotification(mDevice, true);
    }
}
