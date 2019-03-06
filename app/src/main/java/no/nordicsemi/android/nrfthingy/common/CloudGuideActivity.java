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

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import no.nordicsemi.android.nrfthingy.R;
import no.nordicsemi.android.nrfthingy.widgets.CustomPagerAdapter;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class CloudGuideActivity extends AppCompatActivity {

    private int mPreviousPosition = -1;
    private AnimationDrawable mCloudGuideAnimation;
    private View view;
    private ImageView imageView;
    private CustomPagerAdapter mCustomPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_guide);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.cloud_guide_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ViewPager viewPager = findViewById(R.id.cloud_guide_pager);
        mCustomPagerAdapter = new CustomPagerAdapter(this);
        viewPager.setAdapter(mCustomPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        if (position != mPreviousPosition) {
                            if (mCloudGuideAnimation != null) {
                                mCloudGuideAnimation.stop();
                            }
                            view = mCustomPagerAdapter.getCurrentView(position);
                            if (view != null) {
                                mPreviousPosition = position;
                            }
                        }
                        break;
                    case 1:
                        if (position != mPreviousPosition) {
                            if (mCloudGuideAnimation != null) {
                                mCloudGuideAnimation.stop();
                            }
                            view = mCustomPagerAdapter.getCurrentView(position);
                            if (view != null) {
                                imageView = view.findViewById(R.id.image);
                                mCloudGuideAnimation = (AnimationDrawable) imageView.getDrawable();//Background();
                                mCloudGuideAnimation.start();
                                mPreviousPosition = position;
                            }
                        }
                        break;
                    case 2:
                        if (position != mPreviousPosition) {
                            if (mCloudGuideAnimation != null && mCloudGuideAnimation.isRunning()) {
                                mCloudGuideAnimation.stop();
                                mCloudGuideAnimation = null;
                            }
                            view = mCustomPagerAdapter.getCurrentView(position);
                            if (view != null) {
                                imageView = view.findViewById(R.id.image);
                                mCloudGuideAnimation = (AnimationDrawable) imageView.getDrawable();//Background();
                                mCloudGuideAnimation.start();
                                mPreviousPosition = position;
                            }
                        }
                        break;
                    case 3:
                        if (position != mPreviousPosition) {
                            if (mCloudGuideAnimation != null && mCloudGuideAnimation.isRunning()) {
                                mCloudGuideAnimation.stop();
                                mCloudGuideAnimation = null;
                            }
                            view = mCustomPagerAdapter.getCurrentView(position);
                            if (view != null) {
                                imageView = view.findViewById(R.id.image);
                                mCloudGuideAnimation = (AnimationDrawable) imageView.getDrawable();//Background();
                                mCloudGuideAnimation.start();
                                mPreviousPosition = position;
                            }
                        }
                        break;
                    case 4:
                        if (position != mPreviousPosition) {
                            if (mCloudGuideAnimation != null && mCloudGuideAnimation.isRunning()) {
                                mCloudGuideAnimation.stop();
                                mCloudGuideAnimation = null;
                            }
                            view = mCustomPagerAdapter.getCurrentView(position);
                            if (view != null) {
                                imageView = view.findViewById(R.id.image);
                                mCloudGuideAnimation = (AnimationDrawable) imageView.getDrawable();//Background();
                                mCloudGuideAnimation.start();
                                mPreviousPosition = position;
                            }
                        }
                        break;
                    case 5:
                        if (position != mPreviousPosition) {
                            if (mCloudGuideAnimation != null && mCloudGuideAnimation.isRunning()) {
                                mCloudGuideAnimation.stop();
                                mCloudGuideAnimation = null;
                            }
                            view = mCustomPagerAdapter.getCurrentView(position);
                            if (view != null) {
                                imageView = view.findViewById(R.id.image);
                                mCloudGuideAnimation = (AnimationDrawable) imageView.getDrawable();//Background();
                                mCloudGuideAnimation.start();
                                mPreviousPosition = position;
                            }
                        }
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.v(ThingyUtils.TAG, "Selected: " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCustomPagerAdapter != null) {
            mCustomPagerAdapter.clearViews();
            mCustomPagerAdapter.notifyDataSetChanged();
        }

        if (mCloudGuideAnimation != null) {
            mCloudGuideAnimation.stop();
            mCloudGuideAnimation = null;
        }
    }
}
