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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import no.nordicsemi.android.nrfthingy.R;

public class VoiceVisualizer extends SurfaceView implements SurfaceHolder.Callback {
    private final int PRECISSION = 4;
    private final float[] mPointsBuffer = new float[2 * 512 / PRECISSION]; // 512 samples, each has X and Y value, each point (but fist and last) must be doubled: A->B, B->C, C->D etc.
    private final float[] mPointsBuffer2 = new float[2 * 512 / PRECISSION];
    private float[] mCurrentBuffer;
    private float[] mPoints;
    private final Object mLock = new Object();
    private boolean isDrawing = false;

    private int mWidth, mHeight;

    private Paint mLinePaint;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    public VoiceVisualizer(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mLinePaint = new Paint();
        mLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.nordicLake));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setStyle(Paint.Style.STROKE);

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        mHolder = holder;
        Canvas canvas = mCanvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        holder.unlockCanvasAndPost(canvas);
        // Adapt the old points to new surface size after orientation change
        final float[] points = mPoints;
        if (points != null) {
            final float factorX = width / (1.0f * mWidth);
            for (int i = 0; i < points.length; i += 2)
                points[i] = points[i] * factorX;

            final float factorY = height / (2.0f * mHeight);
            for (int i = 1; i < points.length; i += 2)
                points[i] = points[i] * factorY;
        }

        mCurrentBuffer = mPointsBuffer;
        mWidth = width;
        mHeight = height / 2;
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        isDrawing = false;
    }

    public void stopDrawing() {
        isDrawing = false;
    }

    /**
     * Copies the byte array to a float buffer where each odd cell is a X point value and even cell is Y value. X range is from 0 to view width and Y from 0 to height. The PCM value 0 is located on
     * height/2. After the buffer is ready it swaps the buffer for drawing thread.
     *
     * @param pcm the decoded PCM byte array where each sample is defined by 2 bytes
     */
    public void draw(final byte[] pcm) {
        if (mCurrentBuffer == null) // Surface not created yet or destroyed (orientation changes)
            return;

        final float[] buffer = mCurrentBuffer;
        final int length = pcm.length / PRECISSION;
        final float stepHoriz = (float) mWidth / length;
        final float stepVert = (float) mHeight / Short.MAX_VALUE;

        int out = 0;
        for (int i = 0; i < length; i += 2) {
            buffer[out] = buffer[out + 2] = stepHoriz * i;
            buffer[out + 1] = buffer[out + 3] = mHeight + stepVert * readShort(pcm, i * PRECISSION);
            out += i > 0 ? 4 : 2;
        }

        buffer[out] = mWidth;
        buffer[out + 1] = mHeight + stepVert * readShort(pcm, (length - 1) * PRECISSION);

        synchronized (mLock) {
            mPoints = buffer;
        }
        // Swap the buffer
        mCurrentBuffer = mCurrentBuffer == mPointsBuffer ? mPointsBuffer2 : mPointsBuffer;

        if (!isDrawing) {
            isDrawing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isDrawing) {
                        mCanvas = mHolder.lockCanvas();
                        if (mCanvas != null) {
                            doDraw(mCanvas);
                            mHolder.unlockCanvasAndPost(mCanvas);
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * This method clears the view and draws the data points on the surface's canvas.
     *
     * @param canvas the canvas to draw on
     */
    /* package */void doDraw(final Canvas canvas) {
        // Draw white background
        canvas.drawColor(Color.WHITE);

        // Draw the line
        synchronized (mLock) {
            if (mPoints != null)
                canvas.drawLines(mPoints, mLinePaint);
        }
    }

    private static short readShort(final byte[] data, final int start) {
        int b1 = data[start] & 0xff;
        int b2 = data[start + 1] & 0xff;

        return (short) (b2 << 8 | b1);
    }
}
