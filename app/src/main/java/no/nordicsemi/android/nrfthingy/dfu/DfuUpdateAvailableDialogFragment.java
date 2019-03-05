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

package no.nordicsemi.android.nrfthingy.dfu;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;

public class DfuUpdateAvailableDialogFragment extends DialogFragment {
    private static final String FW_FILE_VERSION = "FW_FILE_VERSION";
    private DfuUpdateAvailableListener mListener;
    private String mFwFileVersion;
    private BluetoothDevice mDevice;
    private DatabaseHelper mDatabaseHelper;

    public interface DfuUpdateAvailableListener {
        void onDfuRequested();
    }

    public static DfuUpdateAvailableDialogFragment newInstance(@NonNull final BluetoothDevice device,
                                                               @NonNull final String fwFileVersion) {
        final DfuUpdateAvailableDialogFragment fragment = new DfuUpdateAvailableDialogFragment();

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Utils.CURRENT_DEVICE, device);
        bundle.putString(FW_FILE_VERSION, fwFileVersion);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        mListener = (DfuUpdateAvailableListener) context;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
            mFwFileVersion = getArguments().getString(FW_FILE_VERSION);
        }
        mDatabaseHelper = new DatabaseHelper(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        String deviceName = mDatabaseHelper.getDeviceName(mDevice.getAddress());
        if (deviceName.isEmpty()) {
            deviceName = mDevice.getName();
        }

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dfu_title)
                .setMessage(getString(R.string.fw_update_available, deviceName, mFwFileVersion))
                .setCancelable(false)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDfuRequested();
                    }
                })
                .setNegativeButton(R.string.later, null)
                .create();
    }
}
