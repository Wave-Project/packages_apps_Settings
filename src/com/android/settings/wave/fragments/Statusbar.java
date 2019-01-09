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

package com.android.settings.wave.fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.graph.BatteryMeterDrawableBase;

import static android.provider.Settings.Secure.STATUS_BAR_BATTERY_STYLE;
import static android.provider.Settings.System.SHOW_BATTERY_PERCENT;

public class Statusbar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "Status bar";

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String BATTERY_PERCENT = "show_battery_percent";

    private ListPreference mStatusBarBattery;
    private ListPreference mBatteryPercentage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        int batteryStyle = Settings.Secure.getInt(getActivity().getContentResolver(), STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        mBatteryPercentage = (ListPreference) findPreference(BATTERY_PERCENT);
        int showPercent = Settings.System.getInt(getActivity().getContentResolver(), SHOW_BATTERY_PERCENT, 1);
        mBatteryPercentage.setValue(Integer.toString(showPercent));
        int valueIndex = mBatteryPercentage.findIndexOfValue(String.valueOf(showPercent));
        mBatteryPercentage.setSummary(mBatteryPercentage.getEntries()[valueIndex]);
        mBatteryPercentage.setOnPreferenceChangeListener(this);
        boolean hideForcePercentage = batteryStyle == BatteryMeterDrawableBase.BATTERY_STYLE_TEXT;
        mBatteryPercentage.setEnabled(!hideForcePercentage);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mStatusBarBattery) {
            int battStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(), STATUS_BAR_BATTERY_STYLE, battStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            boolean hideForcePercentage = battStyle == BatteryMeterDrawableBase.BATTERY_STYLE_TEXT;
            mBatteryPercentage.setEnabled(!hideForcePercentage);
            return true;
        } else if (preference == mBatteryPercentage) {
            int showPercent = Integer.valueOf((String) newValue);
            int index = mBatteryPercentage.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(), SHOW_BATTERY_PERCENT, showPercent);
            mBatteryPercentage.setSummary(mBatteryPercentage.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WAVE_METRICSLOGGER;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
