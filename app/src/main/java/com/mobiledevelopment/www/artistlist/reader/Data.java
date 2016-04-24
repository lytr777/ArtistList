package com.mobiledevelopment.www.artistlist.reader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Данные исполнителя.
 */
public class Data implements Parcelable {

    public URL link, smallCover, bigCover;
    public long id;
    public int tracks, albums;
    public String name, description;
    public List<String> genres;

    /**
     * Пустой конструктор.
     */
    public Data() {
        this.id = -1;
        this.name = "";
        this.genres = null;
        this.tracks = -1;
        this.albums = -1;
        this.link = null;
        this.description = "";
        this.smallCover = null;
        this.bigCover = null;
    }

    /**
     * Конструктор с заполнением всех данных исполнителя.
     *
     * @param id          id исполнителя.
     * @param name        имя исполнителя.
     * @param genres      список жанров.
     * @param tracks      количество песен.
     * @param albums      количество альбомов.
     * @param link        ссылка на оффициальный сайт исполнителя.
     * @param description описание исполнителя.
     * @param smallCover  ссылка на обложку 300х300.
     * @param bigCover    ссылка на обложку 1000х1000.
     */
    public Data(long id, String name, List<String> genres, int tracks, int albums, URL link,
                String description, URL smallCover, URL bigCover) {
        this.id = id;
        this.name = name;
        this.genres = genres;
        this.tracks = tracks;
        this.albums = albums;
        this.link = link;
        this.description = description;
        this.smallCover = smallCover;
        this.bigCover = bigCover;
    }

    /**
     * Получить список жанров исполнителя в одной строке.
     *
     * @return строку со всеми жанрами исполнителя.
     */
    public String getGenres() {
        return genres.toString().substring(1, genres.toString().length() - 1);
    }

    /**
     * Получить пару id, <code>smallCover</code> URL.
     *
     * @return пара id, URL.
     */
    public Pair<Long, URL> getIdAndUrlPair(char c) {
        return (c == 's' ? new Pair<>(id, smallCover) : new Pair<>(id, bigCover));
    }

    /**
     * Получить обьект <class>Data</class> из <class>Parcel</class>.
     *
     * @param in объект <class>Parcel</class>.
     * @throws MalformedURLException если не получиловь создать создать ссылку из строки.
     */
    protected Data(Parcel in) throws MalformedURLException {
        // Распаковываем переданый Parcel
        id = in.readLong();
        name = in.readString();
        genres = new ArrayList<>();
        in.readStringList(genres);
        tracks = in.readInt();
        albums = in.readInt();
        description = in.readString();
        smallCover = new URL(in.readString());
        bigCover = new URL(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        // Запаковываем все данные в Parcel.
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeStringList(genres);
        parcel.writeInt(tracks);
        parcel.writeInt(albums);
        parcel.writeString(description);
        parcel.writeString(smallCover.toString());
        parcel.writeString(bigCover.toString());
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            try {
                return new Data(in);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };
}