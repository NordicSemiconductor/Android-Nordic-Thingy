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
import android.os.Handler;
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
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class EnvironmentConfigurationDialogFragment extends DialogFragment {
    private ViewGroup mTemperatureContainer = null;
    private ViewGroup mPressureContainer = null;
    private ViewGroup mHumidityContainer = null;
    private ViewGroup mColorIntensityContainer = null;

    private TextInputLayout mTemperatureIntervalLayout;
    private TextInputLayout mPressureIntervalLayout;
    private TextInputLayout mColorIntervalLayout;
    private TextInputLayout mHumidityIntervalLayout;

    private TextInputEditText mTemperatureIntervalView;
    private TextInputEditText mPressureIntervalView;
    private TextInputEditText mColorIntervalView;
    private TextInputEditText mHumidityIntervalView;

    private RadioGroup mPressureModeView;
    private RadioGroup mGasModeView;

    private RadioButton mGasModeOne;
    private RadioButton mGasModeTwo;
    private RadioButton mGasModeThree;

    private String mTemperatureInterval;
    private String mPressureInterval;
    private String mHumidityInterval;
    private String mColorInterval;
    private String mGasMode;

    private BluetoothDevice mDevice;
    private int mSettingsMode;

    private ThingySdkManager mThingySdkManager;

    public static EnvironmentConfigurationDialogFragment newInstance(final int settingsMode, final BluetoothDevice device) {
        final EnvironmentConfigurationDialogFragment fragment = new EnvironmentConfigurationDialogFragment();

        final Bundle args = new Bundle();
        args.putInt(Utils.SETTINGS_MODE, settingsMode);
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSettingsMode = getArguments().getInt(Utils.SETTINGS_MODE);
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
        mThingySdkManager = ThingySdkManager.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_weather_configuration, null);

        updateUi(view, alertDialogBuilder);

        alertDialogBuilder.setView(view)
                .setPositiveButton(getString(R.string.confirm), null)
                .setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    configureThingy();
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
        if (mTemperatureContainer != null && mTemperatureContainer.getVisibility() == View.VISIBLE) {
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
            return true;
        }

        if (mPressureContainer != null && mPressureContainer.getVisibility() == View.VISIBLE) {
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
            return true;
        }

        if (mHumidityContainer != null && mHumidityContainer.getVisibility() == View.VISIBLE) {
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
            return true;
        }

        if (mColorIntensityContainer != null && mColorIntensityContainer.getVisibility() == View.VISIBLE) {
            final String colorInterval = mColorIntervalView.getText().toString().trim();
            if (colorInterval.isEmpty()) {
                mColorIntervalLayout.setError(getString(R.string.error_color_intensity_interval_empty));
                return false;
            } else {
                final int colorIntervalValue = Integer.parseInt(colorInterval);
                if (colorIntervalValue < ThingyUtils.COLOR_INTENSITY_MIN_INTERVAL || colorIntervalValue > ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                    mColorIntervalLayout.setError(getString(R.string.error_color_intensity_interval));
                    return false;
                }
            }
            return true;
        }

        return true;
    }

    private void configureThingy() {
        final int interval;
        if (mTemperatureContainer != null && mTemperatureContainer.getVisibility() == View.VISIBLE) {
            interval = Integer.parseInt(mTemperatureIntervalView.getText().toString().trim());
            mThingySdkManager.setTemperatureInterval(mDevice, interval);
        } else if (mPressureContainer != null && mPressureContainer.getVisibility() == View.VISIBLE) {
            interval = Integer.parseInt(mPressureIntervalView.getText().toString().trim());
            mThingySdkManager.setPressureInterval(mDevice, interval);
        } else if (mHumidityContainer != null && mHumidityContainer.getVisibility() == View.VISIBLE) {
            interval = Integer.parseInt(mHumidityIntervalView.getText().toString().trim());
            mThingySdkManager.setHumidityInterval(mDevice, interval);
        } else if (mColorIntensityContainer != null && mColorIntensityContainer.getVisibility() == View.VISIBLE) {
            interval = Integer.parseInt(mColorIntervalView.getText().toString().trim());
            mThingySdkManager.setColorIntensityInterval(mDevice, interval);
        } else if (mGasModeView != null && mGasModeView.getVisibility() == View.VISIBLE) {

            int mode;
            if (mGasModeOne.isChecked()) {
                mode = 1;
            } else if (mGasModeTwo.isChecked()) {
                mode = 2;
            } else {
                mode = 3;
            }
            mThingySdkManager.setGasMode(mDevice, mode);
        }
        //added a delay to update the ui to support Samsung S3 on dismissing
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dismiss();
                updateParentFragmentUi();
            }
        });
    }

    private void updateUi(final View view, final AlertDialog.Builder alertDialog) {
        mTemperatureInterval = String.valueOf(mThingySdkManager.getEnvironmentTemperatureInterval(mDevice));
        mPressureInterval = String.valueOf(mThingySdkManager.getPressureInterval(mDevice));
        mHumidityInterval = String.valueOf(mThingySdkManager.getHumidityInterval(mDevice));
        mColorInterval = String.valueOf(mThingySdkManager.getColorIntensityInterval(mDevice));
        mGasMode = String.valueOf(mThingySdkManager.getGasMode(mDevice));

        switch (mSettingsMode) {
            case 0:
                alertDialog.setTitle(getString(R.string.temperature_interval_title));
                mTemperatureContainer = view.findViewById(R.id.temperature_container);
                mTemperatureIntervalLayout = view.findViewById(R.id.layout_temperature);
                mTemperatureIntervalView = view.findViewById(R.id.interval_temperature);
                mTemperatureContainer.setVisibility(View.VISIBLE);
                mTemperatureIntervalView.setText(mTemperatureInterval);

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

                break;
            case 1:
                alertDialog.setTitle(getString(R.string.pressure_interval_title));
                mPressureContainer = view.findViewById(R.id.pressure_container);
                mPressureIntervalLayout = view.findViewById(R.id.layout_pressure);
                mPressureIntervalView = view.findViewById(R.id.interval_pressure);
                mPressureContainer.setVisibility(View.VISIBLE);
                mPressureIntervalView.setText(mPressureInterval);

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
                break;
            case 2:
                alertDialog.setTitle(getString(R.string.humidity_interval_title));
                mHumidityContainer = view.findViewById(R.id.humidity_container);
                mHumidityIntervalLayout = view.findViewById(R.id.layout_humidity);
                mHumidityIntervalView = view.findViewById(R.id.interval_humidity);
                mHumidityContainer.setVisibility(View.VISIBLE);
                mHumidityIntervalView.setText(mHumidityInterval);

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
                break;
            case 3:
                alertDialog.setTitle(getString(R.string.color_intensity_interval_title));
                mColorIntensityContainer = view.findViewById(R.id.color_intensity_container);
                mColorIntervalLayout = view.findViewById(R.id.layout_color_intensity);
                mColorIntervalView = view.findViewById(R.id.interval_color_intensity);
                mColorIntensityContainer.setVisibility(View.VISIBLE);
                mColorIntervalView.setText(mColorInterval);

                mColorIntervalView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().isEmpty()) {
                            mColorIntervalLayout.setError(getString(R.string.error_color_intensity_interval));
                            return;
                        }
                        int value = Integer.parseInt(s.toString());
                        if (value >= ThingyUtils.COLOR_INTENSITY_MIN_INTERVAL && value <= ThingyUtils.ENVIRONMENT_NOTIFICATION_MAX_INTERVAL) {
                            mColorIntervalLayout.setError(null);
                        } else {
                            mColorIntervalLayout.setError(getString(R.string.error_color_intensity_interval));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                break;
            case 4:
                alertDialog.setTitle(getString(R.string.gas_mode_title));
                mGasModeView = view.findViewById(R.id.rg_gas_mode);
                mGasModeView.setVisibility(View.VISIBLE);
                mGasModeView.check(Integer.parseInt(mGasMode));

                mGasModeOne = view.findViewById(R.id.rb_gas_one);
                mGasModeTwo = view.findViewById(R.id.rb_gas_two);
                mGasModeThree = view.findViewById(R.id.rb_gas_three);

                final int gMode = Integer.parseInt(mGasMode);
                if (gMode == 1) {
                    mGasModeOne.setChecked(true);
                } else if (gMode == 2) {
                    mGasModeTwo.setChecked(true);
                } else if (gMode == 3) {
                    mGasModeThree.setChecked(true);
                }
                break;
        }
    }

    private void updateParentFragmentUi() {
        switch (mSettingsMode) {
            case 0:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateTemperatureInterval();
                break;
            case 1:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updatePressureInterval();
                break;
            case 2:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateHumidityInterval();
                break;
            case 3:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateColorIntensityInterval();
                break;
            case 4:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateGasMode();
                break;
        }
    }
}
