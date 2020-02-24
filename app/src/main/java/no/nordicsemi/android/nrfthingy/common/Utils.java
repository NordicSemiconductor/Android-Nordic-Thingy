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

package no.nordicsemi.android.nrfthingy.common;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import no.nordicsemi.android.nrfthingy.thingy.Thingy;

public class Utils {

    public static final String TAG = "THINGY:52";
    public static final String INITIAL_CONFIG_STATE = "INITIAL_CONFIG_STATE";
    public static final String PREFS_INITIAL_SETUP = "INITIAL_SETUP";
    public static final String INITIAL_AUDIO_STREAMING_INFO = "INITIAL_AUDIO_STREAMING_INFO";
    public static final String INITIAL_ENV_TUTORIAL = "INITIAL_ENV_TUTORIAL";
    public static final String INITIAL_MOTION_TUTORIAL = "INITIAL_MOTION_TUTORIAL";
    public static final String INITIAL_SOUND_TUTORIAL = "INITIAL_SOUND_TUTORIAL";
    private static final String KEY_IFTTT_TOKEN = "KEY_IFTTT_TOKEN";
    private static final String KEY_NFC_FEATURE_REQ = "KEY_NFC_FEATURE_REQ";
    public static final String INITIAL_DFU_TUTORIAL = "INITIAL_DFU_TUTORIAL";

    public static final String START_RECORDING = "START_RECORDING";
    public static final String STOP_RECORDING = "STOP_RECORDING";
    public static final String EXTRA_DATA_AUDIO_RECORD = "EXTRA_DATA_AUDIO_RECORD";
    public static final String ERROR_AUDIO_RECORD = "ERROR";

    public static final float CHART_LINE_WIDTH = 2.0f;
    public static final float CHART_VALUE_TEXT_SIZE = 6.5f;

    public static final String EXTRA_APP = "application/vnd.no.nordicsemi.type.app";
    public static final String EXTRA_ADDRESS_DATA = "text/plain";

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";

    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_DATA_TYPE = "EXTRA_DATA_TYPE";
    public static final String EXTRA_DATA_URL = "EXTRA_DATA_URL";

    public static final String CURRENT_DEVICE = "CURRENT_DEVICE";
    public static final String SETTINGS_MODE = "SETTINGS_MODE";

    public static final String INITIAL_CONFIG_FROM_ACTIVITY = "INITIAL_CONFIG_FROM_ACTIVITY";

    //Notification constants
    public static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";
    public static final int DISCONNECT_REQ = 501;
    public static final String THINGY_GROUP_ID = "THINGY_GROUP_ID";
    public static final int NOTIFICATION_ID = 502;
    public static final int OPEN_ACTIVITY_REQ = 503;

    private static final int FORMAT_UINT24 = 0x13;
    private static final int FORMAT_SINT24 = 0x23;
    private static final int FORMAT_UINT16_BIG_INDIAN = 0x62;
    private static final int FORMAT_UINT32_BIG_INDIAN = 0x64;

    public static final String ENVIRONMENT_FRAGMENT = "ENVIRONMENT_FRAGMENT";
    public static final String MOTION_FRAGMENT = "MOTION_FRAGMENT";
    public static final String UI_FRAGMENT = "UI_FRAGMENT";
    public static final String SOUND_FRAGMENT = "SOUND_FRAGMENT";
    public static final String CLOUD_FRAGMENT = "CLOUD_FRAGMENT";
    public static final String PROGRESS_DIALOG_TAG = "PROG_DIALOG";
    public static final String NFC_DIALOG_TAG = "NFC_DIALOG";

    //Drawer menu group ids and item ids
    public static final int GROUP_ID_SAVED_THINGIES = 100;
    public static final int GROUP_ID_ADD_THINGY = 101;
    public static final int GROUP_ID_ABOUT = 102;
    public static final int GROUP_ID_DFU = 103;
    public static final int ITEM_ID_ADD_THINGY = 201;
    public static final int ITEM_ID_SETTINGS = 202;
    public static final int ITEM_ID_DFU = 203;

    public static final int BLE_GAP_ADV_INTERVAL_MIN = 20;//ms
    public static final int BLE_GAP_ADV_INTERVAL_MAX = 10240;//ms
    public static final int BLE_GAP_ADV_TIMEOUT_MIN = 0;//ms
    public static final int BLE_GAP_ADV_TIMEOUT_MAX = 180;//ms
    static final String EXTRA_DATA_TITLE = "EXTRA_DATA_TITLE";
    public static final String EXTRA_DATA_DESCRIPTION = "EXTRA_DATA_DESCRIPTION";
    public static final String EXTRA_DEVICE_CONNECTION_STATE = "EXTRA_DEVICE_CONNECTION_STATE";
    public static final String EXTRA_DEVICE_DFU_COMPLETED = "EXTRA_DEVICE_DFU_COMPLETED";

    public static final int REQUEST_ENABLE_BT = 1020;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1021;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1022;
    public static final int REQ_PERMISSION_WRITE_EXTERNAL_STORAGE = 1023;
    public static final int REQ_PERMISSION_READ_EXTERNAL_STORAGE = 1024;
    public static final int REQ_PERMISSION_RECORD_AUDIO = 1024;

    //DFU Constants
    public static final String NORDIC_FW = "NORDIC_FW";
    public static final String DFU_RECONNECTING = "DFU_RECONNECTING";
    public static final String DFU_BUTTON_STATE = "DFU_BUTTON_STATE";
    public static final String EXTRA_DATA_BOOTLOADER_ALPHA = "bootloader_alpha";
    public static final String EXTRA_DATA_INIT_DFU_ALPHA = "init_dfu_alpha";
    public static final String EXTRA_DATA_UPLOAD_FW_ALPHA = "fw_alpha";
    public static final String EXTRA_DATA_DFU_COMPLETED_ALPHA = "dfu_completed_alpha";
    public static final String DATA_FILE_TYPE = "file_type";
    public static final String DATA_FILE_TYPE_TMP = "file_type_tmp";
    public static final String DATA_FILE_PATH = "file_path";
    public static final String DATA_FILE_STREAM = "file_stream";
    public static final String DATA_INIT_FILE_PATH = "init_file_path";
    public static final String DATA_INIT_FILE_STREAM = "init_file_stream";
    public static final String DATA_STATUS = "status";
    public static final String EXTRA_DATA_FILE_NAME = "EXTRA_DATA_FILE_NAME";
    public static final String EXTRA_DATA_FILE_SIZE = "EXTRA_DATA_FILE_SIZE";
    public static final int SELECT_FILE_REQ = 1;
    public static final int SELECT_INIT_FILE_REQ = 2;

    public static final String EXTRA_URI = "uri";

    static final String[] AUDIO_FILES = new String[]{"ievan_polkka.wav", "bensound_ukulele_8khz.wav", "evil_laugh_8khz.wav", "learning_computer_8khz.wav"};

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
    private static final String NFC_WARNING = "NFC_WARNING";

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

    /**
     * Inverts endianness of the byte array.
     *
     * @param bytes input byte array
     * @return byte array in opposite order
     */
    public static byte[] invertEndianness(final byte[] bytes) {
        if (bytes == null)
            return null;
        final int length = bytes.length;
        final byte[] result = new byte[length];
        for (int i = 0; i < length; i++)
            result[i] = bytes[length - i - 1];
        return result;
    }

    public static void showToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
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

    private static String decodeUrl(final byte[] serviceData, final int start, final int length,
                                    final StringBuilder urlBuilder) {
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
     * @param pos       start position
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
        String uuidString = urn.substring(position);
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

    public static int getValue(final byte[] bytes) {
        if (bytes == null || bytes.length != 2)
            return 0;
        return unsignedToSigned(unsignedBytesToInt(bytes[0], bytes[1]), 16);
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

    public static boolean checkIfVersionIsAboveJellyBean() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean checkIfVersionIsAboveLollipop() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean checkIfVersionIsMarshmallowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean checkIfVersionIsOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean checkIfVersionIsNougatOrAbove() {
        return Build.VERSION.SDK_INT >= 25;
    }

    public static boolean checkIfVersionIsPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static boolean checkIfVersionIsQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isAppInitialisedBefore(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_INITIAL_SETUP, Context.MODE_PRIVATE);
        return sp.getBoolean(INITIAL_CONFIG_STATE, false);
    }

    public static boolean isConnected(final String address, final List<BluetoothDevice> connectedDevices) {
        for (BluetoothDevice device : connectedDevices) {
            if (address.equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnected(final Thingy thingy, final List<BluetoothDevice> connectedDevices) {
        for (BluetoothDevice device : connectedDevices) {
            if (thingy.getDeviceAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnected(final BluetoothDevice thingyDevice, final List<BluetoothDevice> connectedDevices) {
        for (BluetoothDevice device : connectedDevices) {
            if (thingyDevice != null) {
                if (thingyDevice.getAddress().equals(device.getAddress())) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public static String incrementAddress(final String address) {
        String macAddr = address.substring(0, address.length() - 2);
        final String hex = address.substring(address.length() - 2);
        int hexAsInt = Integer.parseInt(hex, 16);
        hexAsInt++;
        //return macAddr + String.format("%02x", Integer.toHexString(hexAsInt).toUpperCase(Locale.US));
        return macAddr + String.format(Locale.US, "%02X", (hexAsInt & 0xFF));
    }

    public static String decrementAddress(final String address) {
        String macAddr = address.substring(0, address.length() - 2);
        final String hex = address.substring(address.length() - 2);
        int hexAsInt = Integer.parseInt(hex, 16);
        hexAsInt--;
        //return macAddr + Long.toHexString(hexAsInt).toUpperCase(Locale.US);
        return macAddr + String.format(Locale.US, "%02X", (hexAsInt & 0xFF));
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static boolean checkIfSequenceIsCompleted(final Context context, final String key) {
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    public static boolean saveSequenceCompletion(final Context context, final String key) {
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, true);
        return editor.commit();
    }

    public static boolean saveIFTTTTokenToPreferences(final Context context, final String token) {
        final SharedPreferences sp = context.getSharedPreferences(PREFS_INITIAL_SETUP, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_IFTTT_TOKEN, token);
        return editor.commit();
    }

    public static String getIFTTTToken(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(PREFS_INITIAL_SETUP, Context.MODE_PRIVATE);
        return sp.getString(KEY_IFTTT_TOKEN, "");
    }

    public static String readAddressPayload(final byte[] payload) {
        try {
            final int length = payload.length;
            final byte[] newPayload = new byte[length - 3];
            System.arraycopy(payload, 3, newPayload, 0, newPayload.length);
            final String addressPayload = new String(newPayload, Charset.forName("UTF-8"));
            if (TextUtils.isEmpty(addressPayload)) {
                return null;
            }
            final String address = addressPayload.substring(0, addressPayload.indexOf(" "));
            return address.toUpperCase(Locale.US);
        } catch (Exception ex) {
            return null;
        }
    }

    public static BluetoothDevice getBluetoothDevice(final Context context, final String address) {
        final BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter ba = bm.getAdapter();
        if (ba != null /*&& ba.isEnabled()*/) {
            try {
                return ba.getRemoteDevice(address);
            } catch (Exception ex) {
                return null;
            }
        }
        return null; //ideally shouldn't go here
    }

    public static boolean showNfcDisabledWarning(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(PREFS_INITIAL_SETUP, Context.MODE_PRIVATE);
        return sp.getBoolean(NFC_WARNING, true);
    }
}
