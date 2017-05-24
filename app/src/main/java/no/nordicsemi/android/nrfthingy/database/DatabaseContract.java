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

package no.nordicsemi.android.nrfthingy.database;

import android.provider.BaseColumns;

public class DatabaseContract {

    public DatabaseContract() {
    }

    public static abstract class ThingyDbColumns implements BaseColumns {
        public static final String TABLE_NAME = "thingy";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_DEVICE_NAME = "name";
        public static final String COLUMN_LAST_SELECTED = "last_selected";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_MARKER_TITLE = "marker_title";
        public static final String COLUMN_MARKER_DESCRIPTION = "marker_description";
        public static final String COLUMN_NOTIFICATION_TEMPERATURE = "notification_temperature";
        public static final String COLUMN_NOTIFICATION_PRESSURE = "notification_pressure";
        public static final String COLUMN_NOTIFICATION_HUMIDITY = "notification_humidity";
        public static final String COLUMN_NOTIFICATION_AIR_QUALITY = "notification_air_quality";
        public static final String COLUMN_NOTIFICATION_COLOR = "notification_color";
        public static final String COLUMN_NOTIFICATION_BUTTON = "notification_button";
        public static final String COLUMN_NOTIFICATION_EULER = "notification_euler";
        public static final String COLUMN_NOTIFICATION_TAP = "notification_tap";
        public static final String COLUMN_NOTIFICATION_HEADING = "notification_heading";
        public static final String COLUMN_NOTIFICATION_GRAVITY_VECTOR = "notification_gravity";
        public static final String COLUMN_NOTIFICATION_ORIENTATION = "notification_orientation";
        public static final String COLUMN_NOTIFICATION_QUATERNION = "notification_quaternion";
        public static final String COLUMN_NOTIFICATION_PEDOMETER = "notification_pedometer";
        public static final String COLUMN_NOTIFICATION_RAW_DATA = "notification_raw_data";
    }

    public static abstract class CloudDbColumns implements BaseColumns {
        public static final String TABLE_NAME = "CLOUD";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_TEMPERATURE_UPLOAD = "temperature_upload";
        public static final String COLUMN_PRESSURE_UPLOAD = "pressure_upload";
        public static final String COLUMN_BUTTON_STATE_UPLOAD = "button_state_upload";
    }
}
