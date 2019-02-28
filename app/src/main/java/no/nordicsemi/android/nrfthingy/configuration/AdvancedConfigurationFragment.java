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
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class AdvancedConfigurationFragment extends Fragment implements ThingyAdvancedSettingsChangeListener {
    private BluetoothDevice mDevice;
    private int mSettingsMode;
    private ThingySdkManager mThingySdkManager;

    private TextView mTemperatureIntervalSummary;
    private TextView mPressureIntervalSummary;
    private TextView mHumidityIntervalSummary;
    private TextView mColorIntensityIntervalSummary;
    private TextView mGasModeSummary;
    private TextView mPedometerIntervalSummary;
    private TextView mMotionTemperatureIntervalSummary;
    private TextView mCompassIntervalSummary;
    private TextView mMotionIntervalSummary;
    private TextView mWakeOnMotionSummary;

    public static AdvancedConfigurationFragment getInstance(final BluetoothDevice device) {
        final AdvancedConfigurationFragment fragment = new AdvancedConfigurationFragment();

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
        final View rootView = inflater.inflate(R.layout.fragment_advanced_configuration, container, false);
        final LinearLayout temperature = rootView.findViewById(R.id.category_temp_interval);
        mTemperatureIntervalSummary = rootView.findViewById(R.id.category_temp_interval_summary);
        final LinearLayout pressure = rootView.findViewById(R.id.category_pressure_interval);
        mPressureIntervalSummary = rootView.findViewById(R.id.category_pressure_interval_summary);
        final LinearLayout humidity = rootView.findViewById(R.id.category_humidity_interval);
        mHumidityIntervalSummary = rootView.findViewById(R.id.category_humidity_interval_summary);
        final LinearLayout colorIntensity = rootView.findViewById(R.id.category_color_intensity_interval);
        mColorIntensityIntervalSummary = rootView.findViewById(R.id.category_color_intensity_interval_summary);
        final LinearLayout gasMode = rootView.findViewById(R.id.category_gas_mode);
        mGasModeSummary = rootView.findViewById(R.id.category_gas_mode_summary);
        final LinearLayout pedometer = rootView.findViewById(R.id.category_pedometer_interval);
        mPedometerIntervalSummary = rootView.findViewById(R.id.category_pedometer_interval_summary);
        final LinearLayout temperatureMotion = rootView.findViewById(R.id.category_motion_temperature_interval);
        mMotionTemperatureIntervalSummary = rootView.findViewById(R.id.category_motion_temperature_interval_summary);
        final LinearLayout compass = rootView.findViewById(R.id.category_compass_interval);
        mCompassIntervalSummary = rootView.findViewById(R.id.category_compass_interval_summary);
        final LinearLayout motion = rootView.findViewById(R.id.category_motion_interval);
        mMotionIntervalSummary = rootView.findViewById(R.id.category_motion_interval_summary);
        final LinearLayout wakeOnMotion = rootView.findViewById(R.id.category_wake_on_motion);
        mWakeOnMotionSummary = rootView.findViewById(R.id.category_wake_on_motion_summary);

        final DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        final String thingyName = databaseHelper.getDeviceName(mDevice.getAddress());

        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 0;
                    final EnvironmentConfigurationDialogFragment fragment = EnvironmentConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        pressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 1;
                    final EnvironmentConfigurationDialogFragment fragment = EnvironmentConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        humidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 2;
                    final EnvironmentConfigurationDialogFragment fragment = EnvironmentConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        colorIntensity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 3;
                    final EnvironmentConfigurationDialogFragment fragment = EnvironmentConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        gasMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 4;
                    final EnvironmentConfigurationDialogFragment fragment = EnvironmentConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        pedometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 0;
                    final MotionConfigurationDialogFragment fragment = MotionConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        temperatureMotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 1;
                    final MotionConfigurationDialogFragment fragment = MotionConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        compass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 2;
                    final MotionConfigurationDialogFragment fragment = MotionConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        motion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 3;
                    final MotionConfigurationDialogFragment fragment = MotionConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        wakeOnMotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mThingySdkManager != null && mThingySdkManager.isConnected(mDevice)) {
                    mSettingsMode = 4;
                    final MotionConfigurationDialogFragment fragment = MotionConfigurationDialogFragment.newInstance(mSettingsMode, mDevice);
                    fragment.show(getChildFragmentManager(), null);
                } else {
                    MessageDialogFragment fragment = MessageDialogFragment.newInstance(getString(R.string.thingy_disconnected, thingyName), getString(R.string.no_thingy_connected_configuration, thingyName));
                    fragment.show(getChildFragmentManager(), null);
                }
            }
        });

        updateTemperatureInterval();
        updatePressureInterval();
        updateHumidityInterval();
        updateColorIntensityInterval();
        updateGasMode();
        updatePedometerInterval();
        updateMotionTemperatureInterval();
        updateCompassInterval();
        updateMotionInterval();
        updateWakeOnMotion();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mConfigurationBroadcastReceiver);
    }

    @Override
    public void updateTemperatureInterval() {
        final int interval = mThingySdkManager.getEnvironmentTemperatureInterval(mDevice);
        if (interval > 0) {
            mTemperatureIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updatePressureInterval() {
        final int interval = mThingySdkManager.getPressureInterval(mDevice);
        if (interval > 0) {
            mPressureIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updateHumidityInterval() {
        final int interval = mThingySdkManager.getHumidityInterval(mDevice);
        if (interval > 0) {
            mHumidityIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updateColorIntensityInterval() {
        final int interval = mThingySdkManager.getColorIntensityInterval(mDevice);
        if (interval > 0) {
            mColorIntensityIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updateGasMode() {
        final int gasMode = mThingySdkManager.getGasMode(mDevice);
        if (gasMode > 0) {
            if (gasMode == 1) {
                mGasModeSummary.setText(R.string.gas_mode_one);
            } else if (gasMode == 2) {
                mGasModeSummary.setText(R.string.gas_mode_two);
            } else if (gasMode == 3) {
                mGasModeSummary.setText(R.string.gas_mode_three);
            }
        }
    }

    @Override
    public void updatePedometerInterval() {
        final int interval = mThingySdkManager.getPedometerInterval(mDevice);
        if (interval > 0) {
            mPedometerIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updateMotionTemperatureInterval() {
        final int interval = mThingySdkManager.getMotionTemperatureInterval(mDevice);
        if (interval > 0) {
            mMotionTemperatureIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updateCompassInterval() {
        final int interval = mThingySdkManager.getCompassInterval(mDevice);
        if (interval > 0) {
            mCompassIntervalSummary.setText(getString(R.string.interval_ms, interval));
        }
    }

    @Override
    public void updateMotionInterval() {
        final int interval = mThingySdkManager.getMotionInterval(mDevice);
        if (interval > 0) {
            mMotionIntervalSummary.setText(getString(R.string.interval_hz, interval));
        }
    }

    @Override
    public void updateWakeOnMotion() {
        mWakeOnMotionSummary.setText(String.valueOf(mThingySdkManager.getWakeOnMotionState(mDevice) ? getString(R.string.on) : getString(R.string.off)));
    }
}
