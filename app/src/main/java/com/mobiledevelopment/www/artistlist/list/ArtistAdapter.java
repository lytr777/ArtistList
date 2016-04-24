package com.mobiledevelopment.www.artistlist.list;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import static android.graphics.BitmapFactory.decodeFile;
import static com.mobiledevelopment.www.artistlist.file.CreateFile.*;

import com.mobiledevelopment.www.artistlist.R;
import com.mobiledevelopment.www.artistlist.reader.Data;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Адаптер для списка исполнителей.
 */
public class ArtistAdapter extends BaseAdapter {

    private List<Data> data;
    private Context context;
    private LayoutInflater layoutInflater;

    /**
     * Создаем новый экземпляр адаптера.
     *
     * @param context контекст приложения.
     * @param data    список исполнителей.
     */
    public ArtistAdapter(final Context context, List<Data> data) {
        this.data = data;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            // Получаем view из xml файла
            view = layoutInflater.inflate(R.layout.artist_item, parent, false);
        } else
            // Получаем view из предыдущих вызовов этой функции.
            view = convertView;

        // Находим компоненты view по их id.
        ImageView cover = (ImageView) view.findViewById(R.id.cover);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView genres = (TextView) view.findViewById(R.id.genres);
        TextView albumsAndTracks = (TextView) view.findViewById(R.id.albums_and_tracks);

        try {
            // Проверяем что файл с обложкой существует и декодируем его.
            Pair<File, Boolean> exist = createCacheFile(context, Long.toString(data.get(position).id) + "_cover");
            if (exist.second)
                cover.setImageBitmap(decodeFile(exist.first.getPath()));
        } catch (IOException e) {
            Log.e(TAG, "Файл не найден : " + e, e);
        }
        // Заполняем наши компоненты.
        name.setText(data.get(position).name);
        genres.setText(data.get(position).getGenres());
        albumsAndTracks.setText(String.format(context.getResources().getString(R.string.tracks_and_albums),
                data.get(position).albums, data.get(position).tracks));

        // Возвращаем готовый view.
        return view;
    }

    private static final String TAG = "ArtistAdapter";
}
