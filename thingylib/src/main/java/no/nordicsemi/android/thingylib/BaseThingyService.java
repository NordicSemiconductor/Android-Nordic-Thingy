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

package no.nordicsemi.android.thingylib;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.thingylib.utils.ThingyUtils;


public abstract class BaseThingyService extends Service implements ThingyConnection.ThingyConnectionGattCallbacks {

    private static final String TAG = "BaseThingyService";
    private BluetoothDevice mDevice;

    protected Map<BluetoothDevice, ThingyConnection> mThingyConnections;
    protected ArrayList<BluetoothDevice> mDevices;

    protected boolean mBound = false;

    public BaseThingyService() {
        super();
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device, int connectionState) {

    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
        mThingyConnections.remove(device);
        mDevices.remove(device);
    }

    public abstract class BaseThingyBinder extends Binder {

        /**
         * Disconnects from all connected devices.
         */
        /*package access*/ final void disconnectFromAllDevices(){
            if (mDevices.size() > 0){
                for(int i = 0; i < mDevices.size(); i++){
                    final ThingyConnection thingyConnection = mThingyConnections.get(mDevices.get(i));
                    if (thingyConnection != null){
                        thingyConnection.disconnect();
                    }
                }
                mDevices.clear();
                mThingyConnections.clear();
            }
        }

        /**
         * Returns the list of connected devices.
         */
        /*package access*/ final List<BluetoothDevice> getConnectedDevices() {
            return Collections.unmodifiableList(mDevices);
        }

        /**
         * Returns the remote connection for the particular bluetooth device.
         *
         * @param device bluetooth device
         */
        /*package access*/ public abstract ThingyConnection getThingyConnection(BluetoothDevice device); /*{
            return mThingyConnections.get(device);
        }*/

        /**
         * Selects the current bluetooth device.
         *
         * @param device bluetooth device
         */
        /*package access*/ final void setSelectedDevice(final BluetoothDevice device) {
            mDevice = device;
        }

        /**
         * Returns the current bluetooth device which was selected from
         * {@link #setSelectedDevice(BluetoothDevice)}.
         */
        /*package access*/ final BluetoothDevice getSelectedDevice() {
            return mDevice;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mThingyConnections = new HashMap<>();
        mDevices = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(ThingyUtils.EXTRA_DEVICE);
            if (bluetoothDevice != null) {
                mThingyConnections.put(bluetoothDevice, new ThingyConnection(this, bluetoothDevice));
                if (!mDevices.contains(bluetoothDevice)) {
                    mDevices.add(bluetoothDevice);
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy called on Base service");
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        disconnectFromAllDevices();
    }

    @Nullable
    @Override
    public abstract BaseThingyBinder onBind(Intent intent);

    @Override
    public final void onRebind(Intent intent) {
        mBound = true;
        onRebind();
    }

    @Override
    public final boolean onUnbind(Intent intent) {
        mBound = false;
        onUnbind();
        return true;
    }

    /**
     * Called when the activity has rebinded to the service after being recreated.
     * This method is not called when the activity was killed and recreated just to change the
     * phone orientation.
     */
    protected void onRebind(){
    }

    /**
     * Called when the activity has unbound from the service after being bound.
     */
    protected void onUnbind(){
    }

    /**
     * Disconnects from all connected devices
     *
     */
     /*package access*/ final void disconnectFromAllDevices(){
        if (mDevices.size() > 0){
            for(int i = 0; i < mDevices.size(); i++){
                final ThingyConnection thingyConnection = mThingyConnections.get(mDevices.get(i));
                if (thingyConnection != null){
                    thingyConnection.disconnect();
                }
            }
            mDevices.clear();
            mThingyConnections.clear();
        }
    }

    /* *
     * Create your own notification target class to display notifications
     */
    /*protected abstract Class<? extends Activity> getNotificationTarget();*/
}
