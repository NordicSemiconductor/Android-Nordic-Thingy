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

package no.nordicsemi.android.nrfthingy.thingy;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import no.nordicsemi.android.nrfthingy.MainActivity;
import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyConnection;

import static no.nordicsemi.android.nrfthingy.common.Utils.NOTIFICATION_ID;

public class ThingyService extends BaseThingyService {
    private static final String PRIMARY_GROUP = "Thingy:52 Connectivity Summary";
    private static final String PRIMARY_GROUP_ID = "no.nordicsemi.android.nrfthingy";
    private static final String PRIMARY_CHANNEL = "Thingy:52 Connectivity Status";
    private static final String PRIMARY_CHANNEL_ID = "no.nordicsemi.android.nrfthingy";
    private DatabaseHelper mDatabaseHelper;
    private boolean mIsActivityFinishing = false;
    private Map<BluetoothDevice, Integer> mLastSelectedAudioTrack;
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;

    public class ThingyBinder extends BaseThingyBinder {
        private boolean mIsScanning;
        private String mLastVisibleFragment = Utils.ENVIRONMENT_FRAGMENT;

        /**
         * Saves the activity state.
         *
         * @param activityFinishing if the activity is finishing or not
         */
        public final void setActivityFinishing(final boolean activityFinishing) {
            mIsActivityFinishing = activityFinishing;
        }

        /**
         * Returns the activity state.
         */
        public final boolean getActivityFinishing() {
            return mIsActivityFinishing;
        }

        /**
         * Saves the last visible fragment in the service
         */
        public final void setLastSelectedAudioTrack(final BluetoothDevice device, final int index) {
            mLastSelectedAudioTrack.put(device, index);
        }

        public final int getLastSelectedAudioTrack(final BluetoothDevice device) {
            final Integer track = mLastSelectedAudioTrack.get(device);
            if (track != null)
                return track;
            return 0;
        }

        /**
         * Saves the last visible fragment in the service
         */
        public final void setLastVisibleFragment(String lastVisibleFragment) {
            mLastVisibleFragment = lastVisibleFragment;
        }

        /**
         * Returns the last visible fragment in the service
         */
        public final String getLastVisibleFragment() {
            return mLastVisibleFragment;
        }

        public void setScanningState(final boolean isScanning) {
            mIsScanning = isScanning;
        }

        public boolean isScanningState() {
            return mIsScanning;
        }

        @Override
        public ThingyConnection getThingyConnection(BluetoothDevice device) {
            return mThingyConnections.get(device);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //@Override
    private Class<? extends Activity> getNotificationTarget() {
        return MainActivity.class;
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device, final int connectionState) {
        createBackgroundNotification();
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device, final int connectionState) {
        super.onDeviceDisconnected(device, connectionState);
        removeLastSelectedAudioTracks(device);
        cancelNotification(device);
        createBackgroundNotification();
    }

    @Nullable
    @Override
    public ThingyBinder onBind(final Intent intent) {
        return new ThingyBinder();
    }

    @Override
    protected void onRebind() {
        cancelNotifications();
        createBackgroundNotification();
    }

    @Override
    protected void onUnbind() {
        if (mIsActivityFinishing) {
            final ArrayList<BluetoothDevice> devices = mDevices;
            if (devices != null && devices.size() == 0) {
                stopForegroundThingyService();
                return;
            }
        }
        createBackgroundNotification();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationPrerequisites();
        startForeground(NOTIFICATION_ID, createForegroundNotification());
        mLastSelectedAudioTrack = new HashMap<>();
        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        registerReceiver(mNotificationDisconnectReceiver, new IntentFilter(Utils.ACTION_DISCONNECT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNotificationDisconnectReceiver);
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForegroundThingyService();
    }

    private void stopForegroundThingyService() {
        stopForeground(true);
        stopSelf();
    }

    private BroadcastReceiver mNotificationDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Utils.ACTION_DISCONNECT:
                    final BluetoothDevice device = intent.getExtras().getParcelable(Utils.EXTRA_DEVICE);
                    if (device != null) {
                        final ThingyConnection thingyConnection = mThingyConnections.get(device);
                        if (thingyConnection != null) {
                            thingyConnection.disconnect();
                            if (mDevices.contains(device)) {
                                mDevices.remove(device);
                            }
                        }
                    }
                    break;
            }
        }
    };

    private void createNotificationPrerequisites() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.checkIfVersionIsOreoOrAbove()) {
            if (mNotificationChannel == null) {
                mNotificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, PRIMARY_CHANNEL, NotificationManager.IMPORTANCE_LOW);
            }
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
    }

    /**
     * Creates a Notifications for the devices that are currently connected.
     */
    private Notification createForegroundNotification() {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.nordicBlue));
        builder.setContentTitle(("Tap to launch Nordic Thingy."));

        return builder.build();
    }

    /**
     * Creates a Notifications for the devices that are currently connected.
     */
    private void createNotificationForConnectedDevice(final BluetoothDevice device, final String deviceName) {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.nordicBlue));
        builder.setDefaults(0).setOngoing(false); // an ongoing notification will not be shown on Android Wear
        builder.setGroup(PRIMARY_GROUP_ID).setGroupSummary(true);
        builder.setContentTitle(getString(R.string.thingy_notification_text, deviceName));

        final Intent disconnect = new Intent(Utils.ACTION_DISCONNECT);
        disconnect.putExtra(Utils.EXTRA_DEVICE, device);
        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, Utils.DISCONNECT_REQ + device.hashCode(), disconnect, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_thingy_white, getString(R.string.thingy_action_disconnect), disconnectAction));
        builder.setSortKey(deviceName + device.getAddress()); // This will keep the same order of notification even after an action was clicked on one of them

        final Notification notification = builder.build();
        mNotificationManager.notify(device.getAddress(), Utils.NOTIFICATION_ID, notification);
    }

    /**
     * Returns a notification builder
     */
    private NotificationCompat.Builder getBackgroundNotificationBuilder() {
        final Intent parentIntent = new Intent(this, getNotificationTarget());
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, Utils.OPEN_ACTIVITY_REQ, new Intent[]{parentIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL);
        builder.setContentIntent(pendingIntent).setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_thingy_white);
        builder.setChannelId(PRIMARY_CHANNEL_ID);
        return builder;
    }

    /**
     * Returns a notification builder
     */
    private NotificationCompat.Builder getSummaryNotifcationBuilder() {
        final Intent parentIntent = new Intent(this, getNotificationTarget()/*MainActivity.class*/);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, Utils.OPEN_ACTIVITY_REQ, new Intent[]{parentIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Utils.checkIfVersionIsOreoOrAbove()) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL);
            builder.setContentIntent(pendingIntent).setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_thingy_white);
            if (mNotificationChannel == null) {
                mNotificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, PRIMARY_CHANNEL, NotificationManager.IMPORTANCE_LOW);
                builder.setChannelId(PRIMARY_CHANNEL_ID);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(mNotificationChannel);
            }
            return builder;
        } else {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(pendingIntent).setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_thingy_white);
            return builder;
        }
    }

    /**
     * Creates a summary notifcation for devices
     */
    private void createSummaryNotification() {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(this, R.color.nordicBlue));
        builder.setShowWhen(false).setDefaults(0).setOngoing(false); // an ongoing notification will not be shown on Android Wear
        builder.setGroup(Utils.THINGY_GROUP_ID).setGroupSummary(true);

        final ArrayList<Thingy> managedDevices = mDatabaseHelper.getSavedDevices();
        final ArrayList<BluetoothDevice> connectedDevices = mDevices;
        if (connectedDevices != null && connectedDevices.isEmpty()) {
            // No connected devices
            final int numberOfManagedDevices = managedDevices.size();
            if (numberOfManagedDevices == 1) {
                final String name = managedDevices.get(0).getDeviceName();
                builder.setContentTitle(getString(R.string.one_disconnected, name));
            } else {
                builder.setContentTitle(getString(R.string.two_disconnected, numberOfManagedDevices));
            }
        } else {
            // There are some proximity tags connected
            final int numberOfConnectedDevices = connectedDevices.size();
            if (numberOfConnectedDevices == 1) {
                final String name = getDeviceName(connectedDevices.get(0));
                builder.setContentTitle(getString(R.string.one_connected, name));
            } else {
                builder.setContentTitle(getString(R.string.two_connected, numberOfConnectedDevices));
            }
            builder.setNumber(numberOfConnectedDevices);

            // If there are some disconnected devices, also print them
            final int numberOfDisconnectedDevices = managedDevices.size() - numberOfConnectedDevices;
            if (numberOfDisconnectedDevices == 1) {
                // Find the single disconnected device to get its name
                for (final Thingy thingy : managedDevices) {
                    if (!isConnected(thingy, connectedDevices)) {
                        final String name = thingy.getDeviceName();
                        builder.setContentText(getResources().getQuantityString(R.plurals.thingy_notification_text_nothing_connected, numberOfDisconnectedDevices, name));
                        break;
                    }
                }
            } else if (numberOfConnectedDevices > 1) {
                // If there are more, just write number of them
                builder.setContentText(getResources().getQuantityString(R.plurals.thingy_notification_text_nothing_connected, numberOfDisconnectedDevices, numberOfDisconnectedDevices));
            }
        }

        final Notification notification = builder.build();
        final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Checks if the device is among the connected devices list.
     *
     * @param thingy           device to be checked
     * @param connectedDevices list of connected devices
     */
    private boolean isConnected(Thingy thingy, ArrayList<BluetoothDevice> connectedDevices) {
        for (BluetoothDevice device : connectedDevices) {
            if (thingy.getDeviceAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the name of a device
     */
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
     * Creates background notifications for devices
     */
    private void createBackgroundNotification() {
        final ArrayList<BluetoothDevice> devices = mDevices;

        for (int i = 0; i < devices.size(); i++) {
            createNotificationForConnectedDevice(devices.get(i), getDeviceName(devices.get(i)));
        }
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    private void cancelNotifications() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);

        final ArrayList<BluetoothDevice> devices = mDevices;
        for (int i = 0; i < devices.size(); i++) {
            nm.cancel(devices.get(i).getAddress(), NOTIFICATION_ID);
        }
    }

    /**
     * Cancels the existing notification for given device. If there is no active notification this method does nothing
     *
     * @param device of whose notification must be removed
     */
    private void cancelNotification(final BluetoothDevice device) {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(device.getAddress(), NOTIFICATION_ID);
    }

    private void removeLastSelectedAudioTracks(final BluetoothDevice device) {
        if (mLastSelectedAudioTrack.containsKey(device)) {
            mLastSelectedAudioTrack.remove(device);
        }
    }
}
