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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import no.nordicsemi.android.nrfthingy.R;

public class PermissionRationaleDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String PERMISSION = "PERMISSION";
    private static final String REQUEST_CODE = "REQUEST_CODE";
    private static final String RATIONALE_MESSAGE = "RATIONALE_MESSAGE";
    private String mRationaleMessage;
    private int mRequestCode;
    private String mPermission;

    public static PermissionRationaleDialogFragment getInstance(final String permission, final int requestCode, final String message) {
        PermissionRationaleDialogFragment fragment = new PermissionRationaleDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PERMISSION, permission);
        bundle.putInt(REQUEST_CODE, requestCode);
        bundle.putString(RATIONALE_MESSAGE, message);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPermission = getArguments().getString(PERMISSION);
            mRequestCode = getArguments().getInt(REQUEST_CODE);
            mRationaleMessage = getArguments().getString(RATIONALE_MESSAGE);
        }
    }

    public interface PermissionDialogListener {
        void onRequestPermission(final String permission, final int requestCode);

        void onCancellingPermissionRationale();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).setTitle(R.string.rationale_title)
                .setMessage(mRationaleMessage)
                .setPositiveButton(R.string.rationale_request, this)
                .setNegativeButton(R.string.rationale_cancel, this).create();
        alertDialog.setCanceledOnTouchOutside(false);

        return alertDialog;
    }

    @Override
    public void onClick(final DialogInterface dialogInterface, final int position) {
        if (position == DialogInterface.BUTTON_POSITIVE) {
            Fragment fragment = getParentFragment();
            if (fragment != null) {
                ((PermissionDialogListener) getParentFragment()).onRequestPermission(mPermission, mRequestCode);
            } else {
                ((PermissionDialogListener) requireActivity()).onRequestPermission(mPermission, mRequestCode);
            }
        } else if (position == DialogInterface.BUTTON_NEGATIVE) {
            Fragment fragment = getParentFragment();
            if (fragment != null) {
                ((PermissionDialogListener) getParentFragment()).onCancellingPermissionRationale();
            } else {
                ((PermissionDialogListener) requireActivity()).onCancellingPermissionRationale();
            }
        }
    }
}
