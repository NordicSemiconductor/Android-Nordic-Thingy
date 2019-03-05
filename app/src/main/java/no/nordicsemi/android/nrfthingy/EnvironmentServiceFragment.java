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

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import no.nordicsemi.android.nrfthingy.common.ScannerFragmentListener;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseContract.ThingyDbColumns;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class EnvironmentServiceFragment extends Fragment implements ScannerFragmentListener {

    private static final int REQUEST_ENABLE_BT = 1021;
    private TextView mTemperatureView;
    private TextView mPressureView;
    private TextView mHumidityView;
    private TextView mCarbon;
    private TextView mTvoc;
    private TextView mColorView;
    private TextView mWeatherSettings;

    private ImageView mBlob;

    private LineChart mLineChartTemperature;
    private LineChart mLineChartPressure;
    private LineChart mLineChartHumidity;

    private GradientDrawable mShape;
    private EnvironmentServiceListener mListener;

    private BluetoothDevice mDevice;

    private ThingySdkManager mThingySdkManager = null;
    private DatabaseHelper mDatabaseHelper;

    private boolean mIsFragmentAttached = false;

    private LinkedHashMap<String, Entry> mTemperatureData = new LinkedHashMap<>();
    private LinkedHashMap<String, Entry> mPressureData = new LinkedHashMap<>();
    private LinkedHashMap<String, Entry> mHumidityData = new LinkedHashMap<>();

    private ThingyListener mThingyListener = new ThingyListener() {

        String mTemperature;
        String mTemperatureTimeStamp;
        String mPressure;
        String mPressureTimeStamp;
        String mHumidityTimeStamp;
        String mHumidity;
        int mECO2;
        int mTVOC;

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
            //Connectivity callbacks handled by main activity
        }

        @Override
        public void onBatteryLevelChanged(final BluetoothDevice bluetoothDevice, final int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
            mTemperature = temperature;
            mTemperatureTimeStamp = ThingyUtils.TIME_FORMAT.format(System.currentTimeMillis());
            if (mIsFragmentAttached) {
                mTemperatureView.setText(String.format(Locale.US, getString(R.string.celsius), temperature));
                handleTemperatureGraphUpdates(mLineChartTemperature);
                addTemperatureEntry(mTemperatureTimeStamp, Float.valueOf(mTemperature));
            }
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, final String pressure) {
            mPressure = pressure;
            mPressureTimeStamp = ThingyUtils.TIME_FORMAT.format(System.currentTimeMillis());
            if (mIsFragmentAttached) {
                mPressureView.setText(getString(R.string.hecto_pascal, mPressure));
                handleTemperatureGraphUpdates(mLineChartPressure);
                addPressureEntry(mPressureTimeStamp, Float.valueOf(mPressure));
            }
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, final String humidity) {
            mHumidity = humidity;
            mHumidityTimeStamp = ThingyUtils.TIME_FORMAT.format(System.currentTimeMillis());
            if (mIsFragmentAttached) {
                mHumidityView.setText(mHumidity + "%");
                handleTemperatureGraphUpdates(mLineChartHumidity);
                addHumidityEntry(mHumidityTimeStamp, Float.parseFloat(mHumidity));
            }
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, final int eco2, final int tvoc) {
            mECO2 = eco2;
            mTVOC = tvoc;
            if (mIsFragmentAttached) {
                mCarbon.setText(getString(R.string.ppm, mECO2));
                mTvoc.setText(getString(R.string.ppb, mTVOC));
            }
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, final float red, final float green, final float blue, final float alpha) {
            final String colorText = createColorValue(red, green, blue, alpha);
            if (mIsFragmentAttached) {
                mColorView.setText(colorText);
            }
        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

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
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };

    public interface EnvironmentServiceListener {
        LinkedHashMap<String, String> getSavedTemperatureData(final BluetoothDevice device);

        LinkedHashMap<String, String> getSavedPressureData(final BluetoothDevice device);

        LinkedHashMap<String, Integer> getSavedHumidityData(final BluetoothDevice device);

        Toolbar getToolbar();
    }

    public EnvironmentServiceFragment() {
        // Required empty public constructor
    }

    public static EnvironmentServiceFragment newInstance(final BluetoothDevice device) {
        EnvironmentServiceFragment fragment = new EnvironmentServiceFragment();
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mThingySdkManager = ThingySdkManager.getInstance();

        final View rootView = inflater.inflate(R.layout.fragment_environment, container, false);
        final Toolbar toolbarEnvironment = rootView.findViewById(R.id.environment_toolbar);

        mTemperatureView = rootView.findViewById(R.id.temperature);
        mPressureView = rootView.findViewById(R.id.pressure);
        mHumidityView = rootView.findViewById(R.id.humidity);
        mCarbon = rootView.findViewById(R.id.carbon);
        mTvoc = rootView.findViewById(R.id.tvoc);
        mColorView = rootView.findViewById(R.id.color);
        mWeatherSettings = rootView.findViewById(R.id.weather_settings);

        mBlob = rootView.findViewById(R.id.blob);
        mShape = (GradientDrawable) mBlob.getDrawable();

        mLineChartTemperature = rootView.findViewById(R.id.line_chart_temperature);
        mLineChartPressure = rootView.findViewById(R.id.line_chart_pressure);
        mLineChartHumidity = rootView.findViewById(R.id.line_chart_humidity);
        prepareTemperatureGraph();
        preparePressureGraph();
        prepareHumidityGraph();
        mDatabaseHelper = new DatabaseHelper(getActivity());

        if (toolbarEnvironment != null) {
            toolbarEnvironment.inflateMenu(R.menu.environment_card_menu);

            if (mDevice != null) {
                updateEnvironmentCardViewOptionsMenu(toolbarEnvironment.getMenu());
            }

            toolbarEnvironment.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int id = item.getItemId();
                    switch (id) {
                        case R.id.action_about:
                            final EnvironmentServiceInfoDialogFragment info = EnvironmentServiceInfoDialogFragment.newInstance();
                            info.show(getChildFragmentManager(), null);
                            break;
                        case R.id.action_temperature_notifications:
                            if (item.isChecked()) {
                                item.setChecked(false);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE);
                                mThingySdkManager.enableTemperatureNotifications(mDevice, item.isChecked());
                                mTemperatureView.setText(R.string.disabled);
                            } else {
                                item.setChecked(true);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE);
                                mThingySdkManager.enableTemperatureNotifications(mDevice, item.isChecked());
                                mTemperatureView.setText("");
                            }
                            break;
                        case R.id.action_pressure_notifications:
                            if (item.isChecked()) {
                                item.setChecked(false);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE);
                                mThingySdkManager.enablePressureNotifications(mDevice, item.isChecked());
                                mPressureView.setText(R.string.disabled);
                            } else {
                                item.setChecked(true);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE);
                                mThingySdkManager.enablePressureNotifications(mDevice, item.isChecked());
                                mPressureView.setText("");
                            }
                            break;
                        case R.id.action_humidity_notifications:
                            if (item.isChecked()) {
                                item.setChecked(false);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY);
                                mThingySdkManager.enableHumidityNotifications(mDevice, item.isChecked());
                                mHumidityView.setText(R.string.disabled);
                            } else {
                                item.setChecked(true);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY);
                                mThingySdkManager.enableHumidityNotifications(mDevice, item.isChecked());
                                mHumidityView.setText("");
                            }
                            break;
                        case R.id.action_air_quality_notifications:
                            if (item.isChecked()) {
                                item.setChecked(false);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY);
                                mThingySdkManager.enableAirQualityNotifications(mDevice, item.isChecked());
                                mCarbon.setText(R.string.disabled);
                                mTvoc.setText(R.string.disabled);
                            } else {
                                item.setChecked(true);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY);
                                mThingySdkManager.enableAirQualityNotifications(mDevice, item.isChecked());
                                mCarbon.setText("");
                                mTvoc.setText("");
                            }
                            break;
                        case R.id.action_color_notifications:
                            if (item.isChecked()) {
                                item.setChecked(false);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_COLOR);
                                mThingySdkManager.enableColorNotifications(mDevice, item.isChecked());
                                mColorView.setText(R.string.disabled);
                            } else {
                                item.setChecked(true);
                                mDatabaseHelper.updateNotificationsState(mDevice.getAddress(), item.isChecked(), ThingyDbColumns.COLUMN_NOTIFICATION_COLOR);
                                mThingySdkManager.enableColorNotifications(mDevice, item.isChecked());
                                mColorView.setText("");
                            }
                            break;
                    }
                    return true;
                }
            });
        }

        mWeatherSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevice != null) {
                    if (mThingySdkManager.isConnected(mDevice)) {
                        EnvironmentServiceSettingsFragment fragment = EnvironmentServiceSettingsFragment.newInstance(mDevice);
                        fragment.show(getChildFragmentManager(), null);
                    } else {
                        final String name = mDatabaseHelper.getDeviceName(mDevice.getAddress());
                        Utils.showToast(getActivity(), getString(R.string.no_thingy_connected_configuration, name));
                    }
                }
            }
        });

        updateEnvironmentCardView();

        plotSavedTemperatureData();
        plotSavedPressureData();
        plotSavedHumidityData();
        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener, mDevice);
        loadFeatureDiscoverySequence(mListener.getToolbar(), toolbarEnvironment);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        mIsFragmentAttached = true;
        if (context instanceof EnvironmentServiceListener) {
            mListener = (EnvironmentServiceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CloudFragmentListener");
        }
    }

    private LinkedHashMap<String, Entry> saveGraphDataOnRotation(final LineChart lineChart) {
        LinkedHashMap<String, Entry> values = new LinkedHashMap<>();
        final LineData lineData = lineChart.getLineData();
        List<String> xValues = lineData.getXVals();
        ILineDataSet set = lineData.getDataSetByIndex(0);

        for (int i = 0; i < xValues.size(); i++) {
            values.put(xValues.get(i), set.getEntryForIndex(i));
        }
        return values;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsFragmentAttached = false;
        mListener = null;
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

    private void updateEnvironmentCardView() {
        if (mDevice != null) {
            final String address = mDevice.getAddress();
            if (!mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE)) {
                mTemperatureView.setText(R.string.disabled);
            }

            if (!mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE)) {
                mPressureView.setText(R.string.disabled);
            }

            if (!mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY)) {
                mHumidityView.setText(R.string.disabled);
            }

            if (!mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY)) {
                mTvoc.setText(R.string.disabled);
                mCarbon.setText(R.string.disabled);
            }

            if (!mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_COLOR)) {
                mColorView.setText(R.string.disabled);
            }
        }
    }

    private void updateEnvironmentCardViewOptionsMenu(final Menu environmentStatusMenu) {
        final String address = mDevice.getAddress();
        if (mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_TEMPERATURE)) {
            environmentStatusMenu.findItem(R.id.action_temperature_notifications).setChecked(true);
        } else {
            environmentStatusMenu.findItem(R.id.action_temperature_notifications).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_PRESSURE)) {
            environmentStatusMenu.findItem(R.id.action_pressure_notifications).setChecked(true);
        } else {
            environmentStatusMenu.findItem(R.id.action_pressure_notifications).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_HUMIDITY)) {
            environmentStatusMenu.findItem(R.id.action_humidity_notifications).setChecked(true);
        } else {
            environmentStatusMenu.findItem(R.id.action_humidity_notifications).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_AIR_QUALITY)) {
            environmentStatusMenu.findItem(R.id.action_air_quality_notifications).setChecked(true);
        } else {
            environmentStatusMenu.findItem(R.id.action_air_quality_notifications).setChecked(false);
        }

        if (mDatabaseHelper.getNotificationsState(address, ThingyDbColumns.COLUMN_NOTIFICATION_COLOR)) {
            environmentStatusMenu.findItem(R.id.action_color_notifications).setChecked(true);
        } else {
            environmentStatusMenu.findItem(R.id.action_color_notifications).setChecked(false);
        }
    }

    private void prepareTemperatureGraph() {
        if (!mLineChartTemperature.isEmpty()) {
            mLineChartTemperature.getData().getXVals().clear();
            mLineChartTemperature.clearValues();
        }
        mLineChartTemperature.setDescription(getString(R.string.time));
        mLineChartTemperature.setTouchEnabled(true);
        mLineChartTemperature.setVisibleXRangeMinimum(5);
        // enable scaling and dragging
        mLineChartTemperature.setDragEnabled(true);
        mLineChartTemperature.setPinchZoom(true);
        mLineChartTemperature.setScaleEnabled(true);
        mLineChartTemperature.setAutoScaleMinMaxEnabled(true);
        mLineChartTemperature.setDrawGridBackground(false);
        mLineChartTemperature.setBackgroundColor(Color.WHITE);
        /*final ChartMarker marker = new ChartMarker(getContext(), R.layout.marker_layout_temperature);
        mLineChartTemperature.setMarkerView(marker);*/

        LineData data = new LineData();
        data.setValueFormatter(new TemperatureChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartTemperature.setData(data);

        Legend legend = mLineChartTemperature.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = mLineChartTemperature.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartTemperature.getAxisLeft();
        leftAxis.setDrawZeroLine(true);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new TemperatureYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(-10f);
        leftAxis.setAxisMaxValue(40f);
        leftAxis.setLabelCount(6, false); //

        YAxis rightAxis = mLineChartTemperature.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createTemperatureDataSet() {
        LineDataSet lineDataSet = new LineDataSet(null, getString(R.string.temperature_graph));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.red));
        lineDataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet.setValueFormatter(new TemperatureChartValueFormatter());
        lineDataSet.setDrawValues(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet.setLineWidth(Utils.CHART_LINE_WIDTH);
        return lineDataSet;
    }

    private void plotSavedTemperatureData() {
        LinkedHashMap<String, String> temperatureData = mListener.getSavedTemperatureData(mDevice);
        if (temperatureData.size() > 0) {
            final Set<String> timeStamps = temperatureData.keySet();
            String temperature;
            for (String timeStamp : timeStamps) {
                temperature = temperatureData.get(timeStamp);
                addTemperatureEntry(timeStamp, Float.valueOf(temperature));

                final LineData data = mLineChartTemperature.getData();
                if (data != null) {
                    mLineChartTemperature.moveViewToX(data.getXValCount() - 11);
                }
            }
            timeStamps.clear();
            temperatureData.clear();
            mTemperatureData.clear();
        }
    }

    private void addTemperatureEntry(final String timeStamp, final float temperatureValue) {
        final  LineData data = mLineChartTemperature.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createTemperatureDataSet();
                data.addDataSet(set);
            }
            data.addXValue(timeStamp);
            final Entry entry = new Entry(temperatureValue, set.getEntryCount());
            data.addEntry(entry, 0);
            final YAxis leftAxis = mLineChartTemperature.getAxisLeft();

            if (temperatureValue > leftAxis.getAxisMaximum()) {
                leftAxis.setAxisMaxValue(leftAxis.getAxisMaximum() + 20f);
            } else if (temperatureValue < leftAxis.getAxisMinimum()) {
                leftAxis.setAxisMinValue(leftAxis.getAxisMinimum() - 20f);
            }

            mLineChartTemperature.notifyDataSetChanged();
            mLineChartTemperature.setVisibleXRangeMaximum(10);

            if (data.getXValCount() >= 10) {
                final int highestVisibleIndex = mLineChartTemperature.getHighestVisibleXIndex();
                if ((data.getXValCount() - 10) < highestVisibleIndex) {
                    mLineChartTemperature.moveViewToX(data.getXValCount() - 11);
                } else {
                    mLineChartTemperature.invalidate();
                }
            } else {
                mLineChartTemperature.invalidate();
            }
        }
    }

    private synchronized void handleTemperatureGraphUpdates(LineChart lineChart) {
        final LineData lineData = lineChart.getData();

        if (lineData.getXVals().size() > ThingyUtils.MAX_VISISBLE_GRAPH_ENTRIES) {
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set != null) {
                if (set.removeFirst()) {
                    lineData.removeXValue(0);
                    final List xValues = lineData.getXVals();
                    for (int i = 0; i < xValues.size(); i++) {
                        Entry entry = set.getEntryForIndex(i);
                        if (entry != null) {
                            entry.setXIndex(i);
                            entry.setVal(entry.getVal());
                        }
                    }
                    lineData.notifyDataChanged();
                }
            }
        }
    }

    private void preparePressureGraph() {
        mLineChartPressure.setDescription(getString(R.string.time));
        mLineChartPressure.setTouchEnabled(true);
        mLineChartPressure.setVisibleXRangeMinimum(5);
        // enable scaling and dragging
        mLineChartPressure.setDragEnabled(true);
        mLineChartPressure.setPinchZoom(true);
        mLineChartPressure.setScaleEnabled(true);
        mLineChartPressure.setAutoScaleMinMaxEnabled(true);
        mLineChartPressure.setDrawGridBackground(false);
        mLineChartPressure.setBackgroundColor(Color.WHITE);
        /*final ChartMarker marker = new ChartMarker(getActivity(), R.layout.marker_layout_pressure);
        mLineChartPressure.setMarkerView(marker);*/

        LineData data = new LineData();
        data.setValueFormatter(new TemperatureChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartPressure.setData(data);

        Legend legend = mLineChartPressure.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = mLineChartPressure.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartPressure.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new PressureChartYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(700f);
        leftAxis.setAxisMaxValue(1100f);
        leftAxis.setLabelCount(10, false); //
        YAxis rightAxis = mLineChartPressure.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createPressureDataSet() {
        LineDataSet lineDataSet = new LineDataSet(null, getString(R.string.pressure_graph));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.red));
        lineDataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet.setValueFormatter(new TemperatureChartValueFormatter());
        lineDataSet.setDrawValues(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet.setLineWidth(Utils.CHART_LINE_WIDTH);
        return lineDataSet;
    }

    private void plotSavedPressureData() {
        LinkedHashMap<String, String> pressureData = mListener.getSavedPressureData(mDevice);/*mPressureData;*/
        if (pressureData.size() > 0) {
            Set<String> timeStamps = pressureData.keySet();
            String pressure;
            for (String timeStamp : timeStamps) {
                pressure = pressureData.get(timeStamp);
                addPressureEntry(timeStamp, Float.parseFloat(pressure));
            }
            timeStamps.clear();
            pressureData.clear();
            mPressureData.clear();
        }
    }

    private void addPressureEntry(final String timestamp, float pressureValue) {
        final LineData data = mLineChartPressure.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createPressureDataSet();
                data.addDataSet(set);
            }

            data.addXValue(timestamp);
            data.addEntry(new Entry(pressureValue, set.getEntryCount()), 0);

            if (pressureValue < 700 && pressureValue > 600 && mLineChartPressure.getAxisLeft().getAxisMinimum() > 600) {
                mLineChartPressure.getAxisLeft().setAxisMinValue(600);
                mLineChartPressure.getAxisLeft().setZeroLineColor(ContextCompat.getColor(requireContext(), R.color.nordicBlue));
            } else if (pressureValue < 600 && pressureValue > 500 && mLineChartPressure.getAxisLeft().getAxisMinimum() > 500) {
                mLineChartPressure.getAxisLeft().setAxisMinValue(500);
            }

            mLineChartPressure.notifyDataSetChanged();
            mLineChartPressure.setVisibleXRangeMaximum(10);

            if (data.getXValCount() >= 10) {
                final int highestVisibleIndex = mLineChartPressure.getHighestVisibleXIndex();
                if ((data.getXValCount() - 10) < highestVisibleIndex) {
                    mLineChartPressure.moveViewToX(data.getXValCount() - 11);
                } else {
                    mLineChartPressure.invalidate();
                }
            } else {
                mLineChartPressure.invalidate();
            }
        }
    }

    private void prepareHumidityGraph() {
        mLineChartHumidity.setDescription(getString(R.string.time));
        mLineChartHumidity.setTouchEnabled(true);
        mLineChartHumidity.setVisibleXRangeMinimum(5);
        // enable scaling and dragging
        mLineChartHumidity.setDragEnabled(true);
        mLineChartHumidity.setPinchZoom(true);
        mLineChartHumidity.setScaleEnabled(true);
        mLineChartHumidity.setAutoScaleMinMaxEnabled(true);
        mLineChartHumidity.setDrawGridBackground(false);
        mLineChartHumidity.setBackgroundColor(Color.WHITE);
        /*final ChartMarker marker = new ChartMarker(getActivity(), R.layout.marker_layout_pressure);
        mLineChartHumidity.setMarkerView(marker);*/

        final LineData data = new LineData();
        data.setValueFormatter(new TemperatureChartValueFormatter());
        data.setValueTextColor(Color.WHITE);
        mLineChartHumidity.setData(data);

        Legend legend = mLineChartHumidity.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = mLineChartHumidity.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mLineChartHumidity.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new PressureChartYValueFormatter());
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setLabelCount(6, false); //
        YAxis rightAxis = mLineChartHumidity.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createHumidityDataSet() {
        final LineDataSet lineDataSet = new LineDataSet(null, getString(R.string.humidity_graph));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.red));
        lineDataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet.setHighLightColor(ContextCompat.getColor(requireContext(), R.color.accent));
        lineDataSet.setValueFormatter(new HumidityChartValueFormatter());
        lineDataSet.setDrawValues(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(Utils.CHART_VALUE_TEXT_SIZE);
        lineDataSet.setLineWidth(Utils.CHART_LINE_WIDTH);
        return lineDataSet;
    }

    private void plotSavedHumidityData() {
        final LinkedHashMap<String, Integer> humidityData = mListener.getSavedHumidityData(mDevice);
        if (humidityData.size() > 0) {
            Set<String> timeStamps = humidityData.keySet();
            Integer pressure;
            for (String timeStamp : timeStamps) {
                pressure = humidityData.get(timeStamp);
                addHumidityEntry(timeStamp, pressure);
            }
            timeStamps.clear();
            humidityData.clear();
            mHumidityData.clear();
        }
    }

    private void addHumidityEntry(final String timestamp, float humidityValue) {
        final LineData data = mLineChartHumidity.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createHumidityDataSet();
                data.addDataSet(set);
            }

            data.addXValue(timestamp);
            data.addEntry(new Entry(humidityValue, set.getEntryCount()), 0);

            mLineChartHumidity.notifyDataSetChanged();
            mLineChartHumidity.setVisibleXRangeMaximum(10);

            if (data.getXValCount() >= 10) {
                final int highestVisibleIndex = mLineChartHumidity.getHighestVisibleXIndex();
                if ((data.getXValCount() - 10) < highestVisibleIndex) {
                    mLineChartHumidity.moveViewToX(data.getXValCount() - 11);
                } else {
                    mLineChartHumidity.invalidate();
                }
            } else {
                mLineChartHumidity.invalidate();
            }
        }
    }

    class TemperatureYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        TemperatureYValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value); //
        }
    }

    class TemperatureChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        TemperatureChartValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    class PressureChartYValueFormatter implements YAxisValueFormatter {
        private DecimalFormat mFormat;

        PressureChartYValueFormatter() {
            mFormat = new DecimalFormat("###,##0.00");
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value);
        }
    }

    class HumidityChartValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        HumidityChartValueFormatter() {
            mFormat = new DecimalFormat("##,##,#0");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

    private String createColorValue(float r, float g, float b, float a) {
        float total = Math.max(r + g + b, 1); //to avoid NaN when rgb values could be zeros
        float r_ratio = r / total;
        float g_ratio = g / total;
        float b_ratio = b / total;

        int clear_at_black = 300;
        int clear_at_white = 400;
        int clear_diff = clear_at_white - clear_at_black;

        float clear_normalized = (a - clear_at_black) / clear_diff;

        if (clear_normalized < 0) {
            clear_normalized = 0;
        }

        int r_8 = (int) (r_ratio * 255 * 3 * clear_normalized);
        if (r_8 > 255)
            r_8 = 255;

        int g_8 = (int) (g_ratio * 255 * 3 * clear_normalized);
        if (g_8 > 255)
            g_8 = 255;

        int b_8 = (int) (b_ratio * 255 * 3 * clear_normalized);
        if (b_8 > 255)
            b_8 = 255;

        final int color = Color.rgb(r_8, g_8, b_8);
        mShape.setColor(color);
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private void loadFeatureDiscoverySequence(final Toolbar mainToolbar, final Toolbar environmentToolbar) {
        if (!Utils.checkIfSequenceIsCompleted(requireContext(), Utils.INITIAL_ENV_TUTORIAL)) {
            final SpannableString desc = new SpannableString(getString(R.string.start_stop_env_sensors));

            final TapTargetSequence sequence = new TapTargetSequence(requireActivity());
            sequence.continueOnCancel(true);
            sequence.targets(
                    TapTarget.forToolbarNavigationIcon(mainToolbar, getString(R.string.discover_features)).
                            dimColor(R.color.grey).
                            outerCircleColor(R.color.accent).id(0),
                    TapTarget.forToolbarOverflow(environmentToolbar, desc).
                            dimColor(R.color.grey).
                            outerCircleColor(R.color.accent).id(1)).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    Utils.saveSequenceCompletion(requireContext(), Utils.INITIAL_ENV_TUTORIAL);
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
