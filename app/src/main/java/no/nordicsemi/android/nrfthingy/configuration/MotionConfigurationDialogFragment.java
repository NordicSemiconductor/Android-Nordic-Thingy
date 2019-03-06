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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class MotionConfigurationDialogFragment extends DialogFragment {
    private static final String CONFIGURATION_DATA = "CONFIGURATION_DATA";

    private ViewGroup mPedometerContainer = null;
    private ViewGroup mTemperatureContainer = null;
    private ViewGroup mCompassContainer = null;
    private ViewGroup mMotionContainer = null;

    private TextInputLayout mPedometerIntervalLayout;
    private TextInputLayout mTemperatureIntervalLayout;
    private TextInputLayout mCompassIntervalLayout;
    private TextInputLayout mMotionIntervalLayout;

    private TextInputEditText mPedometerIntervalView;
    private TextInputEditText mTemperatureIntervalView;
    private TextInputEditText mCompassIntervalView;
    private TextInputEditText mMotionIntervalView;

    private RadioGroup mWakeOnMotionView;

    private RadioButton mOn;
    private RadioButton mOff;

    private String mPedometerInterval;
    private String mMotionTemperatureInterval;
    private String mCompassInterval;
    private String mMpuFrequency;
    private int mWakeOnMotion;

    private BluetoothDevice mDevice;
    private int mSettingsMode;

    private ThingySdkManager mThingySdkManager;

    public static MotionConfigurationDialogFragment newInstance(final int settingsMode, final BluetoothDevice device) {
        final MotionConfigurationDialogFragment fragment = new MotionConfigurationDialogFragment();

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
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_motion_configuration, null);

        updateUi(view, alertDialogBuilder);

        alertDialogBuilder.setView(view)
                .setPositiveButton(getString(R.string.confirm), null)
                .setNegativeButton(getString(R.string.cancel), null);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    configureMotionService();
                    //added a delay to update the ui to support Samsung S3 on dismissing
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                            updateParentFragmentUi();
                        }
                    }, 200);
                }
            }
        });

        return alertDialog;
    }

    private boolean validateInput() {
        if (mPedometerContainer != null && mPedometerContainer.getVisibility() == View.VISIBLE) {
            final String pedometerInterval = mPedometerIntervalView.getText().toString().trim();
            if (pedometerInterval.isEmpty()) {
                mPedometerIntervalLayout.setError(getString(R.string.error_pedometer_interval_empty));
                return false;
            } else {
                final int interval = Integer.parseInt(pedometerInterval);
                if (interval < ThingyUtils.PEDOMETER_MIN_INTERVAL || interval > ThingyUtils.NOTIFICATION_MAX_INTERVAL) { //values in ms
                    mPedometerIntervalLayout.setError(getString(R.string.error_interval_limit));
                    return false;
                }
            }
            return true;
        }
        if (mTemperatureContainer != null && mTemperatureContainer.getVisibility() == View.VISIBLE) {
            final String tempInterval = mTemperatureIntervalView.getText().toString().trim();
            if (tempInterval.isEmpty()) {
                mTemperatureIntervalLayout.setError(getString(R.string.error_temp_interval_empty));
                return false;
            } else {
                final int interval = Integer.parseInt(tempInterval);
                if (interval < ThingyUtils.TEMP_MIN_INTERVAL || interval > ThingyUtils.NOTIFICATION_MAX_INTERVAL) { //values in ms
                    mTemperatureIntervalLayout.setError(getString(R.string.error_interval_limit));
                    return false;
                }
            }
            return true;
        }
        if (mCompassContainer != null && mCompassContainer.getVisibility() == View.VISIBLE) {
            final String compassInterval = mCompassIntervalView.getText().toString().trim();
            if (compassInterval.isEmpty()) {
                mCompassIntervalLayout.setError(getString(R.string.error_compass_interval_empty));
                return false;
            } else {
                final int interval = Integer.parseInt(compassInterval);
                if (interval < ThingyUtils.COMPASS_MIN_INTERVAL || interval > ThingyUtils.NOTIFICATION_MAX_INTERVAL) { //values in ms
                    mCompassIntervalLayout.setError(getString(R.string.error_interval_limit));
                    return false;
                }
            }
            return true;
        }
        if (mMotionContainer != null && mMotionContainer.getVisibility() == View.VISIBLE) {
            final String motionInterval = mMotionIntervalView.getText().toString().trim();
            if (motionInterval.isEmpty()) {
                mMotionIntervalLayout.setError(getString(R.string.error_motion_interval_empty));
                return false;
            } else {
                final int interval = Integer.parseInt(motionInterval);
                if (interval < ThingyUtils.MPU_FREQ_MIN_INTERVAL || interval > ThingyUtils.MPU_FREQ_MAX_INTERVAL) { //values in hz
                    mMotionIntervalLayout.setError(getString(R.string.error_motion_interval_limit));
                    return false;
                }
            }
            return true;
        }

        return true;
    }

    private void configureMotionService() {
        if (mPedometerContainer != null && mPedometerContainer.getVisibility() == View.VISIBLE) {
            final int pedometerInterval = Integer.parseInt(mPedometerIntervalView.getText().toString());
            mThingySdkManager.setPedometerInterval(mDevice, pedometerInterval);
        } else if (mTemperatureContainer != null && mTemperatureContainer.getVisibility() == View.VISIBLE) {
            final int temperatureInterval = Integer.parseInt(mTemperatureIntervalView.getText().toString());
            mThingySdkManager.setTemperatureCompensationInterval(mDevice, temperatureInterval);
        } else if (mCompassContainer != null && mCompassContainer.getVisibility() == View.VISIBLE) {
            final int compassInterval = Integer.parseInt(mCompassIntervalView.getText().toString());
            mThingySdkManager.setMagnetometerCompensationInterval(mDevice, compassInterval);
        } else if (mMotionContainer != null && mMotionContainer.getVisibility() == View.VISIBLE) {
            final int motionInterval = Integer.parseInt(mMotionIntervalView.getText().toString());
            mThingySdkManager.setMotionProcessingFrequency(mDevice, motionInterval);
        } else if (mWakeOnMotionView != null && mWakeOnMotionView.getVisibility() == View.VISIBLE) {
            int mode;
            if (mOn.isChecked()) {
                mode = 1;
            } else
                mode = 0;
            mThingySdkManager.setWakeOnMotion(mDevice, mode);
        }
    }

    private void updateUi(final View view, final AlertDialog.Builder alertDialog) {
        mPedometerInterval = String.valueOf(mThingySdkManager.getPedometerInterval(mDevice));
        mMotionTemperatureInterval = String.valueOf(mThingySdkManager.getMotionTemperatureInterval(mDevice));
        mCompassInterval = String.valueOf(mThingySdkManager.getCompassInterval(mDevice));
        mMpuFrequency = String.valueOf(mThingySdkManager.getMotionInterval(mDevice));
        mWakeOnMotion = mThingySdkManager.getWakeOnMotionState(mDevice) ? 1 : 0;

        switch (mSettingsMode) {
            case 0:
                alertDialog.setTitle(getString(R.string.pedometer_interval_title));
                mPedometerContainer = view.findViewById(R.id.pedometer_container);
                mPedometerIntervalLayout = view.findViewById(R.id.layout_pedometer);
                mPedometerIntervalView = view.findViewById(R.id.interval_pedometer);
                mPedometerContainer.setVisibility(View.VISIBLE);
                mPedometerIntervalView.setText(mPedometerInterval);

                mPedometerIntervalView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().isEmpty()) {
                            mPedometerIntervalLayout.setError(getString(R.string.error_pedometer_interval_empty));
                            return;
                        }
                        int value = Integer.parseInt(s.toString());
                        if (value >= ThingyUtils.PEDOMETER_MIN_INTERVAL && value <= ThingyUtils.NOTIFICATION_MAX_INTERVAL) {
                            mPedometerIntervalLayout.setError(null);
                        } else {
                            mPedometerIntervalLayout.setError(getString(R.string.error_interval_limit));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                break;
            case 1:
                alertDialog.setTitle(getString(R.string.temperature_compensation_interval_title));
                mTemperatureContainer = view.findViewById(R.id.temperature_container);
                mTemperatureIntervalLayout = view.findViewById(R.id.layout_temperature);
                mTemperatureIntervalView = view.findViewById(R.id.interval_temperature);
                mTemperatureContainer.setVisibility(View.VISIBLE);
                mTemperatureIntervalView.setText(mMotionTemperatureInterval);

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
                        if (value >= ThingyUtils.TEMP_MIN_INTERVAL && value <= ThingyUtils.NOTIFICATION_MAX_INTERVAL) {
                            mTemperatureIntervalLayout.setError(null);
                        } else {
                            mTemperatureIntervalLayout.setError(getString(R.string.error_interval_limit));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                break;
            case 2:
                alertDialog.setTitle(getString(R.string.compass_compensation_interval));
                mCompassContainer = view.findViewById(R.id.compass_container);
                mCompassIntervalLayout = view.findViewById(R.id.layout_compass);
                mCompassIntervalView = view.findViewById(R.id.interval_compass);
                mCompassContainer.setVisibility(View.VISIBLE);
                mCompassIntervalView.setText(mCompassInterval);

                mCompassIntervalView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().isEmpty()) {
                            mCompassIntervalLayout.setError(getString(R.string.error_compass_interval_empty));
                            return;
                        }
                        int value = Integer.parseInt(s.toString());
                        if (value >= ThingyUtils.COMPASS_MIN_INTERVAL && value <= ThingyUtils.NOTIFICATION_MAX_INTERVAL) {
                            mCompassIntervalLayout.setError(null);
                        } else {
                            mCompassIntervalLayout.setError(getString(R.string.error_interval_limit));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                break;
            case 3:
                alertDialog.setTitle(getString(R.string.motion_frequency_title));
                mMotionContainer = view.findViewById(R.id.motion_container);
                mMotionIntervalLayout = view.findViewById(R.id.layout_motion);
                mMotionIntervalView = view.findViewById(R.id.interval_motion);
                mMotionContainer.setVisibility(View.VISIBLE);
                mMotionIntervalView.setText(mMpuFrequency);

                mMotionIntervalView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().isEmpty()) {
                            mMotionIntervalLayout.setError(getString(R.string.error_motion_interval_empty));
                            return;
                        }
                        int value = Integer.parseInt(s.toString());
                        if (value >= ThingyUtils.MPU_FREQ_MIN_INTERVAL && value <= ThingyUtils.MPU_FREQ_MAX_INTERVAL) {
                            mMotionIntervalLayout.setError(null);
                        } else {
                            mMotionIntervalLayout.setError(getString(R.string.error_motion_interval_limit));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                break;
            case 4:
                alertDialog.setTitle(getString(R.string.wake_on_motion_title));
                mWakeOnMotionView = view.findViewById(R.id.rg_motion_wake);
                mWakeOnMotionView.setVisibility(View.VISIBLE);

                mOn = view.findViewById(R.id.rb_motion_wake_on);
                mOff = view.findViewById(R.id.rb_motion_wake_off);
                final int pMode = mWakeOnMotion;
                if (pMode == 1) {
                    mOn.setChecked(true);
                } else {
                    mOff.setChecked(true);
                }
                break;
        }
    }

    private void updateParentFragmentUi() {
        switch (mSettingsMode) {
            case 0:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updatePedometerInterval();
                break;
            case 1:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateMotionTemperatureInterval();
                break;
            case 2:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateCompassInterval();
                break;
            case 3:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateMotionInterval();
                break;
            case 4:
                ((ThingyAdvancedSettingsChangeListener) getParentFragment()).updateWakeOnMotion();
                break;
        }
    }
}
