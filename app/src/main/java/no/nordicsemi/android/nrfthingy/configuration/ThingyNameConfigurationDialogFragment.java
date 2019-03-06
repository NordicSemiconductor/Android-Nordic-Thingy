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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class ThingyNameConfigurationDialogFragment extends DialogFragment {

    private TextInputLayout mDeviceNameLayout;
    private TextInputEditText mDeviceNameView;

    private String mDeviceName;

    private DatabaseHelper mDatabaseHelper;
    private BluetoothDevice mDevice;

    private ThingySdkManager mThingySdkManager;

    public static ThingyNameConfigurationDialogFragment newInstance(final BluetoothDevice device) {
        final ThingyNameConfigurationDialogFragment fragment = new ThingyNameConfigurationDialogFragment();

        Bundle args = new Bundle();
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

        mDatabaseHelper = new DatabaseHelper(getActivity());
        mThingySdkManager = ThingySdkManager.getInstance();
        mDeviceName = mThingySdkManager.getDeviceName(mDevice);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(getString(R.string.thingy_name_title));
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_thingy_name, null);


        alertDialogBuilder.setView(view).setPositiveButton(getString(R.string.confirm), null).setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        mDeviceNameLayout = view.findViewById(R.id.layout_device_name);
        mDeviceNameView = view.findViewById(R.id.device_name_view);
        mDeviceNameView.setText(mDeviceName);

        mDeviceNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > 10) {
                    mDeviceNameLayout.setError(getString(R.string.error_long_device_name));
                } else {
                    mDeviceNameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    if (mThingySdkManager != null) {
                        final String name = getValueFromView();
                        mThingySdkManager.setDeviceName(mDevice, name);
                        mDatabaseHelper.updateDeviceName(mDevice.getAddress(), name);
                        dismiss();
                        ((ThingyBasicSettingsChangeListener) getParentFragment()).updateThingyName();
                    }
                }
            }
        });

        return alertDialog;
    }

    private boolean validateInput() {
        final String deviceName = mDeviceNameView.getText().toString().trim();
        if (deviceName.isEmpty()) {
            mDeviceNameLayout.setError(getString(R.string.error_empty_device_name));
            return false;
        } else if (deviceName.length() > 10) {
            mDeviceNameLayout.setError(getString(R.string.error_long_device_name));
            return false;
        }
        return true;
    }

    private String getValueFromView() {
        return mDeviceNameView.getText().toString().trim();
    }
}
