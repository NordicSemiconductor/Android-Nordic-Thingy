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

package no.nordicsemi.android.nrfthingy;

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
import android.widget.Spinner;

import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class EnvironmentServiceSettingsFragment extends DialogFragment {

    private TextInputLayout mTemperatureIntervalLayout;
    private TextInputLayout mPressureIntervalLayout;
    private TextInputLayout mHumidityIntervalLayout;
    private TextInputLayout mColorIntensityIntervalLayout;

    private TextInputEditText mTemperatureIntervalView;
    private TextInputEditText mPressureIntervalView;
    private TextInputEditText mHumidityIntervalView;
    private TextInputEditText mColorIntensityIntervalView;
    private Spinner mGasModeView;

    private BluetoothDevice mDevice;

    private ThingySdkManager mThingySdkManager;

    public static EnvironmentServiceSettingsFragment newInstance(final BluetoothDevice device) {
        EnvironmentServiceSettingsFragment fragment = new EnvironmentServiceSettingsFragment();
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
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(getString(R.string.environment_settings_title));
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_environment_settings, null);

        mTemperatureIntervalLayout = view.findViewById(R.id.layout_temperature);
        mPressureIntervalLayout = view.findViewById(R.id.layout_pressure);
        mHumidityIntervalLayout = view.findViewById(R.id.layout_humidity);
        mColorIntensityIntervalLayout = view.findViewById(R.id.layout_color_intensity);

        mTemperatureIntervalView = view.findViewById(R.id.interval_temperature);
        mPressureIntervalView = view.findViewById(R.id.interval_pressure);
        mHumidityIntervalView = view.findViewById(R.id.interval_humidity);
        mColorIntensityIntervalView = view.findViewById(R.id.interval_color_intensity);

        mGasModeView = view.findViewById(R.id.spinner_gas_mode);

        mTemperatureIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    mTemperatureIntervalLayout.setError(getString(R.string.error_temp_interval_empty));
                    return;
                }
                int value = Integer.parseInt(s.toString());
                if (value >= ThingyUtils.TEMP_MIN_INTERVAL && value <= ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                    mTemperatureIntervalLayout.setError(null);
                } else {
                    mTemperatureIntervalLayout.setError(getString(R.string.error_temp_interval));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPressureIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    mPressureIntervalLayout.setError(getString(R.string.error_pressure_interval_empty));
                    return;
                }
                int value = Integer.parseInt(s.toString());
                if (value >= ThingyUtils.PRESSURE_MIN_INTERVAL && value <= ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                    mPressureIntervalLayout.setError(null);
                } else {
                    mPressureIntervalLayout.setError(getString(R.string.error_pressure_interval));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mHumidityIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    mHumidityIntervalLayout.setError(getString(R.string.error_humidity_interval_empty));
                    return;
                }
                int value = Integer.parseInt(s.toString());
                if (value >= ThingyUtils.HUMIDITY_MIN_INTERVAL && value <= ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                    mHumidityIntervalLayout.setError(null);
                } else {
                    mHumidityIntervalLayout.setError(getString(R.string.error_humidity_interval));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mColorIntensityIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    mColorIntensityIntervalLayout.setError(getString(R.string.error_color_intensity_interval_empty));
                    return;
                }
                int value = Integer.parseInt(s.toString());
                if (value >= ThingyUtils.COLOR_INTENSITY_MIN_INTERVAL && value <= ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                    mColorIntensityIntervalLayout.setError(null);
                } else {
                    mColorIntensityIntervalLayout.setError(getString(R.string.error_color_intensity_interval));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        alertDialogBuilder.setView(view).setPositiveButton(getString(R.string.confirm), null).setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    configureEnvironmentService();
                    dismiss();
                }
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        updateUi();

        return alertDialog;
    }

    private boolean validateInput() {
        final String tempInterval = mTemperatureIntervalView.getText().toString().trim();
        if (tempInterval.isEmpty()) {
            mTemperatureIntervalLayout.setError(getString(R.string.error_temp_interval_empty));
            return false;
        } else {
            final int tempIntervalValue = Integer.parseInt(tempInterval);
            if (tempIntervalValue < ThingyUtils.TEMP_MIN_INTERVAL || tempIntervalValue > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                mTemperatureIntervalLayout.setError(getString(R.string.error_temp_interval));
                return false;
            }
        }

        final String pressureInterval = mPressureIntervalView.getText().toString().trim();
        if (pressureInterval.isEmpty()) {
            mPressureIntervalLayout.setError(getString(R.string.error_pressure_interval_empty));
            return false;
        } else {
            final int pressureIntervalValue = Integer.parseInt(pressureInterval);
            if (pressureIntervalValue < ThingyUtils.PRESSURE_MIN_INTERVAL || pressureIntervalValue > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                mPressureIntervalLayout.setError(getString(R.string.error_pressure_interval));
                return false;
            }
        }

        final String humidityInterval = mHumidityIntervalView.getText().toString().trim();
        if (humidityInterval.isEmpty()) {
            mHumidityIntervalLayout.setError(getString(R.string.error_humidity_interval_empty));
            return false;
        } else {
            final int humidityIntervalValue = Integer.parseInt(humidityInterval);
            if (humidityIntervalValue < ThingyUtils.HUMIDITY_MIN_INTERVAL || humidityIntervalValue > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                mHumidityIntervalLayout.setError(getString(R.string.error_humidity_interval));
                return false;
            }
        }

        final String colorInterval = mColorIntensityIntervalView.getText().toString().trim();
        if (colorInterval.isEmpty()) {
            mColorIntensityIntervalLayout.setError(getString(R.string.error_color_intensity_interval_empty));
            return false;
        } else {
            final int colorIntervalValue = Integer.parseInt(colorInterval);
            if (colorIntervalValue < ThingyUtils.COLOR_INTENSITY_MIN_INTERVAL || colorIntervalValue > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                mColorIntensityIntervalLayout.setError(getString(R.string.error_color_intensity_interval));
                return false;
            }
        }

        return true;
    }

    private void configureEnvironmentService() {
        final int temperatureInterval = Integer.parseInt(mTemperatureIntervalView.getText().toString().trim());

        final int pressureInterval = Integer.parseInt(mPressureIntervalView.getText().toString().trim());

        final int humidityInterval = Integer.parseInt(mHumidityIntervalView.getText().toString().trim());

        final int colorIntensityInterval = Integer.parseInt(mColorIntensityIntervalView.getText().toString().trim());

        final int gasMode = mGasModeView.getSelectedItemPosition() + 1;
        mThingySdkManager.setEnvironmentConfigurationCharacteristic(mDevice, temperatureInterval, pressureInterval, humidityInterval, colorIntensityInterval, gasMode);
    }

    private void updateUi() {
        final String mTemperatureInterval = String.valueOf(mThingySdkManager.getEnvironmentTemperatureInterval(mDevice));
        mTemperatureIntervalView.setText(mTemperatureInterval);

        final String mPressureInterval = String.valueOf(mThingySdkManager.getPressureInterval(mDevice));
        mPressureIntervalView.setText(mPressureInterval);

        final String mHumidityInterval = String.valueOf(mThingySdkManager.getHumidityInterval(mDevice));
        mHumidityIntervalView.setText(mHumidityInterval);

        final String mColorIntensityInterval = String.valueOf(mThingySdkManager.getColorIntensityInterval(mDevice));
        mColorIntensityIntervalView.setText(mColorIntensityInterval);

        final String mGasMode = String.valueOf(mThingySdkManager.getGasMode(mDevice));
        mGasModeView.setSelection(Integer.valueOf(mGasMode) - 1);
    }
}
