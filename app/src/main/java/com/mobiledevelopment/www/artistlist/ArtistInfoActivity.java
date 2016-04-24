package com.mobiledevelopment.www.artistlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiledevelopment.www.artistlist.file.CreateFile;
import com.mobiledevelopment.www.artistlist.file.DownloadFile;
import com.mobiledevelopment.www.artistlist.reader.Data;
import com.mobiledevelopment.www.artistlist.save.Container;
import com.mobiledevelopment.www.artistlist.save.SaveFragment;
import com.mobiledevelopment.www.artistlist.settings.SettingsActivity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * The type Artist info activity.
 */
public class ArtistInfoActivity extends AppCompatActivity {

    private ProgressBar progress;
    private ImageView bigCover;

    private SaveFragment saveFragment;
    private Container container;
    private boolean change_orientation;

    private DownloadCoverTask downloadCoverTask;
    private Data element;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        // Востанавливаем состояние после поворота экрана.
        saveFragment = (SaveFragment) getFragmentManager().findFragmentByTag("SAVE_FRAGMENT");
        if (saveFragment != null) {
            container = saveFragment.getModel();
            downloadCoverTask = container.downloadCoverTask;
            element = container.element;
            change_orientation = true; // устанавливаем флаг, что экран был перевернут
            // и не требует повторной загрузки обложки.
        } else {
            saveFragment = new SaveFragment();
            getFragmentManager().beginTransaction().add(saveFragment, "SAVE_FRAGMENT")
                    .commit();
            element = getIntent().getParcelableExtra("artist");
            change_orientation = false;
        }

        if (element == null) {
            Log.w(TAG, "Данные исполнителя не были переданы");
            finish();
        }
        // изменяем заголовок в Action bar.
        getSupportActionBar().setTitle(element.name);
        progress = (ProgressBar) findViewById(R.id.big_cover_progress);
        bigCover = (ImageView) findViewById(R.id.artist_cover);
        // заполняем данными поля.
        TextView genres = (TextView) findViewById(R.id.artist_genres);
        TextView albumsAndTracks = (TextView) findViewById(R.id.artist_tracks_and_albums);
        TextView description = (TextView) findViewById(R.id.description);
        genres.setText(element.getGenres());
        albumsAndTracks.setText(String.format(getResources().getString(R.string.tracks_and_albums),
                element.albums, element.tracks));
        description.setText(element.name + " - " + element.description);

        downloadCover(element);
    }

    private void downloadCover(Data element) {
        if (downloadCoverTask == null) {
            // Создаем новый таск, только если не было ранее запущенного таска
            downloadCoverTask = new DownloadCoverTask(this, element.getIdAndUrlPair('b'));
            downloadCoverTask.execute();
        } else {
            // Передаем в ранее запущенный таск текущий объект Activity
            downloadCoverTask.attachActivity(this);
        }
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
        container = new Container(downloadCoverTask, element);
        saveFragment.setModel(container);
        super.onPause();
    }

    /**
     * Загрузчик обложки.
     */
    public static class DownloadCoverTask extends AsyncTask<Void, Void, Bitmap> {

        private Pair<Long, URL> url;
        private ArtistInfoActivity activity;

        /**
         * Конструктор нового загрузчика обложки.
         *
         * @param activity текущее активити.
         * @param url      ссылка для загрузки
         */
        public DownloadCoverTask(ArtistInfoActivity activity, Pair<Long, URL> url) {
            this.activity = activity;
            this.url = url;
        }

        /**
         * Смена активити.
         *
         * @param newActivity новое активити.
         */
        public void attachActivity(ArtistInfoActivity newActivity) {
            this.activity = newActivity;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return downloadFile(activity.getApplicationContext(), url.first, url.second);
            } catch (IOException e) {
                Log.e(TAG, "Не удалось загрузить файл : " + e, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                activity.downloadCoverTask = null;
                activity.progress.setVisibility(View.INVISIBLE);
                activity.bigCover.setImageBitmap(result);
            } else
                Toast.makeText(activity.getApplicationContext(), "Ошибка загрузки, проверьте " +
                        "подключение к интернету", Toast.LENGTH_LONG).show();
        }

        private Bitmap downloadFile(Context context, long id, URL url) throws IOException {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            if (preferences.getBoolean(SettingsActivity.KEY_FULL_CACHING, false)) {
                Pair<File, Boolean> exist = CreateFile.createCacheFile(context, id + "_big_cover");
                if (!exist.second)
                    DownloadFile.downloadFile(url, exist.first);
                return BitmapFactory.decodeFile(exist.first.getPath());
            } else {
                Pair<File, Boolean> exist = CreateFile.createCacheFile(context, "big_cover");
                if (!activity.change_orientation || !exist.second)
                    DownloadFile.downloadFile(url, exist.first);
                return BitmapFactory.decodeFile(exist.first.getPath());
            }
        }
    }

    private static final String TAG = "ArtistInfo";
}
