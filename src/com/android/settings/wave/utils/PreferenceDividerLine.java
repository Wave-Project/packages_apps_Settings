/*
 * Copyright (C) 2019 Wave-OS
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

package com.android.settings.wave.utils;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class PreferenceDividerLine extends AbstractPreferenceController implements LifecycleObserver {
    private static final String KEY_PREFERENCE_DIVIDER_LINE = "preference_divider_line";
    private Preference mPreference;

    public PreferenceDividerLine(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_PREFERENCE_DIVIDER_LINE;
    }
}
