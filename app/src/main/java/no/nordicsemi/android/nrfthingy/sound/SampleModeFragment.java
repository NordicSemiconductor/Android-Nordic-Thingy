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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class SampleModeFragment extends Fragment {
    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;

    public static SampleModeFragment newInstance(final BluetoothDevice device) {
        SampleModeFragment fragment = new SampleModeFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
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
        final View rootView = inflater.inflate(R.layout.fragment_sample_mode, container, false);
        final TextView sample1 = rootView.findViewById(R.id.sound_sample_1);
        final TextView sample2 = rootView.findViewById(R.id.sound_sample_2);
        final TextView sample3 = rootView.findViewById(R.id.sound_sample_3);
        final TextView sample4 = rootView.findViewById(R.id.sound_sample_4);
        final TextView sample5 = rootView.findViewById(R.id.sound_sample_5);
        final TextView sample6 = rootView.findViewById(R.id.sound_sample_6);
        final TextView sample7 = rootView.findViewById(R.id.sound_sample_7);
        final TextView sample8 = rootView.findViewById(R.id.sound_sample_8);
        final TextView sample9 = rootView.findViewById(R.id.sound_sample_9);

        sample1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_1);
            }
        });

        sample2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_2);
            }
        });

        sample3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_3);
            }
        });

        sample4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_4);
            }
        });

        sample5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_5);
            }
        });

        sample6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_6);
            }
        });

        sample7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_7);
            }
        });

        sample8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_8);
            }
        });

        sample9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThingySdkManager.playSoundSample(getActivity(), mDevice, ThingyUtils.SAMPLE_9);
            }
        });
        return rootView;
    }
}