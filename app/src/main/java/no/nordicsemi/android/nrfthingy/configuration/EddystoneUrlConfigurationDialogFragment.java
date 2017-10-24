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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class EddystoneUrlConfigurationDialogFragment extends DialogFragment {

    //If you're setting up the Nordc Thingy example app project from GitHub make sure to create your own project
    //on the Google Developer Console and enable the URLShortener API and use the API key in your project.
    private LinearLayout mShortUrlContainer;

    private Spinner mEddystoneUrlTypesView;
    private TextInputLayout mEddystonUrlLayout;
    private TextInputEditText mEddystoneUrlView;
    private TextView mShortUrl;
    private Switch mSwitchPhysicalWeb;

    private String mUrl;

    private BluetoothDevice mDevice;
    private ThingySdkManager mThingySdkManager;
    public EddystoneUrlConfigurationDialogFragment() {

    }

    public static EddystoneUrlConfigurationDialogFragment newInstance(final BluetoothDevice device) {
        EddystoneUrlConfigurationDialogFragment fragment = new EddystoneUrlConfigurationDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(Utils.CURRENT_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(Utils.CURRENT_DEVICE);
        }
        mThingySdkManager = ThingySdkManager.getInstance();
        mUrl = mThingySdkManager.getEddystoneUrl(mDevice);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.physcial_web_url_title));
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_eddystone_url, null);

        mShortUrlContainer = view.findViewById(R.id.short_url_container);
        mEddystonUrlLayout = view.findViewById(R.id.layout_url_data);
        mEddystoneUrlTypesView = view.findViewById(R.id.url_types);
        mEddystoneUrlView = view.findViewById(R.id.url_data_view);
        mShortUrl = view.findViewById(R.id.short_url);
        mSwitchPhysicalWeb = view.findViewById(R.id.switch_physical_web);

        mEddystoneUrlView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mEddystonUrlLayout.setError(null);
                mShortUrlContainer.setVisibility(View.GONE);
            }
        });

        mSwitchPhysicalWeb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mEddystoneUrlView.setEnabled(false);
                    mEddystoneUrlTypesView.setEnabled(false);
                } else {
                    mEddystoneUrlTypesView.setEnabled(true);
                    mEddystoneUrlView.setEnabled(true);
                }
            }
        });

        updateUi();

        alertDialogBuilder.setView(view).setNeutralButton(getString(R.string.shorten_url), null).
                setPositiveButton(getString(R.string.confirm), null).
                setNegativeButton(getString(R.string.cancel), null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = mEddystoneUrlTypesView.getSelectedItem().toString().trim() + mEddystoneUrlView.getText().toString().trim();
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    shortenUrl(url);
                    //shortenUrl(url);
                } else {
                    Utils.showToast(getActivity(), getString(R.string.error_empty_url_text));
                }
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThingySdkManager != null) {
                    if (mSwitchPhysicalWeb.isChecked()) {
                        if (validateInput()) {
                            if (mThingySdkManager.setEddystoneUrl(mDevice, getValueFromView())) {
                                dismiss();
                                ((ThingeeBasicSettingsChangeListener) getParentFragment()).updatePhysicalWebUrl();
                            } else {
                                Utils.showToast(getActivity(), getString(R.string.error_configuring_char));
                            }
                        }
                    } else {
                        mThingySdkManager.disableEddystoneUrl(mDevice);
                        dismiss();
                        ((ThingeeBasicSettingsChangeListener) getParentFragment()).updatePhysicalWebUrl();
                    }
                }
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return alertDialog;
    }

    private void updateUi() {
        mEddystoneUrlTypesView.requestFocus();
        String url;
        if (mUrl.startsWith("https://www.")) {
            url = mUrl.replace("https://www.", "");
            mEddystoneUrlTypesView.setSelection(0);
            mEddystoneUrlView.setText(url);
        } else if (mUrl.startsWith("http://www.")) {
            url = mUrl.replace("http://www.", "");
            mEddystoneUrlTypesView.setSelection(1);
            mEddystoneUrlView.setText(url);
        } else if (mUrl.startsWith("https://")) {
            url = mUrl.replace("https://", "");
            mEddystoneUrlTypesView.setSelection(2);
            mEddystoneUrlView.setText(url);
        } else if (mUrl.startsWith("http://")) {
            url = mUrl.replace("http://", "");
            mEddystoneUrlTypesView.setSelection(3);
            mEddystoneUrlView.setText(url);
        } else {
            if (mUrl != null && mUrl.length() == 0) {
                mSwitchPhysicalWeb.setChecked(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private boolean validateInput() {

        if (mShortUrlContainer.getVisibility() == View.VISIBLE) {
            return true;
        }

        final String urlText = mEddystoneUrlView.getText().toString().trim();
        if (urlText.isEmpty()) {
            mEddystonUrlLayout.setError(getString(R.string.error_empty_url_text));
            return false;
        }

        final String url = mEddystoneUrlTypesView.getSelectedItem().toString().trim() + urlText;
        if (url.isEmpty()) {
            mEddystonUrlLayout.setError(getString(R.string.error_empty_url_text));
            return false;
        } else {

            if (!Patterns.WEB_URL.matcher(url).matches()) {
                mEddystonUrlLayout.setError(getString(R.string.error_empty_url_text));
                return false;
            } else if (Utils.encodeUri(url).length > 18) {
                mEddystonUrlLayout.setError(getString(R.string.url_shortener_message));
                return false;
            }
        }

        return true;
    }

    public String getValueFromView() {
        final String urlText;
        if (mShortUrlContainer.getVisibility() != View.VISIBLE) {
            urlText = mEddystoneUrlTypesView.getSelectedItem().toString().trim() + mEddystoneUrlView.getText().toString().trim();
        } else {
            urlText = mShortUrl.getText().toString().trim();
        }

        return urlText;
    }

    private void shortenUrl(final String longUrl){
        String jsonBody = "{'longUrl':'" + longUrl + "'}";
        CloudTask task = new CloudTask(jsonBody);
        task.execute();
    }

    private void handleJsonResponse(final String response) {
        if (!response.isEmpty()) {
            try {
                final JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("error")) {
                    final JSONObject errorObject = jsonResponse.getJSONObject("error");
                    final JSONArray errorArray = errorObject.getJSONArray("errors");
                    for(int i = 0; i < errorArray.length(); i++){
                        final JSONObject error = errorArray.getJSONObject(i);
                        final String errorMessage;
                        if(error.has("reason") && error.has("message")){
                            final String reason = "Reason: " + error.getString("reason");
                            final String message =  "Message: " + error.getString("message");
                            errorMessage = message + " " + reason;
                        } else {
                            errorMessage = "Unknown error";
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast(getActivity(), errorMessage);
                            }
                        });
                        break;
                    }
                } else {
                    final String newUrl = jsonResponse.getString("id");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mShortUrlContainer.setVisibility(View.VISIBLE);
                            final SpannableString urlAttachment = new SpannableString(newUrl);
                            urlAttachment.setSpan(new UnderlineSpan(), 0, newUrl.length(), 0);
                            mShortUrl.setText(urlAttachment);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeStream(final OutputStream outputStream, final String jsonData) {
        try {
            outputStream.write(jsonData.getBytes());
            outputStream.flush();
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void readStream(final InputStream inputStream) {
        BufferedReader br = null;
        try {
            StringBuffer sb = new StringBuffer();
            br = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            final String result = sb.toString();
            handleJsonResponse(result);
        } catch (IOException e) {

            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class CloudTask extends AsyncTask<Void, Void, Void> {

        private static final String URL_SHORTENER_API_KEY = "URL_SHORTENER_API_KEY";
        private static final String URL = "https://www.googleapis.com/urlshortener/v1/url?key=" + URL_SHORTENER_API_KEY;
        private final String json;

        public CloudTask(final String json) {
            this.json = json;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                java.net.URL url = new URL(URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                writeStream(out, json);

                final int reponseCode = urlConnection.getResponseCode();
                //Check for the reposnse code before reading the error stream, if not causes an exception with stream closed as there may not be an error
                if(reponseCode !=  HttpURLConnection.HTTP_OK){
                    readStream(new BufferedInputStream(urlConnection.getErrorStream()));
                } else {
                    readStream(new BufferedInputStream(urlConnection.getInputStream()));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }
}
