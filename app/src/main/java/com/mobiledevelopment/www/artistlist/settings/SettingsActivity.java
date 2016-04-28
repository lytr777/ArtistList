package com.mobiledevelopment.www.artistlist.settings;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.mobiledevelopment.www.artistlist.R;

/**
 * Экран с настройками.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int SETTINGS_ACTIVITY_CODE = 183; //10110111
    public static final int LANGUAGE_RESULT_CODE = 178; //10110010

    public static final String KEY_LANGUAGE = "language_list";
    public static final String KEY_FULL_CACHING = "full_caching_switch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_LANGUAGE)) {
            Configuration config = Localisation.getConfig(getBaseContext(), sharedPreferences);
            getBaseContext().getResources().updateConfiguration(config, null);
            setResult(LANGUAGE_RESULT_CODE);
            // перезапускаем активити для применения настроек
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Регистрием слушателя на изменение настроек
        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Отменяем регистрацию
        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Фрагмент с настройками.
     */
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }
    }

    private static final String TAG = "Settings";
}
