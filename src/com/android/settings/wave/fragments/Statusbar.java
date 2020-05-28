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

package com.android.settings.wave.fragments;

import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import static android.provider.Settings.System.SHOW_BATTERY_PERCENT;
import static android.provider.Settings.System.STATUS_BAR_BATTERY_STYLE;

public class Statusbar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "Status bar";
    private static final String KEY_BATTERY_STYLE = "battery_style";
    private static final String KEY_BATTERY_PERCENTAGE = "battery_percentage";

    private ListPreference mBatteryStylePref;
    private SwitchPreference mBatteryPercentagePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);

        mBatteryStylePref = (ListPreference) findPreference(KEY_BATTERY_STYLE);
        int value = Settings.System.getInt(getActivity().getContentResolver(), STATUS_BAR_BATTERY_STYLE, 0);
        mBatteryStylePref.setValue(Integer.toString(value));
        int index = mBatteryStylePref.findIndexOfValue(Integer.toString(value));
        mBatteryStylePref.setSummary(mBatteryStylePref.getEntries()[index]);
        mBatteryStylePref.setOnPreferenceChangeListener(this);

        mBatteryPercentagePref = (SwitchPreference) findPreference(KEY_BATTERY_PERCENTAGE);
        boolean enabled = Settings.System.getInt(getActivity().getContentResolver(), SHOW_BATTERY_PERCENT, 0) == 1;
        mBatteryPercentagePref.setChecked(enabled);
        mBatteryPercentagePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryStylePref) {
            int value = Integer.parseInt((String) newValue);
            int index = mBatteryStylePref.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(), STATUS_BAR_BATTERY_STYLE, value);
            mBatteryStylePref.setSummary(mBatteryStylePref.getEntries()[index]);
            return true;
        } else if (preference == mBatteryPercentagePref) {
            boolean showPercentage = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(), SHOW_BATTERY_PERCENT, showPercentage ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }
}
