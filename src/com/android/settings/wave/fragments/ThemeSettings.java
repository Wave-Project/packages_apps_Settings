package com.android.settings.wave.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableData;

import com.android.settings.R.;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.search.SearchIndexableRaw;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class ThemeSettings extends DashboardFragment {

    static final String TAG = "ThemeSettings";
    private static final String KEY_THEME_SETTINGS = "theme_settings";

    @Override
    protected String getLogTag() {
        return "ThemeSettings";
    }

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.theme_settings;
    }

    /** Enable indexing of searchable data */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.theme_settings) {
                @Override
                public List<SearchIndexableRaw> getRawDataToIndex(
                        Context context, boolean enabled) {
                    final List<SearchIndexableRaw> result = new ArrayList<>();

                    SearchIndexableRaw data = new SearchIndexableRaw(context);
                    data.title = context.getString(R.string.theme_settings);
                    data.key = KEY_THEME_SETTINGS;
                    data.screenTitle = context.getString(R.string.theme_settings_title);
                    result.add(data);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> niks = super.getNonIndexableKeys(context);
                    if (UserHandle.myUserId() != 0) {
                        niks.add(KEY_THEME_SETTINGS);
                    }
                    return niks;
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return UserHandle.myUserId() != 0;
                }

            };
}
