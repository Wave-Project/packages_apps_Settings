/*
 * Copyright (C) 2021 Wave-OS
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

package com.android.settings.deviceinfo.aboutphone;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.widget.TextView;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class WaveInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_WAVE_INFO = "wave_info";

    private static final String PROP_WAVE_VERSION = "ro.wave.version";
    private static final String PROP_WAVE_FLAVOUR = "ro.wave.flavour";
    private static final String PROP_WAVE_OFFICIAL = "ro.wave.is_official";
    private static final String PROP_WAVE_MAINTAINER = "ro.wave.maintainer_name";

    public WaveInfoPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference waveInfoPreference = screen.findPreference(KEY_WAVE_INFO);
        final TextView version = waveInfoPreference.findViewById(R.id.version_message);
        final TextView flavour = waveInfoPreference.findViewById(R.id.flavour_message);
        final TextView maintainer = waveInfoPreference.findViewById(R.id.maintainer_message);
        final TextView releaseType = waveInfoPreference.findViewById(R.id.release_type_message);
        final String waveVersion = SystemProperties.get(PROP_WAVE_VERSION, mContext.getString(R.string.device_info_default));
        final String waveFlavour = SystemProperties.get(PROP_WAVE_FLAVOUR, mContext.getString(R.string.device_info_default));
        final String waveMaintainer = SystemProperties.get(PROP_WAVE_MAINTAINER, mContext.getString(R.string.device_info_default));
        final String waveReleaseType = SystemProperties.getBoolean(PROP_WAVE_OFFICIAL, false) ? "Official" : "Unofficial";
        version.setText(waveVersion);
        flavour.setText(waveFlavour);
        maintainer.setText(waveMaintainer);
        releaseType.setText(waveReleaseType);
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
