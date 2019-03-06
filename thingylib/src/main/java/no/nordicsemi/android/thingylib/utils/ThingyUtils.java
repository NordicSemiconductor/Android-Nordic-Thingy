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

package no.nordicsemi.android.thingylib.utils;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ThingyUtils {

    public static final String TAG                                                              = "THINGY:52";

    public static final UUID THINGY_BASE_UUID                                                   = new UUID(0xEF6801009B354933L, 0x9B1052FFA9740042L);
    public static final UUID THINGY_CONFIGURATION_SERVICE                                       = new UUID(0xEF6801009B354933L, 0x9B1052FFA9740042L);
    public static final UUID DEVICE_NAME_CHARACTERISTIC_UUID                                    = new UUID(0xEF6801019B354933L, 0x9B1052FFA9740042L);
    public static final UUID ADVERTISING_PARAM_CHARACTERISTIC_UUID                              = new UUID(0xEF6801029B354933L, 0x9B1052FFA9740042L);
    public static final UUID APPEARANCE_CHARACTERISTIC_UUID                                     = new UUID(0xEF6801039B354933L, 0x9B1052FFA9740042L);
    public static final UUID CONNECTION_PARAM_CHARACTERISTIC_UUID                               = new UUID(0xEF6801049B354933L, 0x9B1052FFA9740042L);
    public static final UUID EDDYSTONE_URL_CHARACTERISTIC_UUID                                  = new UUID(0xEF6801059B354933L, 0x9B1052FFA9740042L);
    public static final UUID CLOUD_TOKEN_CHARACTERISTIC_UUID                                    = new UUID(0xEF6801069B354933L, 0x9B1052FFA9740042L);
    public static final UUID FIRMWARE_VERSION_CHARACTERISTIC_UUID                               = new UUID(0xEF6801079B354933L, 0x9B1052FFA9740042L);
    public static final UUID MTU_CHARACERISTIC_UUID                                             = new UUID(0xEF6801089B354933L, 0x9B1052FFA9740042L);
    public static final UUID NFC_CHARACTERISTIC_UUID                                            = new UUID(0xEF6801099B354933L, 0x9B1052FFA9740042L);

    public static final UUID BATTERY_SERVICE                                                    = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_SERVICE_CHARACTERISTIC                                     = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    public static final UUID THINGY_ENVIRONMENTAL_SERVICE                                       = new UUID(0xEF6802009B354933L, 0x9B1052FFA9740042L);
    public static final UUID TEMPERATURE_CHARACTERISTIC                                         = new UUID(0xEF6802019B354933L, 0x9B1052FFA9740042L);
    public static final UUID PRESSURE_CHARACTERISTIC                                            = new UUID(0xEF6802029B354933L, 0x9B1052FFA9740042L);
    public static final UUID HUMIDITY_CHARACTERISTIC                                            = new UUID(0xEF6802039B354933L, 0x9B1052FFA9740042L);
    public static final UUID AIR_QUALITY_CHARACTERISTIC                                         = new UUID(0xEF6802049B354933L, 0x9B1052FFA9740042L);
    public static final UUID COLOR_CHARACTERISTIC                                               = new UUID(0xEF6802059B354933L, 0x9B1052FFA9740042L);
    public static final UUID CONFIGURATION_CHARACTERISTIC                                       = new UUID(0xEF6802069B354933L, 0x9B1052FFA9740042L);

    public static final UUID THINGY_UI_SERVICE                                                  = new UUID(0xEF6803009B354933L, 0x9B1052FFA9740042L);
    public static final UUID LED_CHARACTERISTIC                                                 = new UUID(0xEF6803019B354933L, 0x9B1052FFA9740042L);
    public static final UUID BUTTON_CHARACTERISTIC                                              = new UUID(0xEF6803029B354933L, 0x9B1052FFA9740042L);

    public static final UUID THINGY_MOTION_SERVICE                                              = new UUID(0xEF6804009B354933L, 0x9B1052FFA9740042L);
    public static final UUID THINGY_MOTION_CONFIGURATION_CHARACTERISTIC                         = new UUID(0xEF6804019B354933L, 0x9B1052FFA9740042L);
    public static final UUID TAP_CHARACTERISTIC                                                 = new UUID(0xEF6804029B354933L, 0x9B1052FFA9740042L);
    public static final UUID ORIENTATION_CHARACTERISTIC                                         = new UUID(0xEF6804039B354933L, 0x9B1052FFA9740042L);
    public static final UUID QUATERNION_CHARACTERISTIC                                          = new UUID(0xEF6804049B354933L, 0x9B1052FFA9740042L);
    public static final UUID PEDOMETER_CHARACTERISTIC                                           = new UUID(0xEF6804059B354933L, 0x9B1052FFA9740042L);
    public static final UUID RAW_DATA_CHARACTERISTIC                                            = new UUID(0xEF6804069B354933L, 0x9B1052FFA9740042L);
    public static final UUID EULER_CHARACTERISTIC                                               = new UUID(0xEF6804079B354933L, 0x9B1052FFA9740042L);
    public static final UUID ROTATION_MATRIX_CHARACTERISTIC                                     = new UUID(0xEF6804089B354933L, 0x9B1052FFA9740042L);
    public static final UUID HEADING_CHARACTERISTIC                                             = new UUID(0xEF6804099B354933L, 0x9B1052FFA9740042L);
    public static final UUID GRAVITY_VECTOR_CHARACTERISTIC                                      = new UUID(0xEF68040A9B354933L, 0x9B1052FFA9740042L);

    public static final UUID THINGY_SOUND_SERVICE                                               = new UUID(0xEF6805009B354933L, 0x9B1052FFA9740042L);
    public static final UUID THINGY_SOUND_CONFIG_CHARACTERISTIC                                 = new UUID(0xEF6805019B354933L, 0x9B1052FFA9740042L);
    public static final UUID THINGY_SPEAKER_DATA_CHARACTERISTIC                                 = new UUID(0xEF6805029B354933L, 0x9B1052FFA9740042L);
    public static final UUID THINGY_SPEAKER_STATUS_CHARACTERISTIC                               = new UUID(0xEF6805039B354933L, 0x9B1052FFA9740042L);
    public static final UUID THINGY_MICROPHONE_CHARACTERISTIC                                   = new UUID(0xEF6805049B354933L, 0x9B1052FFA9740042L);

    public static final UUID CLIENT_CHARACTERISTIC_CONFIGURATOIN_DESCRIPTOR                     = new UUID(0x0000290200001000L, 0x800000805f9B34FBL);

    public final static ParcelUuid PARCEL_SECURE_DFU_SERVICE                                    = ParcelUuid.fromString("0000FE59-0000-1000-8000-00805F9B34FB");
    public final static UUID SECURE_DFU_SERVICE                                                 = UUID.fromString("0000FE59-0000-1000-8000-00805F9B34FB");
    public static final UUID THINGY_BUTTONLESS_DFU_SERVICE                                      = new UUID(0x8E400001F3154F60L, 0x9FB8838830DAEA50L);
    public static final UUID DFU_DEFAULT_CONTROL_POINT_CHARACTERISTIC                           = new UUID(0x8EC90001F3154F60L, 0x9FB8838830DAEA50L);
    public static final UUID DFU_CONTROL_POINT_CHARACTERISTIC_WITHOUT_BOND_SHARING              = new UUID(0x8EC90003F3154F60L, 0x9FB8838830DAEA50L);

    public static final String ACTION_DEVICE_CONNECTED                                          = "ACTION_DEVICE_CONNECTED_";
    public static final String ACTION_DEVICE_DISCONNECTED                                       = "ACTION_DEVICE_DISCONNECTED_";
    public static final String ACTION_SERVICE_DISCOVERY_COMPLETED                               = "ACTION_SERVICE_DISCOVERY_COMPLETED_";
    public static final String ACTION_DATA_RECEIVED                                             = "ACTION_DATA_RECEIVED_";
    public static final String CONNECTION_STATE                                                 = "READING_CONFIGURATION";

    private static final String ACTION_ENVIRONMENT_CONFIGRATION_READ_COMPLETE                   = "ACTION_ENVIRONMENT_CONFIGRATION_READ_COMPLETE_";
    public static final String EXTRA_DATA_TEMPERATURE_INTERVAL                                  = "EXTRA_DATA_TEMPERATURE_INTERVAL";
    public static final String EXTRA_DATA_PRESSURE_INTERVAL                                     = "EXTRA_DATA_PRESSURE_INTERVAL";
    public static final String EXTRA_DATA_HUMIDITY_INTERVAL                                     = "EXTRA_DATA_HUMIDITY_INTERVAL";
    public static final String EXTRA_DATA_GAS_MODE                                              = "EXTRA_DATA_GAS_MODE";
    public static final String EXTRA_DATA_PRESSURE_MODE                                         = "EXTRA_DATA_PRESSURE_MODE";

    public static final String ACTION_MOTION_CONFIGRATION_READ                                  = "ACTION_MOTION_CONFIGRATION_READ";

    public static final String EXTRA_DEVICE                                                     = "EXTRA_DEVICE";
    public static final String EXTRA_DEVICE_NAME                                                = "EXTRA_DEVICE_NAME";
    public static final String EXTRA_DEVICE_ADDRESS                                             = "EXTRA_DEVICE_ADDRESS";

    public static final String BATTERY_LEVEL_NOTIFICATION                                       = "BATTERY_LEVEL_NOTIFICATION_";

    public static final String TEMPERATURE_NOTIFICATION                                         = "TEMPERATURE_NOTIFICATION_";
    public static final String PRESSURE_NOTIFICATION                                            = "PRESSURE_NOTIFICATION_";
    public static final String HUMIDITY_NOTIFICATION                                            = "HUMIDITY_NOTIFICATION_";
    public static final String AIR_QUALITY_NOTIFICATION                                         = "AIR_QUALITY_NOTIFICATION_";
    public static final String COLOR_NOTIFICATION                                               = "COLOR_NOTIFICATION_";
    public static final String CONFIGURATION_DATA                                               = "CONFIGURATION_DATA_";

    public static final String BUTTON_STATE_NOTIFICATION                                        = "BUTTON_STATE_NOTIFICATION_";

    public static final String TAP_NOTIFICATION                                                 = "TAP_NOTIFICATION_";
    public static final String ORIENTATION_NOTIFICATION                                         = "ORIENTATION_NOTIFICATION_";
    public static final String QUATERNION_NOTIFICATION                                          = "QUATERNION_NOTIFICATION_";
    public static final String PEDOMETER_NOTIFICATION                                           = "PEDOMETER_NOTIFICATION_";
    public static final String RAW_DATA_NOTIFICATION                                            = "RAW_DATA_NOTIFICATION_";
    public static final String EULER_NOTIFICATION                                               = "EULER_NOTIFICATION_";
    public static final String ROTATION_MATRIX_NOTIFICATION                                     = "ROTATION_MATRIX_NOTIFICATION_";
    public static final String HEADING_NOTIFICATION                                             = "HEADING_NOTIFICATION_";
    public static final String GRAVITY_NOTIFICATION                                             = "GRAVITY_NOTIFICATION_";

    public static final String SPEAKER_STATUS_NOTIFICATION                                      = "SPEAKER_STATUS_NOTIFICATION";
    public static final String MICROPHONE_NOTIFICATION                                          = "MICROPHONE_NOTIFICATION";

    public static final String EXTRA_DATA                                                       = "EXTRA_DATA";
    public static final String EXTRA_DATA_TIME_STAMP                                            = "EXTRA_DATA_TIME_STAMP";
    public static final String EXTRA_DATA_ECO2                                                  = "EXTRA_DATA_ECO2";
    public static final String EXTRA_DATA_TVOC                                                  = "EXTRA_DATA_TVOC";

    public static final String EXTRA_DATA_RED                                                   = "EXTRA_DATA_RED";
    public static final String EXTRA_DATA_BLUE                                                  = "EXTRA_DATA_BLUE";
    public static final String EXTRA_DATA_GREEN                                                 = "EXTRA_DATA_GREEN";
    public static final String EXTRA_DATA_CLEAR                                                 = "EXTRA_DATA_CLEAR";
    public static final String EXTRA_DATA_BUTTON                                                = "EXTRA_DATA_BUTTON";

    public static final String EXTRA_DATA_TAP_COUNT                                             = "EXTRA_DATA_TAP_COUNT";
    public static final String EXTRA_DATA_TAP_DIRECTION                                         = "EXTRA_DATA_TAP_DIRECTION";

    public static final String EXTRA_DATA_QUATERNION_W                                          = "EXTRA_DATA_QUATERNION_W";
    public static final String EXTRA_DATA_QUATERNION_X                                          = "EXTRA_DATA_QUATERNION_X";
    public static final String EXTRA_DATA_QUATERNION_Y                                          = "EXTRA_DATA_QUATERNION_Y";
    public static final String EXTRA_DATA_QUATERNION_Z                                          = "EXTRA_QUATERNION_Z";

    public static final String EXTRA_DATA_STEP_COUNT                                            = "EXTRA_DATA_STEP_COUNT";
    public static final String EXTRA_DATA_DURATION                                              = "EXTRA_DATA_DURATION";

    public static final String EXTRA_DATA_ACCELEROMETER_X                                       = "EXTRA_DATA_ACCELEROMETER_X";
    public static final String EXTRA_DATA_ACCELEROMETER_Y                                       = "EXTRA_DATA_ACCELEROMETER_Y";
    public static final String EXTRA_DATA_ACCELEROMETER_Z                                       = "EXTRA_DATA_ACCELEROMETER_Z";

    public static final String EXTRA_DATA_GYROSCOPE_X                                           = "EXTRA_DATA_DATA_GYROSCOPE_X";
    public static final String EXTRA_DATA_GYROSCOPE_Y                                           = "EXTRA_DATA_GYROSCOPE_Y";
    public static final String EXTRA_DATA_GYROSCOPE_Z                                           = "EXTRA_DATA_GYROSCOPE_Z";

    public static final String EXTRA_DATA_PITCH                                                 = "EXTRA_DATA_PITCH";
    public static final String EXTRA_DATA_ROLL                                                  = "EXTRA_DATA_ROLL";
    public static final String EXTRA_DATA_YAW                                                   = "EXTRA_DATA_YAW";

    public static final String EXTRA_DATA_GRAVITY_X                                             = "EXTRA_DATA_GRAVITY_X";
    public static final String EXTRA_DATA_GRAVITY_Y                                             = "EXTRA_DATA_GRAVITY_Y";
    public static final String EXTRA_DATA_GRAVITY_Z                                             = "EXTRA_DATA_GRAVITY_Z";

    public static final String EXTRA_DATA_COMPASS_X                                             = "EXTRA_DATA_COMPASS_X";
    public static final String EXTRA_DATA_COMPASS_Y                                             = "EXTRA_DATA_COMPASS_Y";
    public static final String EXTRA_DATA_COMPASS_Z                                             = "EXTRA_DATA_COMPASS_Z";

    public static final String EXTRA_DATA_ROTATION_MATRIX                                       = "EXTRA_DATA_COMPASS_Z";

    public static final String EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION                           = "EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION";
    public static final String EXTRA_DATA_SPEAKER_MODE                                          = "EXTRA_DATA_SPEAKER_MODE";

    public static final String EXTRA_DATA_MICROPHONE_NOTIFICATION                               = "EXTRA_DATA_MICROPHONE_NOTIFICATION";
    public static final String EXTRA_DATA_PCM                                                   = "EXTRA_DATA_PCM";

    public static final String INITIAL_CONFIG_FROM_ACTIVITY                                     = "INITIAL_CONFIG_FROM_ACTIVITY";

    public static final int SAMPLE_1                                                            = 0;
    public static final int SAMPLE_2                                                            = 1;
    public static final int SAMPLE_3                                                            = 2;
    public static final int SAMPLE_4                                                            = 3;
    public static final int SAMPLE_5                                                            = 4;
    public static final int SAMPLE_6                                                            = 5;
    public static final int SAMPLE_7                                                            = 6;
    public static final int SAMPLE_8                                                            = 7;
    public static final int SAMPLE_9                                                            = 8;

    public static final int BUTTON_STATE_RELEASED                                               = 0x00;
    public static final int BUTTON_STATE_PRESSED                                                = 0x01;

    public static final byte OFF                                                                = 0x00;
    public static final byte CONSTANT                                                           = 0x01;
    public static final byte BREATHE                                                            = 0x02;
    public static final byte ONE_SHOT                                                           = 0x03;

    public static final int LED_RED                                                             = 0x01;
    public static final int LED_GREEN                                                           = 0x02;
    public static final int LED_YELLOW                                                          = 0x03;
    public static final int LED_BLUE                                                            = 0x04;
    public static final int LED_PURPLE                                                          = 0x05;
    public static final int LED_CYAN                                                            = 0x06;
    public static final int LED_WHITE                                                           = 0x07;

    public static final int DEFAULT_RED_INTENSITY                                               = 0;
    public static final int DEFAULT_GREEN_INTENSITY                                             = 255;
    public static final int DEFAULT_BLUE_INTENSITY                                              = 255;

    public static final int DEFAULT_RED_CALIBRATION_INTENSITY                                   = 103;
    public static final int DEFAULT_GREEN_CALIBRATION_INTENSITY                                 = 78;
    public static final int DEFAULT_BLUE_CALIBRATION_INTENSITY                                  = 29;

    public static final int DEFAULT_BREATHE_INTERVAL                                            = 3500;
    public static final int DEFAULT_MINIMUM_BREATHE_INTERVAL                                    = 50; //ms
    public static final int DEFAULT_MAXIMUM_BREATHE_INTERVAL                                    = 10000; //ms

    public static final int DEFAULT_LED_INTENSITY                                               = 20;
    public static final int DEFAULT_MINIMUM_LED_INTENSITY                                       = 1;
    public static final int DEFAULT_MAXIIMUM_LED_INTENSITY                                      = 100;

    public static final int DEFAULT_LED_COLOR                                                   = Color.CYAN;

    public static final int TAP_X_UP                                                            = 0x01;
    public static final int TAP_X_DOWN                                                          = 0x02;
    public static final int TAP_Y_UP                                                            = 0x03;
    public static final int TAP_Y_DOWN                                                          = 0x04;
    public static final int TAP_Z_UP                                                            = 0x05;
    public static final int TAP_Z_DOWN                                                          = 0x06;

    public static final String X_UP                                                             = "FRONT";
    public static final String X_DOWN                                                           = "BACK";
    public static final String Y_UP                                                             = "RIGHT";
    public static final String Y_DOWN                                                           = "LEFT";
    public static final String Z_UP                                                             = "TOP";
    public static final String Z_DOWN                                                           = "BOTTOM";

    public static final int PORTRAIT_TYPE                                                       = 0x00;
    public static final int LANDSCAPE_TYPE                                                      = 0x01;
    public static final int REVERSE_PORTRAIT_TYPE                                               = 0x02;
    public static final int REVERSE_LANDSCAPE_TYPE                                              = 0x03;

    public static final String PORTRAIT                                                         = "PORTRAIT";
    public static final String LANDSCAPE                                                        = "LANDSCAPE";
    public static final String REVERSE_PORTRAIT                                                 = "R PORTRAIT";
    public static final String REVERSE_LANDSCAPE                                                = "R LANDSCAPE";

    public static final int FORMAT_UINT24                                                       = 0x13;
    public static final int FORMAT_SINT24                                                       = 0x23;
    public static final int FORMAT_UINT16_BIG_INDIAN                                            = 0x62;
    public static final int FORMAT_UINT32_BIG_INDIAN                                            = 0x64;

    public static final int MAX_VISISBLE_GRAPH_ENTRIES                                          = 300;

    public static final SimpleDateFormat TIME_FORMAT                                            = new SimpleDateFormat("HH:mm:ss:SSS", Locale.US);
    public static final SimpleDateFormat TIME_FORMAT_PEDOMETER                                  = new SimpleDateFormat("mm:ss:SS", Locale.US);
    public static final DecimalFormat GRAVITY_VECTOR_DECIMAL_FORMAT                             = new DecimalFormat("#.##");
    public static final int CLOUD_TOKEN_LENGTH                                                  = 250;

    public static double ADVERTISING_INTERVAL_UNIT                                              = 0.625; //ms
    public static double CONN_INT_UNIT                                                          = 1.25;
    public static int MIN_CONN_VALUE                                                            = 6;
    public static double MIN_CONN_INTERVAL                                                      = 7.5; //ms
    public static int MAX_CONN_VALUE                                                            = 3200;
    public static int MAX_CONN_INTERVAL                                                         = 4000; //ms
    public static int MIN_SLAVE_LATENCY                                                         = 0;
    public static int MAX_SLAVE_LATENCY                                                         = 500;
    public static int MIN_SUPERVISION_TIMEOUT                                                   = 100; //ms
    public static int MAX_SUPERVISION_TIMEOUT                                                   = 32000; //ms

    //Android BLE
    public static double MAX_CONN_INTERVAL_POST_LOLIPOP                                         = 11.25; //ms

    public static final int MAX_MTU_SIZE_THINGY                                                 = 276;
    public static final int MAX_MTU_SIZE_PRE_LOLLIPOP                                           = 23;
    public static final int MAX_AUDIO_PACKET_SIZE                                               = 160; //

    //Notification Intervals in ms
    public static final int ENVIRONMENT_NOTIFICATION_MAX_INTERVAL                               = 60000;
    public static final int NOTIFICATION_MAX_INTERVAL                                           = 5000;
    public static final int TEMP_MIN_INTERVAL                                                   = 100;
    public static final int PRESSURE_MIN_INTERVAL                                               = 50;
    public static final int HUMIDITY_MIN_INTERVAL                                               = 100;
    public static final int COLOR_INTENSITY_MIN_INTERVAL                                        = 200;
    public static final int PEDOMETER_MIN_INTERVAL                                              = 100;
    public static final int COMPASS_MIN_INTERVAL                                                = 100;
    public static final int MPU_FREQ_MIN_INTERVAL                                               = 5; //hz
    public static final int MPU_FREQ_MAX_INTERVAL                                               = 200; //hz

    public static final int GAS_MODE_1                                                          = 1;
    public static final int GAS_MODE_2                                                          = 2;
    public static final int GAS_MODE_3                                                          = 3;

    public static final int WAKE_ON_MOTION_ON                                                   = 0x00;
    public static final int WAKE_ON_MOTION_OFF                                                  = 0x01;

    //Speaker modes
    public static final int FREQUENCY_MODE                                                      = 0x01;
    public static final int PCM_MODE                                                            = 0x02;
    public static final int SAMPLE_MODE                                                         = 0x03;

    //Microphone mode
    public static final int ADPCM_MODE                                                          = 0x01;
    public static final int SPL_MODE                                                            = 0x02;


    //Speaker status notifications
    public static final int SPEAKER_STATUS_FINISHED                                             = 0x00;
    public static final int SPEAKER_STATUS_BUFFER_WARNING                                       = 0x01;
    public static final int SPEAKER_STATUS_BUFFER_READY                                         = 0x02;
    public static final int SPEAKER_STATUS_PACKET_DISREGARDED                                   = 0x10;
    public static final int SPEAKER_STATUS_INVALID_COMMAND                                      = 0x11;


    /**
     * URI Scheme maps a byte code into the scheme and an optional scheme specific prefix.
     */
    private static final SparseArray<String> URI_SCHEMES = new SparseArray<String>() {
        {
            put((byte) 0, "http://www.");
            put((byte) 1, "https://www.");
            put((byte) 2, "http://");
            put((byte) 3, "https://");
            put((byte) 4, "urn:uuid:"); // RFC 2141 and RFC 4122};
        }
    };

    /**
     * Expansion strings for "http" and "https" schemes. These contain strings appearing anywhere in a
     * URL. Restricted to Generic TLDs.
     * <p/>
     * Note: this is a scheme specific encoding.
     */
    private static final SparseArray<String> URL_CODES = new SparseArray<String>() {
        {
            put((byte) 0, ".com/");
            put((byte) 1, ".org/");
            put((byte) 2, ".edu/");
            put((byte) 3, ".net/");
            put((byte) 4, ".info/");
            put((byte) 5, ".biz/");
            put((byte) 6, ".gov/");
            put((byte) 7, ".com");
            put((byte) 8, ".org");
            put((byte) 9, ".edu");
            put((byte) 10, ".net");
            put((byte) 11, ".info");
            put((byte) 12, ".biz");
            put((byte) 13, ".gov");
        }
    };

    public static IntentFilter createSpeakerStatusChangeReceiver(final String address) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SERVICE_DISCOVERY_COMPLETED);
        intentFilter.addAction(ACTION_DEVICE_DISCONNECTED);
        intentFilter.addAction(EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION);
        return intentFilter;
    }

    public static int setValue(final byte[] dest, int offset, int value, int formatType) {
        int len = offset + getTypeLen(formatType);
        if (len > dest.length)
            return offset;

        switch (formatType) {
            case BluetoothGattCharacteristic.FORMAT_SINT8:
                value = intToSignedBits(value, 8);
                // Fall-through intended
            case BluetoothGattCharacteristic.FORMAT_UINT8:
                dest[offset] = (byte) (value & 0xFF);
                break;

            case BluetoothGattCharacteristic.FORMAT_SINT16:
                value = intToSignedBits(value, 16);
                // Fall-through intended
            case BluetoothGattCharacteristic.FORMAT_UINT16:
                dest[offset++] = (byte) (value & 0xFF);
                dest[offset] = (byte) ((value >> 8) & 0xFF);
                break;

            case FORMAT_SINT24:
                value = intToSignedBits(value, 24);
                // Fall-through intended
            case FORMAT_UINT24:
                dest[offset++] = (byte) (value & 0xFF);
                dest[offset++] = (byte) ((value >> 8) & 0xFF);
                dest[offset] = (byte) ((value >> 16) & 0xFF);
                break;

            case FORMAT_UINT16_BIG_INDIAN:
                dest[offset++] = (byte) ((value >> 8) & 0xFF);
                dest[offset] = (byte) (value & 0xFF);
                break;

            case BluetoothGattCharacteristic.FORMAT_SINT32:
                value = intToSignedBits(value, 32);
                // Fall-through intended
            case BluetoothGattCharacteristic.FORMAT_UINT32:
                dest[offset++] = (byte) (value & 0xFF);
                dest[offset++] = (byte) ((value >> 8) & 0xFF);
                dest[offset++] = (byte) ((value >> 16) & 0xFF);
                dest[offset] = (byte) ((value >> 24) & 0xFF);
                break;

            case FORMAT_UINT32_BIG_INDIAN:
                dest[offset++] = (byte) ((value >> 24) & 0xFF);
                dest[offset++] = (byte) ((value >> 16) & 0xFF);
                dest[offset++] = (byte) ((value >> 8) & 0xFF);
                dest[offset] = (byte) (value & 0xFF);
                break;

            default:
                return offset;
        }
        return len;
    }

    private static int getTypeLen(int formatType) {
        return formatType & 0xF;
    }

    private static int intToSignedBits(int i, int size) {
        if (i < 0) {
            i = (1 << size - 1) + (i & ((1 << size - 1) - 1));
        }
        return i;
    }

    public static void showToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String decodeUri(final byte[] serviceData, final int start, final int length) {
        if (start < 0 || serviceData.length < start + length)
            return null;

        final StringBuilder uriBuilder = new StringBuilder();
        int offset = 0;
        if (offset < length) {
            byte b = serviceData[start + offset++];
            String scheme = URI_SCHEMES.get(b);
            if (scheme != null) {
                uriBuilder.append(scheme);
                if (URLUtil.isNetworkUrl(scheme)) {
                    return decodeUrl(serviceData, start + offset, length - 1, uriBuilder);
                } else if ("urn:uuid:".equals(scheme)) {
                    return decodeUrnUuid(serviceData, start + offset, uriBuilder);
                }
            }
            Log.w(TAG, "decodeUri unknown Uri scheme code=" + b);
        }
        return null;
    }

    private static String decodeUrl(final byte[] serviceData, final int start, final int length, final StringBuilder urlBuilder) {
        int offset = 0;
        while (offset < length) {
            byte b = serviceData[start + offset++];
            String code = URL_CODES.get(b);
            if (code != null) {
                urlBuilder.append(code);
            } else {
                urlBuilder.append((char) b);
            }
        }
        return urlBuilder.toString();
    }

    /**
     * Creates the Uri string with embedded expansion codes.
     *
     * @param uri to be encoded
     * @return the Uri string with expansion codes.
     */
    public static byte[] encodeUri(String uri) {
        if (uri.length() == 0) {
            return new byte[0];
        }
        ByteBuffer bb = ByteBuffer.allocate(uri.length());
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN);
        int position = 0;

        // Add the byte code for the scheme or return null if none
        Byte schemeCode = encodeUriScheme(uri);
        if (schemeCode == null) {
            return null;
        }
        String scheme = URI_SCHEMES.get(schemeCode);
        bb.put(schemeCode);
        position += scheme.length();

        if (URLUtil.isNetworkUrl(scheme)) {
            return encodeUrl(uri, position, bb);
        } else if ("urn:uuid:".equals(scheme)) {
            return encodeUrnUuid(uri, position, bb);
        }
        return null;
    }

    private static Byte encodeUriScheme(String uri) {
        String lowerCaseUri = uri.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < URI_SCHEMES.size(); i++) {
            // get the key and value.
            int key = URI_SCHEMES.keyAt(i);
            String value = URI_SCHEMES.valueAt(i);
            if (lowerCaseUri.startsWith(value)) {
                return (byte) key;
            }
        }
        return null;
    }

    private static byte[] encodeUrl(String url, int position, ByteBuffer bb) {
        while (position < url.length()) {
            byte expansion = findLongestExpansion(url, position);
            if (expansion >= 0) {
                bb.put(expansion);
                position += URL_CODES.get(expansion).length();
            } else {
                bb.put((byte) url.charAt(position++));
            }
        }
        return byteBufferToArray(bb);
    }

    private static byte[] byteBufferToArray(ByteBuffer bb) {
        byte[] bytes = new byte[bb.position()];
        bb.rewind();
        bb.get(bytes, 0, bytes.length);
        return bytes;
    }

    /**
     * Finds the longest expansion from the uri at the current position.
     *
     * @param uriString the Uri
     * @param pos start position
     * @return an index in URI_MAP or 0 if none.
     */
    private static byte findLongestExpansion(String uriString, int pos) {
        byte expansion = -1;
        int expansionLength = 0;
        for (int i = 0; i < URL_CODES.size(); i++) {
            // get the key and value.
            int key = URL_CODES.keyAt(i);
            String value = URL_CODES.valueAt(i);
            if (value.length() > expansionLength && uriString.startsWith(value, pos)) {
                expansion = (byte) key;
                expansionLength = value.length();
            }
        }
        return expansion;
    }

    private static byte[] encodeUrnUuid(String urn, int position, ByteBuffer bb) {
        String uuidString = urn.substring(position, urn.length());
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "encodeUrnUuid invalid urn:uuid format - " + urn);
            return null;
        }
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return byteBufferToArray(bb);
    }

    private static String decodeUrnUuid(final byte[] serviceData, final int offset, final StringBuilder urnBuilder) {
        ByteBuffer bb = ByteBuffer.wrap(serviceData);
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN);
        long mostSignificantBytes, leastSignificantBytes;
        try {
            bb.position(offset);
            mostSignificantBytes = bb.getLong();
            leastSignificantBytes = bb.getLong();
        } catch (BufferUnderflowException e) {
            Log.w(TAG, "decodeUrnUuid BufferUnderflowException!");
            return null;
        }
        UUID uuid = new UUID(mostSignificantBytes, leastSignificantBytes);
        urnBuilder.append(uuid.toString());
        return urnBuilder.toString();
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded signed value.
     */

    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    public static byte[] base64Decode(String s) {
        return Base64.decode(s, Base64.DEFAULT);
    }

    public static boolean checkIfVersionIsLollipopOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
    public static boolean checkIfVersionIsOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean validateSlaveLatency(final int slaveLatency, final int maxConIntervalUnits, final int supervisionTimeoutUnits){
        final double maxConInterval = maxConIntervalUnits;
        final int superVisionTimeout = supervisionTimeoutUnits;
        if(slaveLatency < (((superVisionTimeout * 4) / maxConInterval) - 1)) {
            return true;
        }
        return false;
    }

    public static boolean validateSupervisionTimeout(final int slaveLatency, final int maxConIntervalUnits, final int supervisionTimeoutUnits){
        final double maxConInterval = maxConIntervalUnits;
        final int superVisionTimeout = supervisionTimeoutUnits;
        if(superVisionTimeout > (((1 + slaveLatency) * maxConInterval) / 4)) {
            return true;
        }
        return false;
    }

    public static boolean validateMaxConnectionInterval(final int slaveLatency, final int maxConIntervalUnits, final int supervisionTimeoutUnits){
        final double maxConInterval = maxConIntervalUnits;
        final int superVisionTimeout = supervisionTimeoutUnits;
        if(maxConInterval < ((superVisionTimeout * 4) / (1 + slaveLatency))) {
            return true;
        }
        return false;
    }

    public static void removeOldDataForGraphs(final LinkedHashMap linkedHashMap) {
        if(linkedHashMap.size() > MAX_VISISBLE_GRAPH_ENTRIES) {
            Set keys = linkedHashMap.keySet();
            for(Object key : keys) {
                linkedHashMap.remove(key);
                if(linkedHashMap.size()  == MAX_VISISBLE_GRAPH_ENTRIES){
                    break;
                }
            }
        }
    }
}
