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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class CloudTokenConfigurationDialogFragment extends DialogFragment {

    private TextInputLayout mCloudTokenLayout;

    private TextInputEditText mCloudTokenView;

    private String mCloudToken;

    private BluetoothDevice mDevice;

    private ThingySdkManager mThingySdkManager;

    public static CloudTokenConfigurationDialogFragment newInstance(final BluetoothDevice device) {
        final CloudTokenConfigurationDialogFragment fragment = new CloudTokenConfigurationDialogFragment();

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
        mCloudToken = mThingySdkManager.getCloudTokenData(mDevice);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        alertDialogBuilder.setTitle(getString(R.string.cloud_token_settings));
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_cloud_token, null);

        mCloudTokenLayout = view.findViewById(R.id.layout_cloud_token);
        mCloudTokenView = view.findViewById(R.id.cloud_token_view);
        mCloudTokenView.setText(mCloudToken);

        mCloudTokenView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String value = s.toString();
                if (TextUtils.isDigitsOnly(value) || value.length() > 250) {
                    mCloudTokenLayout.setError(getString(R.string.error_cloud_token_length));
                } else {
                    mCloudTokenLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

       return alertDialogBuilder.setView(view)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (mThingySdkManager != null) {
                            if (validateInput()) {
                                if (!mThingySdkManager.setCloudToken(mDevice, getValueFromView())) {
                                    Utils.showToast(getActivity(), getString(R.string.error_cloud_token));
                                    return;
                                }
                                ((ThingyBasicSettingsChangeListener) getParentFragment()).updateCloudToken();
                            }
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .create();
    }

    private boolean validateInput() {
        final String cloudToken = mCloudTokenView.getText().toString().trim();
        if (cloudToken.isEmpty() || cloudToken.getBytes().length > ThingyUtils.CLOUD_TOKEN_LENGTH) {
            mCloudTokenLayout.setError(getString(R.string.error_cloud_token_too_long));
            return false;
        }
        return true;
    }

    private String getValueFromView() {
        return mCloudTokenView.getText().toString().trim();
    }
}
