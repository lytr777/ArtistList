package com.mobiledevelopment.www.artistlist.list;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiledevelopment.www.artistlist.ArtistInfoActivity;
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

    /**
     * The Json link.
     */
    public final String jsonLink = "http://download.cdn.yandex.net/mobilization-2016/artists.json";

    private ListView artistList;
    private ProgressBar progress;
    private TextView wait;

    private SaveFragment saveFragment;
    private Container container;

    private List<Data> data;
    private DownloadJsonTask downloadJsonTask;
    private DownloadCoversTask downloadCoversTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);
        // Применяем стандартные настройки приложения при первом запуске
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        progress = (ProgressBar) findViewById(R.id.cover_progress);
        artistList = (ListView) findViewById(R.id.artist_list);
        wait = (TextView) findViewById(R.id.wait);

        progress.setProgress(0);
        artistList.setOnItemClickListener(this);

         // Востанавливаем состояние после поворота экрана.
        saveFragment = (SaveFragment) getFragmentManager().findFragmentByTag("SAVE_FRAGMENT");
        if (saveFragment != null) {
            container = saveFragment.getModel();
            downloadCoversTask = container.downloadCoversTask;
            downloadJsonTask = container.downloadJsonTask;
            data = container.data;
        } else {
            saveFragment = new SaveFragment();
            getFragmentManager().beginTransaction().add(saveFragment, "SAVE_FRAGMENT")
                    .commit();
            data = null;
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
        // Создаем экземпляр адаптера и передаем его в наш список
        ArtistAdapter adapter = new ArtistAdapter(this, data);
        artistList.setAdapter(adapter);
        // скрываем прогресс загрузки и надпись
        progress.setVisibility(View.INVISIBLE);
        wait.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.list_action_settings) {
            // запускаем экран настроек
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        private DownloadJsonTask() {} // заблокируем пустой конструктор.

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
            } else
                Toast.makeText(activity.getApplicationContext(), "Ошибка загрузки, проверьте подключение к интернету",
                        Toast.LENGTH_LONG).show();
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

        private DownloadCoversTask() {} // заблокируем пустой конструктор.

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
            } else
                Toast.makeText(activity.getApplicationContext(), "Ошибка загрузки, проверьте подключение к интернету",
                        Toast.LENGTH_LONG).show();
        }

        private void downloadFile(Context context, long id, URL url) throws IOException {
            Pair<File, Boolean> exist = CreateFile.createCacheFile(context, Long.toString(id) + "_cover");

            if (!exist.second)
                DownloadFile.downloadFile(url, exist.first);
        }
    }

    private static final String TAG = "ArtistList";
}
