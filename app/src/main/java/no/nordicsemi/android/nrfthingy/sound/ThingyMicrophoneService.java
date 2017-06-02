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

package no.nordicsemi.android.nrfthingy.sound;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingyConnection;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class ThingyMicrophoneService extends IntentService {
    public static final String EXTRA_DATA_AUDIO_RECORD = "EXTRA_DATA_AUDIO_RECORD";
    private static final int AUDIO_BUFFER = 512;

    private boolean mStartRecordingAudio = false;
    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;

    private BroadcastReceiver mAudioBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals("STOP_RECORDING")) {
                stopRecordingAudio(mDevice);
                stopSelf();
            }
        }
    };

    public ThingyMicrophoneService() {
        super("ThingyMicrophoneService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ThingyMicrophoneService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case Utils.START_RECORDING:
                mDevice = intent.getParcelableExtra(Utils.EXTRA_DEVICE);
                startRecordingAudio(mDevice);
                break;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("STOP_RECORDING");
        LocalBroadcastManager.getInstance(this).registerReceiver(mAudioBroadcastReceiver, filter);
        mThingySdkManager = ThingySdkManager.getInstance();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAudioBroadcastReceiver);
    }

    public boolean startRecordingAudio(final BluetoothDevice device) {

        if (mStartRecordingAudio) {
            return false;
        }
        mStartRecordingAudio = true;

        final ThingyConnection thingyConnection = mThingySdkManager.getThingyConnection(device);
        final AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER);
        audioRecorder.startRecording();
        while (mStartRecordingAudio) {
            byte audioData[] = new byte[AUDIO_BUFFER];
            int status = audioRecorder.read(audioData, 0, AUDIO_BUFFER);
            if (status == AudioRecord.ERROR_INVALID_OPERATION ||
                    status == AudioRecord.ERROR_BAD_VALUE) {
                break;
            }
            try {
                thingyConnection.playVoiceInput(downSample(audioData));
                sendAudioRecordBroadcast(device, audioData, status);
            } catch (Exception e) {
                break;
            }
        }

        audioRecorder.stop();
        audioRecorder.release();
        return true;
    }

    public boolean stopRecordingAudio(final BluetoothDevice device) {
        mStartRecordingAudio = false;
        return true;
    }

    private byte[] downSample(final byte[] data) {
        final byte[] output = new byte[data.length / 2];
        int length = data.length;
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < length; i += 2) {
            output[i / 2] = (byte) (((bb.getShort() * 128.0) / 32768.0) + 128.0);
        }
        return output;
    }

    private void sendAudioRecordBroadcast(final BluetoothDevice device, final byte[] data, final int status) {
        final Intent intent = new Intent(EXTRA_DATA_AUDIO_RECORD + device.getAddress());
        intent.putExtra(ThingyUtils.EXTRA_DEVICE, device);
        intent.putExtra(ThingyUtils.EXTRA_DATA_PCM, data);
        intent.putExtra(ThingyUtils.EXTRA_DATA, status);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}