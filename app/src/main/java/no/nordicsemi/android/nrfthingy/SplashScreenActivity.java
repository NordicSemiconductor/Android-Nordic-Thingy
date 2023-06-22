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

package no.nordicsemi.android.nrfthingy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;
import no.nordicsemi.android.nrfthingy.common.Utils;
import no.nordicsemi.android.nrfthingy.thingy.ThingyService;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class SplashScreenActivity extends AppCompatActivity implements ThingySdkManager.ServiceConnectionListener {
    private static final int DURATION = 1000;
    private ThingySdkManager mThingySdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThingySdkManager = ThingySdkManager.getInstance();

        if (Utils.isAppInitialisedBefore(this)) {
            setContentView(R.layout.activity_splash_screen);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mThingySdkManager.bindService(this, ThingyService.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mThingySdkManager.unbindService(this);
    }

    private void animateSplashScreen() {
        if (Utils.isAppInitialisedBefore(this)) {
            startCorrespondingActivity(MainActivity.class);
        } else {
            Intent intent = new Intent(SplashScreenActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
    }

    private void startCorrespondingActivity(final Class<? extends AppCompatActivity> activityClass) {
        final AlphaAnimation alpha = new AlphaAnimation(1, 0);
        alpha.setDuration(200);
        final View view = findViewById(R.id.relative_splash);
        final Handler handler = new Handler();
        handler.postDelayed(() -> view.setAnimation(alpha), 700);
        handler.postDelayed(() -> goToNextActivity(activityClass), DURATION);
    }

    @Override
    public void onServiceConnected() {
        animateSplashScreen();
    }

    private void goToNextActivity(final Class<? extends AppCompatActivity> activityClass) {
        final Intent newIntent = new Intent(SplashScreenActivity.this, activityClass);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(newIntent);
        finish();
    }
}
