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

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class FrequencyModeFragment extends Fragment implements View.OnTouchListener {

    private TextView mVolume;
    private SeekBar mSeekbar;
    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;

    public FrequencyModeFragment() {
    }

    public static FrequencyModeFragment newInstance(final BluetoothDevice device) {
        FrequencyModeFragment fragment = new FrequencyModeFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
        mThingySdkManager = ThingySdkManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_frequency_mode, container, false);
        mSeekbar = rootView.findViewById(R.id.seek_bar_volume);
        mVolume = rootView.findViewById(R.id.frequency_volume);
        final View view1 = rootView.findViewById(R.id.label_1);
        final View view2 = rootView.findViewById(R.id.label_2);
        final View view3 = rootView.findViewById(R.id.label_3);
        final View view4 = rootView.findViewById(R.id.label_4);
        final View view5 = rootView.findViewById(R.id.label_5);
        final View view6 = rootView.findViewById(R.id.label_6);
        final View view7 = rootView.findViewById(R.id.label_7);
        final View view8 = rootView.findViewById(R.id.label_8);
        final View view9 = rootView.findViewById(R.id.label_9);
        final View view10 = rootView.findViewById(R.id.label_10);
        final View view11 = rootView.findViewById(R.id.label_11);
        final View view12 = rootView.findViewById(R.id.label_12);

        view1.setOnTouchListener(this);
        view2.setOnTouchListener(this);
        view3.setOnTouchListener(this);
        view4.setOnTouchListener(this);
        view5.setOnTouchListener(this);
        view6.setOnTouchListener(this);
        view7.setOnTouchListener(this);
        view8.setOnTouchListener(this);
        view9.setOnTouchListener(this);
        view10.setOnTouchListener(this);
        view11.setOnTouchListener(this);
        view12.setOnTouchListener(this);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                mVolume.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });

        return rootView;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        final int id = view.getId();
        int frequency;
        final int duration = 0xFFFF;
        final int volume = mSeekbar.getProgress();
        switch (id) {
            case R.id.label_1:
                frequency = 261;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_2:
                frequency = 293;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_3:
                frequency = 329;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_4:
                frequency = 349;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_5:
                frequency = 391;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_6:
                frequency = 440;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_7:
                frequency = 493;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_8:
                frequency = 277;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_9:
                frequency = 311;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_10:
                frequency = 369;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_11:
                frequency = 415;
                return handleEvent(frequency, duration, volume, event, view);
            case R.id.label_12:
                frequency = 466;
                return handleEvent(frequency, duration, volume, event, view);
            default:
                break;
        }
        return false;
    }

    private boolean handleEvent(final int frequency, final int duration, final int volume, final MotionEvent event, final View view) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mThingySdkManager.playSoundFrequency(getActivity(), mDevice, frequency, duration, volume);
                view.setPressed(true);
                return true;
            case MotionEvent.ACTION_UP:
                mThingySdkManager.playSoundFrequency(getActivity(), mDevice, frequency, 0, volume);
                view.setPressed(false);
                return true;
            default:
                if (MotionEvent.ACTION_CANCEL == event.getActionMasked()) {
                    mThingySdkManager.playSoundFrequency(getActivity(), mDevice, frequency, 0, volume);
                    view.setPressed(false);
                    return true;
                }
                return false;
        }
    }
}