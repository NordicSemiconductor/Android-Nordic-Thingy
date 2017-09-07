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

package no.nordicsemi.android.nrfthingy.thingy;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.nordicsemi.android.nrfthingy.R;

public class ThingyAdapter extends ArrayAdapter<BluetoothDevice> implements ThemedSpinnerAdapter {

    private Context mContext;
    private ArrayList<BluetoothDevice> mThingyList;
    private final ThemedSpinnerAdapter.Helper mDropDownHelper;
    private ActionListener mListener;

    public interface ActionListener {
        void onAddNewThingee();
    }

    public ThingyAdapter(Context context, ArrayList<BluetoothDevice> thingeeList) {
        super(context, R.layout.custom_spinner, thingeeList);
        this.mContext = context;
        this.mDropDownHelper = new ThemedSpinnerAdapter.Helper(this.mContext);
        mThingyList = thingeeList;
        mListener = (ActionListener) mContext;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1; // +1 for the add thingy row!
    }

    @Override
    public BluetoothDevice getItem(int position) {
        if (mThingyList.size() == 0)
            return null;

        return mThingyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position > 0)
            return super.getItemId(position - 1);
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View spinnerView = inflater.inflate(R.layout.custom_spinner, parent, false);
        TextView main_text = spinnerView.findViewById(R.id.tv_dropdown);
        if (mThingyList.size() > 0)
            main_text.setText(mThingyList.get(position).getName());
        return spinnerView;
    }

    @Override
    public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (position == mThingyList.size())
            return newToolbarView(mContext, convertView, parent);

        // Inflate the drop down using the helper's LayoutInflater
        holder = new ViewHolder();
        LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
        view = inflater.inflate(R.layout.custom_drop_down_view, parent, false);
        holder.label = view.findViewById(R.id.dropdown_row);
        view.setTag(holder);


        final String name = mThingyList.get(position).getName();
        if (holder != null)
            holder.label.setText(name);
        return view;
    }

    public View newToolbarView(final Context context, final View convertView, final ViewGroup viewGroup) {

        LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
        final View view = inflater.inflate(R.layout.custom_add_thingy, viewGroup, false);
        final TextView add_thingee = view.findViewById(R.id.add_thingee);
        add_thingee.setText(R.string.action_add);
        add_thingee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddNewThingee();
            }
        });
        return view;
    }

    class ViewHolder {
        TextView label;
    }


}
