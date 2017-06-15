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

package no.nordicsemi.android.thingylib.decoder;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ADPCMDecoder {
	private static final String TAG = "ADPCMDecoder";
	private static final int FRAME_SIZE = 131;

	/** Intel ADPCM step variation table */
	private static final int[] INDEX_TABLE = { -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8, };

	/** ADPCM step size table */
	private static final int[] STEP_SIZE_TABLE = { 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209,
			230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
			5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767 };

	/** The WAV file header (RIFF header) */
	private static final byte[] WAV_HEADER = {
			// RIFF header (46 bytes)
			0x52, 0x49, 0x46, 0x46, // 'RIFF' 
			0x2E, 0x00, 0x00, 0x00, // Size of the main block in bytes (will be overwritten) (46 bytes)
			0x57, 0x41, 0x56, 0x45, // 'WAVE' 
			0x66, 0x6D, 0x74, 0x20, // 'fmt '
			0x12, 0x00, 0x00, 0x00, // Size of the FMT block (18 bytes)
			// -- FMT BLOCK --
			0x01, 0x00, // Linear PCM/uncompressed
			0x01, 0x00, // Number of channels 
			(byte) 0x80, 0x3E, 0x00, 0x00, // Frequency sampling (16 kHz) 
			0x00, 0x7D, 0x00, 0x00, // Average bytes per second (32 000): Avgbps = Number of channels * Sampling Frequency * (Precision >> 3) 
			0x02, 0x00, // Block alignment (2): blkalign = Number of channels * (Precision >> 3)
			0x10, 0x00, // Precision (Number of bits per sample) (16)
			0x00, 0x00, // Nothing?
			// -- END OF FMT BLOCK --
			0x64, 0x61, 0x74, 0x61, // 'DATA'
			0x00, 0x00, 0x00, 0x00 // Size of content in bytes (will be overwritten) 
	};

	private DecoderListener mListener;
	private byte[] mFrame;
	private int mPositionInFrame;
	private int mDecodedDataSize;
	private int mReceivedDataSize;
	private int mFramesCount;
	private int mFramesLost;
	private int mPacketsCount;
	private int mInvalidPacketsCount;
	private long mStartTime;

	private RandomAccessFile mOutputStream;
	private File mTempFile;

	public static interface DecoderListener {
		/**
		 * A full frame (13 packets) has been decoded.
		 * 
		 * @param pcm
		 *            the decoded frame in PCM format
		 * @param frameNumber
		 *            the frame index (starting from 0)
		 */
		public void onFrameDecoded(final byte[] pcm, final int frameNumber);
	}

	public ADPCMDecoder(final Context context, final boolean persistent) {
		mFrame = new byte[FRAME_SIZE];
		mPositionInFrame = 0;
		mDecodedDataSize = 0;
		mReceivedDataSize = 0;
		mPacketsCount = 0;
		mInvalidPacketsCount = 0;
		mFramesCount = 0;
		mFramesLost = 0;
		mStartTime = 0L;

		if (persistent) {
			try {
				final File tempDir = context.getCacheDir();
				final File wav = mTempFile = File.createTempFile("voice", ".wav", tempDir);
				mOutputStream = new RandomAccessFile(wav, "rw");
				mOutputStream.write(WAV_HEADER);
			} catch (final IOException e) {
				Log.e(TAG, "Error while creating temporary file", e);
			}
		}
	}

	/**
	 * Sets the decoder listener.
	 * 
	 * @param listener
	 */
	public void setListener(final DecoderListener listener) {
		mListener = listener;
	}

	/**
	 * Adds the next frame packet. When the frame is completed it will be decoded and saved to the temporary file.
	 * you may register {@link DecoderListener} with {@link #setListener(DecoderListener)} to obtain each decoded frame for visualization.
	 * 
	 * @param packet
	 *            the packet from the Voice Input Module
	 */
	public void add(final byte[] packet) {
		if (mStartTime == 0)
			mStartTime = SystemClock.elapsedRealtime();

		++mPacketsCount;
		mReceivedDataSize += packet.length;

		// Copy the packet to the frame buffer
		System.arraycopy(packet, 0, mFrame, mPositionInFrame, packet.length);
		mPositionInFrame += packet.length;

		// If frame is completed, decode it and save
		if (mPositionInFrame == FRAME_SIZE) {
			mPositionInFrame = 0;
			++mFramesCount;

			// Decode ADPCM -> PCM
			final byte[] pcm = decode(mFrame);

			// Write data to the temporary file
			try {
				if (mOutputStream != null)
					mOutputStream.write(pcm);
				mDecodedDataSize += pcm.length;
			} catch (final IOException e) {
				Log.e(TAG, "Error while writing PCM data to file", e);
			}

			// Notify the listener (if any)
			if (mListener != null)
				mListener.onFrameDecoded(pcm, mFramesCount - 1);
		}
	}

	/**
	 * Saves the PCM voice to the temporary file.
	 * 
	 * @return the WAV file
	 */
	public File save() {
		try {
			// Write RIFF size
			final ByteBuffer riffSize = ByteBuffer.allocate(4);
			riffSize.order(ByteOrder.LITTLE_ENDIAN);
			riffSize.putInt(mDecodedDataSize + 46);
			mOutputStream.seek(4L);
			mOutputStream.write(riffSize.array());

			// Write data size
			riffSize.putInt(0, mDecodedDataSize);
			mOutputStream.seek(42L);
			mOutputStream.write(riffSize.array());

			mOutputStream.close();
			mOutputStream = null;
		} catch (final IOException e) {
			Log.e(TAG, "Error while closing temporary file", e);
		}
		return mTempFile;
	}

	/**
	 * Returns <code>true</code> if no voice packets were decoded, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return mPacketsCount == 0;
	}

	/**
	 * Returns the total number of packets that were received, including invalid ones.
	 * 
	 * @return the total number of received packets
	 */
	public int getPacketsCount() {
		return mPacketsCount;
	}

	/**
	 * Returns the number of frames decoded until now.
	 * 
	 * @return the number of frames
	 */
	public int getFramesCount() {
		return mFramesCount;
	}

	/**
	 * Returns the number of invalid frames. Sometimes, due to the Android lag or bug, a packet is skipped and the frame may not be decoded.
	 * The decoder must discard some packets until a packet with size 19 bytes come (the last one in frame) when it starts decoding a new frame.
	 * 
	 * @return the number of frames that were lost on Android size
	 */
	public int getInvalidFramesCount() {
		return (mInvalidPacketsCount + 12) / 13;
	}

	/**
	 * Returns the estimated number of lost frames. This number is calculated using by knowing the voice duration and expected frames number.
	 * 
	 * @return the number of frames that were skipped by the Voice Input Module due to the slow connection.
	 */
	public int getFramesLost() {
		final float duration = SystemClock.elapsedRealtime() - mStartTime; // transfer duration in milliseconds
		/*
		 * The sample frequency is 16kHz = 16000 samples per second. There are 512 samples in each frame (13 packets), so about 31.25 frames are expected every second. 
		 */
		final int expectedFrames = (int) (duration * 31.25f / 1000.0f);
		if (expectedFrames - mFramesCount - mFramesLost > 0)
			mFramesLost++;
		return mFramesLost;
	}

	/**
	 * Returns the size of the decoded voice (in PCM format) in bytes.
	 * 
	 * @return PCM size in bytes
	 */
	public int getDataSize() {
		return mDecodedDataSize;
	}

	/**
	 * Returns the size of compressed voice (in ADPCM format) sent by the Voice Input Module, including invalid packets.
	 * 
	 * @return ADPCM size in bytes
	 */
	public int getReceivedDataSize() {
		return mReceivedDataSize;
	}

	/**
	 * Decodes the ADPCM frame into PCM format
	 * 
	 * @param adpcm
	 *            the input in ADPCM format
	 * @return the PCM decoded array
	 */
	private byte[] decode(final byte[] adpcm) {
		final byte[] pcm = new byte[512];

		// The first 2 bytes of ADPCM frame are the predicted value
		int valuePredicted = readShort(adpcm, 0);
		// The 3rd byte is the index value
		int index = adpcm[2];
		if (index < 0)
			index = 0;
		if (index > 88)
			index = 88;

		int diff; /* Current change to valuePredicted */
		boolean bufferStep = false;
		int inputBuffer = 0;
		int delta = 0;
		int sign = 0;
		int step = STEP_SIZE_TABLE[index];

		for (int in = 3, out = 0; in < adpcm.length; out += 2) {
			/* Step 1 - get the delta value */
			if (bufferStep) {
				delta = inputBuffer & 0x0F;
				in++;
			} else {
				inputBuffer = adpcm[in];
				delta = (inputBuffer >> 4) & 0x0F;
			}
			bufferStep = !bufferStep;

			/* Step 2 - Find new index value (for later) */
			index += INDEX_TABLE[delta];
			if (index < 0)
				index = 0;
			if (index > 88)
				index = 88;

			/* Step 3 - Separate sign and magnitude */
			sign = delta & 8;
			delta = delta & 7;

			/* Step 4 - Compute difference and new predicted value */
			diff = step >> 3;
			if ((delta & 4) > 0)
				diff += step;
			if ((delta & 2) > 0)
				diff += step >> 1;
			if ((delta & 1) > 0)
				diff += step >> 2;

			if (sign > 0)
				valuePredicted -= diff;
			else
				valuePredicted += diff;

			/* Step 5 - clamp output value */
			if (valuePredicted > 32767)
				valuePredicted = 32767;
			else if (valuePredicted < -32768)
				valuePredicted = -32768;

			/* Step 6 - Update step value */
			step = STEP_SIZE_TABLE[index];

			/* Step 7 - Output value */
			writeShort(pcm, out, valuePredicted);
		}
		return pcm;
	}

	private static short readShort(final byte[] data, final int start) {
		int b1 = data[start] & 0xff;
		int b2 = data[start + 1] & 0xff;

		return (short) (b1 << 8 | b2 << 0);
	}

	private static void writeShort(final byte[] data, final int start, final int value) {
		data[start] = (byte) (value & 0xff);
		data[start + 1] = (byte) ((value >>> 8) & 0xFF);
	}

}
