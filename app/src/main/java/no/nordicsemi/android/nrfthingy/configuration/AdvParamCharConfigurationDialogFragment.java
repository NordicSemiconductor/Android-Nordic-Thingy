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
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.TextView;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class AdvParamCharConfigurationDialogFragment extends DialogFragment {

    private TextInputLayout mAdvertisingIntervalLayout;
    private TextInputLayout mAdvertisingTimeoutLayout;

    private TextInputEditText mAdvertisingIntervalView;
    private TextInputEditText mAdvertisingTimeoutView;

    private TextView mTxtAdvInterval;

    private int mAdvIntervalTimeoutUnits;
    private int mAdvTimeoutUnits;

    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;

    public static AdvParamCharConfigurationDialogFragment newInstance(final BluetoothDevice device) {
        AdvParamCharConfigurationDialogFragment fragment = new AdvParamCharConfigurationDialogFragment();
        Bundle args = new Bundle();
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
        mAdvIntervalTimeoutUnits = mThingySdkManager.getAdvertisingIntervalUnits(mDevice);
        mAdvTimeoutUnits = mThingySdkManager.getAdvertisingIntervalTimeoutUnits(mDevice);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(getString(R.string.adv_param_title));
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_adv_param, null);

        mAdvertisingIntervalLayout = view.findViewById(R.id.layout_adv_interval);
        mAdvertisingTimeoutLayout = view.findViewById(R.id.layout_adv_timeout);

        mAdvertisingIntervalView = view.findViewById(R.id.adv_interval_view);
        mAdvertisingTimeoutView = view.findViewById(R.id.adv_timeout_view);

        mTxtAdvInterval = view.findViewById(R.id.adv_interval);

        double advertisingInterval = mAdvIntervalTimeoutUnits * ThingyUtils.ADVERTISING_INTERVAL_UNIT;
        mAdvertisingIntervalView.setText(String.valueOf(mAdvIntervalTimeoutUnits));
        mTxtAdvInterval.setText(getString(R.string.decimal_format_ms, advertisingInterval));
        mAdvertisingTimeoutView.setText(String.valueOf(mAdvTimeoutUnits));

        alertDialogBuilder.setView(view).setPositiveButton(getString(R.string.confirm), null).setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        mAdvertisingIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String unit = s.toString();
                if (!unit.isEmpty()) {
                    final int intervalUnit = Integer.parseInt(unit);
                    final double interval = intervalUnit * ThingyUtils.ADVERTISING_INTERVAL_UNIT;
                    mTxtAdvInterval.setText(getString(R.string.decimal_format_ms, interval));
                    if (interval < Utils.BLE_GAP_ADV_INTERVAL_MIN || interval > Utils.BLE_GAP_ADV_INTERVAL_MAX) {
                        mAdvertisingIntervalLayout.setError(getString(R.string.error_adv_interval));
                    } else {
                        mAdvertisingIntervalLayout.setError(null);
                    }
                } else {
                    mAdvertisingIntervalLayout.setError(getString(R.string.error_empty_adv_interval));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAdvertisingTimeoutView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mAdvertisingTimeoutLayout.setError(null);
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    if (mThingySdkManager != null) {
                        final int advertisingInterval = Integer.parseInt(mAdvertisingIntervalView.getText().toString().trim());
                        final int advertisingTimeout = Integer.parseInt(mAdvertisingTimeoutView.getText().toString().trim());
                        if (mThingySdkManager.setAdvertisingParameters(mDevice, advertisingInterval, advertisingTimeout)) {
                            dismiss();
                        } else
                            Utils.showToast(getActivity(), getString(R.string.error_configuring_char));
                    }
                }
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return alertDialog;
    }

    private boolean validateInput() {
        final String advertisingInterval = mAdvertisingIntervalView.getText().toString().trim();
        if (advertisingInterval.isEmpty()) {
            mAdvertisingIntervalLayout.setError(getString(R.string.error_empty_adv_interval));
        } else {
            boolean valid;
            int i = 0;
            try {
                i = Integer.parseInt(advertisingInterval);
                valid = (i & 0xFFFF0000) == 0 || (i & 0xFFFF0000) == 0xFFFF0000;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                mAdvertisingIntervalLayout.setError(getString(R.string.error_uint16));
                return false;
            }

            if (i < Utils.BLE_GAP_ADV_INTERVAL_MIN || i > Utils.BLE_GAP_ADV_INTERVAL_MAX) {
                mAdvertisingIntervalLayout.setError(getString(R.string.error_adv_interval));
                return false;
            }
        }

        final String advertisingTimeout = mAdvertisingTimeoutView.getText().toString().trim();
        if (advertisingTimeout.isEmpty()) {
            mAdvertisingTimeoutLayout.setError(getString(R.string.error_empty_adv_timeout));
        } else {
            boolean valid;
            int i = 0;
            try {
                i = Integer.parseInt(advertisingTimeout);
                valid = (i & 0xFFFF0000) == 0 || (i & 0xFFFF0000) == 0xFFFF0000;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                mAdvertisingTimeoutLayout.setError(getString(R.string.error_uint8));
                return false;
            }

            if (i < Utils.BLE_GAP_ADV_TIMEOUT_MIN || i > Utils.BLE_GAP_ADV_TIMEOUT_MAX) {
                mAdvertisingTimeoutLayout.setError(getString(R.string.error_adv_interval));
                return false;
            }
        }

        return true;
    }

    private byte[] getValueFromView() {
        final byte[] data = new byte[3];

        final String advertisingInterval = mAdvertisingIntervalView.getText().toString().trim();
        Utils.setValue(data, 0, Integer.parseInt(advertisingInterval), BluetoothGattCharacteristic.FORMAT_UINT16);

        final String advertisingTimeout = mAdvertisingTimeoutView.getText().toString().trim();
        Utils.setValue(data, 2, Integer.parseInt(advertisingTimeout), BluetoothGattCharacteristic.FORMAT_UINT8);

        return data;
    }
}
