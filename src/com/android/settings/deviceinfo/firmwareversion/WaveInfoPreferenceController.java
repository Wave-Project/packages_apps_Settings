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
import android.widget.TextView;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class WaveInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_WAVE_INFO = "wave_info";

    private static final String PROP_WAVE_VERSION = "ro.wave.version";
    private static final String PROP_WAVE_VERSION_CODE = "ro.wave.version_code";
    private static final String PROP_WAVE_RELEASETYPE = "ro.wave.releasetype";
    private static final String PROP_WAVE_MAINTAINER = "ro.wave.maintainer";

    public WaveInfoPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference waveInfoPreference = screen.findPreference(KEY_WAVE_INFO);
        final TextView version = (TextView) waveInfoPreference.findViewById(R.id.version_message);
        final TextView versionCode = (TextView) waveInfoPreference.findViewById(R.id.version_code_message);
        final TextView releaseType = (TextView) waveInfoPreference.findViewById(R.id.release_type_message);
        final TextView maintainer = (TextView) waveInfoPreference.findViewById(R.id.maintainer_message);
        final String waveVersion = SystemProperties.get(PROP_WAVE_VERSION,
                this.mContext.getString(R.string.device_info_default));
        final String waveVersionCode = SystemProperties.get(PROP_WAVE_VERSION_CODE,
                this.mContext.getString(R.string.device_info_default));
        final String waveReleaseType = SystemProperties.get(PROP_WAVE_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String waveMaintainer = SystemProperties.get(PROP_WAVE_MAINTAINER,
                this.mContext.getString(R.string.device_info_default));
        version.setText(waveVersion);
        versionCode.setText(waveVersionCode);
        releaseType.setText(waveReleaseType);
        maintainer.setText(waveMaintainer);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_WAVE_INFO;
    }
}
