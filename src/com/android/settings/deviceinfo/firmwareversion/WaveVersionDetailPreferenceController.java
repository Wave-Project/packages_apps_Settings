/*
 * Copyright (C) 2020 Wave-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class WaveVersionDetailPreferenceController extends BasePreferenceController {

    private static final String PROP_WAVE_VERSION = "ro.wave.version";
    private static final String PROP_WAVE_VERSION_CODE = "ro.wave.version_code";
    private static final String PROP_WAVE_RELEASETYPE = "ro.wave.releasetype";

    public WaveVersionDetailPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        String waveVersion = SystemProperties.get(PROP_WAVE_VERSION,
                this.mContext.getString(R.string.device_info_default));
        String waveVersionCode = SystemProperties.get(PROP_WAVE_VERSION_CODE,
                this.mContext.getString(R.string.device_info_default));
        String waveReleaseType = SystemProperties.get(PROP_WAVE_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        return waveVersionCode + " " + waveVersion + " | " + waveReleaseType;
    }
}
