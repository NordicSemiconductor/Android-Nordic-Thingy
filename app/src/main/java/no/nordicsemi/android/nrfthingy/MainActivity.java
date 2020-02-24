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

package no.nordicsemi.android.nrfthingy;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import no.nordicsemi.android.nrfthingy.common.AboutActivity;
import no.nordicsemi.android.nrfthingy.common.EnableNFCDialogFragment;
import no.nordicsemi.android.nrfthingy.common.MessageDialogFragment;
import no.nordicsemi.android.nrfthingy.common.NFCTagFoundDialogFragment;
import no.nordicsemi.android.nrfthingy.common.PermissionRationaleDialogFragment;
import no.nordicsemi.android.nrfthingy.common.ProgressDialogFragment;
import no.nordicsemi.android.nrfthingy.configuration.ConfigurationActivity;
import no.nordicsemi.android.nrfthingy.configuration.ConfirmThingyDeletionDialogFragment;
import no.nordicsemi.android.nrfthingy.configuration.InitialConfigurationActivity;
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

import static no.nordicsemi.android.nrfthingy.common.Utils.CLOUD_FRAGMENT;
import static no.nordicsemi.android.nrfthingy.common.Utils.CURRENT_DEVICE;
import static no.nordicsemi.android.nrfthingy.common.Utils.ENVIRONMENT_FRAGMENT;
import static no.nordicsemi.android.nrfthingy.common.Utils.EXTRA_ADDRESS_DATA;
import static no.nordicsemi.android.nrfthingy.common.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfthingy.common.Utils.GROUP_ID_ABOUT;
import static no.nordicsemi.android.nrfthingy.common.Utils.GROUP_ID_ADD_THINGY;
import static no.nordicsemi.android.nrfthingy.common.Utils.GROUP_ID_DFU;
import static no.nordicsemi.android.nrfthingy.common.Utils.GROUP_ID_SAVED_THINGIES;
import static no.nordicsemi.android.nrfthingy.common.Utils.INITIAL_CONFIG_FROM_ACTIVITY;
import static no.nordicsemi.android.nrfthingy.common.Utils.ITEM_ID_ADD_THINGY;
import static no.nordicsemi.android.nrfthingy.common.Utils.ITEM_ID_DFU;
import static no.nordicsemi.android.nrfthingy.common.Utils.ITEM_ID_SETTINGS;
import static no.nordicsemi.android.nrfthingy.common.Utils.MOTION_FRAGMENT;
import static no.nordicsemi.android.nrfthingy.common.Utils.NFC_DIALOG_TAG;
import static no.nordicsemi.android.nrfthingy.common.Utils.NOTIFICATION_ID;
import static no.nordicsemi.android.nrfthingy.common.Utils.PROGRESS_DIALOG_TAG;
import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ACCESS_COARSE_LOCATION;
import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ACCESS_FINE_LOCATION;
import static no.nordicsemi.android.nrfthingy.common.Utils.REQUEST_ENABLE_BT;
import static no.nordicsemi.android.nrfthingy.common.Utils.SOUND_FRAGMENT;
import static no.nordicsemi.android.nrfthingy.common.Utils.TAG;
import static no.nordicsemi.android.nrfthingy.common.Utils.UI_FRAGMENT;
import static no.nordicsemi.android.nrfthingy.common.Utils.checkIfVersionIsMarshmallowOrAbove;
import static no.nordicsemi.android.nrfthingy.common.Utils.checkIfVersionIsQ;
import static no.nordicsemi.android.nrfthingy.common.Utils.getBluetoothDevice;
import static no.nordicsemi.android.nrfthingy.common.Utils.isConnected;
import static no.nordicsemi.android.nrfthingy.common.Utils.readAddressPayload;
import static no.nordicsemi.android.nrfthingy.common.Utils.showNfcDisabledWarning;
import static no.nordicsemi.android.nrfthingy.common.Utils.showToast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        EnvironmentServiceFragment.EnvironmentServiceListener,
        ConfirmThingyDeletionDialogFragment.ConfirmThingyDeletionListener,
        ThingySdkManager.ServiceConnectionListener,
        PermissionRationaleDialogFragment.PermissionDialogListener,
        DfuUpdateAvailableDialogFragment.DfuUpdateAvailableListener,
        NFCTagFoundDialogFragment.OnNfcTagFound,
        EnableNFCDialogFragment.EnableNFCDialogFragmentListener {

    private static final int SCAN_DURATION = 15000;
    private LinearLayout mLocationServicesContainer;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar mActivityToolbar;

    private LinearLayout mHeaderTitleContainer;
    private TextView mHeaderTitle;
    private ImageView mHeaderToggle;
    private RelativeLayout mNoThingyConnectedContainer;

    private ArrayList<BluetoothDevice> mConnectedBleDeviceList;

    private BluetoothDevice mDevice;
    private BluetoothDevice mOldDevice;

    private String mFragmentTag = ENVIRONMENT_FRAGMENT;
    private boolean isHeaderExpanded = true;

    private DatabaseHelper mDatabaseHelper;
    private ThingySdkManager mThingySdkManager;
    private ColorStateList mColorStateList;
    private Drawable mNavigationViewBackground;
    private ProgressDialogFragment mProgressDialog;

    private Handler mProgressHandler = new Handler();
    private boolean mIsScanning;

    private ThingyService.ThingyBinder mBinder;

    private Ringtone mRingtone;
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mIntentFiltersArray;
    private PendingIntent mNfcPendingIntent;
    private LinearLayout mNfcContainer;
    private boolean mIsDrawerOpened;

    private TextView mBatteryLevel;
    private ImageView mBatteryLevelImg;
    private NFCTagFoundDialogFragment mNfcTagFoundDialogFragment;

    private ThingyListener mThingyListener = new ThingyListener() {
        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            final String deviceName = mDatabaseHelper.getDeviceName(device.getAddress());
            updateProgressDialogState(getString(R.string.state_discovering_services, deviceName));
            invalidateOptionsMenu();
            if (!mConnectedBleDeviceList.contains(device)) {
                mConnectedBleDeviceList.add(device);
            }

            updateUiOnDeviceConnected(device);
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            updateBatteryLevelVisibility(View.GONE);
            hideProgressDialog();
            if (mConnectedBleDeviceList.contains(device)) {
                mConnectedBleDeviceList.remove(device);
            }
            updateUiOnDeviceDisconnected(device);
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            updateBatteryLevelVisibility(View.VISIBLE);
            onServiceDiscoveryCompletion(device);
            checkForFwUpdates();
        }

        @Override
        public void onBatteryLevelChanged(final BluetoothDevice bluetoothDevice, final int batteryLevel) {
            Log.v(ThingyUtils.TAG, "Battery Level: " + batteryLevel + "  address: " + bluetoothDevice.getAddress() + " name: " + mDatabaseHelper.getDeviceName(bluetoothDevice.getAddress()));
            if (bluetoothDevice.equals(mThingySdkManager.getSelectedDevice())) {
                if (mIsDrawerOpened) {
                    updateBatteryLevel(batteryLevel);
                }
            }
        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, final int eco2, final int tvoc) {

        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, final int buttonState) {
            if (bluetoothDevice.equals(mDevice)) {
                switch (buttonState) {
                    case 0:
                        if (mRingtone != null) {
                            if (mRingtone.isPlaying()) {
                                mRingtone.stop();
                            }
                            mRingtone = null;
                        }

                        break;
                    case 1:
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (notification != null) {
                            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            if (mRingtone != null) {
                                mRingtone.play();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void onTapValueChangedEvent(final BluetoothDevice bluetoothDevice, final int direction, final int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(final BluetoothDevice bluetoothDevice, final int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(final BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(final BluetoothDevice bluetoothDevice, final int steps, final long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(final BluetoothDevice bluetoothDevice, final float x, final float y, final float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(final BluetoothDevice bluetoothDevice, final float x, final float y, final float z) {

        }

        @Override
        public void onCompassValueChangedEvent(final BluetoothDevice bluetoothDevice, final float x, final float y, final float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(final BluetoothDevice bluetoothDevice, final float roll, final float pitch, final float yaw) {

        }

        @Override
        public void onRotationMatrixValueChangedEvent(final BluetoothDevice bluetoothDevice, final byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(final BluetoothDevice bluetoothDevice, final float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(final BluetoothDevice bluetoothDevice, final float x, final float y, final float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(final BluetoothDevice bluetoothDevice, final int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(final BluetoothDevice bluetoothDevice, final byte[] data) {

        }
    };

    private void updateBatteryLevelVisibility(final int visibility) {
        mBatteryLevel.setVisibility(visibility);
        mBatteryLevelImg.setVisibility(visibility);
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);

        mActivityToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mActivityToolbar);

        mThingySdkManager = ThingySdkManager.getInstance();
        mDatabaseHelper = new DatabaseHelper(this);

        mLocationServicesContainer = findViewById(R.id.location_services_container);
        mNoThingyConnectedContainer = findViewById(R.id.no_thingy_connected);
        mNfcContainer = findViewById(R.id.nfc_container);
        final Button enableNfc = findViewById(R.id.enable_nfc);
        final Button nfcInfo = findViewById(R.id.more_nfc_info);
        final FloatingActionButton connectThingy = findViewById(R.id.connect_thingy);
        mNavigationView = findViewById(R.id.navigation);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        final View headerView = mNavigationView.getHeaderView(0);
        mHeaderTitle = headerView.findViewById(R.id.header_title);
        mBatteryLevel = headerView.findViewById(R.id.battery_level);
        mBatteryLevelImg = headerView.findViewById(R.id.battery_level_img);
        mHeaderToggle = headerView.findViewById(R.id.header_toggle);
        mHeaderTitleContainer = headerView.findViewById(R.id.header_title_container);
        // Ensure that Bluetooth exists
        if (!ensureBleExists())
            finish();
        mColorStateList = ContextCompat.getColorStateList(this, R.color.menu_item_text);
        mNavigationViewBackground = ContextCompat.getDrawable(this, R.drawable.menu_item_background);

        mNavigationView.setNavigationItemSelectedListener(this);

        mConnectedBleDeviceList = new ArrayList<>();
        loadNfcAdapter();

        enableNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                requestNfcFeature();
            }
        });
        nfcInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showNfcDialogRationale();
            }
        });

        final TextView enableLocationServices = findViewById(R.id.enable_location_services);
        enableLocationServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        connectThingy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent initialConfiguration = new Intent(MainActivity.this, InitialConfigurationActivity.class);
                initialConfiguration.putExtra(INITIAL_CONFIG_FROM_ACTIVITY, true);
                startActivity(initialConfiguration);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mActivityToolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Disable the Hamburger icon animation
                super.onDrawerSlide(drawerView, 0);
                if (slideOffset == 1) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        updateBatteryLevel(mThingySdkManager.getBatteryLevel(mDevice));
                    } else {
                        updateBatteryLevelVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mIsDrawerOpened = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mIsDrawerOpened = false;
                createDrawerMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mHeaderTitleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableSelection();
            }
        });

        mHeaderToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableSelection();
            }
        });

        createDrawerMenu();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            registerReceiver(mLocationProviderChangedReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        }
        registerNfcBroadcastReceiver();
        if (savedInstanceState != null) {
            mProgressDialog = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
            mNfcTagFoundDialogFragment = (NFCTagFoundDialogFragment) getSupportFragmentManager().findFragmentByTag(NFC_DIALOG_TAG);
        }
    }

    private void registerNfcBroadcastReceiver() {
        if (mNfcAdapter != null) {
            registerReceiver(mNfcAdapterStateChangedReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        }
    }

    private void unregisterNfcBroadcastReceiver() {
        if (mNfcAdapter != null) {
            unregisterReceiver(mNfcAdapterStateChangedReceiver);
        }
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
        } else {
            mLocationServicesContainer.setVisibility(View.GONE);
        }
        mThingySdkManager.bindService(this, ThingyService.class);
        ThingyListenerHelper.registerThingyListener(this, mThingyListener);
        registerReceiver(mBleStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mIntentFiltersArray, new String[][]{new String[]{NfcF.class.getName()}});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBinder != null) {
            final boolean isFinishing = isFinishing();
            mBinder.setActivityFinishing(isFinishing);
            if (isFinishing) {
                mBinder.setLastVisibleFragment(ENVIRONMENT_FRAGMENT);
            }
        }
        stopScanOnRotation();
        mThingySdkManager.unbindService(this);
        mBinder = null;
        ThingyListenerHelper.unregisterThingyListener(this, mThingyListener);
        unregisterReceiver(mBleStateChangedReceiver);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            ThingySdkManager.clearInstance();
        }
        unregisterNfcBroadcastReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            unregisterReceiver(mLocationProviderChangedReceiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (!isNfcEnabled()) {
                    updateNfcUi(false);
                } else {
                    updateNfcUi(true);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDevice != null) {
            if (mThingySdkManager.isConnected(mDevice)) {
                getMenuInflater().inflate(R.menu.main_menu_disconnect, menu);
            } else {
                if (mDatabaseHelper.isExist(mDevice.getAddress())) {
                    getMenuInflater().inflate(R.menu.main_menu_connect, menu);
                } else {
                    Log.v(TAG, "DELETED");
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_device:
                final DialogFragment dialog = new ConfirmThingyDeletionDialogFragment().newInstance(mDevice);
                dialog.show(getSupportFragmentManager(), null);
                break;
            case R.id.action_connect:
                prepareForScanning(false);
                break;
            case R.id.action_disconnect:
                if (mThingySdkManager != null) {
                    mThingySdkManager.disconnectFromThingy(mDevice);
                }
                break;
            case R.id.action_settings:
                break;
        }
        return true;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        // Sync the enable state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getGroupId();
        switch (id) {
            case R.id.navigation_menu:
                performFragmentNavigation(item);
                break;
            case R.id.configuration_menu:
                if (mThingySdkManager.isConnected(mDevice)) {
                    Intent configurationIntent = new Intent(this, ConfigurationActivity.class);
                    configurationIntent.putExtra(CURRENT_DEVICE, mDevice);
                    startActivity(configurationIntent);
                } else {
                    showToast(this, getString(R.string.no_thingy_connected_configuration, mDatabaseHelper.getDeviceName(mDevice.getAddress())));
                }
                break;
            case GROUP_ID_SAVED_THINGIES:
                performDeviceSelection(item);
                break;
            case GROUP_ID_ADD_THINGY:
                Intent initialConfiguration = new Intent(MainActivity.this, InitialConfigurationActivity.class);
                initialConfiguration.putExtra(INITIAL_CONFIG_FROM_ACTIVITY, true);
                startActivity(initialConfiguration);
                break;
            case GROUP_ID_DFU:
                Intent intent = new Intent(this, SecureDfuActivity.class);
                intent.putExtra(EXTRA_DEVICE, mDevice);
                startActivity(intent);
                break;
            case GROUP_ID_ABOUT:
                Intent aboutActivity = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutActivity);
                break;
            default:

                break;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }, 200);
        return true;
    }

    private void performDeviceSelection(final MenuItem item) {
        final int itemId = item.getItemId();
        final ArrayList<Thingy> thingyList = mDatabaseHelper.getSavedDevices();
        if (thingyList.size() > 0) {
            final Thingy thingy = thingyList.get(itemId);
            if (thingy != null) {
                BluetoothDevice device = getBluetoothDevice(this, thingy.getDeviceAddress());
                //Save the selected device on the base service so that it can be accessed from anywhere in the app.
                if (mThingySdkManager != null) {
                    mThingySdkManager.setSelectedDevice(device);
                    updateSelectionInDb(thingy, true);
                    mConnectedBleDeviceList.clear();
                    mConnectedBleDeviceList.addAll(mThingySdkManager.getConnectedDevices());
                }

                if (device != null && mConnectedBleDeviceList != null) {
                    if (mConnectedBleDeviceList.contains(device)) {
                        if (mDevice != null) {
                            if (!mDevice.equals(device))
                                mDevice = device;
                        } else mDevice = device;
                        checkSelection(item);
                        selectDevice();
                        return;
                    }
                }
                mDevice = device;
                selectDeviceFromDb();
            }
        }
    }

    private void updateSelectionInDb(final Thingy thingy, final boolean selected) {
        final ArrayList<Thingy> thingyList = mDatabaseHelper.getSavedDevices();
        for (int i = 0; i < thingyList.size(); i++) {
            if (thingy.getDeviceAddress().equals(thingyList.get(i).getDeviceAddress())) {
                mDatabaseHelper.setLastSelected(thingy.getDeviceAddress(), selected);
            } else {
                mDatabaseHelper.setLastSelected(thingyList.get(i).getDeviceAddress(), !selected);
            }
        }
    }

    private void checkSelection(final MenuItem item) {
        int size = mNavigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            mNavigationView.getMenu().getItem(i).setChecked(false);
        }
        item.setChecked(true);
    }

    private void performFragmentNavigation(final MenuItem item) {
        final int itemId = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (itemId) {
            case R.id.navigation_environment:
                if (fragmentManager.findFragmentByTag(ENVIRONMENT_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableEnvironmentNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = ENVIRONMENT_FRAGMENT;

                    EnvironmentServiceFragment environmentServiceFragment = EnvironmentServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, environmentServiceFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_motion:
                if (fragmentManager.findFragmentByTag(MOTION_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableMotionNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = MOTION_FRAGMENT;
                    MotionServiceFragment motionServiceFragment = MotionServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, motionServiceFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_ui:
                if (fragmentManager.findFragmentByTag(UI_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableUiNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = UI_FRAGMENT;
                    UiFragment uiFragment = UiFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, uiFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_sound:
                if (fragmentManager.findFragmentByTag(SOUND_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        enableSoundNotifications(mDevice, true);
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = SOUND_FRAGMENT;
                    SoundFragment soundFragment = SoundFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, soundFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_cloud:
                if (fragmentManager.findFragmentByTag(CLOUD_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableTemperatureNotifications(mDevice, mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress()));
                        mThingySdkManager.enablePressureNotifications(mDevice, mDatabaseHelper.getPressureUploadState(mDevice.getAddress()));
                        mThingySdkManager.enableHumidityNotifications(mDevice, false);
                        mThingySdkManager.enableAirQualityNotifications(mDevice, false);
                        mThingySdkManager.enableColorNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, mDatabaseHelper.getButtonUploadState(mDevice.getAddress()));
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = CLOUD_FRAGMENT;
                    CloudFragment mapFragment = CloudFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, mapFragment, mFragmentTag).commit();
                }
                break;
        }

        if (mBinder != null) {
            mBinder.setLastVisibleFragment(mFragmentTag);
        }
        checkSelection(item);
    }

    private void performFragmentNavigation() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (mFragmentTag) {
            case ENVIRONMENT_FRAGMENT:
                if (fragmentManager.findFragmentByTag(ENVIRONMENT_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableEnvironmentNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = ENVIRONMENT_FRAGMENT;
                    EnvironmentServiceFragment environmentServiceFragment = EnvironmentServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, environmentServiceFragment, mFragmentTag).commit();
                }
                break;
            case MOTION_FRAGMENT:
                if (fragmentManager.findFragmentByTag(MOTION_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableMotionNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = MOTION_FRAGMENT;
                    MotionServiceFragment motionServiceFragment = MotionServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, motionServiceFragment, mFragmentTag).commit();
                }
                break;
            case UI_FRAGMENT:
                if (fragmentManager.findFragmentByTag(UI_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableUiNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = UI_FRAGMENT;
                    UiFragment uiFragment = UiFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, uiFragment, mFragmentTag).commit();
                }
                break;
            case SOUND_FRAGMENT:
                if (fragmentManager.findFragmentByTag(SOUND_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        enableSoundNotifications(mDevice, true);
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = SOUND_FRAGMENT;
                    SoundFragment soundFragment = SoundFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, soundFragment, mFragmentTag).commit();
                }
                break;
            case CLOUD_FRAGMENT:
                if (fragmentManager.findFragmentByTag(CLOUD_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        if (mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress())) {
                            mThingySdkManager.enableTemperatureNotifications(mDevice, mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress()));
                            mThingySdkManager.enablePressureNotifications(mDevice, mDatabaseHelper.getPressureUploadState(mDevice.getAddress()));
                            mThingySdkManager.enableHumidityNotifications(mDevice, false);
                            mThingySdkManager.enableAirQualityNotifications(mDevice, false);
                            mThingySdkManager.enableColorNotifications(mDevice, false);
                            mThingySdkManager.enableMotionNotifications(mDevice, false);
                            mThingySdkManager.enableUiNotifications(mDevice, mDatabaseHelper.getButtonUploadState(mDevice.getAddress()));
                            mThingySdkManager.enableSoundNotifications(mDevice, false);
                        }
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = CLOUD_FRAGMENT;
                    CloudFragment mapFragment = CloudFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, mapFragment, mFragmentTag).commit();
                }
                break;
        }
    }

    /**
     * Checks whether the device supports Bluetooth Low Energy communication
     *
     * @return <code>true</code> if BLE is supported, <code>false</code> otherwise
     */
    private boolean ensureBleExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
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
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    private void connect() {
        mThingySdkManager.connectToThingy(this, mDevice, ThingyService.class);
        final Thingy thingy = new Thingy(mDevice);
        mThingySdkManager.setSelectedDevice(mDevice);
        updateSelectionInDb(thingy, true);
    }

    private void connect(final BluetoothDevice device) {
        mThingySdkManager.connectToThingy(this, device, ThingyService.class);
        final Thingy thingy = new Thingy(device);
        mThingySdkManager.setSelectedDevice(device);
        updateSelectionInDb(thingy, true);
        updateUiOnBind();
    }

    private void enableSelection() {
        if (isHeaderExpanded) {
            isHeaderExpanded = false;
            createDeviceDrawerMenu();
        } else {
            isHeaderExpanded = true;
            createFragmentDrawerMenu();
        }
    }

    private void createDrawerMenu() {
        if (mThingySdkManager != null) {
            final List<BluetoothDevice> devices = new ArrayList<>(mThingySdkManager.getConnectedDevices());
            ArrayList<Thingy> thingyList = mDatabaseHelper.getSavedDevices();
            if ((devices.size() > 0) || (thingyList != null && thingyList.size() > 0)) {
                updateHeaderView(View.VISIBLE);
                createFragmentDrawerMenu();
            } else {
                createDeviceDrawerMenu();
            }
        }
    }

    private void createFragmentDrawerMenu() {
        BluetoothDevice device = mDevice;
        mHeaderToggle.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_expand));
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.drawer_menu_options);
        mNavigationView.setItemIconTintList(null);
        mNavigationView.setItemTextColor(mColorStateList);
        mNavigationView.setItemBackground(mNavigationViewBackground);
        isHeaderExpanded = true;
        checkFragmentDrawerItem();
        if (device == null) {
            device = mThingySdkManager.getSelectedDevice();
            mDevice = device;
        }

        updateActionbarTitle(device);
        updateHeaderView(View.VISIBLE);
    }

    private void createDeviceDrawerMenu() {
        mHeaderToggle.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_collapse));
        mNavigationView.getMenu().clear();
        mNavigationView.setItemIconTintList(null);
        mNavigationView.setItemTextColor(mColorStateList);
        mNavigationView.setItemBackground(mNavigationViewBackground);
        int total = 0;
        ArrayList<Thingy> thingyList = mDatabaseHelper.getSavedDevices();
        Thingy thingy;
        for (int i = 0; i < thingyList.size(); i++) {
            thingy = thingyList.get(i);
            if (mThingySdkManager.isConnected(getBluetoothDevice(this, thingy.getDeviceAddress()))) {
                if (!mDatabaseHelper.getLastSelected(thingy.getDeviceAddress())) {
                    mNavigationView.getMenu().add(GROUP_ID_SAVED_THINGIES, i, i, thingyList.get(i).getDeviceName()).setIcon(R.drawable.ic_thingy_blue);
                }
            } else {
                if (!mDatabaseHelper.getLastSelected(thingy.getDeviceAddress())) {
                    mNavigationView.getMenu().add(GROUP_ID_SAVED_THINGIES, i, i, thingyList.get(i).getDeviceName()).setIcon(R.drawable.ic_thingy_gray);
                }
            }
            total = i;
        }

        if (thingyList.size() == 0) {
            mHeaderTitleContainer.setVisibility(View.GONE);
        }

        total = total + 1;
        mNavigationView.getMenu().add(GROUP_ID_ADD_THINGY, ITEM_ID_ADD_THINGY, total, getString(R.string.action_add)).setIcon(R.drawable.ic_add_black);
        total += total;
        mNavigationView.getMenu().add(GROUP_ID_DFU, ITEM_ID_DFU, total, getString(R.string.settings_dfu)).setIcon(R.drawable.ic_dfu_gray);
        total += total;
        mNavigationView.getMenu().add(GROUP_ID_ABOUT, ITEM_ID_SETTINGS, total, getString(R.string.action_about)).setIcon(R.drawable.ic_info_grey);
    }

    private void checkFragmentDrawerItem() {
        final String fragmentTag = mFragmentTag;
        switch (fragmentTag) {
            case ENVIRONMENT_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_environment));
                break;
            case UI_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_ui));
                break;
            case MOTION_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_motion));
                break;
            case SOUND_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_sound));
                break;
            case CLOUD_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_cloud));
                break;
        }
    }

    private void selectDeviceFromDb() {
        final BluetoothDevice device = mDevice;
        if (device != null) {
            mNoThingyConnectedContainer.setVisibility(View.GONE);
            final String deviceName = mDatabaseHelper.getDeviceName(device.getAddress());
            if (!deviceName.equals("")) {
                mHeaderTitle.setText(deviceName);
                getSupportActionBar().setTitle(deviceName);
            } else {
                mHeaderTitle.setText(mDevice.getName());
                getSupportActionBar().setTitle(mDevice.getName());
            }
            //When a new fragment is to be displayed clear the existing fragments
            clearFragments();
            //displayFragment();
            performFragmentNavigation();
            invalidateOptionsMenu();
            mOldDevice = device;
        } else {
            mNoThingyConnectedContainer.setVisibility(View.VISIBLE);
        }
    }

    private void selectDevice() {
        final BluetoothDevice device = mDevice;
        if (device != null) {
            if (mDatabaseHelper.isExist(device.getAddress())) {
                mNoThingyConnectedContainer.setVisibility(View.GONE);
                if (mOldDevice != null && !device.equals(mOldDevice)) {
                    clearFragments();
                }
                //displayFragment();
                performFragmentNavigation();
                invalidateOptionsMenu();
                mOldDevice = device;
            }
        } else {
            mNoThingyConnectedContainer.setVisibility(View.VISIBLE);
        }
    }

    private void clearFragments() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(mFragmentTag);
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitNow();
        }
    }

    private void clearFragments(final String fragmentTag) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitNow();
        }
    }

    private void updateHeaderView(final int visibility) {
        if (mDevice != null) {
            String deviceName = mDatabaseHelper.getDeviceName(mDevice.getAddress());
            if (deviceName.isEmpty()) {
                deviceName = mDevice.getName();
            }
            mHeaderTitle.setText(deviceName);
            mHeaderTitleContainer.setVisibility(visibility);
        } else {
            mHeaderTitle.setText("");
            mHeaderTitleContainer.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void updateActionbarTitle(final BluetoothDevice device) {
        if (device != null) {
            final String address = device.getAddress();
            if (mDatabaseHelper.isExist(address)) {
                String deviceName = mDatabaseHelper.getDeviceName(address);
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = device.getName();
                }
                getSupportActionBar().setTitle(deviceName);
            } else {
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        } else {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void updateUiOnDeviceDeletion(final BluetoothDevice device) {
        final ArrayList<Thingy> savedDevices = mDatabaseHelper.getSavedDevices();
        if (savedDevices.size() == 0) {
            invalidateOptionsMenu();
            createDrawerMenu();
            updateActionbarTitle(device);
            mNoThingyConnectedContainer.setVisibility(View.VISIBLE);
            mConnectedBleDeviceList.clear();
        } else {
            final List<BluetoothDevice> connectedDevices = new ArrayList<>();
            connectedDevices.addAll(mThingySdkManager.getConnectedDevices());
            if (mDevice != null && mDevice.equals(device)) {
                if (connectedDevices.size() > 0) {
                    mThingySdkManager.setSelectedDevice(connectedDevices.get(0));
                    updateSelectionInDb(new Thingy(connectedDevices.get(0)), true);
                    mDevice = mThingySdkManager.getSelectedDevice();
                    updateActionbarTitle(mDevice);
                    updateHeaderView(View.VISIBLE);
                    updateConnectedDevicesList(device);
                } else {
                    if (savedDevices.size() > 0) {
                        final Thingy thingy = savedDevices.get(0);//
                        mThingySdkManager.setSelectedDevice(getBluetoothDevice(this, thingy.getDeviceAddress()));
                        updateSelectionInDb(thingy, true);
                        mDevice = mThingySdkManager.getSelectedDevice();
                        updateActionbarTitle(mDevice);
                        updateHeaderView(View.VISIBLE);
                    }
                }
                invalidateOptionsMenu();
                createDrawerMenu();
                selectDevice();
            }
        }
    }

    private void updateUiOnDeviceConnected(final BluetoothDevice device) {
        final List<BluetoothDevice> connectedDevices = new ArrayList<>();
        connectedDevices.addAll(mThingySdkManager.getConnectedDevices());
        final ArrayList<Thingy> savedDevices = mDatabaseHelper.getSavedDevices();
        if (mDevice != null && mDevice.equals(device)) {
            if (connectedDevices.size() > 0) {
                mThingySdkManager.setSelectedDevice(device);
                updateSelectionInDb(new Thingy(device), true);
                mDevice = mThingySdkManager.getSelectedDevice();
            } else {
                if (savedDevices.size() > 0) {
                    final Thingy thingy = savedDevices.get(0);//
                    mThingySdkManager.setSelectedDevice(getBluetoothDevice(this, thingy.getDeviceAddress()));
                    updateSelectionInDb(thingy, true);
                    mDevice = mThingySdkManager.getSelectedDevice();
                }
            }
            invalidateOptionsMenu();
            updateActionbarTitle(mDevice);
            updateHeaderView(View.VISIBLE);
            createDrawerMenu();
            mNoThingyConnectedContainer.setVisibility(View.GONE);
            selectDevice();
            updateConnectedDevicesList(device);
        }
    }

    private void updateUiOnDeviceDisconnected(final BluetoothDevice device) {
        invalidateOptionsMenu();
        if (!mDatabaseHelper.isExist(device.getAddress())) {
            updateUiOnDeviceDeletion(device);
        }
    }

    private void updateUiOnBind() {
        final ArrayList<Thingy> savedDevices = mDatabaseHelper.getSavedDevices();
        if (savedDevices.size() == 0) {
            invalidateOptionsMenu();
            createDrawerMenu();
            mNoThingyConnectedContainer.setVisibility(View.VISIBLE);
        } else {
            BluetoothDevice device = mThingySdkManager.getSelectedDevice();
            if (mBinder != null) {
                mFragmentTag = mBinder.getLastVisibleFragment();
            } else {
                mFragmentTag = ENVIRONMENT_FRAGMENT;
            }
            if (device == null) {
                Thingy thingy = mDatabaseHelper.getLastSelected();
                if (thingy != null) {
                    device = getBluetoothDevice(this, thingy.getDeviceAddress());
                } else {
                    if (savedDevices.size() > 0) {
                        thingy = savedDevices.get(0);
                        device = getBluetoothDevice(this, thingy.getDeviceAddress());
                        mDevice = device;
                    }
                }
            }

            if (device != null) {
                final Thingy thingy = mDatabaseHelper.getSavedDevice(device.getAddress());
                if (thingy != null) {
                    mDevice = device;
                    invalidateOptionsMenu();
                    updateActionbarTitle(device);
                    updateHeaderView(View.VISIBLE);
                    createDrawerMenu();
                    mNoThingyConnectedContainer.setVisibility(View.GONE);
                    selectDevice();
                    if (!mBinder.getActivityFinishing()) {
                        if (mThingySdkManager.isConnected(device)) {
                            updateConnectedDevicesList(device);
                        } else {
                            updateConnectionProgressDialog();
                        }
                    }
                } else {
                    invalidateOptionsMenu();
                    updateHeaderView(View.GONE);
                    createDrawerMenu();
                    mNoThingyConnectedContainer.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(getString(R.string.app_name));
                }
            }
        }
    }

    private void updateConnectedDevicesList(final BluetoothDevice device) {
        if (mConnectedBleDeviceList != null) {
            if (!mConnectedBleDeviceList.contains(device)) {
                mConnectedBleDeviceList.add(device);
            } else {
                mConnectedBleDeviceList.remove(device);
            }
        }
    }

    private void updateConnectionProgressDialog() {
        if (mBinder != null && mBinder.isScanningState()) {
            showConnectionProgressDialog("");
        }
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
            enableRawDataNotifications(true);
        } else {
            enableRawDataNotifications(false);
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

    public void enableRawDataNotifications(final boolean flag) {
        mThingySdkManager.enableRawDataNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA);
    }

    public void enableEulerNotifications(final boolean flag) {
        mThingySdkManager.enableEulerNotifications(mDevice, flag);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), flag, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_EULER);
    }

    public void enableSoundNotifications(final BluetoothDevice device, final boolean flag) {
        if (mThingySdkManager != null) {
            mThingySdkManager.requestMtu(device);
            mThingySdkManager.enableSpeakerStatusNotifications(device, flag);
        }
    }

    public void enableNotificationsForCloudUpload() {
        mThingySdkManager.enableTemperatureNotifications(mDevice, mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress()));
        mThingySdkManager.enablePressureNotifications(mDevice, mDatabaseHelper.getPressureUploadState(mDevice.getAddress()));
        mThingySdkManager.enableButtonStateNotification(mDevice, mDatabaseHelper.getButtonUploadState(mDevice.getAddress()));
    }

    @Override
    public void onServiceConnected() {
        //Use this binder to access you own methods declared in the ThingyService
        mBinder = (ThingyService.ThingyBinder) mThingySdkManager.getThingyBinder();
        cancelNotifications();
        updateUiOnBind();
        if (mBinder.isScanningState()) {
            prepareForScanning(false);
        } else {
            if (mThingySdkManager.hasInitialServiceDiscoverCompleted(mDevice)) {
                onServiceDiscoveryCompletion(mDevice);
            }
        }
    }

    @Override
    public LinkedHashMap<String, String> getSavedTemperatureData(BluetoothDevice device) {
        if (mThingySdkManager != null) {
            return mThingySdkManager.getSavedTemperatureData(device);
        }
        return null;
    }

    @Override
    public LinkedHashMap<String, String> getSavedPressureData(BluetoothDevice device) {
        if (mThingySdkManager != null) {
            return mThingySdkManager.getSavedPressureData(device);
        }
        return null;
    }

    @Override
    public LinkedHashMap<String, Integer> getSavedHumidityData(BluetoothDevice device) {
        if (mThingySdkManager != null) {
            return mThingySdkManager.getSavedHumidityData(device);
        }
        return null;
    }

    @Override
    public Toolbar getToolbar() {
        return mActivityToolbar;
    }

    /**
     * Cancels the existing mNotification. If there is no active mNotification this method does nothing
     */
    private void cancelNotifications() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);


            final List<BluetoothDevice> devices = new ArrayList<>(mThingySdkManager.getConnectedDevices());
            for (int i = 0; i < devices.size(); i++) {
                nm.cancel(devices.get(i).getAddress(), NOTIFICATION_ID);
            }
        }
    }

    @Override
    public void deleteThingy(BluetoothDevice device) {
        clearFragments();
        if (mThingySdkManager.isConnected(device)) {
            mDatabaseHelper.removeDevice(device.getAddress());
            mThingySdkManager.disconnectFromThingy(device);
        } else {
            mDatabaseHelper.removeDevice(device.getAddress());
            updateUiOnDeviceDeletion(device);
        }

        showToast(this, getString(R.string.device_deleted));
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

    private void prepareForScanning(final boolean nfcInitiated) {
        if (checkIfRequiredPermissionsGranted()) {
            if (isLocationEnabled()) {
                if (mBinder != null) {
                    mBinder.setScanningState(true);
                    if (nfcInitiated) {
                        showConnectionProgressDialog(getString(R.string.nfc_tag_connecting));
                    } else {
                        showConnectionProgressDialog(getString(R.string.state_connecting));
                    }
                    startScan();
                }
            } else {
                final MessageDialogFragment messageDialogFragment = MessageDialogFragment.newInstance(getString(R.string.location_services_title), getString(R.string.rationale_message_location));
                messageDialogFragment.show(getSupportFragmentManager(), null);
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
        if (mBinder != null) {
            mBinder.setScanningState(false);
        }

        if (mIsScanning) {
            Log.v(TAG, "Stopping scan");
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mScanCallback);
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable);
            mIsScanning = false;
        }
    }

    private void stopScanOnRotation() {
        if (!isFinishing() && mIsScanning) {
            if (mBinder != null) {
                mBinder.setScanningState(true);
            }
            Log.v(TAG, "Stopping scan on rotation");
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable);
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mScanCallback);
            mIsScanning = false;
        }
    }

    private String mAddress;
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // do nothing
            final BluetoothDevice device = result.getDevice();
            if (mAddress != null && mAddress.equals(device.getAddress())) {
                mProgressHandler.removeCallbacks(mProgressDialogRunnable);
                stopScan();
                connect(device);
                mAddress = null;
                return;
            }

            if (device.equals(mDevice)) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressHandler.removeCallbacks(mProgressDialogRunnable);
                        stopScan();
                        connect();
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

    final BroadcastReceiver mBleStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    showToast(MainActivity.this, getString(R.string.ble_turned_off));
                    enableBle();
                }
            }
        }
    };

    private void showConnectionProgressDialog(final String message) {
        if (mProgressDialog != null) {
            if (!mProgressDialog.isAdded()) {
                mProgressDialog = ProgressDialogFragment.newInstance(message);
                mProgressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
            } else {
                return;
            }
        } else {
            mProgressDialog = ProgressDialogFragment.newInstance(message);
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

    final Runnable mBleScannerTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
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

    @Override
    public void onRequestPermission(final String permission, final int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onCancellingPermissionRationale() {
        showToast(this, getString(R.string.requested_permission_not_granted_rationale));
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
        Intent intent = new Intent(this, SecureDfuActivity.class);
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

    private void loadNfcAdapter() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        final TextView addThingySummary = findViewById(R.id.add_thingy_summary);
        if (mNfcAdapter != null) {
            addThingySummary.setText(R.string.add_thingy_nfc_summary);
            mNfcPendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            ndef.addDataScheme("vnd.android.nfc");
            ndef.addDataAuthority("ext", null);
            mIntentFiltersArray = new IntentFilter[]{ndef};
        } else {
            addThingySummary.setText(R.string.add_thingy_summary);
        }
    }

    private boolean isNfcEnabled() {
        return !(mNfcAdapter != null && !mNfcAdapter.isEnabled());
    }

    private void updateNfcUi(final boolean isNfcEnabled) {
        if (isNfcEnabled) {
            mNfcContainer.setVisibility(View.GONE);
        } else {
            if (showNfcDisabledWarning(this)) {
                mNfcContainer.setVisibility(View.VISIBLE);
            }
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

                                if (mDatabaseHelper.isExist(address)) {
                                    if (isBleEnabled()) {
                                        final BluetoothDevice device = getBluetoothDevice(this, address);
                                        if (device != null) {
                                            if (!isConnected(device, mThingySdkManager.getConnectedDevices())) {
                                                mAddress = address;
                                                prepareForScanning(true);
                                            } else {
                                                showToast(this, getString(R.string.thingy_already_connected, mDatabaseHelper.getDeviceName(address)));
                                            }
                                        } else {
                                            showToast(this, getString(R.string.error_nfc_tag));
                                        }
                                    }
                                } else {
                                    showNfcMessage(address);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void configureThingy(final String address) {
        final Intent intent = new Intent(this, InitialConfigurationActivity.class);
        intent.putExtra(INITIAL_CONFIG_FROM_ACTIVITY, true);
        intent.putExtra(EXTRA_ADDRESS_DATA, address);
        startActivity(intent);
    }

    private void showNfcMessage(final String address) {
        if (mNfcTagFoundDialogFragment != null) {
            if (!mNfcTagFoundDialogFragment.isAdded()) {
                mNfcTagFoundDialogFragment = NFCTagFoundDialogFragment.newInstance(address);
            } else {
                return;
            }
        } else {
            mNfcTagFoundDialogFragment = NFCTagFoundDialogFragment.newInstance(address);
        }
        mNfcTagFoundDialogFragment.show(getSupportFragmentManager(), NFC_DIALOG_TAG);
    }

    private void onServiceDiscoveryCompletion(final BluetoothDevice device) {
        hideProgressDialog();
        mThingySdkManager.enableBatteryLevelNotifications(device, true);
        switch (mFragmentTag) {
            case MOTION_FRAGMENT:
                enableMotionNotifications();
                break;
            case UI_FRAGMENT:
                enableUiNotifications();
                break;
            case SOUND_FRAGMENT:
                enableSoundNotifications(device, true);
                break;
            case CLOUD_FRAGMENT:
                enableNotificationsForCloudUpload();
                break;
            default:
                enableEnvironmentNotifications();
                break;
        }
    }

    @Override
    public void requestNfcFeature() {
        startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
    }

    private void updateBatteryLevel(final int batteryLevel) {
        if (batteryLevel > -1) {
            updateBatteryLevelVisibility(View.VISIBLE);
            mBatteryLevel.setText(getString(R.string.battery_level_percent, batteryLevel));
            mBatteryLevelImg.setImageLevel(batteryLevel);
        }
    }
}
