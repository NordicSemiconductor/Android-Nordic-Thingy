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

package no.nordicsemi.android.nrfthingy.sound;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.common.FileBrowserAppsAdapter;
import no.nordicsemi.android.nrfthingy.common.FileHelper;
import no.nordicsemi.android.nrfthingy.common.PermissionRationaleDialogFragment;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.thingy.ThingyService;
import no.nordicsemi.android.nrfthingy.widgets.AudioFileRecyclerAdapter;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class PcmModeFragment extends Fragment implements PermissionRationaleDialogFragment.PermissionDialogListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mAudioRecyclerView;
    private FloatingActionButton mFabPlay;
    private FloatingActionButton mFabImport;

    private boolean mIsPlaying = false;

    private String mFilePath;

    private Uri mFileStreamUri;

    private BluetoothDevice mDevice;

    private AudioFileRecyclerAdapter mAudioFileAdapter;
    private ThingySdkManager mThingySdkManager;

    private BroadcastReceiver mConnectionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(ThingyUtils.EXTRA_DEVICE);
            final String action = intent.getAction();
            if (action.startsWith(ThingyUtils.ACTION_DEVICE_DISCONNECTED)) {
                if (device.equals(mDevice)) {
                    mFabPlay.setImageResource(R.drawable.ic_play_white);
                    mIsPlaying = false;
                }
            } else if (action.startsWith(ThingyUtils.ACTION_SERVICE_DISCOVERY_COMPLETED)) {

            } else if (action.startsWith(ThingyUtils.EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION)) {
                final int mode = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_SPEAKER_MODE);
                switch (mode) {
                    case ThingyUtils.PCM_MODE:
                        final int status = intent.getExtras().getInt(ThingyUtils.EXTRA_DATA_SPEAKER_STATUS_NOTIFICATION);
                        if (ThingyUtils.SPEAKER_STATUS_FINISHED == status) {
                            mIsPlaying = false;
                            mFabPlay.setImageResource(R.drawable.ic_play_white);
                        }
                        break;
                }
            }
        }
    };

    public static PcmModeFragment newInstance(final BluetoothDevice device) {
        PcmModeFragment fragment = new PcmModeFragment();
        final Bundle args = new Bundle();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_pcm_mode, container, false);
        mFabPlay = rootView.findViewById(R.id.fab_play);
        mFabImport = rootView.findViewById(R.id.fab_import);
        mAudioRecyclerView = rootView.findViewById(R.id.audio_recycler_view);
        mAudioFileAdapter = new AudioFileRecyclerAdapter(getActivity());
        mAudioRecyclerView.setAdapter(mAudioFileAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        mAudioRecyclerView.setLayoutManager(layoutManager);
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
        mAudioRecyclerView.addItemDecoration(dividerItemDecoration, 0);
        mAudioFileAdapter.notifyDataSetChanged();
        listFiles();

        mFabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isConnected(mDevice, mThingySdkManager.getConnectedDevices())) {
                    if (!mIsPlaying) {
                        final File file = mAudioFileAdapter.getSelectedItem();
                        if (file != null) {
                            //parseFile(file);
                            //mThingySdkManager.playPcmSample(getActivity(), mDevice, file);
                            if (mThingySdkManager != null) {
                                if (!mThingySdkManager.isAnotherThingyIsStreamingAudio(mDevice)) {
                                    mThingySdkManager.playPcmSample(getActivity(), mDevice, file);
                                    mFabPlay.setImageResource(R.drawable.ic_stop_white);
                                    mIsPlaying = true;
                                } else {
                                    ThingyUtils.showToast(getActivity(), getString(R.string.already_streaming));
                                }
                            }
                        } else {
                            Utils.showToast(getActivity(), getString(R.string.no_audio_selected));
                        }
                    } else {
                        mThingySdkManager.stopPcmSample(mDevice);
                        mFabPlay.setImageResource(R.drawable.ic_play_white);
                        mIsPlaying = false;
                    }
                } else {
                    Utils.showToast(getActivity(), getString(R.string.thingy_not_connected));
                }
            }
        });

        mFabImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        if (mThingySdkManager != null && mThingySdkManager.isThingyStreamingAudio(mDevice)) {
            mFabPlay.setImageResource(R.drawable.ic_stop_white);
            mIsPlaying = true;
            final int selectedAudioTrackPosition = ((ThingyService.ThingyBinder) (mThingySdkManager.getThingyBinder())).getLastSelectedAudioTrack(mDevice);
            mAudioFileAdapter.setSelectedItemPosition(selectedAudioTrackPosition);
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mConnectionBroadcastReceiver, ThingyUtils.createSpeakerStatusChangeReceiver(mDevice.getAddress()));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsPlaying) {
            mFabPlay.setImageResource(R.drawable.ic_stop_white);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("SELECTED_FILE_POSITION", mAudioFileAdapter.getSelectedItemPosition());
        outState.putBoolean("AUDIO_STATE", mIsPlaying);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mThingySdkManager.isThingyStreamingAudio(mDevice)) {
            ((ThingyService.ThingyBinder) (mThingySdkManager.getThingyBinder())).setLastSelectedAudioTrack(mDevice, mAudioFileAdapter.getSelectedItemPosition());
        }
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mConnectionBroadcastReceiver);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.SELECT_FILE_REQ: {
                // clear previous data
                mFilePath = null;
                mFileStreamUri = null;
                Uri uri;
                // and read new one
                if (data != null) {
                    uri = data.getData();
                    if (uri == null) {
                        Utils.showToast(getActivity(), getString(R.string.audio_file_import_aborted));
                        break;
                    }
                } else {
                    Utils.showToast(getActivity(), getString(R.string.audio_file_import_aborted));
                    break;
                }
                /*
                 * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
                 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
                 */
                if (uri.getScheme().equals("file")) {
                    // the direct path to the file has been returned
                    mFilePath = uri.getPath();

                    //updateFileInfo(file.getName(), file.length(), mFileType);
                } else if (uri.getScheme().equals("content")) {
                    // an Uri has been returned
                    mFileStreamUri = uri;
                    // if application returned Uri for streaming, let's us it. Does it works?
                    // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
                    final Bundle extras = data.getExtras();
                    if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
                        mFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM);

                    // file name and size must be obtained from Content Provider
                    final Bundle bundle = new Bundle();
                    bundle.putParcelable(Utils.EXTRA_URI, uri);
                    getLoaderManager().restartLoader(Utils.SELECT_FILE_REQ, bundle, this);
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermission(final String permission, final int requestCode) {
        //Since the nested child fragment (activity > fragment > fragment) wasn't getting called the exact fragment index has to be used to get the fragment.
        //Also super.onRequestPermissionResult had to be used in both the main activity, fragment  inorder to propogate the request permission callback to the nested fragment
        getParentFragment().getChildFragmentManager().getFragments().get(1).requestPermissions(new String[]{permission}, requestCode);
    }

    @Override
    public void onCancellingPermissionRationale() {
        Utils.showToast(getActivity(), getString(R.string.requested_permission_not_granted_rationale));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.REQ_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Utils.showToast(getActivity(), getString(R.string.rationale_permission_denied));
                } else {
                    openFileChooser();
                }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = args.getParcelable(Utils.EXTRA_URI);
        /*
         * Some apps, f.e. Google Drive allow to select file that is not on the device. There is no "_data" column handled by that provider. Let's try to obtain
         * all columns and than check which columns are present.
         */
        // final String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
        return new CursorLoader(requireContext(), uri, null /* all columns, instead of projection */, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToNext()) {
            // Here we have to check the column indexes by name as we have requested for all. The order may be different.
            File file;
            String fileName = null;
            if (!TextUtils.isEmpty(fileName)) {
                mFilePath = fileName;
                file = new File(mFilePath);
                if (FileHelper.copyAudioFilesToLocalAppStorage(getContext(), file.getPath(), fileName)) {
                    mAudioFileAdapter.addFiles(file);
                    mAudioFileAdapter.notifyDataSetChanged();
                } else {
                    Utils.showToast(getActivity(), getString(R.string.audio_file_already_exists));
                }
            } else { //Files with URI scheme content may have a display name different to the file name
                final int index = data.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME);
                fileName = data.getString(index);
                Log.v("Tag", "filename: " + fileName);
                final ContentResolver cR = getActivity().getContentResolver(); //possible null pointer fix
                final MimeTypeMap mime = MimeTypeMap.getSingleton();
                final String type = mime.getExtensionFromMimeType(cR.getType(mFileStreamUri));
                Log.v("Tag", "stream: " + mFileStreamUri.toString());
                if (type != null && type.equalsIgnoreCase("wav")) {
                    if (FileHelper.copyAudioFilesToLocalAppStorage(getContext(), mFileStreamUri, fileName)) {
                        file = new File(String.valueOf(getContext().getFilesDir()), fileName);
                        mAudioFileAdapter.addFiles(file);
                        mAudioFileAdapter.notifyDataSetChanged();
                        mAudioRecyclerView.scrollToPosition(mAudioFileAdapter.getItemCount() - 1);
                        mAudioRecyclerView.performClick();
                    } else {
                        Utils.showToast(getActivity(), getString(R.string.audio_file_already_exists));
                    }
                } else {
                    Utils.showToast(getActivity(), getString(R.string.invalid_audio_file_format));
                }
            }
        } else {
            mFilePath = null;
            mFileStreamUri = null;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mFilePath = null;
        mFileStreamUri = null;
    }

    private void listFiles() {
        FileHelper.copyAudioFiles(requireContext());
        final File root = new File(String.valueOf(requireActivity().getFilesDir()));
        File[] files = root.listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".wav")) {
                mAudioFileAdapter.addFiles(f);
            }
        }

        mAudioFileAdapter.notifyDataSetChanged();
    }

    private void parseFile(final File file) {
        InputStream is = null;
        try {
            if (!file.getPath().startsWith("content")) {
                is = new FileInputStream(file);
            } else {
                Uri uri = Uri.parse(file.getPath());
                is = getActivity().getContentResolver().openInputStream(uri);
            }

            is.skip(44);
            int size = is.available();

            byte[] output = new byte[size / 2];
            byte[] bytes = new byte[1024];
            int length, offset = 0;
            while ((length = is.read(bytes)) > 0) {
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < length; i += 2) {
                    output[offset + i / 2] = (byte) (((bb.getShort() * 128.0) / 32768.0) + 128.0);
                }
                offset += length / 2;
            }
            if (mThingySdkManager != null) {
                if (!mThingySdkManager.isAnotherThingyIsStreamingAudio(mDevice)) {
                    mThingySdkManager.playPcmSample(getActivity(), mDevice, output);
                    mFabPlay.setImageResource(R.drawable.ic_stop_white);
                    mIsPlaying = true;
                } else {
                    ThingyUtils.showToast(getActivity(), getString(R.string.already_streaming));
                }
            }
        } catch (Exception e) {
            Utils.showToast(getActivity(), "Unable to stream audio");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openFileChooser() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // file browser has been found on the device
            startActivityForResult(intent, Utils.SELECT_FILE_REQ);
        } else {
            // there is no any file browser app, let's try to download one
            final View customView = getActivity().getLayoutInflater().inflate(R.layout.app_file_browser, null);
            final ListView appsList = customView.findViewById(android.R.id.list);
            appsList.setAdapter(new FileBrowserAppsAdapter(getActivity()));
            appsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            appsList.setItemChecked(0, true);
            new AlertDialog.Builder(getActivity()).setTitle(R.string.dfu_alert_no_file_browser_title).setView(customView)
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final int pos = appsList.getCheckedItemPosition();
                    if (pos >= 0) {
                        final String query = getResources().getStringArray(R.array.dfu_app_file_browser_action)[pos];
                        final Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
                        startActivity(storeIntent);
                    }
                }
            }).show();
        }
    }
}