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
import android.app.NotificationManager;
import android.app.ProgressDialog;
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
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import no.nordicsemi.android.nrfthingy.common.AboutActivity;
import no.nordicsemi.android.nrfthingy.common.PermissionRationaleDialogFragment;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.configuration.ConfigurationActivity;
import no.nordicsemi.android.nrfthingy.configuration.ConfirmThingyDeletionDialogFragment;
import no.nordicsemi.android.nrfthingy.configuration.InitialConfigurationActivity;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.dfu.DfuUpdateAvailableDialogFragment;
import no.nordicsemi.android.nrfthingy.dfu.SecureDfuActivity;
import no.nordicsemi.android.nrfthingy.thingy.Thingy;
import no.nordicsemi.android.nrfthingy.thingy.ThingyAdapter;
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

import static no.nordicsemi.android.nrfthingy.common.Utils.NOTIFICATION_ID;
import static no.nordicsemi.android.nrfthingy.common.Utils.TAG;
import static no.nordicsemi.android.nrfthingy.common.Utils.showToast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        EnvironmentServiceFragment.EnvironmentServiceListener,
        UiFragment.UIFragmetnListener,
        MotionServiceFragment.MotionFragmentListener,
        CloudFragment.CloudFragmentListener,
        EnvironmentServiceSettingsFragment.EnvironmentServiceSettingsFragmentListener,
        ConfirmThingyDeletionDialogFragment.ConfirmThingeeDeletionListener,
        ThingyAdapter.ActionListener,
        ThingySdkManager.ServiceConnectionListener,
        PermissionRationaleDialogFragment.PermissionDialogListener, DfuUpdateAvailableDialogFragment.DfuUpdateAvailableListener {

    private static final int SCAN_DURATION = 15000;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar mActivityToolbar;

    private LinearLayout mHeaderTitleContainer;
    private TextView mHeaderTitle;
    private ImageView mHeaderToggle;
    private LinearLayout mNoThingyConnectedContainer;

    private ArrayList<BluetoothDevice> mConnectedBleDeviceList;

    private BluetoothDevice mDevice;
    private BluetoothDevice mOldDevice;

    private String mFragmentTag = Utils.ENVIRONMENT_FRAGMENT;
    private boolean isHeaderExpanded = true;

    private DatabaseHelper mDatabaseHelper;
    private ThingySdkManager mThingySdkManager;
    private ColorStateList mColorStateList;
    private Drawable mNavigationViewBackground;
    private ProgressDialog mProgressDialog;

    private Handler mProgressHandler = new Handler();
    private boolean mIsScanning;
    private String mFirmwareFileVersion;

    private ThingyService.ThingyBinder mBinder;

    private Ringtone mRingtone;
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
            hideProgressDialog();
            if (mConnectedBleDeviceList.contains(device)) {
                mConnectedBleDeviceList.remove(device);
            }
            updateUiOnDeviceDisconnected(device);
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            hideProgressDialog();
            switch (mFragmentTag) {
                case Utils.MOTION_FRAGMENT:
                    enableMotionNotifications();
                    break;
                case Utils.UI_FRAGMENT:
                    enableUiNotifications();
                    break;
                case Utils.SOUND_FRAGMENT:
                    enableSoundNotifications(device, true);
                    break;
                case Utils.CLOUD_FRAGMENT:
                    enableNotificationsForCloudUpload();
                    break;
                default:
                    enableEnvironmentNotifications();
                    break;
            }
            checkForFwUpdates();
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
                        if(mRingtone != null) {
                            if(mRingtone.isPlaying()) {
                                mRingtone.stop();
                            }
                            mRingtone = null;
                        }

                        break;
                    case 1:
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if(notification != null) {
                            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            if(mRingtone != null) {
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
        public void onRotationMatixValueChangedEvent(final BluetoothDevice bluetoothDevice, final byte[] matrix) {

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

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivityToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActivityToolbar);

        mThingySdkManager = ThingySdkManager.getInstance();

        mDatabaseHelper = new DatabaseHelper(this);

        mNoThingyConnectedContainer = (LinearLayout) findViewById(R.id.no_thingee_connected);
        final Button mConnectThingy = (Button) findViewById(R.id.connect_thingy);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        final View headerView = mNavigationView.getHeaderView(0);
        mHeaderTitle = (TextView) headerView.findViewById(R.id.header_title);
        mHeaderToggle = (ImageView) headerView.findViewById(R.id.header_toggle);
        mHeaderTitleContainer = (LinearLayout) headerView.findViewById(R.id.header_title_container);

        // Ensure that Bluetooth exists
        if (!ensureBleExists())
            finish();
        mColorStateList = ContextCompat.getColorStateList(this, R.color.menu_item_text);
        mNavigationViewBackground = ContextCompat.getDrawable(this, R.drawable.menu_item_background);

        mNavigationView.setNavigationItemSelectedListener(this);

        mConnectedBleDeviceList = new ArrayList<>();

        mConnectThingy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent initialConfiguration = new Intent(MainActivity.this, InitialConfigurationActivity.class);
                initialConfiguration.putExtra(Utils.INITIAL_CONFIG_FROM_ACTIVITY, true);
                startActivity(initialConfiguration);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mActivityToolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Disable the Hamburger icon animation
                super.onDrawerSlide(drawerView, 0);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isBleEnabled()) {
            enableBle();
        }
        mThingySdkManager.bindService(this, ThingyService.class);
        ThingyListenerHelper.registerThingyListener(this, mThingyListener);
        registerReceiver(mBleStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isFinishing()) {
            if (mBinder != null) {
                mBinder.setActivityFinishing(false);
                mBinder.setLastVisibleFragment(mFragmentTag);
            }
        } else {
            if (mBinder != null) {
                mBinder.setActivityFinishing(true);
                mBinder.setLastVisibleFragment(Utils.ENVIRONMENT_FRAGMENT);
            }
        }
        mThingySdkManager.unbindService(this);
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
        hideProgressDialog();
        if (isFinishing()) {
            stopScan();
            ThingySdkManager.clearInstance();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                ConfirmThingyDeletionDialogFragment confirmThingyDeletionDialogFragment = new ConfirmThingyDeletionDialogFragment().newInstance(mDevice);
                confirmThingyDeletionDialogFragment.show(getSupportFragmentManager(), null);
                break;
            case R.id.action_connect:
                prepareForScanning();
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
                final Thingy thingy = mDatabaseHelper.getSavedDevice(mDevice.getAddress());
                if (mThingySdkManager.isConnected(mDevice)) {
                    Intent configurationIntent = new Intent(this, ConfigurationActivity.class);
                    configurationIntent.putExtra(Utils.CURRENT_DEVICE, mDevice);
                    startActivity(configurationIntent);
                } else {
                    showToast(this, "Please connect to " + thingy.getDeviceName() + " before you proceed!");
                }
                break;
            case Utils.GROUP_ID_SAVED_THINGIES:
                performDeviceSelection(item);
                break;
            case Utils.GROUP_ID_ADD_THINGY:
                Intent initialConfiguration = new Intent(MainActivity.this, InitialConfigurationActivity.class);
                initialConfiguration.putExtra(Utils.INITIAL_CONFIG_FROM_ACTIVITY, true);
                startActivity(initialConfiguration);
                break;
            case Utils.GROUP_ID_DFU:
                Intent intent = new Intent(this, SecureDfuActivity.class);
                intent.putExtra(Utils.EXTRA_DEVICE, mDevice);
                startActivity(intent);
                break;
            case Utils.GROUP_ID_ABOUT:
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
                BluetoothDevice device = getBluetoothDevice(thingy.getDeviceAddress());
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
                if (fragmentManager.findFragmentByTag(Utils.ENVIRONMENT_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableEnvironmentNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.ENVIRONMENT_FRAGMENT;
                    EnvironmentServiceFragment environmentServiceFragment = EnvironmentServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, environmentServiceFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_motion:
                if (fragmentManager.findFragmentByTag(Utils.MOTION_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableMotionNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.MOTION_FRAGMENT;
                    MotionServiceFragment motionServiceFragment = MotionServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, motionServiceFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_ui:
                if (fragmentManager.findFragmentByTag(Utils.UI_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableUiNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.UI_FRAGMENT;
                    UiFragment uiFragment = UiFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, uiFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_sound:
                if (fragmentManager.findFragmentByTag(Utils.SOUND_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        enableSoundNotifications(mDevice, true);
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.SOUND_FRAGMENT;
                    SoundFragment soundFragment = SoundFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, soundFragment, mFragmentTag).commit();
                }
                break;
            case R.id.navigation_cloud:
                if (fragmentManager.findFragmentByTag(Utils.CLOUD_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableTemperatureNotifications(mDevice, mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress()));
                        mThingySdkManager.enablePressureNotifications(mDevice,  mDatabaseHelper.getPressureUploadState(mDevice.getAddress()));
                        mThingySdkManager.enableHumidityNotifications(mDevice, false);
                        mThingySdkManager.enableAirQualityNotifications(mDevice, false);
                        mThingySdkManager.enableColorNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, mDatabaseHelper.getButtonUploadState(mDevice.getAddress()));
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.CLOUD_FRAGMENT;
                    CloudFragment mapFragment = CloudFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, mapFragment, mFragmentTag).commit();
                }
                break;
        }
        checkSelection(item);
    }

    private void performFragmentNavigation() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (mFragmentTag) {
            case Utils.ENVIRONMENT_FRAGMENT:
                if (fragmentManager.findFragmentByTag(Utils.ENVIRONMENT_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableEnvironmentNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.ENVIRONMENT_FRAGMENT;
                    EnvironmentServiceFragment environmentServiceFragment = EnvironmentServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, environmentServiceFragment, mFragmentTag).commit();
                }
                break;
            case Utils.MOTION_FRAGMENT:
                if (fragmentManager.findFragmentByTag(Utils.MOTION_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableUiNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableMotionNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.MOTION_FRAGMENT;
                    MotionServiceFragment motionServiceFragment = MotionServiceFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, motionServiceFragment, mFragmentTag).commit();
                }
                break;
            case Utils.UI_FRAGMENT:
                if (fragmentManager.findFragmentByTag(Utils.UI_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        mThingySdkManager.enableSoundNotifications(mDevice, false);
                        enableUiNotifications();
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.UI_FRAGMENT;
                    UiFragment uiFragment = UiFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, uiFragment, mFragmentTag).commit();
                }
                break;
            case Utils.SOUND_FRAGMENT:
                if (fragmentManager.findFragmentByTag(Utils.SOUND_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        mThingySdkManager.enableEnvironmentNotifications(mDevice, false);
                        mThingySdkManager.enableMotionNotifications(mDevice, false);
                        enableSoundNotifications(mDevice, true);
                    }

                    final String fragmentTag = mFragmentTag;
                    clearFragments(fragmentTag);
                    mFragmentTag = Utils.SOUND_FRAGMENT;
                    SoundFragment soundFragment = SoundFragment.newInstance(mDevice);
                    getSupportFragmentManager().beginTransaction().add(R.id.container, soundFragment, mFragmentTag).commit();
                }
                break;
            case Utils.CLOUD_FRAGMENT:
                if (fragmentManager.findFragmentByTag(Utils.CLOUD_FRAGMENT) == null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        if (mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress())) {
                            mThingySdkManager.enableTemperatureNotifications(mDevice, mDatabaseHelper.getTemperatureUploadState(mDevice.getAddress()));
                            mThingySdkManager.enablePressureNotifications(mDevice,  mDatabaseHelper.getPressureUploadState(mDevice.getAddress()));
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
                    mFragmentTag = Utils.CLOUD_FRAGMENT;
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
        startActivityForResult(enableIntent, Utils.REQUEST_ENABLE_BT);
    }

    private void connect() {
        mThingySdkManager.connectToThingy(this, mDevice, ThingyService.class);
        final Thingy thingy = new Thingy(mDevice);
        mThingySdkManager.setSelectedDevice(mDevice);
        updateSelectionInDb(thingy, true);
    }

    @Override
    public void onAddNewThingee() {
        Intent intent = new Intent(this, InitialConfigurationActivity.class);
        intent.putExtra(Utils.INITIAL_CONFIG_FROM_ACTIVITY, true);
        startActivity(intent);
    }

    @Override
    public void configureEnvironmentServiceSettings(final byte[] data) {

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
            final List<BluetoothDevice> devices = new ArrayList<>();
            devices.addAll(mThingySdkManager.getConnectedDevices());
            ArrayList<Thingy> thigyList = mDatabaseHelper.getSavedDevices();
            if ((devices.size() > 0) || (thigyList != null && thigyList.size() > 0)) {
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
            if (mThingySdkManager.isConnected(getBluetoothDevice(thingy.getDeviceAddress()))) {
                if (!mDatabaseHelper.getLastSelected(thingy.getDeviceAddress())) {
                    mNavigationView.getMenu().add(Utils.GROUP_ID_SAVED_THINGIES, i, i, thingyList.get(i).getDeviceName()).setIcon(R.drawable.ic_thingy_blue);
                }
            } else {
                if (!mDatabaseHelper.getLastSelected(thingy.getDeviceAddress())) {
                    mNavigationView.getMenu().add(Utils.GROUP_ID_SAVED_THINGIES, i, i, thingyList.get(i).getDeviceName()).setIcon(R.drawable.ic_thingy_gray);
                }
            }
            total = i;
        }

        if (thingyList.size() == 0) {
            mHeaderTitleContainer.setVisibility(View.GONE);
        }

        total = total + 1;
        mNavigationView.getMenu().add(Utils.GROUP_ID_ADD_THINGY, Utils.ITEM_ID_ADD_THINGY, total, getString(R.string.action_add)).setIcon(R.drawable.ic_add);
        total += total;
        mNavigationView.getMenu().add(Utils.GROUP_ID_DFU, Utils.ITEM_ID_DFU, total, getString(R.string.settings_dfu)).setIcon(R.drawable.ic_dfu_gray);
        total += total;
        mNavigationView.getMenu().add(Utils.GROUP_ID_ABOUT, Utils.ITEM_ID_SETTINGS, total, getString(R.string.action_about)).setIcon(R.drawable.ic_info_grey);

    }

    private void checkFragmentDrawerItem() {
        final String fragmentTag = mFragmentTag;
        switch (fragmentTag) {
            case Utils.ENVIRONMENT_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_environment));
                break;
            case Utils.UI_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_ui));
                break;
            case Utils.MOTION_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_motion));
                break;
            case Utils.SOUND_FRAGMENT:
                checkSelection(mNavigationView.getMenu().findItem(R.id.navigation_sound));
                break;
            case Utils.CLOUD_FRAGMENT:
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

    private BluetoothDevice getBluetoothDevice(final String thingeeAddress) {
        final BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter ba = bm.getAdapter();
        if (ba != null /*&& ba.isEnabled()*/) {
            return ba.getRemoteDevice(thingeeAddress);
        }
        return null; //ideally shouldn't go here
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

    private void updateActionbarTitle(final BluetoothDevice device) {
        if (device != null) {
            if (mDatabaseHelper.isExist(device.getAddress())) {
                String deviceName = mDatabaseHelper.getDeviceName(device.getAddress());
                if (deviceName.isEmpty()) {
                    deviceName = mDevice.getName();
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
                        mThingySdkManager.setSelectedDevice(getBluetoothDevice(thingy.getDeviceAddress()));
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
                    mThingySdkManager.setSelectedDevice(getBluetoothDevice(thingy.getDeviceAddress()));
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
                mFragmentTag = Utils.ENVIRONMENT_FRAGMENT;
            }
            if (device == null) {
                Thingy thingy = mDatabaseHelper.getLastSelected();
                if (thingy != null) {
                    device = getBluetoothDevice(thingy.getDeviceAddress());
                } else {
                    if (savedDevices.size() > 0) {
                        thingy = savedDevices.get(0);
                        device = getBluetoothDevice(thingy.getDeviceAddress());
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
        if (mBinder != null && mBinder.getScanningState()) {
            showConnectionProgressDialog();
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
    }

    @Override
    public void OnUiFragmetnListener(BluetoothDevice device) {

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

    private String getDeviceName(final BluetoothDevice device) {
        if (device != null) {
            final String deviceName = device.getName();
            if (!TextUtils.isEmpty(deviceName)) {
                return deviceName;
            }
        }
        return getString(R.string.default_thingy_name);
    }

    /**
     * Cancels the existing mNotification. If there is no active mNotification this method does nothing
     */
    private void cancelNotifications() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);

        final List<BluetoothDevice> devices = new ArrayList<>();
        devices.addAll(mThingySdkManager.getConnectedDevices());
        for (int i = 0; i < devices.size(); i++) {
            nm.cancel(devices.get(i).getAddress(), NOTIFICATION_ID);
        }
    }

    @Override
    public void deleteThingee(BluetoothDevice device) {
        clearFragments();
        if (mThingySdkManager.isConnected(device)) {
            mDatabaseHelper.removeDevice(device.getAddress());
            mThingySdkManager.disconnectFromThingy(device);
        } else {
            mDatabaseHelper.removeDevice(device.getAddress());
            updateUiOnDeviceDeletion(device);
        }

        Utils.showToast(this, getString(R.string.device_deleted));
    }

    private boolean checkIfRequiredPermissionsGranted() {
        if (Utils.checkIfVersionIsMarshmallowOrAbove()) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    private void prepareForScanning() {
        if (checkIfRequiredPermissionsGranted()) {
            if (mBinder != null) {
                mBinder.setScanningState(true);
                showConnectionProgressDialog();
                startScan();
            }
        }
    }

    private void startScan() {
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
     * Stop scan if user tap Cancel button
     */
    private void stopScan() {
        if (mIsScanning) {
            if (mBinder != null) {
                mBinder.setScanningState(false);
            }
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mScanCallback);
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable);
            mIsScanning = false;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            // do nothing
            final BluetoothDevice device = result.getDevice();
            if (mDevice != null && device.equals(mDevice)) {
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
        public void onBatchScanResults(final List<ScanResult> results) {
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
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Utils.showToast(MainActivity.this, getString(R.string.ble_turned_off));
                        enableBle();
                        break;
                }
            }
        }
    };

    private void showConnectionProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(/*no.nordicsemi.android.thingylib.*/R.string.please_wait));
        mProgressDialog.setMessage(getString(R.string.state_connecting));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mProgressHandler.postDelayed(mProgressDialogRunnable, SCAN_DURATION);
        mProgressDialog.show();
    }

    final Runnable mProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    };

    final Runnable mBleScannerTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };


    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void updateProgressDialogState(String message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
        }
    }

    @Override
    public void onRequestPermission(final String permission, final int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onCancellingPermissionRationale() {
        Utils.showToast(this, getString(R.string.requested_permission_not_granted_rationale));
    }

    @Override
    public void OnCloudFragmentListener(BluetoothDevice device) {

    }

    private boolean checkIfFirmwareUpdateAvailable() {
        final String[] fwVersion = mThingySdkManager.getFirmwareVersion(mDevice).split("\\.");

        final int fwVersionMajor = Integer.parseInt(fwVersion[fwVersion.length - 3]);
        final int fwVersionMinor = Integer.parseInt(fwVersion[fwVersion.length - 2]);
        final int fwVersionPatch = Integer.parseInt(fwVersion[fwVersion.length - 1]);

        final String name = getResources().getResourceEntryName(R.raw.thingy_dfu_pkg_app_v1_1_0).replace("v", "");
        final String[] resourceEntryNames = name.split("_");

        final int fwFileVersionMajor = Integer.parseInt(resourceEntryNames[resourceEntryNames.length - 3]);
        final int fwFileVersionMinor = Integer.parseInt(resourceEntryNames[resourceEntryNames.length - 2]);
        final int fwFileVersionPatch = Integer.parseInt(resourceEntryNames[resourceEntryNames.length - 1]);

        mFirmwareFileVersion = resourceEntryNames[resourceEntryNames.length - 3] + "." +
                resourceEntryNames[resourceEntryNames.length - 2] + "." +
                resourceEntryNames[resourceEntryNames.length - 1];

        if (fwFileVersionMajor > fwVersionMajor || fwFileVersionMinor > fwVersionMinor || fwFileVersionPatch > fwVersionPatch) {
            return true;
        }

        return false;
    }

    private void checkForFwUpdates() {
        if (checkIfFirmwareUpdateAvailable()) {
            DfuUpdateAvailableDialogFragment fragment = DfuUpdateAvailableDialogFragment.newInstance(mDevice, mFirmwareFileVersion);
            fragment.show(getSupportFragmentManager(), null);
            mFirmwareFileVersion = null;
        }
    }

    @Override
    public void onDfuRequested() {
        Intent intent = new Intent(this, SecureDfuActivity.class);
        intent.putExtra(Utils.EXTRA_DEVICE, mDevice);
        startActivity(intent);
    }
}
