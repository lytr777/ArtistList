package com.mobiledevelopment.www.artistlist.list;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobiledevelopment.www.artistlist.ArtistInfoActivity;
import com.mobiledevelopment.www.artistlist.settings.Localisation;
import com.mobiledevelopment.www.artistlist.R;
import com.mobiledevelopment.www.artistlist.file.CreateFile;
import com.mobiledevelopment.www.artistlist.file.DownloadFile;
import com.mobiledevelopment.www.artistlist.reader.Data;
import com.mobiledevelopment.www.artistlist.reader.Reader;
import com.mobiledevelopment.www.artistlist.save.Container;
import com.mobiledevelopment.www.artistlist.save.SaveFragment;
import com.mobiledevelopment.www.artistlist.settings.SettingsActivity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Экран со списком исполнителей.
 */
public class ArtistListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public final String jsonLink = "http://download.cdn.yandex.net/mobilization-2016/artists.json";

    private ListView artistList;
    private ProgressBar progress;
    private TextView wait;
    private Button reload;

    private SaveFragment saveFragment;
    private Container container;

    private List<Data> data;
    private DownloadJsonTask downloadJsonTask;
    private DownloadCoversTask downloadCoversTask;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);
        // Применяем стандартные настройки приложения при первом запуске
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Configuration config = Localisation.getConfig(getBaseContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        getResources().updateConfiguration(config, null);
        getSupportActionBar().setTitle(R.string.app_name);
        progress = (ProgressBar) findViewById(R.id.cover_progress);
        artistList = (ListView) findViewById(R.id.artist_list);
        wait = (TextView) findViewById(R.id.wait);
        wait.setText(R.string.wait);
        reload = (Button) findViewById(R.id.list_reload_button);
        reload.setText(R.string.connection_error_button);
        reload.setVisibility(View.INVISIBLE);
        progress.setProgress(0);
        artistList.setOnItemClickListener(this);

        // Востанавливаем состояние после поворота экрана.
        saveFragment = (SaveFragment) getFragmentManager().findFragmentByTag("SAVE_FRAGMENT");
        if (saveFragment != null) {
            container = saveFragment.getModel();
            downloadCoversTask = container.downloadCoversTask;
            downloadJsonTask = container.downloadJsonTask;
            data = container.data;
            Log.d(TAG + "fr", "Фрагмент не найден");
        } else {
            saveFragment = new SaveFragment();
            getFragmentManager().beginTransaction().add(saveFragment, "SAVE_FRAGMENT")
                    .commit();
            data = null;
            Log.d(TAG, "Фрагмент не найден");
        }

        downloadJson();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, ArtistInfoActivity.class);
        intent.putExtra("artist", data.get(i));
        startActivity(intent);
    }

    private void downloadJson() {
        if (data == null) {
            if (downloadJsonTask == null) {
                // Создаем новый таск, только если не было ранее запущенного таска
                try {
                    URL url = new URL(jsonLink);
                    // Создаем новый таск, только если не было ранее запущенного таска
                    downloadJsonTask = new DownloadJsonTask(this, url);
                    downloadJsonTask.execute();
                } catch (IOException e) {
                    Log.e(TAG, "Не удалось загрузить Json файл : " + e, e);
                }
            } else {
                // Передаем в ранее запущенный таск текущий объект Activity
                downloadJsonTask.attachActivity(this);
            }
        } else {
            // если данные уже загружены то загружаем обложки.
            downloadCovers();
        }
    }

    private void downloadCovers() {
        if (data != null) {
            if (downloadCoversTask == null) {
                // Создаем новый таск, только если не было ранее запущенного таска
                List<Pair<Long, URL>> urls = new ArrayList<>();
                for (int i = 0; i < data.size(); i++)
                    urls.add(data.get(i).getIdAndUrlPair('s'));
                downloadCoversTask = new DownloadCoversTask(this, urls);
                downloadCoversTask.execute();
            } else {
                // Передаем в ранее запущенный таск текущий объект Activity
                downloadCoversTask.attachActivity(this);
            }
        }
    }

    private void createList() {
        // Перезагружаем меню опций
        invalidateOptionsMenu();
        // Создаем экземпляр адаптера и передаем его в наш список
        ArtistAdapter adapter = new ArtistAdapter(this, data);
        artistList.setAdapter(adapter);
        // скрываем прогресс загрузки и надпись
        progress.setVisibility(View.INVISIBLE);
        wait.setVisibility(View.INVISIBLE);
    }

    /**
     * Пытаемся повторно загрузить данные.
     *
     * @param view view элемент обьекта, вызвавшего метод.
     */
    public void tryReload(View view) {
        if (view.getId() == R.id.list_reload_button) {
            wait.setText(R.string.wait);
            reload.setVisibility(View.INVISIBLE);
            if (data == null)
                downloadJson();
            else
                downloadCovers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return (downloadJsonTask == null && downloadCoversTask == null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.list_action_settings) {
            // запускаем экран настроек
            startActivityForResult(new Intent(this, SettingsActivity.class),
                    SettingsActivity.SETTINGS_ACTIVITY_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsActivity.SETTINGS_ACTIVITY_CODE) {
            if (resultCode == SettingsActivity.LANGUAGE_RESULT_CODE)
                // Перезапускаем активити для смены языка.
                startActivity(new Intent(this, ArtistListActivity.class));
            startActivityForResult(new Intent(this, SettingsActivity.class),
                    SettingsActivity.SETTINGS_ACTIVITY_CODE);
        }
    }

    @Override
    protected void onPause() {
        // Сохраняем состояние экрана при разрушении активити.
        container = new Container(downloadJsonTask, downloadCoversTask, data);
        saveFragment.setModel(container);
        super.onPause();
    }

    /**
     * Загрузчик списка из Json файла.
     */
    public static class DownloadJsonTask extends AsyncTask<Void, Void, Boolean> {

        private URL url;
        private ArtistListActivity activity;

        /**
         * Конструктор нового загрузчика списка.
         *
         * @param activity активити из которого запущен загрузчик
         * @param url      URL для загрузки
         */
        public DownloadJsonTask(ArtistListActivity activity, URL url) {
            this.activity = activity;
            this.url = url;
        }

        private DownloadJsonTask() {
        } // заблокируем пустой конструктор.

        /**
         * Смена активити.
         *
         * @param newActivity новое активити.
         */
        public void attachActivity(ArtistListActivity newActivity) {
            this.activity = newActivity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                activity.data = Reader.downloadJson(url);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Не удалось загрузить Json файл : " + e, e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean resultCode) {
            if (resultCode) {
                activity.downloadJsonTask = null;
                activity.downloadCovers();
            } else {
                activity.downloadJsonTask = null;
                activity.wait.setText(R.string.connection_error);
                activity.reload.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Загрузчик обложек для исполнителей.
     */
    public static class DownloadCoversTask extends AsyncTask<Void, Integer, Boolean> {

        private ArtistListActivity activity;
        private List<Pair<Long, URL>> urls;
        private int data_size;
        private int current_progress;

        /**
         * Конструктор нового загрузчика обложек.
         *
         * @param activity текущее активити.
         * @param urls     список id и ссылок для загрузки
         */
        public DownloadCoversTask(ArtistListActivity activity, List<Pair<Long, URL>> urls) {
            this.activity = activity;
            this.urls = urls;
            data_size = urls.size();
            current_progress = 0;
        }

        private DownloadCoversTask() {
        } // заблокируем пустой конструктор.

        /**
         * Смена активити.
         *
         * @param newActivity новое активити.
         */
        public void attachActivity(ArtistListActivity newActivity) {
            this.activity = newActivity;
        }

        /**
         * Обновление состояния загрузки.
         *
         * @param values новое состояние.
         */
        protected void onProgressUpdate(Integer... values) {
            this.current_progress = values[values.length - 1];
            if (activity != null)
                activity.progress.setProgress(current_progress);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = 0; i < data_size; i++) {
                try {
                    downloadFile(activity.getApplicationContext(), urls.get(i).first, urls.get(i).second);
                    // если новое состояние больше текущего, то обновляем состояние загрузки
                    if (current_progress < (i + 1) * 100 / data_size)
                        publishProgress((i + 1) * 100 / data_size);
                } catch (IOException e) {
                    Log.e(TAG, "Не удалось загрузить " + urls.get(i).first + " cover : " + e, e);
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean resultCode) {
            if (resultCode) {
                activity.downloadCoversTask = null;
                activity.createList();
            } else {
                activity.downloadCoversTask = null;
                activity.wait.setText(R.string.connection_error);
                activity.reload.setVisibility(View.VISIBLE);
            }
        }

        private void downloadFile(Context context, long id, URL url) throws IOException {
            Pair<File, Boolean> exist = CreateFile.createCacheFile(context, Long.toString(id) + "_cover");

            if (!exist.second)
                DownloadFile.downloadFile(url, exist.first);
        }
    }

    private static final String TAG = "ArtistList";
}
