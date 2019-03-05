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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.nrfthingy.widgets.Renderer;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class MotionServiceFragment extends Fragment implements ScannerFragmentListener {
    private Toolbar mQuaternionToolbar;

    private TextView mTapCount;
    private TextView mTapDirection;
    private TextView mOrientation;
    private TextView mHeading;
    private TextView mPedometerSteps;
    private TextView mPedometerDuration;
    private TextView mHeadingDirection;

    private ImageView mPortraitImage;

    private RajawaliSurfaceView mGlSurfaceView;
    private BluetoothDevice mDevice;

    private DatabaseHelper mDatabaseHelper;
    private ThingySdkManager mThingySdkManager = null;
    private boolean mIsConnected = false;

    private ImageView mHeadingImage;
    private LineChart mLineChartGravityVector;
    private boolean mIsFragmentAttached = false;
    private Renderer mRenderer;

    private ThingyListener mThingyListener = new ThingyListener() {
        float mCurrentDegree = 0.0f;
        private float mHeadingDegrees;
        private RotateAnimation mHeadingAnimation;

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            //Connectivity callbacks handled by main activity
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            if (mDevice.equals(device)) {
                mRenderer.setConnectionState(false);
                if (Utils.checkIfVersionIsAboveJellyBean()) {
                    mRenderer.setNotificationEnabled(false);
                }
            }
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            if (mDevice.equals(device)) {
                mIsConnected = true;
                if (Utils.checkIfVersionIsAboveJellyBean()) {
                    mRenderer.setConnectionState(true);
                    if (mDatabaseHelper.getNotificationsState(mDevice.getAddress(), DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
                        mRenderer.setNotificationEnabled(true);
                    }
                }
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

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int tapCount) {
            if (mIsFragmentAttached) {
                mTapCount.setText(String.valueOf(tapCount));
                switch (direction) {
                    case ThingyUtils.TAP_X_UP:
                        mTapDirection.setText(ThingyUtils.X_UP);
                        break;
                    case ThingyUtils.TAP_X_DOWN:
                        mTapDirection.setText(ThingyUtils.X_DOWN);
                        break;
                    case ThingyUtils.TAP_Y_UP:
                        mTapDirection.setText(ThingyUtils.Y_UP);
                        break;
                    case ThingyUtils.TAP_Y_DOWN:
                        mTapDirection.setText(ThingyUtils.Y_DOWN);
                        break;
                    case ThingyUtils.TAP_Z_UP:
                        mTapDirection.setText(ThingyUtils.Z_UP);
                        break;
                    case ThingyUtils.TAP_Z_DOWN:
                        mTapDirection.setText(ThingyUtils.Z_DOWN);
                        break;
                }
            }
        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {
            mPortraitImage.setPivotX(mPortraitImage.getWidth() / 2.0f);
            mPortraitImage.setPivotY(mPortraitImage.getHeight() / 2.0f);
            mPortraitImage.setRotation(0);

            if (mIsFragmentAttached) {
                switch (orientation) {
                    case ThingyUtils.PORTRAIT_TYPE:
                        mPortraitImage.setRotation(0);

                        mOrientation.setText(ThingyUtils.PORTRAIT);
                        break;
                    case ThingyUtils.LANDSCAPE_TYPE:
                        mPortraitImage.setRotation(90);

                        mOrientation.setText(ThingyUtils.LANDSCAPE);
                        break;
                    case ThingyUtils.REVERSE_PORTRAIT_TYPE:
                        mPortraitImage.setRotation(-180);
                        mOrientation.setText(ThingyUtils.REVERSE_PORTRAIT);
                        break;
                    case ThingyUtils.REVERSE_LANDSCAPE_TYPE:
                        mPortraitImage.setRotation(-90);

                        mOrientation.setText(ThingyUtils.REVERSE_LANDSCAPE);
                        break;
                }
            }
        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            if (mIsFragmentAttached) {
                if (mGlSurfaceView != null) {
                    mRenderer.setQuaternions(x, y, z, w);
                }
            }
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {
            if (mIsFragmentAttached) {
                mPedometerSteps.setText(String.valueOf(steps));
                mPedometerDuration.setText(ThingyUtils.TIME_FORMAT_PEDOMETER.format(duration));
            }
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
            if (mIsFragmentAttached) {
                mHeadingDegrees = heading;
                if (mHeadingAnimation != null) {
                    mHeadingAnimation.reset();
                }

                if (mHeadingDegrees >= 0 && mHeadingDegrees <= 10) {
                    mHeadingDirection.setText(R.string.north);
                } else if (mHeadingDegrees >= 35 && mHeadingDegrees <= 55) {
                    mHeadingDirection.setText(R.string.north_east);
                } else if (mHeadingDegrees >= 80 && mHeadingDegrees <= 100) {
                    mHeadingDirection.setText(R.string.east);
                } else if (mHeadingDegrees >= 125 && mHeadingDegrees <= 145) {
                    mHeadingDirection.setText(R.string.south_east);
                } else if (mHeadingDegrees >= 170 && mHeadingDegrees <= 190) {
                    mHeadingDirection.setText(R.string.south);
                } else if (mHeadingDegrees >= 215 && mHeadingDegrees <= 235) {
                    mHeadingDirection.setText(R.string.south_west);
                } else if (mHeadingDegrees >= 260 && mHeadingDegrees <= 280) {
                    mHeadingDirection.setText(R.string.west);
                } else if (mHeadingDegrees >= 305 && mHeadingDegrees <= 325) {
                    mHeadingDirection.setText(R.string.north_west);
                } else if (mHeadingDegrees >= 350 && mHeadingDegrees <= 359) {
                    mHeadingDirection.setText(R.string.north);
                }

                mHeadingAnimation = new RotateAnimation(mCurrentDegree, -mHeadingDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                mHeadingAnimation.setFillAfter(true);
                mHeadingImage.startAnimation(mHeadingAnimation);
                mHeading.setText(getString(R.string.degrees_2, mHeadingDegrees));
                mCurrentDegree = -mHeadingDegrees;
            }
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            addGravityVectorEntry(x, y, z);
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, final byte[] data) {

        }
    };

    public static MotionServiceFragment newInstance(final BluetoothDevice device) {
        final  MotionServiceFragment fragment = new MotionServiceFragment();

        final Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(getActivity());
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_motion, container, false);
        mThingySdkManager = ThingySdkManager.getInstance();

        mTapCount = rootView.findViewById(R.id.tap_count);
        mTapDirection = rootView.findViewById(R.id.tap_direction);
        mOrientation = rootView.findViewById(R.id.orientation);
        mPedometerSteps = rootView.findViewById(R.id.step_count);
        mPedometerDuration = rootView.findViewById(R.id.duration);
        mHeading = rootView.findViewById(R.id.heading);
        mHeadingDirection = rootView.findViewById(R.id.heading_direction);

        mOrientation.setText(ThingyUtils.PORTRAIT);

        mHeadingImage = rootView.findViewById(R.id.heading_image);
        mPortraitImage = rootView.findViewById(R.id.portrait_image);

        mLineChartGravityVector = rootView.findViewById(R.id.line_chart_gravity_vector);

        mIsConnected = isConnected(mDevice);
        if (Utils.checkIfVersionIsAboveJellyBean()) {
            mQuaternionToolbar = rootView.findViewById(R.id.card_toolbar_euler);
            mGlSurfaceView = rootView.findViewById(R.id.rajwali_surface);
            mRenderer = new Renderer(getActivity());
            mGlSurfaceView.setSurfaceRenderer(mRenderer);
            mRenderer.setConnectionState(mIsConnected);
            if (mDatabaseHelper.getNotificationsState(mDevice.getAddress(), DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
                mRenderer.setNotificationEnabled(true);
            }
        }

        if (mQuaternionToolbar != null) {
            mQuaternionToolbar.inflateMenu(R.menu.quaternion_card_menu);

            if (mDevice != null) {
                updateQuaternionCardOptionsMenu(mQuaternionToolbar.getMenu());
            }

            mQuaternionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_quaternion_angles_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableQuaternionNotifications(item.isChecked());
                            break;
                    }
                    return true;
                }
            });
            loadFeatureDiscoverySequence();
        }

        final Toolbar motionToolbar = rootView.findViewById(R.id.card_toolbar_motion);
        if (motionToolbar != null) {
            motionToolbar.inflateMenu(R.menu.motion_card_menu);

            if (mDevice != null) {
                updateMotionCardOptionsMenu(motionToolbar.getMenu());
            }

            motionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_about:
                            final MotionServiceInfoDialogFragment info = MotionServiceInfoDialogFragment.newInstance();
                            info.show(getChildFragmentManager(), null);
                            break;
                        case R.id.action_pedometer_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enablePedometerNotifications(item.isChecked());
                            break;
                        case R.id.action_tap_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableTapNotifications(item.isChecked());
                            break;
                        case R.id.action_heading_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableHeadingNotifications(item.isChecked());
                            break;
                        case R.id.action_orientation_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableOrientationNotifications(item.isChecked());
                            break;
                    }
                    return true;
                }
            });
        }

        final Toolbar gravityToolbar = rootView.findViewById(R.id.card_toolbar_gravity);
        if (gravityToolbar != null) {
            gravityToolbar.inflateMenu(R.menu.gravity_card_menu);

            if (mDevice != null) {
                updateGravityCardOptionsMenu(gravityToolbar.getMenu());
            }

            gravityToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_gravity_vector_notification:
                            if (item.isChecked()) {
                                item.setChecked(false);
                            } else {
                                item.setChecked(true);
                            }
                            enableGravityVectorNotifications(item.isChecked());
                            break;
                    }
                    return true;
                }
            });
        }

        prepareGravityVectorChart();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mDevice);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        mIsFragmentAttached = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onPause();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsFragmentAttached = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        ThingyListenerHelper.unregisterThingyListener(getContext(), mThingyListener);
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
    }

    @Override
    public void onNothingSelected() {

    }

    private boolean isConnected(final BluetoothDevice device) {
        if (mThingySdkManager != null) {
            final List<BluetoothDevice> connectedDevices = mThingySdkManager.getConnectedDevices();
            for (BluetoothDevice dev : connectedDevices) {
                if (device.getAddress().equals(dev.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void prepareGravityVectorChart() {
        if (!mLineChartGravityVector.isEmpty()) {
            mLineChartGravityVector.clearValues();
        }
        mLineChartGravityVector.setDescription(getString(R.string.title_gravity_vector));
        mLineChartGravityVector.setTouchEnabled(true);
        mLineChartGravityVector.setVisibleXRangeMinimum(5);
        mLineChartGravityVector.setVisibleXRangeMaximum(5);
        // enable scaling and dragging
        mLineChartGravityVector.setDragEnabled(true);
        mLineChartGravityVector.setPinchZoom(true);
        mLineChartGravityVector.setScaleEnabled(true);
        mLineChartGravityVector.setAutoScaleMinMaxEnabled(true);
        mLineChartGravityVector.setDrawGridBackground(false);
        mLineChartGravityVector.setBackgroundColor(Color.WHITE);
        /*final ChartMarker marker = new ChartMarker(getActivity(), R.layout.marker_layout_temperature);
        mLineChartGravityVector.setMarkerView(marker);*/

        LineData data = new LineData();
        data.setValueFormatter(new GravityVectorChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartGravityVector.setData(data);

        Legend legend = mLineChartGravityVector.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = mLineChartGravityVector.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartGravityVector.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new GravityVectorYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(-10f);
        leftAxis.setAxisMaxValue(10f);
        leftAxis.setLabelCount(3, false); //
        leftAxis.setDrawZeroLine(true);

        YAxis rightAxis = mLineChartGravityVector.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet[] createGravityVectorDataSet() {
        final LineDataSet[] lineDataSets = new LineDataSet[3];
        LineDataSet lineDataSetX = new LineDataSet(null, getString(R.string.gravity_vector_x));
        lineDataSetX.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetX.setColor(ContextCompat.getColor(requireContext(), R.color.red));
        lineDataSetX.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSetX.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSetX.setDrawValues(true);
        lineDataSetX.setDrawCircles(true);
        lineDataSetX.setDrawCircleHole(false);
        lineDataSetX.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetX.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[0] = lineDataSetX;

        LineDataSet lineDataSetY = new LineDataSet(null, getString(R.string.gravity_vector_y));
        lineDataSetY.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetY.setColor(ContextCompat.getColor(requireContext(), R.color.green));
        lineDataSetY.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSetY.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSetY.setDrawValues(true);
        lineDataSetY.setDrawCircles(true);
        lineDataSetY.setDrawCircleHole(false);
        lineDataSetY.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetY.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[1] = lineDataSetY;

        LineDataSet lineDataSetZ = new LineDataSet(null, getString(R.string.gravity_vector_z));
        lineDataSetZ.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetZ.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
        lineDataSetZ.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSetZ.setValueFormatter(new GravityVectorChartValueFormatter());
        lineDataSetZ.setDrawValues(true);
        lineDataSetZ.setDrawCircles(true);
        lineDataSetZ.setDrawCircleHole(false);
        lineDataSetZ.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSetZ.setLineWidth(Utils.CHART_LINE_WIDTH);
        lineDataSets[2] = lineDataSetZ;
        return lineDataSets;
    }

    private void addGravityVectorEntry(final float gravityVectorX, final float gravityVectorY, final float gravityVectorZ) {
        LineData data = mLineChartGravityVector.getData();

        if (data != null) {
            ILineDataSet setX = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(2);

            if (setX == null || setY == null || setZ == null) {
                final LineDataSet[] dataSets = createGravityVectorDataSet();
                setX = dataSets[0];
                setY = dataSets[1];
                setZ = dataSets[2];
                data.addDataSet(setX);
                data.addDataSet(setY);
                data.addDataSet(setZ);
            }

            data.addXValue(ThingyUtils.TIME_FORMAT_PEDOMETER.format(new Date()));
            data.addEntry(new Entry(gravityVectorX, setX.getEntryCount()), 0);
            data.addEntry(new Entry(gravityVectorY, setY.getEntryCount()), 1);
            data.addEntry(new Entry(gravityVectorZ, setZ.getEntryCount()), 2);

            mLineChartGravityVector.notifyDataSetChanged();
            mLineChartGravityVector.setVisibleXRangeMaximum(10);
            mLineChartGravityVector.moveViewToX(data.getXValCount() - 11);
        }
    }

    class GravityVectorYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        GravityVectorYValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value); //
        }
    }

    class GravityVectorChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        GravityVectorChartValueFormatter() {
            mFormat = new DecimalFormat("#0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    private void updateQuaternionCardOptionsMenu(final Menu eulerCardMotion) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION)) {
            eulerCardMotion.findItem(R.id.action_quaternion_angles_notification).setChecked(true);
        } else {
            eulerCardMotion.findItem(R.id.action_quaternion_angles_notification).setChecked(false);
        }
    }

    private void updateMotionCardOptionsMenu(final Menu motionCardMenu) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER)) {
            motionCardMenu.findItem(R.id.action_pedometer_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_pedometer_notification).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TAP)) {
            motionCardMenu.findItem(R.id.action_tap_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_tap_notification).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HEADING)) {
            motionCardMenu.findItem(R.id.action_heading_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_heading_notification).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION)) {
            motionCardMenu.findItem(R.id.action_orientation_notification).setChecked(true);
        } else {
            motionCardMenu.findItem(R.id.action_orientation_notification).setChecked(false);
        }
    }

    private void updateGravityCardOptionsMenu(final Menu gravityCardMotion) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR)) {
            gravityCardMotion.findItem(R.id.action_gravity_vector_notification).setChecked(true);
        } else {
            gravityCardMotion.findItem(R.id.action_gravity_vector_notification).setChecked(false);
        }
    }

    @SuppressWarnings("unused")
    public void enableRawDataNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableRawDataNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_RAW_DATA);
    }

    private void enableOrientationNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableOrientationNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_ORIENTATION);
    }

    private void enableHeadingNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableHeadingNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_HEADING);
    }

    private void enableTapNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableTapNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_TAP);
    }

    private void enablePedometerNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enablePedometerNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_PEDOMETER);
    }

    private void enableQuaternionNotifications(final boolean notificationEnabled) {
        mRenderer.setNotificationEnabled(notificationEnabled);
        mThingySdkManager.enableQuaternionNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_QUATERNION);
    }

    private void enableGravityVectorNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableGravityVectorNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_GRAVITY_VECTOR);
    }

    @SuppressWarnings("unused")
    private void enableEulerNotifications(final boolean notificationEnabled) {
        mThingySdkManager.enableEulerNotifications(mDevice, notificationEnabled);
        mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), notificationEnabled, DatabaseContract.ThingyDbColumns.COLUMN_NOTIFICATION_EULER);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadFeatureDiscoverySequence() {
        if (!Utils.checkIfSequenceIsCompleted(requireContext(), Utils.INITIAL_MOTION_TUTORIAL)) {

            final SpannableString desc = new SpannableString(getString(R.string.start_stop_motion_sensors));

            final TapTargetSequence sequence = new TapTargetSequence(requireActivity());
            sequence.continueOnCancel(true);
            sequence.targets(
                    TapTarget.forToolbarOverflow(mQuaternionToolbar, desc).
                            dimColor(R.color.grey).
                            outerCircleColor(R.color.accent).id(0)).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    Utils.saveSequenceCompletion(requireContext(), Utils.INITIAL_MOTION_TUTORIAL);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {

                }
            }).start();
        }
    }
}