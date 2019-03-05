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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import no.nordicsemi.android.nrfthingy.R;

public class AboutDialogFragment extends DialogFragment {

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        final View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_about, null);

        final AlertDialog alertDialog = alertDialogBuilder.setView(view)
                .setPositiveButton(R.string.ok, null)
                .create();

        view.findViewById(R.id.action_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/227282803964174"));
                final PackageManager packageManager = v.getContext().getPackageManager();
                final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.isEmpty()) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/nordicsemiconductor"));
                }
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.action_twitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/NordicTweets"));
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.action_linkedin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://company/23302")); // This does not work in LinkedIn 3.3.3 (the current until now)
                final PackageManager packageManager = v.getContext().getPackageManager();
                final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.isEmpty()) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://touch.www.linkedin.com/?dl=no#company/23302"));
                }
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.action_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/user/NordicSemi"));
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.action_devzone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://devzone.nordicsemi.com/questions/"));
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        // Obtain version number
        try {
            final String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            final TextView version = view.findViewById(R.id.version);
            version.setText(getString(R.string.version, versionName));
        } catch (final Exception e) {
            // do nothing
        }

        return alertDialog;
    }
}
