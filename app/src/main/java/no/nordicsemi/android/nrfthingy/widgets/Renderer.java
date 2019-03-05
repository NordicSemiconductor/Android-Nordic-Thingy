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
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.renderer.RajawaliRenderer;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.database.DatabaseHelper;

public class Renderer extends RajawaliRenderer {

    private final DatabaseHelper mDatabaseHelper;
    private Context mContext;

    private Object3D mObjectModel;

    private double x, y, z, w;

    private boolean mIsConnected = false;
    private boolean mIsNotificationEnabled = false;

    public Renderer(Context context) {
        super(context);
        this.mContext = context;
        setFrameRate(60);
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public void setConnectionState(final boolean flag) {
        mIsConnected = flag;
    }

    public void setNotificationEnabled(final boolean flag) {
        mIsNotificationEnabled = flag;
    }

    public void setQuaternions(final double x, final double y, final double z, final double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    protected void initScene() {
        DirectionalLight directionalLight1 = new DirectionalLight(-1f, -1f, -1.0f);
        directionalLight1.setColor(1.0f, 1.0f, 1.0f);
        directionalLight1.setPower(10);
        //getCurrentScene().addLight(directionalLight1);

        PointLight pl1 = new PointLight();
        pl1.setPosition(0.6, 0.9, 10.4);
        pl1.setRotation(-45, 20, 45);
        pl1.setColor(1.0f, 1.0f, 1.0f);
        pl1.setPower(60);
        getCurrentScene().addLight(pl1);

        PointLight pl2 = new PointLight();
        pl2.setPosition(-2.5, 3.9, 10.4);
        pl2.setRotation(-45, -20, 45);
        pl1.setColor(1.0f, 1.0f, 1.0f);
        pl2.setPower(20);
        getCurrentScene().addLight(pl2);

        getCurrentScene().setBackgroundColor(0xFFFFFF);

        try {

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.thingymodel_obj);
            objParser.parse();
            mObjectModel = objParser.getParsedObject();

            getCurrentScene().addChild(mObjectModel);
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        if (mIsConnected && mIsNotificationEnabled) {
            Quaternion q = mObjectModel.getOrientation();
            q.setAll(w, -y, -x, z);
            mObjectModel.setOrientation(q);
        } else {
            Quaternion q = mObjectModel.getOrientation();
            q.setAll(1, 0, 0, 0);
            mObjectModel.setOrientation(q);
        }
    }
}
