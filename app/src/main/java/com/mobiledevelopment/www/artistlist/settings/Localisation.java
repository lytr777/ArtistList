package com.mobiledevelopment.www.artistlist.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Класс локализации.
 */
public class Localisation {

    /**
     * Возвращает кофигурацию с текущей локализацией.
     *
     * @param baseContext       базовый контекст приложения.
     * @param sharedPreferences настройки, в которых хранится локализация.
     * @return новую конфигурацию.
     */
    public static Configuration getConfig(Context baseContext, SharedPreferences sharedPreferences) {
        String lang = sharedPreferences.getString(SettingsActivity.KEY_LANGUAGE, "default");
        Locale locale = new Locale(lang);
        Configuration config = baseContext.getResources().getConfiguration();
        config.locale = locale;
        return config;
    }
}
