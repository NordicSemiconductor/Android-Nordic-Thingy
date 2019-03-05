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

package no.nordicsemi.android.nrfthingy.widgets;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import no.nordicsemi.android.nrfthingy.R;

public class AudioFileRecyclerAdapter extends RecyclerView.Adapter<AudioFileRecyclerAdapter.CustomViewHolder> {
    private ArrayList<File> mAudioFileList;
    private ArrayList<String> mAudioFileDisplayNameList;
    private final LayoutInflater inflater;
    private RadioButton lastCheckedRadioButton;
    private int mSelectedItemPosition = -1;
    private int selectedItemPosition;
    private boolean mOnClickEnabled = true;

    public AudioFileRecyclerAdapter(final Context context) {
        this.inflater = LayoutInflater.from(context);
        mAudioFileList = new ArrayList<>();
        mAudioFileDisplayNameList = new ArrayList<>();
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int type) {

        View view = inflater.inflate(R.layout.list_item_audio, viewGroup, false);
        view.setSelected(true);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder customViewHolder, final int position) {
        final File file = mAudioFileList.get(position);
        customViewHolder.audioFileNameView.setText(file.getName());
        customViewHolder.relativeLayout.setTag(position);

        if (mSelectedItemPosition == (int) customViewHolder.relativeLayout.getTag()) {
            customViewHolder.audioRadioButton.setChecked(true);
            if (lastCheckedRadioButton != null && lastCheckedRadioButton != customViewHolder.audioRadioButton) {
                lastCheckedRadioButton.setChecked(false);
            }
            lastCheckedRadioButton = customViewHolder.audioRadioButton;
        }

        customViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickEnabled) {
                    mSelectedItemPosition = position;
                    customViewHolder.audioRadioButton.setChecked(true);
                    if (lastCheckedRadioButton != null && lastCheckedRadioButton != customViewHolder.audioRadioButton) {
                        lastCheckedRadioButton.setChecked(false);
                    }
                    lastCheckedRadioButton = customViewHolder.audioRadioButton;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAudioFileList.size();
    }

    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    public boolean addFiles(final File audioFile) {
        if (!mAudioFileDisplayNameList.contains(audioFile.getName())) {
            mAudioFileList.add(audioFile);
            mAudioFileDisplayNameList.add(audioFile.getName());
            return true;
        }
        return false;
    }

    public boolean addFiles(final File audioFile, final String mediaDisplayName) {
        if (!mAudioFileDisplayNameList.contains(mediaDisplayName)) {
            mAudioFileList.add(audioFile);
            mAudioFileDisplayNameList.add(mediaDisplayName);
            return true;
        }
        return false;
    }

    private void setSelection(CustomViewHolder viewHolder, int position) {

    }

    /**
     * Enabled by default
     */

    public void enableOnClick(final boolean flag) {
        mOnClickEnabled = flag;
    }

    public File getSelectedItem() {
        if (mSelectedItemPosition > -1) {
            return mAudioFileList.get(mSelectedItemPosition);
        }

        return null;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        mSelectedItemPosition = selectedItemPosition;
        notifyItemChanged(selectedItemPosition);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        LinearLayout relativeLayout;
        TextView audioFileNameView;
        RadioButton audioRadioButton;

        CustomViewHolder(final View view) {
            super(view);
            view.setSelected(false);
            relativeLayout = view.findViewById(R.id.audio_list_item_container);
            audioFileNameView = view.findViewById(R.id.audio_file_name);
            audioRadioButton = view.findViewById(R.id.rb_audio);
        }
    }
}