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

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.thingy.Thingy;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class UiFragment extends Fragment implements ScannerFragmentListener {
    private static final String LAST_VISIBLE_UI_MODE = "selected_color";

    private int mSelectedColorIndex = ThingyUtils.DEFAULT_LED_INTENSITY;
    private int mSelectedRgbColorIntensity;

    private LinearLayout mLedRgbColorContainer;
    private LinearLayout mLedControllerContainer;
    private LinearLayout mRgbIntensityControllerContainer;
    private ConstraintLayout mColorContainer;

    private TextView mIntensity;
    private TextView mDelay;
    private TextView mButtonState;
    private TextView mRedIntensityView;
    private TextView mBlueIntensityView;
    private TextView mGreenIntensityView;

    private SeekBar mRedIntensity;
    private SeekBar mGreenIntensity;
    private SeekBar mBlueIntensity;
    private SeekBar mLedIntensity;
    private SeekBar mBreatheDelay;

    private Button mOneShot;
    private Button mConstant;
    private Button mBreathe;
    private Button mOff;
    private TextView mLedRgbView;

    private ImageView mLedRgb;
    private ImageView mLedRed;
    private ImageView mLedGreen;
    private ImageView mLedYellow;
    private ImageView mLedBlue;
    private ImageView mLedPurple;
    private ImageView mLedCyan;
    private ImageView mLedWhite;

    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;

    private DatabaseHelper mDatabaseHelper;
    private int mCurrentDelay;
    private int mCurrentIntensity;
    private int mCurrentLedMode;

    private Drawable mSwatchSelected;
    private GradientDrawable mRgbDrawable;

    private ThingyListener mThingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            //Connectivity callbacks handled by main activity
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            //Connectivity callbacks handled by main activity
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            if (device.equals(mDevice)) {
                loadLedUI();
            }
        }

        @Override
        public void onBatteryLevelChanged(final BluetoothDevice bluetoothDevice, final int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, final String pressure) {
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, final String humidity) {
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, final int eco2, final int tvoc) {
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, final float red, final float green, final float blue, final float alpha) {
        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {
            switch (buttonState) {
                case ThingyUtils.BUTTON_STATE_RELEASED:
                    mButtonState.setText(R.string.button_state_released);
                    break;
                case ThingyUtils.BUTTON_STATE_PRESSED:
                    mButtonState.setText(R.string.button_state_pressed);
                    break;
                default:
                    mButtonState.setText(R.string.button_state_unknown);
                    break;
            }
        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, final byte[] data) {

        }
    };

    public static UiFragment newInstance(final BluetoothDevice device) {
        UiFragment fragment = new UiFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
        mThingySdkManager = ThingySdkManager.getInstance();
        mDatabaseHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_ui, container, false);

        mLedRgbColorContainer = rootView.findViewById(R.id.led_rgb_color_container);
        mLedControllerContainer = rootView.findViewById(R.id.led_controller_container);
        mRgbIntensityControllerContainer = rootView.findViewById(R.id.led_rgb_container);
        mColorContainer = rootView.findViewById(R.id.led_color_container);

        mLedRgb = rootView.findViewById(R.id.img_led_rgb);
        mLedRed = rootView.findViewById(R.id.img_led_red);
        mLedGreen = rootView.findViewById(R.id.img_led_green);
        mLedYellow = rootView.findViewById(R.id.img_led_yellow);
        mLedBlue = rootView.findViewById(R.id.img_led_blue);
        mLedPurple = rootView.findViewById(R.id.img_led_purple);
        mLedCyan = rootView.findViewById(R.id.img_led_cyan);
        mLedWhite = rootView.findViewById(R.id.img_led_white);

        mSwatchSelected = ContextCompat.getDrawable(requireContext(), R.drawable.ic_colorpicker_swatch_selected);
        mRgbDrawable = (GradientDrawable) mLedRgb.getDrawable();

        mLedRgbView = rootView.findViewById(R.id.led_rgb);
        mRedIntensityView = rootView.findViewById(R.id.red_val);
        mGreenIntensityView = rootView.findViewById(R.id.green_val);
        mBlueIntensityView = rootView.findViewById(R.id.blue_val);
        mIntensity = rootView.findViewById(R.id.intensity_percentage);
        mDelay = rootView.findViewById(R.id.delay_ms);
        mButtonState = rootView.findViewById(R.id.button_state);
        mOneShot = rootView.findViewById(R.id.one_shot);
        mConstant = rootView.findViewById(R.id.constant);
        mBreathe = rootView.findViewById(R.id.breathe);
        mOff = rootView.findViewById(R.id.off);

        mRedIntensity = rootView.findViewById(R.id.seek_bar_red);
        mGreenIntensity = rootView.findViewById(R.id.seek_bar_green);
        mBlueIntensity = rootView.findViewById(R.id.seek_bar_blue);
        mLedIntensity = rootView.findViewById(R.id.seek_bar_intensity);
        mBreatheDelay = rootView.findViewById(R.id.seek_bar_delay);

        mIntensity.setText(getString(R.string.led_percentage, ThingyUtils.DEFAULT_LED_INTENSITY));
        mDelay.setText(getString(R.string.interval_ms, ThingyUtils.DEFAULT_BREATHE_INTERVAL));

        mLedRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_RED);
                updateSelectedImageView((ImageView) view);
            }
        });

        mLedGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_GREEN);
                updateSelectedImageView((ImageView) view);
            }
        });

        mLedYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_YELLOW);
                updateSelectedImageView((ImageView) view);
            }
        });

        mLedBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_BLUE);
                updateSelectedImageView((ImageView) view);
            }
        });

        mLedPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_PURPLE);
                updateSelectedImageView((ImageView) view);
            }
        });

        mLedCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_CYAN);
                updateSelectedImageView((ImageView) view);
            }
        });

        mLedWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedColor(ThingyUtils.LED_WHITE);
                updateSelectedImageView((ImageView) view);
            }
        });

        mOneShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedMode(ThingyUtils.ONE_SHOT);
                final int ledIntensity = mCurrentIntensity;
                updateLedOneShotModeUI(ledIntensity);
            }
        });

        mConstant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedMode(ThingyUtils.CONSTANT);
                updateLedConstantModeUI();
            }
        });

        mBreathe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedMode(ThingyUtils.BREATHE);
                final int ledIntensity = mCurrentIntensity;
                final int delay = mCurrentDelay;
                updateLedBreatheModeUI(ledIntensity, delay);
            }
        });

        mOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLedMode(ThingyUtils.OFF);
                updateLedOffModeUI();
            }
        });

        final SeekBar.OnSeekBarChangeListener colorListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setupRgbColor();
            }
        };
        mRedIntensity.setOnSeekBarChangeListener(colorListener);
        mGreenIntensity.setOnSeekBarChangeListener(colorListener);
        mBlueIntensity.setOnSeekBarChangeListener(colorListener);

        mLedIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress < ThingyUtils.DEFAULT_MINIMUM_LED_INTENSITY) {
                        seekBar.setProgress(1);
                        mIntensity.setText(getString(R.string.led_percentage, 1));
                    } else {
                        mIntensity.setText(getString(R.string.led_percentage, progress));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int ledMode = mThingySdkManager.getLedMode(mDevice);
                setupLedMode((byte) ledMode);
            }
        });

        mBreatheDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress < ThingyUtils.DEFAULT_MINIMUM_BREATHE_INTERVAL) {
                        seekBar.setProgress(ThingyUtils.DEFAULT_MINIMUM_BREATHE_INTERVAL);
                        mDelay.setText(getString(R.string.interval_ms, ThingyUtils.DEFAULT_MINIMUM_BREATHE_INTERVAL));
                    } else {
                        mDelay.setText(getString(R.string.interval_ms, progress));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int ledMode = mThingySdkManager.getLedMode(mDevice);
                setupLedMode((byte) ledMode);
            }
        });

        if (savedInstanceState != null) {
            int color = mThingySdkManager.getLedColorIndex(mDevice);
            int colorRgbIntensity = mThingySdkManager.getLedRgbIntensity(mDevice);
            final int ledIntensity = mCurrentIntensity = mThingySdkManager.getLedColorIntensity(mDevice);
            final int delay = mCurrentDelay = mThingySdkManager.getLedColorBreatheDelay(mDevice);
            mSelectedRgbColorIntensity = colorRgbIntensity;
            mSelectedColorIndex = color;

            final int mode = savedInstanceState.getInt(LAST_VISIBLE_UI_MODE);
            switch (mode) {
                case ThingyUtils.CONSTANT:
                    updateLedConstantModeUI();
                    break;
                case ThingyUtils.BREATHE:
                    updateSelectedImageView(color);
                    updateLedBreatheModeUI(ledIntensity, delay);
                    break;
                case ThingyUtils.ONE_SHOT:
                    updateSelectedImageView(color);
                    updateLedOneShotModeUI(ledIntensity);
                    break;
            }
        } else {
            loadLedUI();
        }

        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mDevice);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLedRgbColorContainer.getVisibility() == View.VISIBLE) {
            outState.putInt(LAST_VISIBLE_UI_MODE, ThingyUtils.CONSTANT);
        } else if (mBreatheDelay.isEnabled()) {
            outState.putInt(LAST_VISIBLE_UI_MODE, ThingyUtils.BREATHE);
        } else if (!mBreatheDelay.isEnabled()) {
            outState.putInt(LAST_VISIBLE_UI_MODE, ThingyUtils.ONE_SHOT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ThingyListenerHelper.unregisterThingyListener(getContext(), mThingyListener);
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
    }

    @Override
    public void onNothingSelected() {

    }

    private void loadLedUI() {
        final BluetoothDevice device = mDevice;
        if (device != null && mThingySdkManager.isConnected(device)) {
            final int ledMode = mCurrentLedMode = mThingySdkManager.getLedMode(mDevice);
            int color = mThingySdkManager.getLedColorIndex(mDevice);
            final int ledIntensity = mCurrentIntensity = mThingySdkManager.getLedColorIntensity(mDevice);
            final int delay = mCurrentDelay = mThingySdkManager.getLedColorBreatheDelay(mDevice);
            mSelectedRgbColorIntensity = color;
            mSelectedColorIndex = color;

            switch (ledMode) {
                case ThingyUtils.CONSTANT:
                    updateLedConstantModeUI();
                    break;
                case ThingyUtils.BREATHE:
                    updateSelectedImageView(color);
                    updateLedBreatheModeUI(ledIntensity, delay);
                    break;
                case ThingyUtils.ONE_SHOT:
                    updateSelectedImageView(color);
                    updateLedOneShotModeUI(ledIntensity);
                    break;
                case ThingyUtils.OFF:
                    updateSelectedImageView(color);
                    updateLedOffModeUI();
                    break;
            }
        }
    }

    private void setupLedColor(final int colorIndex) {
        if (mDevice != null) {
            final Thingy thingy = mDatabaseHelper.getSavedDevice(mDevice.getAddress());
            final BluetoothDevice device = mDevice;
            if (mThingySdkManager.isConnected(mDevice)) {
                int ledIntensity;
                final int ledMode = mCurrentLedMode;
                switch (ledMode) {
                    case ThingyUtils.BREATHE:
                        ledIntensity = mLedIntensity.getProgress();
                        final int delay = mBreatheDelay.getProgress();
                        if (mCurrentLedMode != ThingyUtils.OFF) {
                            mSelectedColorIndex = colorIndex;
                            mThingySdkManager.setBreatheLedMode(device, colorIndex, ledIntensity, delay);
                        } else {
                            mSelectedColorIndex = getIndexFromColor(ThingyUtils.DEFAULT_LED_COLOR);
                            mThingySdkManager.setBreatheLedMode(device, mSelectedColorIndex, ThingyUtils.DEFAULT_LED_INTENSITY, ThingyUtils.DEFAULT_BREATHE_INTERVAL);
                        }
                        mCurrentLedMode = ThingyUtils.BREATHE;
                        updateLedBreatheModeUI(ledIntensity, delay);
                        break;
                    case ThingyUtils.ONE_SHOT:
                        ledIntensity = mLedIntensity.getProgress();
                        if (mCurrentLedMode != ThingyUtils.OFF) {
                            mSelectedColorIndex = colorIndex;
                            mThingySdkManager.setOneShotLedMode(device, colorIndex, ledIntensity);
                        } else {
                            mSelectedColorIndex = getIndexFromColor(ThingyUtils.DEFAULT_LED_COLOR);
                            mThingySdkManager.setOneShotLedMode(device, mSelectedColorIndex, ThingyUtils.DEFAULT_LED_INTENSITY);
                        }
                        mCurrentLedMode = ThingyUtils.ONE_SHOT;
                        updateLedOneShotModeUI(ledIntensity);
                        break;
                    case ThingyUtils.OFF:
                        mThingySdkManager.turnOffLed(device);
                        mCurrentLedMode = ThingyUtils.OFF;
                        break;
                }
            } else {
                Utils.showToast(getActivity(), "Please configureThingy to " + thingy.getDeviceName() + " before you proceed!");
            }
        }
    }

    private void setupRgbColor() {
        if (mDevice != null) {
            final Thingy thingy = mDatabaseHelper.getSavedDevice(mDevice.getAddress());
            final BluetoothDevice device = mDevice;
            if (mThingySdkManager.isConnected(mDevice)) {
                if (mCurrentLedMode != ThingyUtils.OFF) {
                    final int r = mRedIntensity.getProgress();
                    final int g = mGreenIntensity.getProgress();
                    final int b = mBlueIntensity.getProgress();
                    mSelectedRgbColorIntensity = Color.rgb(r, g, b);
                    mThingySdkManager.setConstantLedMode(device, r, g, b);
                } else {
                    final int color = mSelectedRgbColorIntensity = getColorFromIndex(ThingyUtils.DEFAULT_LED_COLOR);
                    mThingySdkManager.setConstantLedMode(device, Color.red(color), Color.green(color), Color.blue(color));
                }
                mCurrentLedMode = ThingyUtils.CONSTANT;
                updateLedConstantModeUI();
            } else {
                Utils.showToast(getActivity(), "Please configureThingy to " + thingy.getDeviceName() + " before you proceed!");
            }
        }
    }

    private void setupLedMode(final byte ledMode) {
        if (mDevice != null) {
            final Thingy thingy = mDatabaseHelper.getSavedDevice(mDevice.getAddress());
            final BluetoothDevice device = mDevice;
            if (mThingySdkManager.isConnected(mDevice)) {
                switch (ledMode) {
                    case ThingyUtils.CONSTANT:
                        if (mCurrentLedMode != ThingyUtils.OFF) {
                            if (mCurrentLedMode != ThingyUtils.CONSTANT) {
                                if (mCurrentLedMode == ThingyUtils.BREATHE || mCurrentLedMode == ThingyUtils.ONE_SHOT) {
                                    final int color = getColorFromIndex(mSelectedColorIndex);
                                    mRedIntensity.setProgress(Color.red(color));
                                    mGreenIntensity.setProgress(Color.green(color));
                                    mBlueIntensity.setProgress(Color.blue(color));
                                }

                                final int r = mRedIntensity.getProgress();
                                final int g = mGreenIntensity.getProgress();
                                final int b = mBlueIntensity.getProgress();

                                mSelectedRgbColorIntensity = Color.rgb(r, g, b);
                                mThingySdkManager.setConstantLedMode(device, r, g, b);
                            }
                        } else {
                            final int color = mSelectedRgbColorIntensity = getColorFromIndex(ThingyUtils.DEFAULT_LED_COLOR);
                            mThingySdkManager.setConstantLedMode(device, Color.red(color), Color.green(color), Color.blue(color));
                        }
                        mCurrentLedMode = ThingyUtils.CONSTANT;
                        break;
                    case ThingyUtils.BREATHE:
                        if (mCurrentLedMode != ThingyUtils.OFF) {
                            if (mCurrentLedMode != ThingyUtils.CONSTANT) {
                                final int ledIntensity = mCurrentIntensity = mLedIntensity.getProgress();
                                final int delay = mCurrentDelay = mBreatheDelay.getProgress();
                                int colorIndex = mSelectedColorIndex;
                                if (colorIndex == 0) {
                                    colorIndex = mSelectedColorIndex = getColorFromIndex(colorIndex);
                                }
                                mThingySdkManager.setBreatheLedMode(device, colorIndex, ledIntensity, delay);
                            } else {
                                final int color = mSelectedColorIndex = getIndexFromColor(mSelectedRgbColorIntensity);
                                mThingySdkManager.setBreatheLedMode(device, color, ThingyUtils.DEFAULT_LED_INTENSITY, ThingyUtils.DEFAULT_BREATHE_INTERVAL);
                                updateSelectedImageView(color);
                            }
                        } else {
                            mCurrentIntensity = ThingyUtils.DEFAULT_LED_INTENSITY;
                            mCurrentDelay = ThingyUtils.DEFAULT_BREATHE_INTERVAL;
                            final int color = mSelectedColorIndex = getIndexFromColor(ThingyUtils.DEFAULT_LED_COLOR);
                            mThingySdkManager.setBreatheLedMode(device, color, ThingyUtils.DEFAULT_LED_INTENSITY, ThingyUtils.DEFAULT_BREATHE_INTERVAL);
                            updateSelectedImageView(color);
                        }
                        mCurrentLedMode = ThingyUtils.BREATHE;
                        break;
                    case ThingyUtils.ONE_SHOT:
                        if (mCurrentLedMode != ThingyUtils.OFF) {
                            if (mCurrentLedMode != ThingyUtils.CONSTANT) {
                                final int ledIntensity = mCurrentIntensity = mLedIntensity.getProgress();
                                int colorIndex = mSelectedColorIndex;
                                if (colorIndex == 0) {
                                    colorIndex = mSelectedColorIndex = getColorFromIndex(colorIndex);
                                }
                                mThingySdkManager.setOneShotLedMode(device, colorIndex, ledIntensity);
                            } else {
                                final int color = mSelectedColorIndex = getIndexFromColor(mSelectedRgbColorIntensity);
                                mThingySdkManager.setOneShotLedMode(device, color, ThingyUtils.DEFAULT_LED_INTENSITY);
                                updateSelectedImageView(color);
                            }
                        } else {
                            mCurrentIntensity = ThingyUtils.DEFAULT_LED_INTENSITY;
                            mCurrentDelay = ThingyUtils.DEFAULT_BREATHE_INTERVAL;
                            final int color = mSelectedColorIndex = getIndexFromColor(ThingyUtils.DEFAULT_LED_COLOR);
                            mThingySdkManager.setOneShotLedMode(device, color, ThingyUtils.DEFAULT_LED_INTENSITY);
                            updateSelectedImageView(color);
                        }
                        mCurrentLedMode = ThingyUtils.ONE_SHOT;
                        break;
                    case ThingyUtils.OFF:
                        if (mCurrentLedMode != ThingyUtils.OFF) {
                            mThingySdkManager.turnOffLed(device);
                        }
                        mCurrentLedMode = ThingyUtils.OFF;
                        break;
                }
            } else {
                Utils.showToast(getActivity(), "Please configureThingy to " + thingy.getDeviceName() + " before you proceed!");
            }
        }
    }

    private void updateLedBreatheModeUI(final int ledIntensity, final int delay) {
        mLedRed.setEnabled(true);
        mLedGreen.setEnabled(true);
        mLedYellow.setEnabled(true);
        mLedBlue.setEnabled(true);
        mLedPurple.setEnabled(true);
        mLedCyan.setEnabled(true);
        mLedWhite.setEnabled(true);

        mColorContainer.setVisibility(View.VISIBLE);
        mRgbIntensityControllerContainer.setVisibility(View.GONE);
        mLedControllerContainer.setVisibility(View.VISIBLE);
        mLedRgbColorContainer.setVisibility(View.GONE);

        mDelay.setText(getString(R.string.interval_ms, delay));
        mIntensity.setText(getString(R.string.led_percentage, ledIntensity));

        mBreathe.setSelected(true);
        mOneShot.setSelected(false);
        mConstant.setSelected(false);
        mOff.setSelected(false);

        mBreatheDelay.setProgress(delay);
        mLedIntensity.setProgress(ledIntensity);

        mBreatheDelay.setEnabled(true);
        mLedIntensity.setEnabled(true);
    }

    private void updateLedOneShotModeUI(final int ledIntensity) {
        mLedRed.setEnabled(true);
        mLedGreen.setEnabled(true);
        mLedYellow.setEnabled(true);
        mLedBlue.setEnabled(true);
        mLedPurple.setEnabled(true);
        mLedCyan.setEnabled(true);
        mLedWhite.setEnabled(true);

        mColorContainer.setVisibility(View.VISIBLE);
        mRgbIntensityControllerContainer.setVisibility(View.GONE);
        mLedControllerContainer.setVisibility(View.VISIBLE);
        mLedRgbColorContainer.setVisibility(View.GONE);

        mIntensity.setText(getString(R.string.led_percentage, ledIntensity));

        mBreathe.setSelected(false);
        mOneShot.setSelected(true);
        mConstant.setSelected(false);
        mOff.setSelected(false);

        mLedIntensity.setProgress(ledIntensity);

        mBreatheDelay.setEnabled(false);
        mLedIntensity.setEnabled(true);
    }

    private void updateLedConstantModeUI() {
        mLedRgbColorContainer.setVisibility(View.VISIBLE);
        mRedIntensity.setEnabled(true);
        mGreenIntensity.setEnabled(true);
        mBlueIntensity.setEnabled(true);

        mColorContainer.setVisibility(View.GONE);

        mRgbIntensityControllerContainer.setVisibility(View.VISIBLE);
        mLedControllerContainer.setVisibility(View.GONE);

        mBreathe.setSelected(false);
        mOneShot.setSelected(false);
        mConstant.setSelected(true);
        mOff.setSelected(false);

        final int r = Color.red(mSelectedRgbColorIntensity);
        final int g = Color.green(mSelectedRgbColorIntensity);
        final int b = Color.blue(mSelectedRgbColorIntensity);

        mRgbDrawable.setColor(mSelectedRgbColorIntensity);
        mLedRgbView.setText(String.format("#%06X", (0xFFFFFF & mSelectedRgbColorIntensity)));

        mRedIntensity.setProgress(r);
        mGreenIntensity.setProgress(g);
        mBlueIntensity.setProgress(b);

        mRedIntensityView.setText(String.valueOf(r));
        mGreenIntensityView.setText(String.valueOf(g));
        mBlueIntensityView.setText(String.valueOf(b));
    }

    private void updateLedOffModeUI() {
        updateSelectedImageView(0);

        mLedRed.setEnabled(false);
        mLedGreen.setEnabled(false);
        mLedYellow.setEnabled(false);
        mLedBlue.setEnabled(false);
        mLedPurple.setEnabled(false);
        mLedCyan.setEnabled(false);
        mLedWhite.setEnabled(false);

        mRedIntensity.setEnabled(false);
        mGreenIntensity.setEnabled(false);
        mBlueIntensity.setEnabled(false);

        mBreatheDelay.setEnabled(false);
        mLedIntensity.setEnabled(false);

        mBreathe.setSelected(false);
        mOneShot.setSelected(false);
        mConstant.setSelected(false);
        mOff.setSelected(true);
    }

    private void updateSelectedImageView(final ImageView view) {
        final int id = view.getId();
        switch (id) {
            case R.id.img_led_red:
                mLedRed.setImageDrawable(mSwatchSelected);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case R.id.img_led_green:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(mSwatchSelected);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case R.id.img_led_yellow:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(mSwatchSelected);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case R.id.img_led_blue:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(mSwatchSelected);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case R.id.img_led_purple:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(mSwatchSelected);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case R.id.img_led_cyan:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(mSwatchSelected);
                mLedWhite.setImageDrawable(null);
                break;
            case R.id.img_led_white:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(mSwatchSelected);
                break;
        }
    }

    private void updateSelectedImageView(final int color) {
        switch (color) {
            case ThingyUtils.LED_RED:
                mLedRed.setImageDrawable(mSwatchSelected);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case ThingyUtils.LED_GREEN:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(mSwatchSelected);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case ThingyUtils.LED_YELLOW:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(mSwatchSelected);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case ThingyUtils.LED_BLUE:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(mSwatchSelected);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case ThingyUtils.LED_PURPLE:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(mSwatchSelected);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
            case ThingyUtils.LED_CYAN:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(mSwatchSelected);
                mLedWhite.setImageDrawable(null);
                break;
            case ThingyUtils.LED_WHITE:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(mSwatchSelected);
                break;
            case ThingyUtils.OFF:
                mLedRed.setImageDrawable(null);
                mLedGreen.setImageDrawable(null);
                mLedYellow.setImageDrawable(null);
                mLedBlue.setImageDrawable(null);
                mLedPurple.setImageDrawable(null);
                mLedCyan.setImageDrawable(null);
                mLedWhite.setImageDrawable(null);
                break;
        }
    }

    private int getColorFromIndex(final int colorIndex) {
        switch (colorIndex) {
            case ThingyUtils.LED_RED:
                return Color.RED;
            case ThingyUtils.LED_GREEN:
                return Color.GREEN;
            case ThingyUtils.LED_YELLOW:
                return Color.YELLOW;
            case ThingyUtils.LED_BLUE:
                return Color.BLUE;
            case ThingyUtils.LED_PURPLE:
                return Color.MAGENTA;
            case ThingyUtils.LED_CYAN:
                return Color.CYAN;
            case ThingyUtils.LED_WHITE:
                return Color.WHITE;
            default:
                return Color.CYAN;
        }
    }

    private int getIndexFromColor(final int color) {
        switch (color) {
            case Color.RED:
                return ThingyUtils.LED_RED;
            case Color.GREEN:
                return ThingyUtils.LED_GREEN;
            case Color.YELLOW:
                return ThingyUtils.LED_YELLOW;
            case Color.BLUE:
                return ThingyUtils.LED_BLUE;
            case Color.MAGENTA:
                return ThingyUtils.LED_PURPLE;
            case Color.CYAN:
                return ThingyUtils.LED_CYAN;
            case Color.WHITE:
                return ThingyUtils.LED_WHITE;
            default:
                return ThingyUtils.LED_CYAN;
        }
    }
}