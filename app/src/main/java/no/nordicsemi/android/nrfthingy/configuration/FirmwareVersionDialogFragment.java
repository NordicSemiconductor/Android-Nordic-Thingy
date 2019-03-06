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

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.dfu.DfuHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class FirmwareVersionDialogFragment extends DialogFragment {

    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;

    public interface FirmwareVersionDialogFragmentListener {
        void onUpdateFirmwareClickListener();
    }

    public static FirmwareVersionDialogFragment newInstance(final BluetoothDevice device) {
        final FirmwareVersionDialogFragment fragment = new FirmwareVersionDialogFragment();

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(getString(R.string.settings_fw_version_title));
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_firmware_version, null);

        final TextView fwVersion = view.findViewById(R.id.fw_version);
        final String currentVersion = mThingySdkManager.getFirmwareVersion(mDevice);
        boolean isFirmwareUpdateDate = DfuHelper.isFirmwareUpdateAvailable(requireContext(), currentVersion);
        if (isFirmwareUpdateDate) {
            final DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
            String deviceName = databaseHelper.getDeviceName(mDevice.getAddress());
            if (deviceName.isEmpty()) {
                deviceName = mDevice.getName();
            }

            final String newestVersion = DfuHelper.getCurrentFwVersion(requireContext());
            fwVersion.setText(getString(R.string.fw_update_available, deviceName, newestVersion));
            alertDialogBuilder.setView(view)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((FirmwareVersionDialogFragmentListener) getParentFragment()).onUpdateFirmwareClickListener();
                        }
                    })
                    .setNegativeButton(getString(R.string.later), null).setNeutralButton(getString(R.string.update_custom_firmware), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((FirmwareVersionDialogFragmentListener) getParentFragment()).onUpdateFirmwareClickListener();
                }
            });
        } else {
            fwVersion.setText(R.string.thingy_fw_version_summary);
            alertDialogBuilder.setView(view)
                    .setPositiveButton(getString(R.string.ok), null)
                    .setNeutralButton(getString(R.string.update_custom_firmware), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((FirmwareVersionDialogFragmentListener) getParentFragment()).onUpdateFirmwareClickListener();
                        }
                    });
        }

        return alertDialogBuilder.create();
    }
}
