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
import androidx.viewpager.widget.PagerAdapter;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.nrfthingy.R;

public class CustomPagerAdapter extends PagerAdapter {
    private Context mContext;
    private SparseArray<View> viewMap = new SparseArray<>();

    public CustomPagerAdapter(@NonNull final Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup collection, final int position) {
        final CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final ViewGroup layout = (ViewGroup) inflater.inflate(customPagerEnum.getLayoutResId(), collection, false);
        final ImageView imageView = layout.findViewById(R.id.image);
        viewMap.put(position, imageView);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull final ViewGroup container, final int position, final @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return CustomPagerEnum.values().length;
    }

    public boolean isViewFromObject(@NonNull final View view, @NonNull final Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        final CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        return mContext.getString(customPagerEnum.getTitleResId());
    }

    public View getCurrentView(final int key) {
        return viewMap.get(key);
    }

    public void clearViews() {
        if (viewMap != null) {
            viewMap.clear();
        }
    }

    public enum CustomPagerEnum {
        CLOUD_STEP_1(R.string.intro_ifttt, R.layout.cloud_step0),
        CLOUD_STEP_2(R.string.account_preparation, R.layout.cloud_step1),
        CLOUD_STEP_3(R.string.maker_webhooks_service, R.layout.cloud_step2),
        CLOUD_STEP_4(R.string.maker_webhooks_service_key, R.layout.cloud_step3),
        CLOUD_STEP_5(R.string.access_key_to_thingy, R.layout.cloud_step4),
        CLOUD_STEP_6(R.string.enable_cloud_features, R.layout.cloud_step5);

        private int mTitleResId;
        private int mLayoutResId;

        CustomPagerEnum(final int titleResId, final int layoutResId) {
            mTitleResId = titleResId;
            mLayoutResId = layoutResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public int getLayoutResId() {
            return mLayoutResId;
        }
    }
}
