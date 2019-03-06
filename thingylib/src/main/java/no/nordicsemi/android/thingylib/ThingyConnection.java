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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import no.nordicsemi.android.thingylib.decoder.ADPCMDecoder;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ThingyConnection extends BluetoothGattCallback {

    private static final String TAG = "ThingyConnection";
    private BluetoothGattCharacteristic mDeviceNameCharacteristic;
    private BluetoothGattCharacteristic mAdvertisingParamCharacteristic;
    private BluetoothGattCharacteristic mConnectionParamCharacteristic;
    private BluetoothGattCharacteristic mEddystoneUrlCharacteristic;
    private BluetoothGattCharacteristic mCloudTokenCharacteristic;
    private BluetoothGattCharacteristic mFirmwareVersionCharacteristic;
    private BluetoothGattCharacteristic mNfcCharacteristic;

    private BluetoothGattCharacteristic mBatteryLevelCharacteristic;

    private BluetoothGattCharacteristic mTemperatureCharacteristic;
    private BluetoothGattCharacteristic mPressureCharacteristic;
    private BluetoothGattCharacteristic mHumidityCharacteristic;
    private BluetoothGattCharacteristic mAirQualityCharacteristic;
    private BluetoothGattCharacteristic mColorCharacteristic;
    private BluetoothGattCharacteristic mEnvironmentConfigurationCharacteristic;

    private BluetoothGattCharacteristic mLedCharacteristic;
    private BluetoothGattCharacteristic mButtonCharacteristic;

    private BluetoothGattCharacteristic mMotionConfigurationCharacteristic;
    private BluetoothGattCharacteristic mTapCharacteristic;
    private BluetoothGattCharacteristic mOrientationCharacteristic;
    private BluetoothGattCharacteristic mQuaternionCharacteristic;
    private BluetoothGattCharacteristic mPedometerCharacteristic;
    private BluetoothGattCharacteristic mRawDataCharacteristic;
    private BluetoothGattCharacteristic mEulerCharacteristic;
    private BluetoothGattCharacteristic mRotationMatrixCharacteristic;
    private BluetoothGattCharacteristic mHeadingCharacteristic;
    private BluetoothGattCharacteristic mGravityVectorCharacteristic;

    private BluetoothGattCharacteristic mSoundConfigurationCharacteristic;
    private BluetoothGattCharacteristic mSpeakerDataCharacteristic;
    private BluetoothGattCharacteristic mSpeakerStatusCharacteristic;
    private BluetoothGattCharacteristic mMicrophoneCharacteristic;

    private BluetoothGattCharacteristic mDfuControlPointCharacteristic;

    private final Queue<Request> mQueue;
    private final Context mContext;
    private final Handler mHandler;
    private final Handler mMtuHandler;
    private final BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;

    private boolean isConnected = false;
    private String mDeviceName;

    private int mAdvertisingIntervalUnits = -1;
    private int mAdvertisingIntervalTimeoutUnits = -1;

    private int mMinConnectionIntervalUnits = -1;
    private int mMaxConnectionIntervalUnits = -1;
    private int mSlaveLatency = -1;
    private int mConnectionSupervisionTimeoutUnits = -1;

    private int mBatteryLevel;

    private int mTemperatureInterval = -1;
    private int mPressureInterval = -1;
    private int mHumidityInterval = -1;
    private int mColorIntensityInterval = -1;
    private int mGasMode = -1;
    private int mLedMode = ThingyUtils.BREATHE;
    private int mLedColorIndex = ThingyUtils.LED_CYAN;
    private int mRedIntensity = ThingyUtils.DEFAULT_RED_INTENSITY;
    private int mGreenIntensity = ThingyUtils.DEFAULT_GREEN_INTENSITY;
    private int mBlueIntensity = ThingyUtils.DEFAULT_BLUE_INTENSITY;

    private int mCallibrationRIntensity;
    private int mCallibrationGIntensity;
    private int mCallibrationBIntensity;

    private int mLedColorIntensity = ThingyUtils.DEFAULT_LED_INTENSITY;
    private int mLedBreatheDelay = ThingyUtils.DEFAULT_BREATHE_INTERVAL;
    private int mButtonState = 0; // Button state is released by default
    private int mPedometerInterval = -1;
    private int mMotionTemperatureInterval = -1;
    private int mCompassInterval = -1;
    private int mMotionIntervalFrequency = -1;
    private boolean mWakeOnMotion = false;

    private int mSpeakerMode;
    private int mMicrophoneMode;

    private ADPCMDecoder mAdpcmDecoder;

    private LinkedHashMap<String, String> mTemperatureData;
    private LinkedHashMap<String, String> mPressureData;
    private LinkedHashMap<String, Integer> mHumidityData;

    private BluetoothGattService mThingyConfigurationService;
    private BluetoothGattService mButtonLessDfuService;
    private ThingyConnectionGattCallbacks mListener;
    private boolean mInitialServiceDiscoveryCompleted = false;
    private BluetoothGattCharacteristic mLastCharacteristic;

    private int mMtu;
    private boolean mPlayPcmRequested = false;
    private boolean mPlayVoiceInput = false;
    private boolean mEnableThingyMicrophone = false;
    private byte[] mPcmSample;
    private boolean mWait = false;

    private int mtu = ThingyUtils.MAX_MTU_SIZE_THINGY;
    private int mNumOfAudioChunks = 0;
    private boolean mBufferWarningReceived = false;

    private int mPacketCounter = 0;
    private AudioTrack mAudioTrack;
    private boolean mStartPlaying;

    public boolean getConnectionState() {
        return isConnected;
    }

    boolean hasInitialServiceDiscoverCompleted() {
        return mInitialServiceDiscoveryCompleted;
    }

    boolean isAudioStreamingInProgress() {
        return mPlayPcmRequested;
    }

    void setAudioStreamingInProgress(boolean inProgress) {
        this.mPlayPcmRequested = inProgress;
    }

    public ThingyConnection(final Context context, final BluetoothDevice bluetoothDevice) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mMtuHandler = new Handler();
        this.mBluetoothDevice = bluetoothDevice;
        this.mQueue = new LinkedList<>();
        connect(bluetoothDevice);
        this.mTemperatureData = new LinkedHashMap<>();
        this.mPressureData = new LinkedHashMap<>();
        this.mHumidityData = new LinkedHashMap<>();
        this.mListener = (ThingyConnectionGattCallbacks) mContext;
    }

    public interface ThingyConnectionGattCallbacks {
        //Connection state listener callbacks
        void onDeviceConnected(BluetoothDevice device, int connectionState);

        void onDeviceDisconnected(BluetoothDevice device, int connectionState);
    }

    @Override
    public final void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.v(TAG, "Error " + status + " : " + gatt.getDevice().getAddress());
            isConnected = false;
            mListener.onDeviceDisconnected(mBluetoothDevice, newState);

            Intent intent = new Intent(ThingyUtils.ACTION_DEVICE_DISCONNECTED);
            intent.putExtra(ThingyUtils.EXTRA_DATA, newState);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            gatt.close();

            return;
        }

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            isConnected = true;
            Log.v(TAG, "Connected " + status);
            Intent intent = new Intent(ThingyUtils.ACTION_DEVICE_CONNECTED);
            intent.putExtra(ThingyUtils.EXTRA_DATA, newState);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            mListener.onDeviceConnected(mBluetoothDevice, newState);
            Log.v(TAG, "Starting service discovery");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gatt.discoverServices();
                }
            }, 200);
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            isConnected = false;
            Log.v(TAG, "Disconnected " + status);
            mListener.onDeviceDisconnected(mBluetoothDevice, newState);

            Intent intent = new Intent(ThingyUtils.ACTION_DEVICE_DISCONNECTED);
            intent.putExtra(ThingyUtils.EXTRA_DATA, newState);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            gatt.close();
        }
    }

    @Override
    public final void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.v(TAG, "Service discovery error: " + status);
            return;
        }

        Log.v(TAG, "Service discovery completed");

        mThingyConfigurationService = gatt.getService(ThingyUtils.THINGY_CONFIGURATION_SERVICE);
        if (mThingyConfigurationService != null) {
            mDeviceNameCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.DEVICE_NAME_CHARACTERISTIC_UUID);
            mAdvertisingParamCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.ADVERTISING_PARAM_CHARACTERISTIC_UUID);
            mConnectionParamCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.CONNECTION_PARAM_CHARACTERISTIC_UUID);
            mEddystoneUrlCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.EDDYSTONE_URL_CHARACTERISTIC_UUID);
            mCloudTokenCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.CLOUD_TOKEN_CHARACTERISTIC_UUID);
            mFirmwareVersionCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.FIRMWARE_VERSION_CHARACTERISTIC_UUID);
            mNfcCharacteristic = mThingyConfigurationService.getCharacteristic(ThingyUtils.NFC_CHARACTERISTIC_UUID);
            Log.v(TAG, "Reading thingy config chars");
        }

        final BluetoothGattService mBatteryService = gatt.getService(ThingyUtils.BATTERY_SERVICE);
        if (mBatteryService != null) {
            mBatteryLevelCharacteristic = mBatteryService.getCharacteristic(ThingyUtils.BATTERY_SERVICE_CHARACTERISTIC);
            Log.v(TAG, "Reading battery characteristic");
        }

        final BluetoothGattService mEnvironmentService = gatt.getService(ThingyUtils.THINGY_ENVIRONMENTAL_SERVICE);
        if (mEnvironmentService != null) {
            mTemperatureCharacteristic = mEnvironmentService.getCharacteristic(ThingyUtils.TEMPERATURE_CHARACTERISTIC);
            mPressureCharacteristic = mEnvironmentService.getCharacteristic(ThingyUtils.PRESSURE_CHARACTERISTIC);
            mHumidityCharacteristic = mEnvironmentService.getCharacteristic(ThingyUtils.HUMIDITY_CHARACTERISTIC);
            mAirQualityCharacteristic = mEnvironmentService.getCharacteristic(ThingyUtils.AIR_QUALITY_CHARACTERISTIC);
            mColorCharacteristic = mEnvironmentService.getCharacteristic(ThingyUtils.COLOR_CHARACTERISTIC);
            mEnvironmentConfigurationCharacteristic = mEnvironmentService.getCharacteristic(ThingyUtils.CONFIGURATION_CHARACTERISTIC);
            Log.v(TAG, "Reading environment config chars");
        }

        final BluetoothGattService mUiService = gatt.getService(ThingyUtils.THINGY_UI_SERVICE);
        if (mUiService != null) {
            mLedCharacteristic = mUiService.getCharacteristic(ThingyUtils.LED_CHARACTERISTIC);
            mButtonCharacteristic = mUiService.getCharacteristic(ThingyUtils.BUTTON_CHARACTERISTIC);
        }

        final BluetoothGattService mMotionService = gatt.getService(ThingyUtils.THINGY_MOTION_SERVICE);
        if (mMotionService != null) {
            mMotionConfigurationCharacteristic = mMotionService.getCharacteristic(ThingyUtils.THINGY_MOTION_CONFIGURATION_CHARACTERISTIC);
            mTapCharacteristic = mMotionService.getCharacteristic(ThingyUtils.TAP_CHARACTERISTIC);
            mOrientationCharacteristic = mMotionService.getCharacteristic(ThingyUtils.ORIENTATION_CHARACTERISTIC);
            mQuaternionCharacteristic = mMotionService.getCharacteristic(ThingyUtils.QUATERNION_CHARACTERISTIC);
            mPedometerCharacteristic = mMotionService.getCharacteristic(ThingyUtils.PEDOMETER_CHARACTERISTIC);
            mRawDataCharacteristic = mMotionService.getCharacteristic(ThingyUtils.RAW_DATA_CHARACTERISTIC);
            mEulerCharacteristic = mMotionService.getCharacteristic(ThingyUtils.EULER_CHARACTERISTIC);
            mRotationMatrixCharacteristic = mMotionService.getCharacteristic(ThingyUtils.ROTATION_MATRIX_CHARACTERISTIC);
            mHeadingCharacteristic = mMotionService.getCharacteristic(ThingyUtils.HEADING_CHARACTERISTIC);
            mGravityVectorCharacteristic = mMotionService.getCharacteristic(ThingyUtils.GRAVITY_VECTOR_CHARACTERISTIC);
            Log.v(TAG, "Reading motion config chars");
        }

        final BluetoothGattService mSoundService = gatt.getService(ThingyUtils.THINGY_SOUND_SERVICE);
        if (mSoundService != null) {
            mSoundConfigurationCharacteristic = mSoundService.getCharacteristic(ThingyUtils.THINGY_SOUND_CONFIG_CHARACTERISTIC);
            mSpeakerDataCharacteristic = mSoundService.getCharacteristic(ThingyUtils.THINGY_SPEAKER_DATA_CHARACTERISTIC);
            mSpeakerStatusCharacteristic = mSoundService.getCharacteristic(ThingyUtils.THINGY_SPEAKER_STATUS_CHARACTERISTIC);
            mMicrophoneCharacteristic = mSoundService.getCharacteristic(ThingyUtils.THINGY_MICROPHONE_CHARACTERISTIC);
        }

        mButtonLessDfuService = gatt.getService(ThingyUtils.SECURE_DFU_SERVICE);
        if (mButtonLessDfuService != null) {
            mDfuControlPointCharacteristic = mButtonLessDfuService.getCharacteristic(ThingyUtils.DFU_DEFAULT_CONTROL_POINT_CHARACTERISTIC);
        }

        readThingyCharacteristics();
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (characteristic.equals(mBatteryLevelCharacteristic)) {

            final int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            final Intent intent = new Intent((ThingyUtils.BATTERY_LEVEL_NOTIFICATION));
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, batteryLevel);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mTemperatureCharacteristic)) {

            final int mTemperatureInt = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
            final int mTemperatureDec = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            final String mTemperatureTimestamp = ThingyUtils.TIME_FORMAT.format(System.currentTimeMillis());
            mTemperatureData.put(mTemperatureTimestamp, String.valueOf(mTemperatureInt) + "." + String.valueOf(mTemperatureDec));

            final Intent intent = new Intent(ThingyUtils.TEMPERATURE_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, String.valueOf(mTemperatureInt) + "." + String.valueOf(mTemperatureDec));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            ThingyUtils.removeOldDataForGraphs(mTemperatureData);
        } else if (characteristic.equals(mPressureCharacteristic)) {
            final int mPressureInt = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
            final int mPressureDec = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4);

            final String mPressureTimestamp = ThingyUtils.TIME_FORMAT.format(System.currentTimeMillis());
            mPressureData.put(mPressureTimestamp, mPressureInt + "." + mPressureDec);

            final Intent intent = new Intent(ThingyUtils.PRESSURE_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, mPressureInt + "." + mPressureDec);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            ThingyUtils.removeOldDataForGraphs(mPressureData);
        } else if (characteristic.equals(mHumidityCharacteristic)) {
            final int mHumidity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            final String mHumidityTimestamp = ThingyUtils.TIME_FORMAT.format(System.currentTimeMillis());
            mHumidityData.put(mHumidityTimestamp, mHumidity);

            final Intent intent = new Intent(ThingyUtils.HUMIDITY_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, String.valueOf(mHumidity));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            ThingyUtils.removeOldDataForGraphs(mHumidityData);
        } else if (characteristic.equals(mAirQualityCharacteristic)) {
            final int mECO2 = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            final int mTVOC = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);

            final Intent intent = new Intent(ThingyUtils.AIR_QUALITY_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_ECO2, mECO2);
            intent.putExtra(ThingyUtils.EXTRA_DATA_TVOC, mTVOC);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mColorCharacteristic)) {
            final float mRed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            final float mGreen = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
            final float mBlue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
            final float mClear = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);

            final Intent intent = new Intent(ThingyUtils.COLOR_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_RED, mRed);
            intent.putExtra(ThingyUtils.EXTRA_DATA_GREEN, mGreen);
            intent.putExtra(ThingyUtils.EXTRA_DATA_BLUE, mBlue);
            intent.putExtra(ThingyUtils.EXTRA_DATA_CLEAR, mClear);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mButtonCharacteristic)) {
            mButtonState = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            final Intent intent = new Intent(ThingyUtils.BUTTON_STATE_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_BUTTON, mButtonState);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mTapCharacteristic)) {
            final int mDirection = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            int mTap = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);

            final Intent intent = new Intent(ThingyUtils.TAP_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_TAP_COUNT, mTap);
            intent.putExtra(ThingyUtils.EXTRA_DATA_TAP_DIRECTION, mDirection);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mOrientationCharacteristic)) {
            final int mOrientation = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            final Intent intent = new Intent(ThingyUtils.ORIENTATION_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, mOrientation);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mQuaternionCharacteristic)) {

            final float mQuaternionW = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0)) / (1 << 30);
            final float mQuaternionX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4)) / (1 << 30);
            final float mQuaternionY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 8)) / (1 << 30);
            final float mQuaternionZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 12)) / (1 << 30);

            final Intent intent = new Intent(ThingyUtils.QUATERNION_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_QUATERNION_W, mQuaternionW);
            intent.putExtra(ThingyUtils.EXTRA_DATA_QUATERNION_X, mQuaternionX);
            intent.putExtra(ThingyUtils.EXTRA_DATA_QUATERNION_Y, mQuaternionY);
            intent.putExtra(ThingyUtils.EXTRA_DATA_QUATERNION_Z, mQuaternionZ);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mPedometerCharacteristic)) {
            final int mStepCount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
            final int mDuration = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 4);

            final Intent intent = new Intent(ThingyUtils.PEDOMETER_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_STEP_COUNT, mStepCount);
            intent.putExtra(ThingyUtils.EXTRA_DATA_DURATION, ThingyUtils.TIME_FORMAT_PEDOMETER.format(mDuration));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mRawDataCharacteristic)) {
            final float mAccelerometerX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0)) / (1 << 10);
            final float mAccelerometerY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2)) / (1 << 10);
            final float mAccelerometerZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4)) / (1 << 10);

            final float mGyroscopeX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 6)) / (1 << 5);
            final float mGyroscopeY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 8)) / (1 << 5);
            final float mGyroscopeZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 10)) / (1 << 5);

            final float mCompassZ = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 12)) / (1 << 4);
            final float mCompassX = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 14)) / (1 << 4);
            final float mCompassY = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 16)) / (1 << 4);

            final Intent intent = new Intent(ThingyUtils.RAW_DATA_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_ACCELEROMETER_X, mAccelerometerX);
            intent.putExtra(ThingyUtils.EXTRA_DATA_ACCELEROMETER_Y, mAccelerometerY);
            intent.putExtra(ThingyUtils.EXTRA_DATA_ACCELEROMETER_Z, mAccelerometerZ);

            intent.putExtra(ThingyUtils.EXTRA_DATA_GYROSCOPE_X, mGyroscopeX);
            intent.putExtra(ThingyUtils.EXTRA_DATA_GYROSCOPE_Y, mGyroscopeY);
            intent.putExtra(ThingyUtils.EXTRA_DATA_GYROSCOPE_Z, mGyroscopeZ);

            intent.putExtra(ThingyUtils.EXTRA_DATA_COMPASS_X, mCompassX);
            intent.putExtra(ThingyUtils.EXTRA_DATA_COMPASS_Y, mCompassY);
            intent.putExtra(ThingyUtils.EXTRA_DATA_COMPASS_Z, mCompassZ);

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mEulerCharacteristic)) {
            final float mRoll = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0)) / (1 << 16);
            final float mPitch = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4)) / (1 << 16);
            final float mYaw = (float) (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 8)) / (1 << 16);

            final Intent intent = new Intent(ThingyUtils.EULER_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_ROLL, mRoll);
            intent.putExtra(ThingyUtils.EXTRA_DATA_PITCH, mPitch);
            intent.putExtra(ThingyUtils.EXTRA_DATA_YAW, mYaw);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mRotationMatrixCharacteristic)) {
            final byte[] attitudeInRotationMatrix = characteristic.getValue();
            final Intent intent = new Intent(ThingyUtils.ROTATION_MATRIX_NOTIFICATION);

            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, attitudeInRotationMatrix);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mHeadingCharacteristic)) {

            final float mHeading = ((float) characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0)) / (1 << 16);
            final Intent intent = new Intent(ThingyUtils.HEADING_NOTIFICATION);

            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA, Float.valueOf(String.format(Locale.US, "%.2f", mHeading)));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mGravityVectorCharacteristic)) {
            final byte[] data = characteristic.getValue();
            final ByteBuffer mByteBuffer = ByteBuffer.wrap(data);
            mByteBuffer.order(ByteOrder.LITTLE_ENDIAN); // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
            float mGravityVectorX = mByteBuffer.getFloat(0);
            float mGravityVectorY = mByteBuffer.getFloat(4);
            float mGravityVectorZ = mByteBuffer.getFloat(8);

            final Intent intent = new Intent(ThingyUtils.GRAVITY_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_GRAVITY_X, mGravityVectorX);
            intent.putExtra(ThingyUtils.EXTRA_DATA_GRAVITY_Y, mGravityVectorY);
            intent.putExtra(ThingyUtils.EXTRA_DATA_GRAVITY_Z, mGravityVectorZ);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else if (characteristic.equals(mSpeakerStatusCharacteristic)) {
            final int speakerStatus = mSpeakerStatusCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            final Intent intent = new Intent(ThingyUtils.SPEAKER_STATUS_NOTIFICATION);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            intent.putExtra(ThingyUtils.EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION, speakerStatus);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            switch (speakerStatus) {
                case ThingyUtils.SPEAKER_STATUS_FINISHED:
                    if (mPlayPcmRequested) {
                        mWait = false;
                        if (mQueue.size() == 0) {
                            broadcastAudioStreamComplete();
                        }
                    } else if (mPlayVoiceInput) {
                        mHandler.post(mProcessNextTask);
                    }
                    break;
                case ThingyUtils.SPEAKER_STATUS_BUFFER_WARNING:
                    Log.v(TAG, "Buffer warning received");
                    mBufferWarningReceived = true;
                    break;
                case ThingyUtils.SPEAKER_STATUS_BUFFER_READY:
                    Log.v(TAG, "Buffer ready received");
                    mWait = false;
                    mBufferWarningReceived = false;
                    if (mPlayVoiceInput) {
                        mHandler.post(mProcessNextTask);
                    }
                    break;
                case ThingyUtils.SPEAKER_STATUS_PACKET_DISREGARDED:
                    break;
                case ThingyUtils.SPEAKER_STATUS_INVALID_COMMAND:
                    break;
            }
        } else if (characteristic.equals(mMicrophoneCharacteristic)) {
            if (mAdpcmDecoder != null) {
                if (mMtu == ThingyUtils.MAX_MTU_SIZE_THINGY) { //Pre lollipop devices may not have the max mtu size hence the check
                    final byte[] data = new byte[131];
                    final byte[] tempData = characteristic.getValue();
                    System.arraycopy(tempData, 0, data, 0, 131);
                    mAdpcmDecoder.add(data);
                } else {
                    final byte[] data = characteristic.getValue();
                    mAdpcmDecoder.add(data);
                }
            }
        }
    }

    @Override
    public final void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        if (characteristic.equals(mDeviceNameCharacteristic)) {
            readDeviceName();
        } else if (characteristic.equals(mAdvertisingParamCharacteristic)) {
            readAdvertisingParameters();
        } else if (characteristic.equals(mConnectionParamCharacteristic)) {
            readConnectionParamCharacteristicData();
        } else if (characteristic.equals(mEnvironmentConfigurationCharacteristic)) {
            readEnvironmentConfigurationCharacteristic();
        } else if (characteristic.equals(mLedCharacteristic)) {
            readLedCharacteristic();
        } else if (characteristic.equals(mButtonCharacteristic)) {
            readButtonCharacteristic();
        } else if (characteristic.equals(mMotionConfigurationCharacteristic)) {
            readMotionConfigurationCharacteristic();
        } else if (characteristic.equals(mSoundConfigurationCharacteristic)) {
            readSoundConfigurationCharacteristic();
        } else if (characteristic.equals(mBatteryLevelCharacteristic)) {
            readBatteryLevelCharacteristic();
        }

        mHandler.post(mProcessNextTask);
    }

    @Override
    public final void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (characteristic.equals(mMotionConfigurationCharacteristic)) {
            add(RequestType.READ_CHARACTERISTIC, characteristic);
        } else if (characteristic.equals(mSoundConfigurationCharacteristic)) {
            final int speakerMode = mSoundConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            final int microphoneMode = mSoundConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            mSpeakerMode = speakerMode;
            mMicrophoneMode = microphoneMode;
        } else if (characteristic.equals(mSpeakerDataCharacteristic)) {
            if (mPlayPcmRequested) {
                if (!mStartPlaying) {
                    handleAudioRequests();
                } else {
                    if (!mBufferWarningReceived) {
                        mWait = false;
                    }
                }
                return;
            }
        }

        mHandler.post(mProcessNextTask);
    }

    @Override
    public final void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        final BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();

        if (descriptor.getUuid().equals(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR)) {
            final byte[] value = descriptor.getValue();
            enableNotifications(gatt, characteristic, value);
        }

        mHandler.post(mProcessNextTask);
    }

    @Override
    public final void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        final BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();

        if (characteristic.equals(mMicrophoneCharacteristic)) {
            mLastCharacteristic = characteristic;
        }

        if (!mInitialServiceDiscoveryCompleted && characteristic.equals(mLastCharacteristic)) {
            mInitialServiceDiscoveryCompleted = true;

            Intent intent = new Intent(ThingyUtils.ACTION_SERVICE_DISCOVERY_COMPLETED);
            intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        mHandler.post(mProcessNextTask);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.v(ThingyUtils.TAG, "onMtuChanged() " + mtu + " Status: " + status);
            mMtu = mtu;
        } else {
            ThingyUtils.showToast(mContext, mContext.getString(R.string.thingy_error_mtu_failed, status));
        }
    }

    /**
     * Connects to a particular thingy
     *
     * @param device Bluetooth device to connect to
     */
    private void connect(final BluetoothDevice device) {
        mBluetoothGatt = device.connectGatt(mContext, false, this);
    }

    /**
     * Disconnects from a particular thingy
     */
    public final void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * Check and enable notifications for characteristic
     *
     * @param gatt           bluetooth gatt
     * @param characteristic of which the notifications to be enabled
     * @param value          notification value
     */
    private void enableNotifications(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final byte[] value) {
        final int notificationValue = value[0]; //Checking if notifications are enabled
        switch (notificationValue) {
            case 0:
                gatt.setCharacteristicNotification(characteristic, false);
                break;
            case 1:
                gatt.setCharacteristicNotification(characteristic, true);

                if (mDfuControlPointCharacteristic != null && characteristic.getUuid().equals(mDfuControlPointCharacteristic.getUuid())) {
                    add(RequestType.WRITE_CHARACTERISTIC, mDfuControlPointCharacteristic, new byte[]{0x01}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                }
                break;
        }
    }

    /**
     * Check and notifications are already enabled for the characteristic
     *
     * @param descriptor of whose characteristic notifications to be enabled
     */
    private boolean isNotificationsAlreadyEnabled(final BluetoothGattDescriptor descriptor) {
        final byte[] data = descriptor.getValue();
        if (data != null) {
            final int notificationValue = data[0]; //Checking if notifications are enabled
            return notificationValue == 1;
        }
        return false;
    }

    private void readBatteryLevelCharacteristic() {
        if (mBatteryLevelCharacteristic != null) {
            final BluetoothGattCharacteristic characteristic = mBatteryLevelCharacteristic;
            mBatteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }
    }

    public final int getBatteryLevel() {
        return mBatteryLevel;
    }

    /**
     * Enable battery level notifications
     *
     * @param enable notifications on/off
     */
    public final void enableBatteryLevelNotifications(final boolean enable) {
        if (mBatteryLevelCharacteristic != null) {
            final BluetoothGattDescriptor mColorCharacteristicDescriptor = mBatteryLevelCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(mColorCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, mColorCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(mColorCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, mColorCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for environment service
     *
     * @param enable notifications on/off
     */
    public final void enableEnvironmentNotifications(final boolean enable) {
        enableTemperatureNotifications(enable);
        enablePressureNotifications(enable);
        enableHumidityNotifications(enable);
        enableAirQualityNotifications(enable);
        enableColorNotifications(enable);
    }

    /**
     * Enable notifications for temperature
     *
     * @param enabled notifications on/off
     */
    final void enableTemperatureNotifications(final boolean enabled) {
        if (mTemperatureCharacteristic != null) {
            final BluetoothGattDescriptor temperatureCharacteristicDescriptor = mTemperatureCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enabled) {
                if (!isNotificationsAlreadyEnabled(temperatureCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, temperatureCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(temperatureCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, temperatureCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for pressure
     *
     * @param enable notifications on/off
     */
    final void enablePressureNotifications(final boolean enable) {
        if (mPressureCharacteristic != null) {
            final BluetoothGattDescriptor pressureCharacteristicDescriptor = mPressureCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(pressureCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, pressureCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(pressureCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, pressureCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for humidity
     *
     * @param enable notifications on/off
     */
    final void enableHumidityNotifications(final boolean enable) {
        if (mHumidityCharacteristic != null) {
            final BluetoothGattDescriptor humidityCharacteristicDescriptor = mHumidityCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(humidityCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, humidityCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(humidityCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, humidityCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for humidity
     *
     * @param enable notifications on/off
     */
    final void enableAirQualityNotifications(final boolean enable) {
        if (mAirQualityCharacteristic != null) {
            final BluetoothGattDescriptor airQualityDescriptor = mAirQualityCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(airQualityDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, airQualityDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(airQualityDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, airQualityDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for color
     *
     * @param enable notifications on/off
     */
    final void enableColorNotifications(final boolean enable) {
        if (mColorCharacteristic != null) {
            final BluetoothGattDescriptor mColorCharacteristicDescriptor = mColorCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(mColorCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, mColorCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(mColorCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, mColorCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enables all notifications for motion
     *
     * @param enable notifications on/off
     */
    final void enableMotionNotifications(final boolean enable) {
        enableTapNotifications(enable);
        enableOrientationNotifications(enable);
        enableQuaternionNotifications(enable);
        enablePedometerNotifications(enable);
        enableRawDataNotifications(enable);
        enableEulerNotifications(enable);
        enableRotationMatrixNotifications(enable);
        enableHeadingNotifications(enable);
        enableGravityVectorNotifications(enable);
    }

    /**
     * Reads all the characteristics from the Thingy:52
     */
    private void readThingyCharacteristics() {
        if (mThingyConfigurationService != null) {

            if (mDeviceNameCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mDeviceNameCharacteristic);
            }

            if (mAdvertisingParamCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mAdvertisingParamCharacteristic);
            }

            if (mConnectionParamCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mConnectionParamCharacteristic);
            }

            if (mEddystoneUrlCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mEddystoneUrlCharacteristic);
            }

            if (mCloudTokenCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mCloudTokenCharacteristic);
            }

            if (mFirmwareVersionCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mFirmwareVersionCharacteristic);
            }

            if (mNfcCharacteristic != null) {
                add(RequestType.READ_CHARACTERISTIC, mNfcCharacteristic);
            }
        }

        if (mBatteryLevelCharacteristic != null) {
            add(RequestType.READ_CHARACTERISTIC, mBatteryLevelCharacteristic);
            add(RequestType.READ_DESCRIPTOR, mBatteryLevelCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        }

        if (mEnvironmentConfigurationCharacteristic != null) {
            add(RequestType.READ_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic);
        }

        add(RequestType.READ_DESCRIPTOR, mTemperatureCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mPressureCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mHumidityCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mAirQualityCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mColorCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));

        if (mLedCharacteristic != null) {
            add(RequestType.READ_CHARACTERISTIC, mLedCharacteristic);
        }

        if (mButtonCharacteristic != null) {
            add(RequestType.READ_DESCRIPTOR, mButtonCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        }

        if (mMotionConfigurationCharacteristic != null) {
            add(RequestType.READ_CHARACTERISTIC, mMotionConfigurationCharacteristic);
        }

        add(RequestType.READ_DESCRIPTOR, mTapCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mOrientationCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mQuaternionCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mPedometerCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mRawDataCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mEulerCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mRotationMatrixCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mHeadingCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        add(RequestType.READ_DESCRIPTOR, mGravityVectorCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));

        add(RequestType.READ_CHARACTERISTIC, mSoundConfigurationCharacteristic);
        add(RequestType.READ_DESCRIPTOR, mSpeakerStatusCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        if (mMicrophoneCharacteristic != null) {
            add(RequestType.READ_DESCRIPTOR, mMicrophoneCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR));
        }
    }

    /**
     * Configure a device name for the thingy which would be used for advertising
     *
     * @param name device name
     */
    final void setDeviceName(final String name) {
        final byte[] data = name.getBytes();
        add(RequestType.WRITE_CHARACTERISTIC, mDeviceNameCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
    }

    /**
     * Returns the device name for the specific thingy
     */
    /*package access*/
    final String readDeviceName() {
        if (mDeviceNameCharacteristic != null) {
            return mDeviceName = mDeviceNameCharacteristic.getStringValue(0);
        }

        return null;
    }

    /**
     * Returns the advertising parameters for the particular thingy
     */
    private void readAdvertisingParameters() {
        if (mAdvertisingParamCharacteristic != null) {
            mAdvertisingIntervalUnits = mAdvertisingParamCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            mAdvertisingIntervalTimeoutUnits = mAdvertisingParamCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
        }
    }

    /**
     * Returns the advertising interval for the particular thingy
     */
    /*package access*/
    final int getAdvertisingIntervalUnits() {
        if (mAdvertisingIntervalUnits == -1) {
            readAdvertisingParameters();
        }
        return mAdvertisingIntervalUnits;
    }

    /**
     * Returns the advertising interval timeout for the particular thingy
     */
    /*package access*/
    final int getAdvertisingIntervalTimeoutUnits() {
        if (mAdvertisingIntervalTimeoutUnits == -1) {
            readAdvertisingParameters();
        }
        return mAdvertisingIntervalTimeoutUnits;
    }

    /**
     * Confgiures the advertising parameters for a particular thingy
     *
     * @param advertisingIntervalUnits advertising parameters
     * @param advertisingTimeoutUnits  advertising timeout
     */
    final boolean setAdvertisingParameters(final int advertisingIntervalUnits, final int advertisingTimeoutUnits) {
        if (mAdvertisingParamCharacteristic != null) {
            mAdvertisingIntervalUnits = advertisingIntervalUnits;
            mAdvertisingIntervalTimeoutUnits = advertisingTimeoutUnits;
            final byte[] data = new byte[3];
            ThingyUtils.setValue(data, 0, advertisingIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, advertisingTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mAdvertisingParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Confgiures the advertising parameters for a particular thingy
     *
     * @param advertisingIntervalUnits advertising parameters
     */
    final boolean setAdvertisingIntervalUnits(final int advertisingIntervalUnits) {
        if (mAdvertisingParamCharacteristic != null) {
            mAdvertisingIntervalUnits = advertisingIntervalUnits;
            final byte[] data = new byte[3];
            ThingyUtils.setValue(data, 0, advertisingIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, mAdvertisingIntervalTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mAdvertisingParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Confgiures the advertising parameters for a particular thingy
     *
     * @param advertisingTimeoutUnits advertising timeout
     */
    final boolean setAdvertisingTimeoutUnits(final int advertisingTimeoutUnits) {
        if (mAdvertisingParamCharacteristic != null) {
            mAdvertisingIntervalTimeoutUnits = advertisingTimeoutUnits;
            final byte[] data = new byte[3];
            ThingyUtils.setValue(data, 0, mAdvertisingIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, advertisingTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mAdvertisingParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Configures the Connection parameters for a particular thingy
     *
     * @param minConnectionIntervalUnits   min connection interval
     * @param maxConnectionIntervalUnits   max connection interval
     * @param slaveLatency
     * @param connectionSupervisionTimeout
     */
    final boolean setConnectionParameters(final int minConnectionIntervalUnits, final int maxConnectionIntervalUnits,
                                          final int slaveLatency, final int connectionSupervisionTimeout) {
        if (mConnectionParamCharacteristic != null) {
            mMinConnectionIntervalUnits = minConnectionIntervalUnits;
            mMaxConnectionIntervalUnits = maxConnectionIntervalUnits;
            mSlaveLatency = slaveLatency;
            mConnectionSupervisionTimeoutUnits = connectionSupervisionTimeout;

            final byte[] data = new byte[8];
            ThingyUtils.setValue(data, 0, minConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, maxConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, slaveLatency, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 6, connectionSupervisionTimeout, BluetoothGattCharacteristic.FORMAT_UINT16);
            mConnectionParamCharacteristic.setValue(data);
            add(RequestType.WRITE_CHARACTERISTIC, mConnectionParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    private void readConnectionParamCharacteristicData() {
        if (mConnectionParamCharacteristic != null) {
            mMinConnectionIntervalUnits = mConnectionParamCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            mMaxConnectionIntervalUnits = mConnectionParamCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
            mSlaveLatency = mConnectionParamCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
            mConnectionSupervisionTimeoutUnits = mConnectionParamCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
        }
    }

    boolean setMinimumConnectionIntervalUnits(final int connectionIntervalUnits) {
        if (mConnectionParamCharacteristic != null) {
            mMinConnectionIntervalUnits = connectionIntervalUnits;

            final byte[] data = new byte[8];
            ThingyUtils.setValue(data, 0, connectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, mMaxConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, mSlaveLatency, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 6, mConnectionSupervisionTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            mConnectionParamCharacteristic.setValue(data);
            add(RequestType.WRITE_CHARACTERISTIC, mConnectionParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    int getMinimumConnectionIntervalUnits() {
        if (mMinConnectionIntervalUnits == -1) {
            readConnectionParamCharacteristicData();
        }
        return mMinConnectionIntervalUnits;
    }

    boolean setMaximumConnectionIntervalUnits(final int connectionIntervalUnits) {
        if (mConnectionParamCharacteristic != null) {
            mMaxConnectionIntervalUnits = connectionIntervalUnits;

            final byte[] data = new byte[8];
            ThingyUtils.setValue(data, 0, mMinConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, connectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, mSlaveLatency, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 6, mConnectionSupervisionTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            mConnectionParamCharacteristic.setValue(data);
            add(RequestType.WRITE_CHARACTERISTIC, mConnectionParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    int getMaximumConnectionIntervalUnits() {
        if (mMaxConnectionIntervalUnits == -1) {
            readConnectionParamCharacteristicData();
        }
        return mMaxConnectionIntervalUnits;
    }

    boolean setSlaveLatency(final int slaveLatency) {
        if (mConnectionParamCharacteristic != null) {
            mSlaveLatency = slaveLatency;

            final byte[] data = new byte[8];
            ThingyUtils.setValue(data, 0, mMinConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, mMaxConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, slaveLatency, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 6, mConnectionSupervisionTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            mConnectionParamCharacteristic.setValue(data);
            add(RequestType.WRITE_CHARACTERISTIC, mConnectionParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    int getSlaveLatency() {
        if (mSlaveLatency == -1) {
            readConnectionParamCharacteristicData();
        }
        return mSlaveLatency;
    }

    boolean setConnectionSupervisionTimeout(final int supervisionTimeoutUnits) {
        if (mConnectionParamCharacteristic != null) {
            mConnectionSupervisionTimeoutUnits = supervisionTimeoutUnits;

            final byte[] data = new byte[8];
            ThingyUtils.setValue(data, 0, mMinConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, mMaxConnectionIntervalUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, mSlaveLatency, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 6, supervisionTimeoutUnits, BluetoothGattCharacteristic.FORMAT_UINT16);
            mConnectionParamCharacteristic.setValue(data);
            add(RequestType.WRITE_CHARACTERISTIC, mConnectionParamCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    int getConnectionSupervisionTimeoutUnit() {
        if (mConnectionSupervisionTimeoutUnits == -1) {
            readConnectionParamCharacteristicData();
        }
        return mConnectionSupervisionTimeoutUnits;
    }

    /**
     * Returns the Eddystone URL for a particular thingy
     */
    /*package access*/
    final String getEddystoneUrl() {
        if (mEddystoneUrlCharacteristic != null) {
            final byte[] data = mEddystoneUrlCharacteristic.getValue();
            if (data != null && data.length > 0) {
                return ThingyUtils.decodeUri(data, 0, data.length);
            } else {
                return "";
            }
        }
        return null;
    }

    /**
     * Configure Eddystone URL
     *
     * @param url eddystone url
     */
    /*package access*/
    final boolean setEddystoneUrl(final String url) {
        if (mEddystoneUrlCharacteristic != null) {
            if (!URLUtil.isValidUrl(url)) {
                Log.v(ThingyUtils.TAG, "Please enter a valid value for URL");
                return false;
            } else if (!URLUtil.isNetworkUrl(url)) {
                Log.v(ThingyUtils.TAG, "Please enter a valid value for URL");
                return false;
            } else if (ThingyUtils.encodeUri(url).length > 18) {
                Log.v(ThingyUtils.TAG, "Please enter a shortened URL or press use the URL shortener!");
                return false;
            }

            final byte[] data = ThingyUtils.encodeUri(url);
            add(RequestType.WRITE_CHARACTERISTIC, mEddystoneUrlCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Disables the Eddystone URL advertising on the thingy
     * writing zero bytes to this characteristic will disable this feature.
     */
    final boolean disableEddystoneUrl() {
        if (mEddystoneUrlCharacteristic != null) {

            final byte[] data = new byte[0];
            add(RequestType.WRITE_CHARACTERISTIC, mEddystoneUrlCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Return cloud token
     */
    final String getCloudTokenData() {
        if (mCloudTokenCharacteristic != null) {
            return mCloudTokenCharacteristic.getStringValue(0);
        }
        return null;
    }

    /**
     * Return firmware version of thingy
     */
    final String getFirmwareVersion() {
        if (mFirmwareVersionCharacteristic != null) {
            final String major = String.valueOf(mFirmwareVersionCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            final String minor = String.valueOf(mFirmwareVersionCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1));
            final String patch = String.valueOf(mFirmwareVersionCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2));
            return major + "." + minor + "." + patch;
        }
        return null;
    }

    /**
     * Returns NFC content of thingy.
     */
    final byte[] getNfcContent() {
        if (mNfcCharacteristic != null) {
            return mNfcCharacteristic.getValue();
        }
        return null;
    }

    /**
     * Sets the NFC content. Available form fw version 2.2.0 onwards.
     *
     * @param content the exact content that will be available through NFC.
     * @return True, if the request has been queued, false if there is no NFC characteristic.
     */
    final boolean setNfcContent(final byte[] content) {
        if (mNfcCharacteristic != null) {
            add(RequestType.WRITE_CHARACTERISTIC, mNfcCharacteristic, content, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Configure cloud token characteristic
     *
     * @param cloudToken
     */
    final boolean setCloudToken(final String cloudToken) {
        if (cloudToken.length() > ThingyUtils.CLOUD_TOKEN_LENGTH) {
            ThingyUtils.showToast(mContext, "Cloud token length cannot exceed 250 characters");
            return false;
        }

        if (mCloudTokenCharacteristic != null) {
            final byte[] data = cloudToken.getBytes();
            add(RequestType.WRITE_CHARACTERISTIC, mCloudTokenCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Configures the Environment configuration characteristic for a particular thingy
     *
     * @param temperatureInterval    in ms
     * @param pressureInterval       in ms
     * @param humidityInterval       in ms
     * @param colorIntensityInterval in ms
     * @param gasMode                as an interval in ms
     */
    boolean setEnvironmentConfigurationCharacteristic(final int temperatureInterval, final int pressureInterval, final int humidityInterval, final int colorIntensityInterval, final int gasMode) {
        if (mEnvironmentConfigurationCharacteristic != null) {
            if (temperatureInterval < ThingyUtils.TEMP_MIN_INTERVAL || temperatureInterval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                Log.v(ThingyUtils.TAG, "Invalid temperature interval range");
                return false;
            }

            if (pressureInterval < ThingyUtils.PRESSURE_MIN_INTERVAL || pressureInterval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                Log.v(ThingyUtils.TAG, "Invalid pressure interval range");
                return false;
            }

            if (humidityInterval < ThingyUtils.PRESSURE_MIN_INTERVAL || humidityInterval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                Log.v(ThingyUtils.TAG, "Invalid pressure interval range");
                return false;
            }

            if (colorIntensityInterval < ThingyUtils.COLOR_INTENSITY_MIN_INTERVAL || colorIntensityInterval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                Log.v(ThingyUtils.TAG, "Invalid color interval range");
                return false;
            }

            if (gasMode != ThingyUtils.GAS_MODE_1 && gasMode != ThingyUtils.GAS_MODE_2 && gasMode != ThingyUtils.GAS_MODE_3) {
                Log.v(ThingyUtils.TAG, "Invalid gas mode");
                return false;
            }

            mTemperatureInterval = temperatureInterval;
            mPressureInterval = pressureInterval;
            mHumidityInterval = humidityInterval;
            mColorIntensityInterval = colorIntensityInterval;
            mGasMode = gasMode;

            byte[] data;
            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                data = new byte[12];
                ThingyUtils.setValue(data, 0, temperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, pressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, humidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, colorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, gasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 9, mCallibrationRIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 10, mCallibrationGIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 11, mCallibrationBIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
            } else {
                data = new byte[9];
                ThingyUtils.setValue(data, 0, temperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, pressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, humidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, colorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, gasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
            }

            add(RequestType.WRITE_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }

        return false;
    }

    /**
     * Configure the temperature intervals
     *
     * @param interval in ms
     */
    final boolean setTemperatureInterval(final int interval) {
        if (interval < ThingyUtils.TEMP_MIN_INTERVAL || interval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
            Log.v(ThingyUtils.TAG, "Invalid temperature interval range");
            return false;
        }

        if (mEnvironmentConfigurationCharacteristic != null) {
            mTemperatureInterval = interval;

            byte[] data;
            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                data = new byte[12];
                ThingyUtils.setValue(data, 0, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 9, mCallibrationRIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 10, mCallibrationGIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 11, mCallibrationBIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
            } else {
                data = new byte[9];
                ThingyUtils.setValue(data, 0, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
            }

            add(RequestType.WRITE_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Configure pressure intervals
     *
     * @param interval in ms
     */
    final boolean setPressureInterval(final int interval) {
        if (interval < ThingyUtils.PRESSURE_MIN_INTERVAL || interval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
            Log.v(ThingyUtils.TAG, "Invalid pressure interval range");
            return false;
        }

        if (mEnvironmentConfigurationCharacteristic != null) {
            mPressureInterval = interval;

            byte[] data;
            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                data = new byte[12];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 9, mCallibrationRIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 10, mCallibrationGIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 11, mCallibrationBIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
            } else {
                data = new byte[9];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
            }

            add(RequestType.WRITE_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Configure the humidity intervals
     *
     * @param interval in ms
     */
    final boolean setHumidityInterval(final int interval) {
        if (interval < ThingyUtils.PRESSURE_MIN_INTERVAL || interval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
            Log.v(ThingyUtils.TAG, "Invalid pressure interval range");
            return false;
        }

        if (mEnvironmentConfigurationCharacteristic != null) {
            mHumidityInterval = interval;

            byte[] data;
            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                data = new byte[12];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 9, mCallibrationRIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 10, mCallibrationGIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 11, mCallibrationBIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
            } else {
                data = new byte[9];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
            }
            add(RequestType.WRITE_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }
        return false;
    }

    /**
     * Configure color intensity intervals
     *
     * @param interval in ms
     */
    final boolean setColorIntensityInterval(final int interval) {
        if (interval < ThingyUtils.COLOR_INTENSITY_MIN_INTERVAL || interval > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
            Log.v(ThingyUtils.TAG, "Invalid color interval range");
            return false;
        }

        if (mEnvironmentConfigurationCharacteristic != null) {
            mColorIntensityInterval = interval;

            byte[] data;
            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                data = new byte[12];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 9, mCallibrationRIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 10, mCallibrationGIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 11, mCallibrationBIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
            } else {
                data = new byte[9];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, interval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, mGasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
            }
            add(RequestType.WRITE_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Configures gas mode
     *
     * @param gasMode
     */
    @SuppressWarnings("UnusedReturnValue")
    final boolean setGasMode(final int gasMode) {
        if (gasMode != ThingyUtils.GAS_MODE_1 && gasMode != ThingyUtils.GAS_MODE_2 && gasMode != ThingyUtils.GAS_MODE_3) {
            Log.v(ThingyUtils.TAG, "Invalid gas mode");
            return false;
        }

        if (mEnvironmentConfigurationCharacteristic != null) {
            mGasMode = gasMode;
            byte[] data;
            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                data = new byte[12];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, gasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 9, mCallibrationRIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 10, mCallibrationGIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
                ThingyUtils.setValue(data, 11, mCallibrationBIntensity, BluetoothGattCharacteristic.FORMAT_UINT8);
            } else {
                data = new byte[9];
                ThingyUtils.setValue(data, 0, mTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 2, mPressureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 4, mHumidityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 6, mColorIntensityInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
                ThingyUtils.setValue(data, 8, gasMode, BluetoothGattCharacteristic.FORMAT_UINT8);
            }

            add(RequestType.WRITE_CHARACTERISTIC, mEnvironmentConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }
        return false;
    }

    /**
     * Read the environment configuration characteristic
     */
    private void readEnvironmentConfigurationCharacteristic() {
        if (mEnvironmentConfigurationCharacteristic != null) {
            mTemperatureInterval = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            mPressureInterval = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
            mHumidityInterval = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
            mColorIntensityInterval = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
            mGasMode = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 8);

            //Thingy pre-release fw contained only 9 bytes in the configuration characteristic
            //Adding this check will make it compatible with all users who haven't updated from the pre-release fw
            //If not writing to this characteristic will fail as the fw will accept 12 bytes
            if (mEnvironmentConfigurationCharacteristic.getValue().length == 12) {
                mCallibrationRIntensity = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 9);
                mCallibrationGIntensity = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 10);
                mCallibrationBIntensity = mEnvironmentConfigurationCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 11);
            }
        }
    }

    /**
     * Reads the motion configuration characteristic for a particular thingy
     */
    private void readLedCharacteristic() {
        if (mLedCharacteristic != null) {
            final BluetoothGattCharacteristic characteristic = mLedCharacteristic;
            mLedMode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            switch (mLedMode) {
                case ThingyUtils.CONSTANT:
                    final int r = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                    final int g = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    final int b = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
                    mLedColorIndex = Color.rgb(r, g, b);
                    break;
                case ThingyUtils.BREATHE:
                    mLedColorIndex = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                    mLedColorIntensity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    mLedBreatheDelay = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 3);
                    break;
                case ThingyUtils.ONE_SHOT:
                    mLedColorIndex = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                    mLedColorIntensity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    break;
                case ThingyUtils.OFF:
                    mLedColorIndex = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Reads the motion configuration characteristic for a particular thingy
     */
    private void readButtonCharacteristic() {
        if (mButtonCharacteristic != null) {
            final BluetoothGattCharacteristic characteristic = mButtonCharacteristic;
            mButtonState = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }
    }

    /**
     * Returns the saved temperature data per thingy connection
     *
     * @return mTemperatureData Map containing timestamps and temperature as a K,V pair
     */
    final Map<String, String> getSavedTemperatureData() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(mTemperatureData));
    }

    /**
     * Returns the saved pressure data per thingy connection
     *
     * @return mPressureData Map containing timestamps and pressure as a K,V pair
     */
    final Map<String, String> getSavedPressureData() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(mPressureData));
    }

    /**
     * Clears the saved pressure data in the thingy connection
     */
    final void clearSavedPressureData() {
        if (mPressureData != null)
            mPressureData.clear();
    }

    /**
     * Returns the saved humidity data per thingy connection
     *
     * @return mPressureData Map containing timestamps and pressure as a K,V pair
     */
    final Map<String, Integer> getSavedHumidityData() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(mHumidityData));
    }

    /**
     * Clears the saved humidity data in the thingy connection
     */
    final void clearSavedHumidityData() {
        if (mHumidityData != null)
            mHumidityData.clear();
    }

    /**
     * Returns the  temperature interval
     */
    final int getEnvironmentTemperatureInterval() {
        if (mTemperatureInterval == -1) {
            readEnvironmentConfigurationCharacteristic();
        }
        return mTemperatureInterval;
    }

    /**
     * Returns the  pressure interval
     */
    final int getPressureInterval() {
        if (mPressureInterval == -1) {
            readEnvironmentConfigurationCharacteristic();
        }
        return mPressureInterval;
    }

    /**
     * Returns the  humidity interval
     */
    final int getHumidityInterval() {
        if (mHumidityInterval == -1) {
            readEnvironmentConfigurationCharacteristic();
        }
        return mHumidityInterval;
    }

    /**
     * Returns the  color intensity interval
     */
    final int getColorIntensityInterval() {
        if (mColorIntensityInterval == -1) {
            readEnvironmentConfigurationCharacteristic();
        }
        return mColorIntensityInterval;
    }

    /**
     * Returns the  gas mode
     */
    final int getGasMode() {
        if (mGasMode == 0) {
            readEnvironmentConfigurationCharacteristic();
        }
        return mGasMode;
    }

    /**
     * Reads the motion configuration characteristic for a particular thingy
     */
    private void readMotionConfigurationCharacteristic() {
        if (mMotionConfigurationCharacteristic != null) {
            final BluetoothGattCharacteristic characteristic = mMotionConfigurationCharacteristic;
            mPedometerInterval = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            mMotionTemperatureInterval = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
            mCompassInterval = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
            mMotionIntervalFrequency = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
            mWakeOnMotion = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 8) > 0;
        }
    }

    /**
     * Configures the Motion configuration characteristic for a particular thingy
     *
     * @param pedometerInterval                in ms
     * @param temperatureCompensationInterval  in ms
     * @param magnetoMeterCompensationInterval in ms
     * @param motionInterval                   in ms
     * @param wakeOnMotion                     as an interval in ms
     */
    final boolean setMotionConfigurationCharacteristic(final int pedometerInterval,
                                                       final int temperatureCompensationInterval,
                                                       final int magnetoMeterCompensationInterval,
                                                       final int motionInterval, final int wakeOnMotion) {
        if (mMotionConfigurationCharacteristic != null) {
            if ((pedometerInterval < ThingyUtils.PEDOMETER_MIN_INTERVAL || pedometerInterval > ThingyUtils.NOTIFICATION_MAX_INTERVAL)) {
                Log.v(ThingyUtils.TAG, "Invalid pedometer interval");
                return false;
            }

            if ((temperatureCompensationInterval < ThingyUtils.TEMP_MIN_INTERVAL || temperatureCompensationInterval > ThingyUtils.NOTIFICATION_MAX_INTERVAL)) {
                Log.v(ThingyUtils.TAG, "Invalid temperature compensation interval");
                return false;
            }

            if ((magnetoMeterCompensationInterval < ThingyUtils.COMPASS_MIN_INTERVAL || magnetoMeterCompensationInterval > ThingyUtils.NOTIFICATION_MAX_INTERVAL)) {
                Log.v(ThingyUtils.TAG, "Invalid magnetometer compensation interval");
                return false;
            }

            if (motionInterval < ThingyUtils.MPU_FREQ_MIN_INTERVAL || motionInterval > ThingyUtils.MPU_FREQ_MAX_INTERVAL) {
                Log.v(ThingyUtils.TAG, "Invalid motion processing frequency");
                return false;
            }

            if (wakeOnMotion != ThingyUtils.WAKE_ON_MOTION_ON && wakeOnMotion != ThingyUtils.WAKE_ON_MOTION_OFF) {
                Log.v(ThingyUtils.TAG, "Invalid mode for wake on motion");
                return false;
            }

            mPedometerInterval = pedometerInterval;
            mMotionTemperatureInterval = temperatureCompensationInterval;
            mCompassInterval = magnetoMeterCompensationInterval;
            mMotionIntervalFrequency = motionInterval;
            mWakeOnMotion = wakeOnMotion > 0;

            final byte[] data = new byte[9];

            ThingyUtils.setValue(data, 0, pedometerInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, temperatureCompensationInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, magnetoMeterCompensationInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 6, motionInterval, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 8, wakeOnMotion > 0 ? 1 : 0, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mMotionConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }

        return false;
    }

    /**
     * Configure the pedometer intervals
     *
     * @param interval in ms
     */
    final boolean setPedometerInterval(final int interval) {
        if ((interval < ThingyUtils.PEDOMETER_MIN_INTERVAL || interval > ThingyUtils.NOTIFICATION_MAX_INTERVAL)) {
            Log.v(ThingyUtils.TAG, "Invalid pedometer interval");
            return false;
        }

        if (mMotionConfigurationCharacteristic != null) {
            mPedometerInterval = interval;

            final byte[] data = new byte[9];
            ThingyUtils.setValue(data, 0, interval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 2, mMotionTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 4, mCompassInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 6, mMotionIntervalFrequency, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 8, mWakeOnMotion ? 1 : 0, BluetoothGattCharacteristic.FORMAT_UINT8);

            add(RequestType.WRITE_CHARACTERISTIC, mMotionConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }
        return false;
    }

    /**
     * Configure pressure intervals
     *
     * @param interval in ms
     */
    final boolean setTemperatureCompensationInterval(final int interval) {
        if ((interval < ThingyUtils.TEMP_MIN_INTERVAL || interval > ThingyUtils.NOTIFICATION_MAX_INTERVAL)) {
            Log.v(ThingyUtils.TAG, "Invalid temperature compensation interval");
            return false;
        }

        if (mMotionConfigurationCharacteristic != null) {
            mMotionTemperatureInterval = interval;

            final byte[] data = new byte[9];
            ThingyUtils.setValue(data, 0, mPedometerInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 2, interval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 4, mCompassInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 6, mMotionIntervalFrequency, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 8, mWakeOnMotion ? 1 : 0, BluetoothGattCharacteristic.FORMAT_UINT8);

            add(RequestType.WRITE_CHARACTERISTIC, mMotionConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }
        return false;
    }

    /**
     * Configure the humidity intervals
     *
     * @param interval in ms
     */
    final boolean setMagnetometerCompensationInterval(final int interval) {
        if ((interval < ThingyUtils.COMPASS_MIN_INTERVAL || interval > ThingyUtils.NOTIFICATION_MAX_INTERVAL)) {
            Log.v(ThingyUtils.TAG, "Invalid magnetometer compensation interval");
            return false;
        }

        if (mMotionConfigurationCharacteristic != null) {
            mCompassInterval = interval;
            final byte[] data = new byte[9];
            ThingyUtils.setValue(data, 0, mPedometerInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 2, mMotionTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 4, interval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 6, mMotionIntervalFrequency, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 8, mWakeOnMotion ? 1 : 0, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mMotionConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }
        return false;
    }

    /**
     * Configure color intensity intervals
     *
     * @param interval in ms
     */
    final boolean setMotionProcessingFrequency(final int interval) {
        if (interval < ThingyUtils.MPU_FREQ_MIN_INTERVAL || interval > ThingyUtils.MPU_FREQ_MAX_INTERVAL) {
            Log.v(ThingyUtils.TAG, "Invalid motion processing frequency");
            return false;
        }

        if (mMotionConfigurationCharacteristic != null) {
            mMotionIntervalFrequency = interval;
            final byte[] data = new byte[9];
            ThingyUtils.setValue(data, 0, mPedometerInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 2, mMotionTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 4, mCompassInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 6, interval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 8, mWakeOnMotion ? 1 : 0, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mMotionConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return true;
        }
        return false;
    }

    /**
     * Configures gas mode
     *
     * @param wakeOnMotion
     */
    final boolean setWakeOnMotion(final int wakeOnMotion) {
        if (wakeOnMotion != ThingyUtils.WAKE_ON_MOTION_ON && wakeOnMotion != ThingyUtils.WAKE_ON_MOTION_OFF) {
            Log.v(ThingyUtils.TAG, "Invalid mode for wake on motion");
            return false;
        }

        if (mMotionConfigurationCharacteristic != null) {
            mWakeOnMotion = wakeOnMotion > 0;
            final byte[] data = new byte[9];
            ThingyUtils.setValue(data, 0, mPedometerInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 2, mMotionTemperatureInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 4, mCompassInterval, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 6, mMotionIntervalFrequency, BluetoothGattCharacteristic.FORMAT_UINT16);

            ThingyUtils.setValue(data, 8, wakeOnMotion, BluetoothGattCharacteristic.FORMAT_UINT8);
            add(RequestType.WRITE_CHARACTERISTIC, mMotionConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            readMotionConfigurationCharacteristic();
            return true;
        }
        return false;
    }

    /**
     * Returns the  pedometer interval
     */
    final int getPedometerInterval() {
        if (mPedometerInterval > -1) {
            readMotionConfigurationCharacteristic();
        }
        return mPedometerInterval;
    }

    /**
     * Returns the temperature interval for the motion sensor
     */
    final int getMotionTemperatureInterval() {
        if (mMotionTemperatureInterval > -1) {
            readMotionConfigurationCharacteristic();
        }
        return mMotionTemperatureInterval;
    }

    /**
     * Returns the compass interval for the thingy
     */
    final int getCompassInterval() {
        if (mCompassInterval > -1) {
            readMotionConfigurationCharacteristic();
        }
        return mCompassInterval;
    }

    /**
     * Returns the motion interval for the thingy
     */
    final int getMotionInterval() {
        if (mMotionIntervalFrequency > -1) {
            readMotionConfigurationCharacteristic();
        }
        return mMotionIntervalFrequency;
    }

    /**
     * Returns the wake on motion state
     */
    final boolean getWakeOnMotionState() {
        readMotionConfigurationCharacteristic();
        return mWakeOnMotion;
    }

    /**
     * Enable notifications for orientation
     *
     * @param enable notifications on/off
     */
    final void enableOrientationNotifications(final boolean enable) {
        if (mOrientationCharacteristic != null) {
            final BluetoothGattDescriptor orientationCharacteristicDescriptor = mOrientationCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(orientationCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, orientationCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(orientationCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, orientationCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for heading
     *
     * @param enable notifications on/off
     */
    final void enableHeadingNotifications(final boolean enable) {
        if (mHeadingCharacteristic != null) {
            final BluetoothGattDescriptor headingCharacteristicDescriptor = mHeadingCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(headingCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, headingCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(headingCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, headingCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for tap
     *
     * @param enable notifications on/off
     */
    final void enableTapNotifications(final boolean enable) {
        if (mTapCharacteristic != null) {
            final BluetoothGattDescriptor tapCharacteristicDescriptor = mTapCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(tapCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, tapCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(tapCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, tapCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for quaternions
     *
     * @param enable notifications on/off
     */
    final void enableQuaternionNotifications(final boolean enable) {
        if (mQuaternionCharacteristic != null) {
            final BluetoothGattDescriptor mQuaternionCharacteristicDescriptor = mQuaternionCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(mQuaternionCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, mQuaternionCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(mQuaternionCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, mQuaternionCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for pedometer
     *
     * @param enable notifications on/off
     */
    final void enablePedometerNotifications(final boolean enable) {
        if (mPedometerCharacteristic != null) {
            final BluetoothGattDescriptor pedometerCharacteriDescriptor = mPedometerCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(pedometerCharacteriDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, pedometerCharacteriDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(pedometerCharacteriDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, pedometerCharacteriDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for gravity vector
     *
     * @param enable notifications on/off
     */
    final void enableGravityVectorNotifications(final boolean enable) {
        if (mGravityVectorCharacteristic != null) {
            final BluetoothGattDescriptor gravityVectorDescriptor = mGravityVectorCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(gravityVectorDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, gravityVectorDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(gravityVectorDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, gravityVectorDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for euler
     *
     * @param enable notifications on/off
     */
    final void enableEulerNotifications(final boolean enable) {
        if (mEulerCharacteristic != null) {
            final BluetoothGattDescriptor eulerDescriptor = mEulerCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(eulerDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, eulerDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(eulerDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, eulerDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for rotation matrix
     *
     * @param enable notifications on/off
     */
    final void enableRotationMatrixNotifications(final boolean enable) {
        if (mRotationMatrixCharacteristic != null) {
            final BluetoothGattDescriptor rotationMatrixDescriptor = mRotationMatrixCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(rotationMatrixDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, rotationMatrixDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(rotationMatrixDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, rotationMatrixDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for raw data
     *
     * @param enable notifications on/off
     */
    final void enableRawDataNotifications(final boolean enable) {
        if (mRawDataCharacteristic != null) {
            final BluetoothGattDescriptor rawDataCharacteristicDescriptor = mRawDataCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(rawDataCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, rawDataCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(rawDataCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, rawDataCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications for UI service
     *
     * @param enable notifications on/off
     */
    final void enableUiNotifications(final boolean enable) {
        enableButtonStateNotification(enable);
    }

    /**
     * get LED mode
     */
    final int getLedMode() {
        if (mLedMode == -1)
            readLedCharacteristic();
        return mLedMode;
    }

    /**
     * set LED mode
     *
     * @param ledColor led mode
     */
    final void setLedMode(final byte[] ledColor) {
        if (mLedCharacteristic != null) {
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, ledColor, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }

    /**
     * Set LED color
     *
     * @param ledConfiguration led color
     */
    final void setLedColor(final byte[] ledConfiguration) {
        if (mLedCharacteristic != null) {
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, ledConfiguration, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            readLedCharacteristic();
        }
    }

    /**
     * Set LED color
     *
     * @param redIntensity   for the led
     * @param greenIntensity for the led
     * @param blueIntensity  for the led
     */
    final void setConstantLedMode(final int redIntensity, final int greenIntensity, final int blueIntensity) {
        if (mLedCharacteristic != null) {
            final byte[] colorData = new byte[4];
            mLedMode = colorData[0] = ThingyUtils.CONSTANT;
            mRedIntensity = redIntensity;
            mGreenIntensity = greenIntensity;
            mBlueIntensity = blueIntensity;
            colorData[1] = (byte) mRedIntensity;
            colorData[2] = (byte) mGreenIntensity;
            colorData[3] = (byte) mBlueIntensity;
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, colorData, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }

    /**
     * Set LED color
     *
     * @param colorIndex for the led
     */
    final void setBreatheLedMode(final int colorIndex, final int intensity, final int delay) {
        if (mLedCharacteristic != null) {
            final byte[] colorData = new byte[5];
            mLedMode = colorData[0] = ThingyUtils.BREATHE;
            mLedColorIndex = colorData[1] = (byte) (colorIndex);
            mLedColorIntensity = colorData[2] = (byte) intensity;
            ThingyUtils.setValue(colorData, 3, delay, BluetoothGattCharacteristic.FORMAT_UINT16);
            mLedBreatheDelay = delay;
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, colorData, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }

    /**
     * Set LED color
     *
     * @param colorIndex for the led
     */
    final void setOneShotLedMode(final int colorIndex, final int intensity) {
        if (mLedCharacteristic != null) {
            final byte[] colorData = new byte[3];
            mLedMode = colorData[0] = ThingyUtils.ONE_SHOT;
            mLedColorIndex = colorData[1] = (byte) colorIndex;
            mLedColorIntensity = colorData[2] = (byte) intensity;
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, colorData, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }

    /**
     * turn off the LED
     */
    /*package access*/
    final void turnOffLed() {
        if (mLedCharacteristic != null) {
            final byte[] colorData = new byte[1];
            mLedMode = colorData[0] = ThingyUtils.OFF;
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, colorData, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }
    }

    /**
     * Get LED color index
     */
    final int getLedColorIndex() {
        return mLedColorIndex;
    }

    /**
     * Get LED color index
     */
    final int getLedRgbIntensity() {
        return Color.rgb(mRedIntensity, mGreenIntensity, mBlueIntensity);
    }

    /**
     * Set LED color intensity
     *
     * @param ledConfiguration led color
     */
    final void setLedColorIntensity(final byte[] ledConfiguration) {
        if (mLedCharacteristic != null) {
            add(RequestType.WRITE_CHARACTERISTIC, mLedCharacteristic, ledConfiguration, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            readLedCharacteristic();
        }
    }

    /**
     * Get LED color intensity
     */
    final int getLedColorIntensity() {
        return mLedColorIntensity;
    }

    /**
     * Get LED color breathe delay
     */
    /*package access*/
    final int getLedColorBreatheDelay() {
        return mLedBreatheDelay;
    }

    /**
     * Enable notifications for button press state
     *
     * @param enable notifications on/off
     */
    final void enableButtonStateNotification(final boolean enable) {
        if (mButtonCharacteristic != null) {
            final BluetoothGattDescriptor buttonCharacteristicDescriptor = mButtonCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(buttonCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, buttonCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(buttonCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, buttonCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Enable notifications in sound service for both Speaker status and microphone notifications
     *
     * @param enable notifications on/off
     */
    final void enableSoundNotifications(final boolean enable) {
        if (!enable) {
            stopPcmSample();
            stopPlayingVoiceInput();
        }
        enableSpeakerStatusNotifications(false);
        enableThingyMicrophoneNotifications(false);
    }

    /**
     * Enable notifications for button press state
     *
     * @param enable notifications on/off
     */
    final void enableSpeakerStatusNotifications(final boolean enable) {
        if (mSpeakerStatusCharacteristic != null) {
            final BluetoothGattDescriptor speakerStatusCharacteristicDescriptor = mSpeakerStatusCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(speakerStatusCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, speakerStatusCharacteristicDescriptor, data);
                }
            } else {
                if (isNotificationsAlreadyEnabled(speakerStatusCharacteristicDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, speakerStatusCharacteristicDescriptor, data);
                }
            }
        }
    }

    /**
     * Reads the sound service configuration characteristic for a particular thingy
     */
    final void readSoundConfigurationCharacteristic() {
        if (mSoundConfigurationCharacteristic != null) {
            final BluetoothGattCharacteristic characteristic = mSoundConfigurationCharacteristic;
            mSpeakerMode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            mMicrophoneMode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            Log.v(TAG, "Sound service configuration read completed");
        }
    }

    /**
     * Play the requested sound sample on a particular thingy
     *
     * @param frequency to be played
     * @param duration  to be played for
     * @param volume    of the sound
     */
    final void playSoundFrequency(final int frequency, final int duration, final int volume) {
        clearQueue();
        mPlayPcmRequested = false;
        if (mSpeakerDataCharacteristic != null) {
            final byte[] data = new byte[5];
            ThingyUtils.setValue(data, 0, frequency, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 2, duration, BluetoothGattCharacteristic.FORMAT_UINT16);
            ThingyUtils.setValue(data, 4, volume, BluetoothGattCharacteristic.FORMAT_UINT8);
            if (mSpeakerMode == ThingyUtils.FREQUENCY_MODE) {
                add(RequestType.WRITE_CHARACTERISTIC, mSpeakerDataCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
                mSpeakerMode = ThingyUtils.FREQUENCY_MODE;
                add(RequestType.WRITE_CHARACTERISTIC, mSoundConfigurationCharacteristic, new byte[]{ThingyUtils.FREQUENCY_MODE, (byte) mMicrophoneMode}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                add(RequestType.WRITE_CHARACTERISTIC, mSpeakerDataCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            }
        }
    }

    /**
     * Play the requested sound sample on a particular thingy
     *
     * @param sample the requested sound sample
     */
    final void playSoundSample(final int sample) {
        clearQueue();
        mPlayPcmRequested = false;
        if (mSpeakerDataCharacteristic != null) {
            if (mSpeakerMode == ThingyUtils.SAMPLE_MODE) {
                add(RequestType.WRITE_CHARACTERISTIC, mSpeakerDataCharacteristic, new byte[]{(byte) sample}, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
                mSpeakerMode = ThingyUtils.SAMPLE_MODE;
                add(RequestType.WRITE_CHARACTERISTIC, mSoundConfigurationCharacteristic, new byte[]{ThingyUtils.SAMPLE_MODE, (byte) mMicrophoneMode}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                add(RequestType.WRITE_CHARACTERISTIC, mSpeakerDataCharacteristic, new byte[]{(byte) sample}, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            }
        }
    }

    /**
     * Play the requested pcm sample on a particular thingy
     *
     * @param sample the requested pcm sample
     */
    final void playPcmSample(final byte[] sample) {
        if (mSpeakerDataCharacteristic != null) {
            mPcmSample = sample;
            if (mSpeakerMode != ThingyUtils.PCM_MODE) {
                final byte[] data = new byte[]{ThingyUtils.PCM_MODE, (byte) mMicrophoneMode};
                add(RequestType.WRITE_CHARACTERISTIC, mSoundConfigurationCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                mPlayPcmRequested = true;
                streamAudio(sample);
            } else {
                mPlayPcmRequested = true;
                streamAudio(sample);
            }
        }
    }

    boolean playPcmAudio(final File file) {
        return readAudioFile(file);
    }

    private boolean readAudioFile(final File file) {
        InputStream is = null;
        try {
            if (!file.getPath().startsWith("content")) {
                is = new FileInputStream(file);
            } else {
                Uri uri = Uri.parse(file.getPath());
                is = mContext.getContentResolver().openInputStream(uri);
            }

            is.skip(44);
            int size = is.available();

            byte[] output = new byte[size / 2];
            byte[] bytes = new byte[1024];
            int length, offset = 0;
            while ((length = is.read(bytes)) > 0) {
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < length; i += 2) {
                    output[offset + i / 2] = (byte) (((bb.getShort() * 128.0) / 32768.0) + 128.0);
                }
                offset += length / 2;
            }

            playPcmSample(output);
            return true;
        } catch (Exception e) {
            ThingyUtils.showToast(mContext, "Unable to stream audio");
            return false;
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

    private void handleAudioRequests() {
        if (mStartPlaying) {
            return;
        }
        mStartPlaying = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mPlayPcmRequested) {
                    while (mPacketCounter < mNumOfAudioChunks) {
                        if (!mWait && !mBufferWarningReceived) {
                            mWait = true;
                            mHandler.post(mProcessNextTask);
                            mPacketCounter++;
                            if (mPacketCounter == 1) {
                                try {
                                    //This sleep is added for Google pixel because the first packet was splitted in to 3 small packets
                                    //until data length extension request was agreed with the device.
                                    Thread.sleep(70);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void broadcastAudioStreamComplete() {
        mNumOfAudioChunks = 0;
        mPacketCounter = 0;
        if (mPlayPcmRequested) {
            sendPcmBroadcast(ThingyUtils.SPEAKER_STATUS_FINISHED);
            mPlayPcmRequested = false;
            mWait = false;
            mBufferWarningReceived = false;
        }
    }

    /**
     * clear the request queue in case the user switches from pcm mode to frequency/sample mode while the audio track is streamingg
     */
    private synchronized void clearQueue() {
        mWait = true;
        if (mPlayPcmRequested) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mQueue.size() > 0) {
                mQueue.clear();
            }
            mPacketCounter = mNumOfAudioChunks;
            broadcastAudioStreamComplete();
        } else if (mPlayVoiceInput) {
            if (mQueue.size() > 0) {
                mQueue.clear();
            }
        }

        mWait = false;
        mStartPlaying = false;
    }

    /**
     * Fills up the request queue to startHandlingAudioRequestPackets streaming audio packets
     */
    private void streamAudio(final byte[] sample) {
        int index = 0;
        int offset = 0;
        int length;
        int mChunkSize;
        if (mMtu > ThingyUtils.MAX_MTU_SIZE_PRE_LOLLIPOP) {
            mChunkSize = ThingyUtils.MAX_AUDIO_PACKET_SIZE;
        } else {
            mChunkSize = ThingyUtils.MAX_MTU_SIZE_PRE_LOLLIPOP;
        }

        mNumOfAudioChunks = (int) Math.ceil((double) sample.length / mChunkSize);
        while (index < mNumOfAudioChunks) {
            length = Math.min(mPcmSample.length - offset, mChunkSize);
            final byte[] audio = new byte[length];
            System.arraycopy(mPcmSample, offset, audio, 0, length);
            add(RequestType.WRITE_CHARACTERISTIC, mSpeakerDataCharacteristic, audio, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            index++;
            offset += length;
        }
    }

    /**
     * Play the requested pcm sample on a particular thingy
     */
    final void stopPcmSample() {
        clearQueue();
        mPlayPcmRequested = false;
    }

    /**
     * Play the requested pcm sample on a particular thingy
     *
     * @param sample the requested pcm sample
     */
    public final void playVoiceInput(final byte[] sample) {
        if (mSpeakerDataCharacteristic != null) {
            mPlayVoiceInput = true;
            mPcmSample = sample;
            if (mSpeakerMode != ThingyUtils.PCM_MODE) {
                add(RequestType.WRITE_CHARACTERISTIC, mSoundConfigurationCharacteristic, new byte[]{ThingyUtils.PCM_MODE, (byte) mMicrophoneMode}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                streamAudio(sample);
            } else {
                streamAudio(sample);
            }
        }
    }

    /**
     * Play the requested pcm sample on a particular thingy
     */
    final void stopPlayingVoiceInput() {
        if (mSpeakerDataCharacteristic != null) {
            clearQueue();
            mPlayVoiceInput = false;
        }
    }

    /**
     * Requests the mtu to be changed to the desired max transmission unit
     */
    public void requestMtu() {
        mMtuHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    if (ThingyUtils.checkIfVersionIsLollipopOrAbove()) {
                        if (mMtu != mtu) {
                            boolean isMtuRequestSuccess = mBluetoothGatt.requestMtu(mtu);
                            if (!isMtuRequestSuccess) {
                                Log.v(ThingyUtils.TAG, "MTU request failed");
                            }
                        }
                    } else {
                        mMtu = ThingyUtils.MAX_MTU_SIZE_PRE_LOLLIPOP;
                    }
                }
            }
        }, 1000);
    }

    @SuppressWarnings("SameParameterValue")
    private void sendPcmBroadcast(final int status) {
        final Intent intent = new Intent(ThingyUtils.EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION);
        intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
        intent.putExtra(ThingyUtils.EXTRA_DATA_SPEAKER_MODE, mSpeakerMode);
        intent.putExtra(ThingyUtils.EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION, status);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Enable microphone on a particular thingy
     *
     * @param enable the requested pcm sample
     */
    final void enableThingyMicrophoneNotifications(final boolean enable) {
        if (mMicrophoneCharacteristic != null) {
            final BluetoothGattDescriptor microphoneDescriptor = mMicrophoneCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            if (enable) {
                if (!isNotificationsAlreadyEnabled(microphoneDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, microphoneDescriptor, data);
                }
                enableAdpcmMode(enable);
            } else {
                if (isNotificationsAlreadyEnabled(microphoneDescriptor)) {
                    byte[] data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    add(RequestType.WRITE_DESCRIPTOR, microphoneDescriptor, data);
                }
                mEnableThingyMicrophone = enable;
            }
        }
    }

    /**
     * Enable ADPCM mode on the microphone on a particular thingy
     */
    private void enableAdpcmMode(final boolean enable) {
        if (mMicrophoneCharacteristic != null) {
            mEnableThingyMicrophone = enable;
            if (mMicrophoneMode != ThingyUtils.ADPCM_MODE) {
                mMicrophoneMode = ThingyUtils.ADPCM_MODE;
                add(RequestType.WRITE_CHARACTERISTIC, mSoundConfigurationCharacteristic, new byte[]{ThingyUtils.ADPCM_MODE, (byte) mSpeakerMode}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }

            int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
            mAudioTrack.play();

            mAdpcmDecoder = new ADPCMDecoder(mContext, false);
            mAdpcmDecoder.setListener(new ADPCMDecoder.DecoderListener() {
                @Override
                public void onFrameDecoded(byte[] pcm, int frameNumber) {
                    if (mEnableThingyMicrophone && mAudioTrack != null) {
                        final int status = mAudioTrack.write(pcm, 0, pcm.length/*, AudioTrack.WRITE_NON_BLOCKING*/);

                        if (status == AudioTrack.ERROR_INVALID_OPERATION || status == AudioTrack.ERROR_BAD_VALUE
                                || status == AudioTrack.ERROR_DEAD_OBJECT || status == AudioTrack.ERROR) {
                            ThingyUtils.showToast(mContext, "Error: " + status);
                            mEnableThingyMicrophone = false;
                            mAudioTrack.stop();
                            mAudioTrack.release();
                            mAudioTrack = null;
                            mAdpcmDecoder = null;
                        } else {
                            sendMicrophoneBroadcast(pcm, status);
                        }
                    } else {
                        mAudioTrack.stop();
                        mAudioTrack.release();
                        mAudioTrack = null;
                        mAdpcmDecoder = null;
                    }
                }
            });
        }
    }

    private void sendMicrophoneBroadcast(final byte[] data, final int status) {
        final Intent intent = new Intent(ThingyUtils.MICROPHONE_NOTIFICATION);
        intent.putExtra(ThingyUtils.EXTRA_DEVICE, mBluetoothDevice);
        intent.putExtra(ThingyUtils.EXTRA_DATA_PCM, data);
        intent.putExtra(ThingyUtils.EXTRA_DATA, status);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Check if device supports Dfu without bond sharing
     */
    public boolean checkIfDfuWithoutBondSharingIsSupported() {
        final List<BluetoothGattCharacteristic> characteristics = mButtonLessDfuService.getCharacteristics();

        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (characteristic.getUuid().equals(ThingyUtils.DFU_CONTROL_POINT_CHARACTERISTIC_WITHOUT_BOND_SHARING)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trigger bootloader mode
     */
    final boolean triggerBootLoaderMode() {
        if (mDfuControlPointCharacteristic != null) {
            final BluetoothGattDescriptor dfuCharacteristicDescriptor = mDfuControlPointCharacteristic.getDescriptor(ThingyUtils.CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR);
            add(RequestType.WRITE_DESCRIPTOR, dfuCharacteristicDescriptor, new byte[]{0x01, 0x00});
            return true;
        }
        return false;
    }

    final boolean isInBootloaderMode() {
        if (mButtonLessDfuService != null) {
            return mButtonLessDfuService.getCharacteristics().size() == 2;
        }
        return false;
    }

    /**
     * Runnable used to push processing ble requests to the a ui thread.
     * This is due to samsung galaxy devices and HUAWEI nexus 6P had a synchronizing issue
     * when process next was called from the same thread as the callbacks which was noticed during audio streaming
     */
    private Runnable mProcessNextTask = new Runnable() {
        @Override
        public void run() {
            processNext();
        }
    };

    /**
     * BluetoothGatt request types.
     */
    private enum RequestType {
        READ_CHARACTERISTIC,
        READ_DESCRIPTOR,
        WRITE_CHARACTERISTIC,
        WRITE_DESCRIPTOR
    }

    /**
     * Add descriptor read requests
     *
     * @param type       READ_DESCRIPTOR
     * @param descriptor descriptor to be read
     */
    @SuppressWarnings("SameParameterValue")
    private void add(RequestType type, BluetoothGattDescriptor descriptor) {
        Request request = new Request(type, descriptor);
        add(request);
    }

    /**
     * Add descriptor write requests
     *
     * @param type       READ_DESCRIPTOR
     * @param descriptor descriptor to be read
     * @param data       to be written to the descriptor
     */
    @SuppressWarnings("SameParameterValue")
    private void add(RequestType type, BluetoothGattDescriptor descriptor, byte[] data) {
        Request request = new Request(type, descriptor, data);
        add(request);
    }

    /**
     * Add characteristic write requests
     *
     * @param type           WRITE_CHARACTERISTIC
     * @param characteristic to be written to
     * @param data           to be written to the characteristic
     * @param writeType      for the characteristic
     */
    @SuppressWarnings("SameParameterValue")
    private void add(RequestType type, BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
        Request request = new Request(type, characteristic, data, writeType);
        add(request);
    }

    /**
     * Add characteristic read requests
     *
     * @param type           READ_DESCRIPTOR
     * @param characteristic to be read
     */
    @SuppressWarnings("SameParameterValue")
    private void add(RequestType type, BluetoothGattCharacteristic characteristic) {
        Request request = new Request(type, characteristic);
        add(request);
    }

    synchronized private void add(Request request) {
        mQueue.add(request);
        if (mQueue.size() == 1) {
            mQueue.peek().start(mBluetoothGatt);
        }
    }

    /**
     * Process the next request in the queue for a BluetoothGatt function (such as characteristic read).
     */
    synchronized private void processNext() {
        // The currently executing request is kept on the head of the queue until this is called.
        if (mQueue.isEmpty()) {
            return;
        }
        //in case buffer warning is received during audio streaming the request will not be removed from the queue will be sent again
        if (!mBufferWarningReceived) {
            mQueue.remove();
        }

        if (!mQueue.isEmpty() && !mBufferWarningReceived) {
            mQueue.peek().start(mBluetoothGatt);
        }
    }

    /**
     * The object that holds a Gatt request while in the queue.
     * <br>
     * This object holds the parameters for calling BluetoothGatt methods (see startHandlingAudioRequestPackets());
     */
    private final class Request {
        final RequestType requestType;
        BluetoothGattCharacteristic characteristic;
        BluetoothGattDescriptor descriptor;
        byte[] data;
        int writeType;

        Request(RequestType requestType, BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
            this.requestType = requestType;
            this.characteristic = characteristic;
            this.data = data;
            this.writeType = writeType;
        }

        Request(RequestType requestType, BluetoothGattCharacteristic characteristic) {
            this.requestType = requestType;
            this.characteristic = characteristic;
            this.data = null;
            this.writeType = 0;
        }

        Request(RequestType requestType, BluetoothGattDescriptor descriptor, byte[] data) {
            this.requestType = requestType;
            this.descriptor = descriptor;
            this.data = data;
            this.writeType = 0;
        }

        Request(RequestType requestType, BluetoothGattDescriptor descriptor) {
            this.requestType = requestType;
            this.descriptor = descriptor;
            this.data = null;
            this.writeType = 0;
        }

        void start(BluetoothGatt bluetoothGatt) {
            switch (requestType) {
                case READ_CHARACTERISTIC:
                    if (!bluetoothGatt.readCharacteristic(characteristic)) {
                        throw new IllegalArgumentException("Characteristic is not valid: " + characteristic.getUuid().toString());
                    }
                    break;
                case READ_DESCRIPTOR:
                    if (!bluetoothGatt.readDescriptor(descriptor)) {
                        throw new IllegalArgumentException("Descriptor is not valid");
                    }
                    break;
                case WRITE_CHARACTERISTIC:
                    characteristic.setValue(data);
                    characteristic.setWriteType(writeType);
                    if (!bluetoothGatt.writeCharacteristic(characteristic)) {
                        throw new IllegalArgumentException("Characteristic is not valid");
                    }
                    break;
                case WRITE_DESCRIPTOR:
                    descriptor.setValue(data);
                    if (!bluetoothGatt.writeDescriptor(descriptor)) {
                        throw new IllegalArgumentException("Descriptor is not valid");
                    }
                    break;
            }
        }
    }
}