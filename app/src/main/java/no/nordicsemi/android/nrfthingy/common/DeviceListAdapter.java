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

package no.nordicsemi.android.nrfthingy.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name, address and RSSI image based on RSSI values.
 */
public class DeviceListAdapter extends BaseAdapter {
    private static final int TYPE_TITLE = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_EMPTY = 2;

    private final List<ExtendedBluetoothDevice> mDevices = new ArrayList<>();

    /**
     * If such device exists on the bonded device list, this method does nothing. If not then the device is updated (rssi value) or added.
     *
     * @param results scan results
     */
    public void update(final List<ScanResult> results) {
        for (final ScanResult result : results) {
            final ExtendedBluetoothDevice device = findDevice(result);
            if (device == null) {
                mDevices.add(new ExtendedBluetoothDevice(result));
            } else {
                device.name = result.getScanRecord() != null ? result.getScanRecord().getDeviceName() : null;
                device.rssi = result.getRssi();
            }
        }
        notifyDataSetChanged();
    }

    private ExtendedBluetoothDevice findDevice(final ScanResult result) {
        for (final ExtendedBluetoothDevice device : mDevices)
            if (device.matches(result))
                return device;
        return null;
    }

    void clearDevices() {
        mDevices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices.isEmpty() ? 2 : mDevices.size() + 1; // 1 for title, 1 for empty text
    }

    @Override
    public Object getItem(int position) {
        if (position == 0)
            return R.string.scanner_subtitle_not_bonded;
        else
            return mDevices.get(position - 1);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_ITEM;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_TITLE;

        if (position == getCount() - 1 && mDevices.isEmpty())
            return TYPE_EMPTY;

        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View oldView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final int type = getItemViewType(position);

        View view = oldView;
        switch (type) {
            case TYPE_EMPTY:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_empty, parent, false);
                }
                break;
            case TYPE_TITLE:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_title, parent, false);
                }
                final TextView title = (TextView) view;
                title.setText((Integer) getItem(position));
                break;
            default:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_row, parent, false);
                    final ViewHolder holder = new ViewHolder();
                    holder.name = view.findViewById(R.id.name);
                    holder.address = view.findViewById(R.id.address);
                    holder.thingy = view.findViewById(R.id.icon_view);
                    holder.rssi = view.findViewById(R.id.rssi);
                    view.setTag(holder);
                }

                final ExtendedBluetoothDevice device = (ExtendedBluetoothDevice) getItem(position);
                final ViewHolder holder = (ViewHolder) view.getTag();
                final String name = device.name;
                holder.name.setText(name != null ? name : parent.getContext().getString(R.string.not_available));
                holder.address.setText(device.device.getAddress());
                if (device.rssi != ScannerFragment.NO_RSSI) {
                    final int rssiPercent = (int) (100.0f * (127.0f + device.rssi) / (127.0f + 20.0f));
                    holder.rssi.setImageLevel(rssiPercent);
                    holder.rssi.setVisibility(View.VISIBLE);
                } else {
                    holder.rssi.setVisibility(View.GONE);
                    holder.thingy.setVisibility(View.GONE);
                }
                break;
        }

        return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
        private ImageView rssi;
        private ImageView thingy;
    }
}
