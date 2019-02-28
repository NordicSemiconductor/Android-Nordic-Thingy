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
import android.widget.TextView;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class ConnParamCharConfigurationDialogFragment extends DialogFragment {
    private TextInputLayout mMinConnectionIntervalLayout;
    private TextInputLayout mMaxConnectionIntervalLayout;
    private TextInputLayout mSlaveLatencyLayout;
    private TextInputLayout mSupervisionTimeoutLayout;

    private TextInputEditText mMinConnectionIntervalView;
    private TextInputEditText mMaxConnectionIntervalView;
    private TextInputEditText mSlaveLatencyView;
    private TextInputEditText mSupervisionTimeoutView;

    private TextView mMinInterval;
    private TextView mMaxInterval;
    private TextView mConnSupervisionTimeout;

    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;
    private int mMinConnectionIntervalUnits;
    private int mMaxConnectionIntervalUnits;
    private int mSlaveLatency;
    private int mSupervisionTimeoutUnits;

    public static ConnParamCharConfigurationDialogFragment newInstance(final BluetoothDevice device) {
        ConnParamCharConfigurationDialogFragment fragment = new ConnParamCharConfigurationDialogFragment();
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
        mThingySdkManager = ThingySdkManager.getInstance();
        mMinConnectionIntervalUnits = mThingySdkManager.getMinimumConnectionIntervalUnits(mDevice);
        mMaxConnectionIntervalUnits = mThingySdkManager.getMaximumConnectionIntervalUnits(mDevice);
        mSlaveLatency = mThingySdkManager.getSlaveLatency(mDevice);
        mSupervisionTimeoutUnits = mThingySdkManager.getConnectionSupervisionTimeout(mDevice);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(getString(R.string.connection_parameters_title));
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_conn_params, null);

        mMinConnectionIntervalLayout = view.findViewById(R.id.layout_min_connection_params);
        mMaxConnectionIntervalLayout = view.findViewById(R.id.layout_max_connection_params);
        mSlaveLatencyLayout = view.findViewById(R.id.layout_slave_latency);
        mSupervisionTimeoutLayout = view.findViewById(R.id.layout_conn_sup_timeout);

        mMinConnectionIntervalView = view.findViewById(R.id.min_conn_int_view);
        mMaxConnectionIntervalView = view.findViewById(R.id.max_conn_int_view);
        mSlaveLatencyView = view.findViewById(R.id.slave_latency_view);
        mSupervisionTimeoutView = view.findViewById(R.id.conn_sup_timeout_view);

        mMinInterval = view.findViewById(R.id.min_interval);
        mMaxInterval = view.findViewById(R.id.max_interval);
        mConnSupervisionTimeout = view.findViewById(R.id.supervision_timeout);

        alertDialogBuilder.setView(view).setPositiveButton(getString(R.string.confirm), null).setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        updateUi();

        mMinConnectionIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final String connInterval = s.toString();
                if (!connInterval.isEmpty()) {
                    int value = Integer.parseInt(connInterval);
                    final double minConnectionInterval = value * 1.25;
                    mMinInterval.setText(getString(R.string.decimal_format_ms, minConnectionInterval));
                    if (ThingyUtils.MIN_CONN_INTERVAL <= minConnectionInterval && ThingyUtils.MAX_CONN_INTERVAL >= minConnectionInterval) {
                        mMinConnectionIntervalLayout.setError(null);
                    } else {
                        mMinConnectionIntervalLayout.setError(getString(R.string.error_invalid_conn_interval));
                    }
                } else {
                    mMinConnectionIntervalLayout.setError(getString(R.string.error_empty_min_conn_interval));
                }
            }
        });

        mMaxConnectionIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final String value = s.toString();
                if (!value.isEmpty()) {
                    int maxConnIntervalUnits = Integer.parseInt(value);
                    final double maxConnectionInterval = maxConnIntervalUnits * 1.25;
                    mMaxInterval.setText(getString(R.string.decimal_format_ms, maxConnectionInterval));
                    if (ThingyUtils.MIN_CONN_INTERVAL <= maxConnectionInterval && ThingyUtils.MAX_CONN_INTERVAL >= maxConnectionInterval) {
                        final int slaveLatency = Integer.parseInt(mSlaveLatencyView.getText().toString().trim());
                        final int supervisionTimeoutUnits = Integer.parseInt(mSupervisionTimeoutView.getText().toString().trim());
                        if (ThingyUtils.validateMaxConnectionInterval(slaveLatency, maxConnIntervalUnits, supervisionTimeoutUnits)) {
                            mMaxConnectionIntervalLayout.setError(null);
                        } else {
                            mMaxConnectionIntervalLayout.setError(getString(R.string.invalid_value));
                        }
                    } else {
                        mMaxConnectionIntervalLayout.setError(getString(R.string.error_invalid_conn_interval));
                    }
                } else {
                    mMaxConnectionIntervalLayout.setError(getString(R.string.error_empty_max_conn_interval));
                }
            }
        });

        mSlaveLatencyView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String val = s.toString();
                if (!val.isEmpty()) {
                    final int slaveLatency = Integer.parseInt(val);
                    if (slaveLatency >= ThingyUtils.MIN_SLAVE_LATENCY && slaveLatency <= ThingyUtils.MAX_SLAVE_LATENCY) {
                        final int maxConnIntervalUnits = Integer.parseInt(mMaxConnectionIntervalView.getText().toString().trim());
                        final int supervisionTimeoutUnits = Integer.parseInt(mSupervisionTimeoutView.getText().toString().trim());
                        if (ThingyUtils.validateSlaveLatency(slaveLatency, maxConnIntervalUnits, supervisionTimeoutUnits)) {
                            mSlaveLatencyLayout.setError(null);
                        } else {
                            mSlaveLatencyLayout.setError(getString(R.string.invalid_value));
                        }
                    } else {
                        mSlaveLatencyLayout.setError(getString(R.string.error_invalid_slave_latency));
                    }
                } else {
                    mSlaveLatencyLayout.setError(getString(R.string.error_empty_slave_latency));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSupervisionTimeoutView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String value = s.toString();
                if (!value.isEmpty()) {
                    final int supervisionTimeoutUnits = Integer.parseInt(value);
                    final int superVisionTimeOut = supervisionTimeoutUnits * 10;
                    mConnSupervisionTimeout.setText(getString(R.string.interval_ms, superVisionTimeOut));
                    if (supervisionTimeoutUnits >= ThingyUtils.MIN_SUPERVISION_TIMEOUT && supervisionTimeoutUnits <= ThingyUtils.MAX_SUPERVISION_TIMEOUT) {
                        final String sLatency = mSlaveLatencyView.getText().toString().trim();
                        if (sLatency.isEmpty()) {
                            return;
                        }
                        final int slaveLatency = Integer.parseInt(sLatency);

                        final String mConnInterval = mMaxConnectionIntervalView.getText().toString().trim();
                        if (mConnInterval.isEmpty()) {
                            return;
                        }
                        final int maxConnIntervalUnits = Integer.parseInt(mConnInterval);
                        if (ThingyUtils.validateSupervisionTimeout(slaveLatency, maxConnIntervalUnits, supervisionTimeoutUnits)) {
                            mSupervisionTimeoutLayout.setError(null);
                        } else {
                            mSupervisionTimeoutLayout.setError(getString(R.string.invalid_value));
                        }
                    } else {
                        mSupervisionTimeoutLayout.setError(getString(R.string.error_invalid_supervision_timeout));
                    }
                } else {
                    mSupervisionTimeoutLayout.setError(getString(R.string.error_empty_supervision_timeout));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput() && mThingySdkManager != null) {
                    final int minConnInterval = Integer.parseInt(mMinConnectionIntervalView.getText().toString().trim());
                    final int maxConnInterval = Integer.parseInt(mMaxConnectionIntervalView.getText().toString().trim());
                    final int slaveLatency = Integer.parseInt(mSlaveLatencyView.getText().toString().trim());
                    final int supervisionTimeout = Integer.parseInt(mSupervisionTimeoutView.getText().toString().trim());
                    if (mThingySdkManager.setConnectionParameters(mDevice, minConnInterval, maxConnInterval, slaveLatency, supervisionTimeout)) {
                        dismiss();
                    } else {
                        ThingyUtils.showToast(getActivity(), getString(R.string.error_configuring_char));
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

    private void updateUi() {
        mMinConnectionIntervalView.setText(String.valueOf(mMinConnectionIntervalUnits));
        mMinInterval.setText(getString(R.string.decimal_format_ms, mMinConnectionIntervalUnits * 1.25));

        mMaxConnectionIntervalView.setText(String.valueOf(mMaxConnectionIntervalUnits));
        mMaxInterval.setText(getString(R.string.decimal_format_ms, mMaxConnectionIntervalUnits * 1.25));

        mSlaveLatencyView.setText(String.valueOf(mSlaveLatency));

        mSupervisionTimeoutView.setText(String.valueOf(mSupervisionTimeoutUnits));
        mConnSupervisionTimeout.setText(getString(R.string.interval_ms, mSupervisionTimeoutUnits * 10));
    }

    private boolean validateInput() {
        final String minConInterval = mMinConnectionIntervalView.getText().toString().trim();
        if (minConInterval.isEmpty()) {
            mMinConnectionIntervalLayout.setError(getString(R.string.error_empty_min_conn_interval));
            return false;
        } else {
            boolean valid;
            int i = 0;
            try {
                i = Integer.parseInt(minConInterval);
                valid = (i & 0xFFFF0000) == 0 || (i & 0xFFFF0000) == 0xFFFF0000;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                mMinConnectionIntervalLayout.setError(getString(R.string.error_uint16));
                return false;
            }

            if (i < ThingyUtils.MIN_CONN_VALUE) {
                mMinConnectionIntervalLayout.setError(getString(R.string.error_invalid_conn_interval));
                return false;
            }

            if (i > ThingyUtils.MAX_CONN_VALUE) {
                mMinConnectionIntervalLayout.setError(getString(R.string.error_invalid_conn_interval));
                return false;
            }
        }

        final String maxConInterval = mMaxConnectionIntervalView.getText().toString().trim();
        if (maxConInterval.isEmpty()) {
            mMaxConnectionIntervalLayout.setError(getString(R.string.error_empty_max_conn_interval));
            return false;
        } else {
            boolean valid;
            int i = 0;
            try {
                i = Integer.parseInt(maxConInterval);
                valid = (i & 0xFFFF0000) == 0 || (i & 0xFFFF0000) == 0xFFFF0000;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                mMaxConnectionIntervalLayout.setError(getString(R.string.error_uint16));
                return false;
            }

            if (i < ThingyUtils.MIN_CONN_VALUE) {
                mMaxConnectionIntervalLayout.setError(getString(R.string.error_invalid_conn_interval));
                return false;
            }

            if (i > ThingyUtils.MAX_CONN_VALUE) {
                mMaxConnectionIntervalLayout.setError(getString(R.string.error_invalid_conn_interval));
                return false;
            }
        }

        final String slaveLatency = mSlaveLatencyView.getText().toString().trim();
        if (slaveLatency.isEmpty()) {
            mSlaveLatencyLayout.setError(getString(R.string.error_empty_slave_latency));
            return false;
        } else {
            boolean valid;
            int i = 0;
            try {
                i = Integer.parseInt(slaveLatency);
                valid = (i & 0xFFFF0000) == 0 || (i & 0xFFFF0000) == 0xFFFF0000;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                mSlaveLatencyLayout.setError(getString(R.string.error_uint16));
                return false;
            }

            if (ThingyUtils.MIN_SLAVE_LATENCY > i * 10 || ThingyUtils.MAX_SLAVE_LATENCY < i) {
                mSlaveLatencyLayout.setError(getString(R.string.error_invalid_slave_latency));
                return false;
            }
        }

        final String supervisionTimeout = mSupervisionTimeoutView.getText().toString().trim();
        if (supervisionTimeout.isEmpty()) {
            mSupervisionTimeoutView.setError(getString(R.string.error_empty_supervision_timeout));
            return false;
        } else {
            boolean valid;
            int i = 0;
            try {
                i = Integer.parseInt(supervisionTimeout);
                valid = (i & 0xFFFF0000) == 0 || (i & 0xFFFF0000) == 0xFFFF0000;
            } catch (NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                mSupervisionTimeoutLayout.setError(getString(R.string.error_uint16));
                return false;
            }

            if (ThingyUtils.MIN_SUPERVISION_TIMEOUT > i * 10 || ThingyUtils.MAX_SUPERVISION_TIMEOUT < i * 10) {
                mSupervisionTimeoutLayout.setError(getString(R.string.error_invalid_supervision_timeout));
                return false;
            }
        }
        return true;
    }
}
