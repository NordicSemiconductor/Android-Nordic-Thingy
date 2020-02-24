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

package no.nordicsemi.android.nrfthingy.configuration;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrfthingy.MainActivity;
import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.EnableNFCDialogFragment;
import no.nordicsemi.android.nrfthingy.common.PermissionRationaleDialogFragment;
import no.nordicsemi.android.nrfthingy.common.ProgressDialogFragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.dfu.DfuHelper;
import no.nordicsemi.android.nrfthingy.dfu.DfuUpdateAvailableDialogFragment;
import no.nordicsemi.android.nrfthingy.dfu.SecureDfuActivity;
import no.nordicsemi.android.nrfthingy.thingy.Thingy;
import no.nordicsemi.android.nrfthingy.thingy.ThingyService;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

import static no.nordicsemi.android.nrfthingy.common.Utils.*;
import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ACCESS_COARSE_LOCATION;
import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ACCESS_FINE_LOCATION;
import static no.nordicsemi.android.nrfthingy.common.Utils.TAG;
import static no.nordicsemi.android.nrfthingy.common.Utils.checkIfVersionIsMarshmallowOrAbove;
import static no.nordicsemi.android.nrfthingy.common.Utils.checkIfVersionIsQ;
import static no.nordicsemi.android.nrfthingy.common.Utils.getBluetoothDevice;
import static no.nordicsemi.android.nrfthingy.common.Utils.isConnected;

public class InitialConfigurationActivity extends AppCompatActivity implements ScannerFragmentListener,
        PermissionRationaleDialogFragment.PermissionDialogListener,
        ThingySdkManager.ServiceConnectionListener,
        CancelInitialConfigurationDialogFragment.CancelInitialConfigurationListener,
        DfuUpdateAvailableDialogFragment.DfuUpdateAvailableListener,
        EnableNFCDialogFragment.EnableNFCDialogFragmentListener {

    private static final int SCAN_DURATION = 15000;
    private LinearLayout mThingyInfoContainer;
    private LinearLayout mDeviceNameContainer;
    private LinearLayout mSetupCompleteContainer;
    private LinearLayout mLocationServicesContainer;

    private TextInputEditText mDeviceInfo;

    private Button mConfirmThingy;
    private Button mConfirmDeviceName;
    private LinearLayout mNfcContainer;

    private TextView mStepOne;
    private TextView mStepTwo;
    private TextView mStepOneSummary;

    private View mView;

    private ScrollView mScrollView;

    private boolean mStepOneComplete;
    private boolean mStepTwoComplete;

    private String mDeviceName;

    private boolean mConfig;
    private BluetoothDevice mDevice;

    private DatabaseHelper mDatabaseHelper;

    private ThingySdkManager mThingySdkManager;

    private Handler mProgressHandler = new Handler();
    private ProgressDialogFragment mProgressDialog;

    private ScannerFragment mScannerFragment;

    private ThingyService.ThingyBinder mBinder;
    private boolean mIsScanning;
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mIntentFiltersArray;
    private PendingIntent mNfcPendingIntent;
    private String mAddressNfc;

    private BroadcastReceiver mLocationProviderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean enabled = isLocationEnabled();
            if (enabled) {
                mLocationServicesContainer.setVisibility(View.GONE);
            } else {
                mLocationServicesContainer.setVisibility(View.VISIBLE);
            }
        }
    };

    private BroadcastReceiver mNfcAdapterStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean enabled = isNfcEnabled();
            updateNfcUi(enabled);
        }
    };

    private ThingyListener mThingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            if (device.equals(mDevice)) {
                updateProgressDialogState(getString(R.string.state_discovering_services, device.getName()));
                mStepOneSummary.setText(getString(R.string.status_connected_to_device, device.getName()));
            }
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            if (device.equals(mDevice)) {
                mStepOneSummary.setText(R.string.connect_thingy_summary);
                showToast(InitialConfigurationActivity.this, getString(R.string.thingy_disconnected, device.getName()));
                hideProgressDialog();
            }
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            onServiceDiscoveryCompletion(device);
        }

        @Override
        public void onBatteryLevelChanged(final BluetoothDevice bluetoothDevice, final int batteryLevel) {

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
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

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
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_configuration);

        mThingySdkManager = ThingySdkManager.getInstance();

        final Toolbar mainToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mainToolbar);
        mainToolbar.setTitle(getString(R.string.initial_configuration));

        mConfig = getIntent().getBooleanExtra(INITIAL_CONFIG_FROM_ACTIVITY, false);

        if (mConfig) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        }

        mDatabaseHelper = new DatabaseHelper(this);
        mScannerFragment = ScannerFragment.getInstance(ThingyUtils.THINGY_BASE_UUID);

        mThingyInfoContainer = findViewById(R.id.thingy_container);
        mDeviceNameContainer = findViewById(R.id.device_name_container);
        mSetupCompleteContainer = findViewById(R.id.setup_complete_container);
        mLocationServicesContainer = findViewById(R.id.location_services_container);
        Button enableLocationServices = findViewById(R.id.enable_location_services);

        mScrollView = findViewById(R.id.scroll_view);

        mDeviceInfo = findViewById(R.id.device_name);
        mNfcContainer = findViewById(R.id.nfc_container);
        final Button mEnableNfc = findViewById(R.id.enable_nfc);
        final Button mNfcMore = findViewById(R.id.more_nfc_info);
        mConfirmThingy = findViewById(R.id.confirm_thingy);
        mConfirmDeviceName = findViewById(R.id.confirm_device_name);
        Button skipDeviceName = findViewById(R.id.skip_device_name);
        Button getStarted = findViewById(R.id.get_started);

        mStepOne = findViewById(R.id.step_one);
        mStepTwo = findViewById(R.id.step_two);
        mStepOneSummary = findViewById(R.id.step_one_summary);
        mView = findViewById(R.id.vertical_line);

        loadNfcAdapter();

        mEnableNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                requestNfcFeature();
            }
        });

        mNfcMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showNfcDialogRationale();
            }
        });

        enableLocationServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        mStepOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateOnStepOneComplete();
            }
        });

        mStepTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateOnStepTwoComplete();
            }
        });

        mConfirmThingy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkIfVersionIsQ()) {
                    if (ActivityCompat.checkSelfPermission(InitialConfigurationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (isLocationEnabled()) {
                            if (isBleEnabled()) {
                                final String title = mConfirmThingy.getText().toString().trim();
                                if (title.contains("Disconnect")) {
                                    mAddressNfc = null;
                                    mThingySdkManager.disconnectFromThingy(mDevice);
                                    mConfirmThingy.setText(R.string.scan_thingy);
                                }
                                mScannerFragment.show(getSupportFragmentManager(), null);
                            } else enableBle();
                        } else {
                            showToast(InitialConfigurationActivity.this, getString(R.string.location_services_disabled));
                        }
                    } else {
                        final PermissionRationaleDialogFragment dialog = PermissionRationaleDialogFragment.getInstance(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_ACCESS_FINE_LOCATION, getString(R.string.rationale_message_location));
                        dialog.show(getSupportFragmentManager(), null);
                    }
                } else if (checkIfVersionIsMarshmallowOrAbove()) {
                    if (ActivityCompat.checkSelfPermission(InitialConfigurationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (isLocationEnabled()) {
                            if (isBleEnabled()) {
                                final String title = mConfirmThingy.getText().toString().trim();
                                if (title.contains("Disconnect")) {
                                    mAddressNfc = null;
                                    mThingySdkManager.disconnectFromThingy(mDevice);
                                    mConfirmThingy.setText(R.string.scan_thingy);
                                }
                                mScannerFragment.show(getSupportFragmentManager(), null);
                            } else enableBle();
                        } else {
                            showToast(InitialConfigurationActivity.this, getString(R.string.location_services_disabled));
                        }
                    } else {
                        final PermissionRationaleDialogFragment dialog = PermissionRationaleDialogFragment.getInstance(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION, getString(R.string.rationale_message_location));
                        dialog.show(getSupportFragmentManager(), null);
                    }
                } else {
                    if (isBleEnabled()) {
                        final String title = mConfirmThingy.getText().toString().trim();
                        if (title.contains("Disconnect")) {
                            mAddressNfc = null;
                            mThingySdkManager.disconnectFromThingy(mDevice);
                        }
                        mScannerFragment.show(getSupportFragmentManager(), null);
                    } else enableBle();
                }
            }
        });

        mConfirmDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateStepTwo(true);
            }
        });

        skipDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateStepTwo(false);
            }
        });

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStarted();
            }
        });

        mDeviceInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mConfirmDeviceName.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (savedInstanceState != null) {
            mProgressDialog = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
            mDevice = savedInstanceState.getParcelable(EXTRA_DEVICE);
            mStepOneComplete = savedInstanceState.getBoolean("Step1", false);
            mStepTwoComplete = savedInstanceState.getBoolean("Step2", false);
            mAddressNfc = savedInstanceState.getString("ADDRESS_FOR_NFC");
            if (mProgressDialog != null) {
                if (savedInstanceState.getBoolean("IS_SCANNING")) {
                    startScan();
                }
            }

            if (mStepOneComplete) {
                mStepOneSummary.setText(getString(R.string.status_connected_to_device, mDevice.getName()));
                mConfirmThingy.setText(R.string.disconnect_connect);
                animateStepOne();
            }

            if (mStepTwoComplete) {
                animateStepTwo(true);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            registerReceiver(mLocationProviderChangedReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        }
        registerReceiver(mNfcAdapterStateChangedReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        handleNfcForegroundDispatch(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isBleEnabled()) {
            enableBle();
        }

        updateNfcUi(isNfcEnabled());

        if (!isLocationEnabled()) {
            mLocationServicesContainer.setVisibility(View.VISIBLE);
        }

        mThingySdkManager.bindService(this, ThingyService.class);
        ThingyListenerHelper.registerThingyListener(this, mThingyListener);
        registerReceiver(mBleStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfRequiredPermissionsGranted();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mIntentFiltersArray, new String[][]{new String[]{NfcF.class.getName()}});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        final SharedPreferences sp = getSharedPreferences("APP_STATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("APP_STATE", isFinishing());
        editor.apply();

        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mIsScanning) {
            if (mBinder != null) {
                mBinder.setScanningState(true);
                mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable);
                final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(mScanCallback);
                mIsScanning = false;
            }
        }

        mThingySdkManager.unbindService(this);
        ThingyListenerHelper.unregisterThingyListener(this, mThingyListener);
        unregisterReceiver(mBleStateChangedReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            unregisterReceiver(mLocationProviderChangedReceiver);
        }
        unregisterReceiver(mNfcAdapterStateChangedReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                handleOnBackPressed();
                return true;
            case android.R.id.closeButton:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_DEVICE, mDevice);
        outState.putBoolean("Step1", mStepOneComplete);
        outState.putBoolean("Step2", mStepTwoComplete);
        outState.putString("ADDRESS_FOR_NFC", mAddressNfc);
        outState.putBoolean("IS_SCANNING", mIsScanning);
    }

    private void handleOnBackPressed() {
        if (!isAppInitialisedBefore(this)) {
            mThingySdkManager.disconnectFromAllThingies();
            stopService(new Intent(InitialConfigurationActivity.this, ThingyService.class));
            super.onBackPressed();
        } else {
            CancelInitialConfigurationDialogFragment cancelInitialConfiguration = new CancelInitialConfigurationDialogFragment().newInstance();
            cancelInitialConfiguration.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onBackPressed() {
        handleOnBackPressed();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                if (mScannerFragment != null && mScannerFragment.isVisible()) {
                    mScannerFragment.dismiss();
                }
                // finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermission(final String permission, final int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onCancellingPermissionRationale() {
        showToast(this, getString(R.string.requested_permission_not_granted_rationale));
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isBleEnabled()) {
                        enableBle();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.rationale_permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Checks whether the Bluetooth adapter is enabled.
     */
    private boolean isBleEnabled() {
        final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        return ba != null && ba.isEnabled();
    }

    /**
     * Tries to start Bluetooth adapter.
     */
    private void enableBle() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice device, final String name) {
        if (mThingySdkManager != null) {
            mThingySdkManager.connectToThingy(this, device, ThingyService.class);
        }
        mDevice = device;
        animateStepOne();
        showConnectionProgressDialog();
    }

    @Override
    public void onNothingSelected() {

    }

    private void animateStepOne() {
        mStepOne.setText("✔");
        mThingyInfoContainer.animate()
                .translationX(mThingyInfoContainer.getHeight())
                .alpha(0.0f)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mThingyInfoContainer.setVisibility(View.GONE);
                        mDeviceNameContainer.setVisibility(View.VISIBLE);

                        //Resetting the animation parameters, if not the views are not visible in case they are made visible
                        mThingyInfoContainer.setAlpha(1.0f);
                        mThingyInfoContainer.setTranslationX(0);
                        mThingyInfoContainer.clearAnimation();
                    }
                });

        mStepOneComplete = true;
    }

    private void animateStepTwo(final boolean confirmName) {
        mStepTwoComplete = true;
        mStepTwo.setText("✔");

        mDeviceNameContainer.animate()
                .translationX(mDeviceNameContainer.getHeight())
                .alpha(0.0f)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (confirmName) {
                            mDeviceName = mDeviceInfo.getText().toString();
                            if (mDevice != null && !mDeviceName.isEmpty()) {
                                if (mThingySdkManager != null) {
                                    mThingySdkManager.setDeviceName(mDevice, mDeviceName);
                                }
                            }
                        }
                        mDeviceNameContainer.setVisibility(View.GONE);
                        mView.setVisibility(View.GONE);

                        //Resetting the animation parameters, if not the views are not visible in case they are made visible
                        mDeviceNameContainer.setAlpha(1.0f);
                        mDeviceNameContainer.setTranslationX(0);
                        mDeviceNameContainer.clearAnimation();
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                        mSetupCompleteContainer.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void animateOnStepOneComplete() {
        if (mStepOneComplete) {
            mThingyInfoContainer.setVisibility(View.VISIBLE);
            mConfirmThingy.setText(R.string.disconnect_connect);
            if (mDeviceNameContainer.getVisibility() == View.VISIBLE) {
                mDeviceNameContainer.animate()
                        .translationX(mDeviceNameContainer.getHeight())
                        .alpha(0.0f)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mDeviceNameContainer.setVisibility(View.GONE);
                                if (mSetupCompleteContainer.getVisibility() == View.VISIBLE)
                                    mSetupCompleteContainer.setVisibility(View.GONE);

                                //Resetting the animation parameters, if not the views are not visible in case they are made visible
                                mDeviceNameContainer.setAlpha(1.0f);
                                mDeviceNameContainer.setTranslationX(0);
                                mDeviceNameContainer.clearAnimation();
                            }
                        });
            } else {
                mSetupCompleteContainer.animate()
                        .translationY(mSetupCompleteContainer.getHeight())
                        .alpha(0.0f)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (mSetupCompleteContainer.getVisibility() == View.VISIBLE)
                                    mSetupCompleteContainer.setVisibility(View.GONE);

                                //Resetting the animation parameters, if not the views are not visible in case they are made visible
                                mSetupCompleteContainer.setAlpha(1.0f);
                                mSetupCompleteContainer.setTranslationY(0);
                                mSetupCompleteContainer.clearAnimation();
                            }
                        });
            }
        }
    }

    private void animateOnStepTwoComplete() {
        if (mDevice != null) {
            final Thingy thingy = new Thingy(mDevice);
            if (isConnected(thingy, mThingySdkManager.getConnectedDevices())) {
                if (mStepOneComplete) {
                    if (mThingyInfoContainer.getVisibility() == View.VISIBLE) {
                        mThingyInfoContainer.animate()
                                .translationX(mThingyInfoContainer.getHeight())
                                .alpha(0.0f)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mDeviceNameContainer.setVisibility(View.VISIBLE);
                                        mThingyInfoContainer.setVisibility(View.GONE);
                                        if (mSetupCompleteContainer.getVisibility() == View.VISIBLE)
                                            mSetupCompleteContainer.setVisibility(View.GONE);

                                        //Resetting the animation parameters, if not the views are not visible in case they are made visible
                                        mThingyInfoContainer.setAlpha(1.0f);
                                        mThingyInfoContainer.setTranslationX(0);
                                        mThingyInfoContainer.clearAnimation();
                                    }
                                });
                    } else if (mDeviceNameContainer.getVisibility() == View.GONE) {
                        mDeviceNameContainer.setVisibility(View.VISIBLE);
                    } else if (mSetupCompleteContainer.getVisibility() == View.VISIBLE) {
                        mSetupCompleteContainer.animate()
                                .translationY(mSetupCompleteContainer.getHeight())
                                .alpha(0.0f)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        mSetupCompleteContainer.setVisibility(View.GONE);

                                        //Resetting the animation parameters, if not the views are not visible in case they are made visible
                                        mSetupCompleteContainer.setAlpha(1.0f);
                                        mSetupCompleteContainer.setTranslationX(0);
                                        mSetupCompleteContainer.clearAnimation();
                                    }
                                });
                    }
                }
            } else {
                showToast(InitialConfigurationActivity.this, getString(R.string.no_thingy_connected_step_one));
            }
        } else {
            showToast(InitialConfigurationActivity.this, getString(R.string.no_thingy_connected_step_one));
        }
    }

    private void getStarted() {
        if (!isAppInitialisedBefore(this)) {
            final SharedPreferences sp = getSharedPreferences(PREFS_INITIAL_SETUP, MODE_PRIVATE);
            sp.edit().putBoolean(INITIAL_CONFIG_STATE, true).apply();
        }

        final String address = mDevice.getAddress();

        if (!mDatabaseHelper.isExist(address)) {
            if (mDeviceName == null || mDeviceName.isEmpty()) {
                mDeviceName = mDevice.getName();
            }
            mDatabaseHelper.insertDevice(address, mDeviceName);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_COLOR);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_BUTTON);
            mDatabaseHelper.updateNotificationsState(address, true, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION);
            mThingySdkManager.setSelectedDevice(mDevice);
        }
        updateSelectionInDb(new Thingy(mDevice), true);

        if (!mConfig) {
            final Intent intent = new Intent(InitialConfigurationActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_DEVICE, mDevice);
            startActivity(intent);
        }
        finish();
    }

    private void updateSelectionInDb(final no.nordicsemi.android.nrfthingy.thingy.Thingy thingy, final boolean selected) {
        final ArrayList<no.nordicsemi.android.nrfthingy.thingy.Thingy> thingyList = mDatabaseHelper.getSavedDevices();
        for (int i = 0; i < thingyList.size(); i++) {
            if (thingy.getDeviceAddress().equals(thingyList.get(i).getDeviceAddress())) {
                mDatabaseHelper.setLastSelected(thingy.getDeviceAddress(), selected);
            } else {
                mDatabaseHelper.setLastSelected(thingyList.get(0).getDeviceAddress(), !selected);
            }
        }
    }

    @Override
    public void onServiceConnected() {
        //Use this binder to access you own methods declared in the ThingyService
        mBinder = (ThingyService.ThingyBinder) mThingySdkManager.getThingyBinder();
        handleIntent(getIntent());
        if (mThingySdkManager.hasInitialServiceDiscoverCompleted(mDevice)) {
            onServiceDiscoveryCompletion(mDevice);
        }
    }

    @Override
    public void cancelInitialConfiguration() {
        if (mThingySdkManager != null) {
            mThingySdkManager.disconnectFromThingy(mDevice);
        }
        super.onBackPressed();
    }

    final BroadcastReceiver mBleStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        enableBle();
                        break;
                }
            }
        }
    };

    private void showConnectionProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialogFragment.newInstance(getString(R.string.state_connecting));
        }

        final Dialog dialog = mProgressDialog.getDialog();
        if (dialog == null || !dialog.isShowing()) {
            mProgressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        }

        mProgressHandler.postDelayed(mProgressDialogRunnable, SCAN_DURATION);
    }

    final Runnable mProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            hideProgressDialog();
        }
    };

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            final Dialog dialog = mProgressDialog.getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    private void updateProgressDialogState(String message) {
        if (mProgressDialog != null) {
            final Dialog dialog = mProgressDialog.getDialog();
            if (dialog != null) {
                mProgressDialog.setMessage(message);
            }
        }
    }

    private void checkForFwUpdates() {
        final String currentVersion = mThingySdkManager.getFirmwareVersion(mDevice);
        if (DfuHelper.isFirmwareUpdateAvailable(this, currentVersion)) {
            final String newestVersion = DfuHelper.getCurrentFwVersion(this);
            final DfuUpdateAvailableDialogFragment fragment = DfuUpdateAvailableDialogFragment.newInstance(mDevice, newestVersion);
            fragment.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onDfuRequested() {
        final Intent intent = new Intent(this, SecureDfuActivity.class);
        intent.putExtra(EXTRA_DEVICE, mDevice);
        startActivity(intent);
    }

    /**
     * Since Marshmallow location services must be enabled in order to scan.
     *
     * @return true on Android 6.0+ if location mode is different than LOCATION_MODE_OFF. It always returns true on Android versions prior to Marshmellow.
     */
    public boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (final Settings.SettingNotFoundException e) {
                // do nothing
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return true;
    }

    private void handleIntent(final Intent intent) {
        mConfig = intent.getBooleanExtra(INITIAL_CONFIG_FROM_ACTIVITY, false);
        if (mConfig) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final String address = intent.getStringExtra(EXTRA_ADDRESS_DATA);
        if (address != null && !address.isEmpty()) {
            mAddressNfc = address;
            final BluetoothDevice device = getBluetoothDevice(this, address);
            if (device != null) {
                if (!mIsScanning && !mThingySdkManager.isConnected(device)) {
                    prepareForScanning(address);
                }
            } else {
                showToast(this, getString(R.string.error_nfc_tag));
                return;
            }
        }

        //We set the intent to null so that the same intent is not returned every time.
        intent.removeExtra(EXTRA_ADDRESS_DATA);
    }

    private void prepareForScanning(final String address) {
        if (checkIfVersionIsMarshmallowOrAbove()) {
            if (ActivityCompat.checkSelfPermission(InitialConfigurationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    if (isBleEnabled()) {
                        handleStartScan(address);
                    } else enableBle();
                } else {
                    showToast(InitialConfigurationActivity.this, getString(R.string.location_services_disabled));
                }
            } else {
                final PermissionRationaleDialogFragment dialog = PermissionRationaleDialogFragment.getInstance(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_ACCESS_FINE_LOCATION, getString(R.string.rationale_message_location));
                dialog.show(getSupportFragmentManager(), null);
            }
        } else {
            if (isBleEnabled()) {
                final String title = mConfirmThingy.getText().toString().trim();
                if (title.contains("Disconnect")) {
                    mThingySdkManager.disconnectFromThingy(mDevice);
                }
                handleStartScan(address);
                //mScannerFragment.show(getSupportFragmentManager(), null);
            } else enableBle();
        }
    }

    private void handleStartScan(final String address) {
        if (!isConnected(address, mThingySdkManager.getConnectedDevices()) && !mBinder.isScanningState()) {
            mDevice = getBluetoothDevice(this, address);
            final String title = mConfirmThingy.getText().toString().trim();
            if (title.contains("Disconnect")) {
                mThingySdkManager.disconnectFromThingy(mDevice);
                mConfirmThingy.setText(R.string.scan_thingy);
            }

            if (mBinder != null) {
                mBinder.setScanningState(true);
                startScan();
            }
        }
    }

    private void startScan() {
        if (mIsScanning) {
            return;
        }

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(ThingyUtils.THINGY_BASE_UUID)).build());
        scanner.startScan(filters, settings, mScanCallback);
        mIsScanning = true;

        //Handler to stop scan after the duration time out
        mProgressHandler.postDelayed(mBleScannerTimeoutRunnable, SCAN_DURATION);
    }

    /**
     * Stop scan on rotation or on app closing.
     * In case the stopScan is called inside onDestroy we have to check if the app is finishing as the mIsScanning flag becomes false on rotation
     */
    private void stopScan() {
        if (mIsScanning) {
            if (mBinder != null) {
                mBinder.setScanningState(false);
            }
            Log.v(TAG, "Stopping scan");
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable);
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mScanCallback);
            mIsScanning = false;
        } else if (!isFinishing()) {
            if (mBinder != null) {
                mBinder.setScanningState(false);
            }
            Log.v(TAG, "Stopping scan on rotation");
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable);
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mScanCallback);
            mIsScanning = false;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // do nothing
            final BluetoothDevice device = result.getDevice();
            if (device.equals(mDevice)) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressHandler.removeCallbacks(mProgressDialogRunnable);
                        stopScan();
                        onDeviceSelected(result.getDevice(), result.getScanRecord().getDeviceName());
                        Log.v(TAG, "Connect?");
                    }
                });
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

    final Runnable mBleScannerTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private void loadNfcAdapter() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            mStepOneSummary.setText(R.string.add_thingy_nfc_summary);
            mNfcPendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            ndef.addDataScheme("vnd.android.nfc");
            ndef.addDataAuthority("ext", null);
            mIntentFiltersArray = new IntentFilter[]{ndef};
        } else {
            mStepOneSummary.setText(R.string.add_thingy_summary);
        }
    }

    private boolean isNfcEnabled() {
        return !(mNfcAdapter != null && !mNfcAdapter.isEnabled());
    }

    private void updateNfcUi(final boolean isNfcEnabled) {
        if (isNfcEnabled) {
            mNfcContainer.setVisibility(View.GONE);
        } else {
            mNfcContainer.setVisibility(View.VISIBLE);
        }
    }

    public void showNfcDialogRationale() {
        final EnableNFCDialogFragment fragment = EnableNFCDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), null);
    }

    private void handleNfcForegroundDispatch(final Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            final Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                for (final Parcelable rawMsg : rawMsgs) {
                    final NdefMessage msg = (NdefMessage) rawMsg;
                    final NdefRecord[] records = msg.getRecords();

                    for (NdefRecord record : records) {
                        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                            final String mimeType = record.toMimeType();
                            if (mimeType != null && mimeType.equals(EXTRA_ADDRESS_DATA)) {
                                final String address = readAddressPayload(record.getPayload());
                                if (TextUtils.isEmpty(address)) {
                                    showToast(this, getString(R.string.error_reading_nfc_tag));
                                    return;
                                }
                                if (!TextUtils.isEmpty(mAddressNfc)) {
                                    if (!address.equals(mAddressNfc)) {
                                        showToast(this, getString(R.string.error_adding_multiple_devices_over_nfc));
                                        return;
                                    }
                                }

                                if (!mDatabaseHelper.isExist(address)) {
                                    if (isBleEnabled()) {
                                        final BluetoothDevice device = getBluetoothDevice(this, address);
                                        if (device != null) {
                                            if (!isConnected(device, mThingySdkManager.getConnectedDevices())) {
                                                if (mDatabaseHelper.getLastSelected(address)) {
                                                    prepareForScanning(device.getAddress());
                                                } else {
                                                    mDatabaseHelper.setLastSelected(address, true);
                                                }
                                            } else {
                                                showToast(this, getString(R.string.thingy_already_connected, device.getName()));
                                            }
                                        } else {
                                            showToast(this, getString(R.string.error_nfc_tag));
                                        }
                                    }
                                } else {
                                    showToast(this, getString(R.string.thingy_already_added, mDatabaseHelper.getDeviceName(address)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkIfRequiredPermissionsGranted() {
        if (checkIfVersionIsQ()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                final PermissionRationaleDialogFragment dialog = PermissionRationaleDialogFragment.getInstance(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_ACCESS_FINE_LOCATION, getString(R.string.rationale_message_location));
                dialog.show(getSupportFragmentManager(), null);
                return false;
            }
        } else if (checkIfVersionIsMarshmallowOrAbove()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                final PermissionRationaleDialogFragment dialog = PermissionRationaleDialogFragment.getInstance(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION, getString(R.string.rationale_message_location));
                dialog.show(getSupportFragmentManager(), null);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void requestNfcFeature() {
        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
    }

    private void onServiceDiscoveryCompletion(final BluetoothDevice device) {
        mThingySdkManager.enableEnvironmentNotifications(device, true);
        hideProgressDialog();
        checkForFwUpdates();
    }
}
