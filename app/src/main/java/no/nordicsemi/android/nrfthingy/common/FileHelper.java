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

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper {
    private static final String TAG = "FileHelper";

    private static final String NORDIC_FOLDER = "Nordic Semiconductor";

    public static void copyAudioFiles(final Context context) {
        /*
         * Copy example HEX files to the external storage. Files will be copied if the DFU Applications folder is missing
         */
        final File root = new File(String.valueOf(context.getFilesDir()));

        File audioFile;
        String audioFileName;
        for (int i = 0; i < Utils.AUDIO_FILES.length; i++) {
            audioFileName = Utils.AUDIO_FILES[i];
            audioFile = new File(root, audioFileName);
            if (!audioFile.exists()) {
                copyRawAudioResource(context, context.getResources().getIdentifier(audioFileName.replace(".wav", ""), "raw", context.getPackageName()), audioFile);
            }
        }
    }

    public static boolean copyAudioFilesToLocalAppStorage(final Context context, final Uri uri, final String audioFileName) {
        /*
         * Copy example HEX files to the external storage. Files will be copied if the DFU Applications folder is missing
         */
        final File root = new File(String.valueOf(context.getFilesDir()));

        File audioFile;
        audioFile = new File(root, audioFileName);
        if (!audioFile.exists()) {
            copyExternalAudioFile(context, uri, audioFile);
            return true;
        }
        return false;
    }

    public static boolean copyAudioFilesToLocalAppStorage(final Context context, final String path, final String audioFileName) {
        /*
         * Copy example HEX files to the external storage. Files will be copied if the DFU Applications folder is missing
         */
        final File root = new File(Environment.getExternalStorageDirectory(), NORDIC_FOLDER);

        File audioFile = new File(root, audioFileName);
        if (!audioFile.exists()) {
            copyExternalAudioFile(context, path, audioFile);
            return true;
        }
        return false;
    }

    /**
     * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
     *
     * @param context  activity context
     * @param rawResId the resource id
     * @param dest     destination file
     */
    public static void copyRawResource(final Context context, final int rawResId, final File dest) {
        try {
            final InputStream is = context.getResources().openRawResource(rawResId);
            final FileOutputStream fos = new FileOutputStream(dest);

            final byte[] buf = new byte[1024];
            int read;
            try {
                while ((read = is.read(buf)) > 0)
                    fos.write(buf, 0, read);
            } finally {
                is.close();
                fos.close();
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error while copying HEX file " + e.toString());
        }
    }

    /**
     * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
     *
     * @param context  activity context
     * @param rawResId the resource id
     * @param dest     destination file
     */
    private static void copyRawAudioResource(final Context context, final int rawResId, final File dest) {
        try {
            final InputStream is = context.getResources().openRawResource(rawResId);
            final FileOutputStream fos = new FileOutputStream(dest);
            int size = is.available();
            final byte[] buf = new byte[size];
            int read;
            try {
                while ((read = is.read(buf)) > 0)
                    fos.write(buf, 0, read);
            } finally {
                is.close();
                fos.close();
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error while copying HEX file " + e.toString());
        }
    }

    /**
     * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
     *
     * @param context activity context
     * @param uri     the uri
     * @param dest    the destination file
     */
    private static void copyExternalAudioFile(final Context context, final Uri uri, final File dest) {
        try {
            final InputStream is = context.getContentResolver().openInputStream(uri);
            final FileOutputStream fos = new FileOutputStream(dest);
            int size = is.available();
            final byte[] buf = new byte[size];
            int read;
            try {
                while ((read = is.read(buf)) > 0)
                    fos.write(buf, 0, read);
            } finally {
                is.close();
                fos.close();
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error while copying HEX file " + e.toString());
        }
    }

    /**
     * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
     *
     * @param context activity context
     * @param path    the uri
     * @param dest    the destination file
     */
    private static void copyExternalAudioFile(final Context context, final String path, final File dest) {
        try {
            final InputStream is = new FileInputStream(path);
            final FileOutputStream fos = new FileOutputStream(dest);
            int size = is.available();
            final byte[] buf = new byte[size];
            int read;
            try {
                while ((read = is.read(buf)) > 0)
                    fos.write(buf, 0, read);
            } finally {
                is.close();
                fos.close();
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error while copying HEX file " + e.toString());
        }
    }
}
