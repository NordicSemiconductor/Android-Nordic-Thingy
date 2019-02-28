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

package no.nordicsemi.android.nrfthingy.configuration;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.MessageDialogFragment;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.dfu.SecureDfuActivity;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class BasicConfigurationFragment extends Fragment implements ThingyBasicSettingsChangeListener, FirmwareVersionDialogFragment.FirmwareVersionDialogFragmentListener {

    private BluetoothDevice mDevice;

    private TextView mNameSummary;
    private TextView mPhysicalWebUrlSummary;
    private TextView mFirmwareVersionSummary;

    private ThingySdkManager mThingySdkManager;

    public BasicConfigurationFragment() {

    }

    public static BasicConfigurationFragment getInstance(final BluetoothDevice device) {
        BasicConfigurationFragment fragment = new BasicConfigurationFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
        mThingySdkManager = ThingySdkManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater,
                             final @Nullable ViewGroup container,
                             final @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_basic_configuration, container, false);
        final LinearLayout name = rootView.findViewById(R.id.category_device_name);
        mNameSummary = rootView.findViewById(R.id.category_device_name_summary);
        final LinearLayout advParams = rootView.findViewById(R.id.category_adv_param_char);
        final LinearLayout connectionParams = rootView.findViewById(R.id.category_connection_param_char);
        final LinearLayout eddystoneUrl = rootView.findViewById(R.id.category_eddystone_url);
        mPhysicalWebUrlSummary = rootView.findViewById(R.id.category_eddystone_url_summary);
        final LinearLayout firmwareVersion = rootView.findViewById(R.id.category_fw_version);
        mFirmwareVersionSummary = rootView.findViewById(R.id.category_fw_version_summary);

        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        final String thingyName = databaseHelper.getDeviceName(mDevice.getAddress());

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    ThingyNameConfigurationDialogFragment fragment = ThingyNameConfigurationDialogFragment.newInstance(mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        advParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    AdvParamCharConfigurationDialogFragment fragment = AdvParamCharConfigurationDialogFragment.newInstance(mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        connectionParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    ConnParamCharConfigurationDialogFragment fragment = ConnParamCharConfigurationDialogFragment.newInstance(mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        eddystoneUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    EddystoneUrlConfigurationDialogFragment fragment = EddystoneUrlConfigurationDialogFragment.newInstance(mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        firmwareVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    FirmwareVersionDialogFragment fragment = FirmwareVersionDialogFragment.newInstance(mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        updateThingyName();
        updatePhysicalWebUrl();
        updateCloudToken();
        updateFirmwareVersion();
        return rootView;
    }

    @Override
    public void updateThingyName() {
        final String name = mThingySdkManager.getDeviceName(mDevice);
        if (name != null) {
            mNameSummary.setText(name);
        }
    }

    @Override
    public void updatePhysicalWebUrl() {
        final String url = mThingySdkManager.getEddystoneUrl(mDevice);
        if (url != null) {
            if (url.isEmpty()) {
                mPhysicalWebUrlSummary.setText(R.string.physical_web_disabled);
            } else {
                mPhysicalWebUrlSummary.setText(url);
            }
        }
    }

    @Override
    public void updateCloudToken() {
    }

    @Override
    public void updateFirmwareVersion() {
        final String firmwareVersion = mThingySdkManager.getFirmwareVersion(mDevice);
        if (firmwareVersion != null) {
            mFirmwareVersionSummary.setText(firmwareVersion);
        }
    }

    @Override
    public void onUpdateFirmwareClickListener() {
        final Intent intent = new Intent(getActivity(), SecureDfuActivity.class);
        intent.putExtra(Utils.EXTRA_DEVICE, mDevice);
        startActivity(intent);
    }
}
