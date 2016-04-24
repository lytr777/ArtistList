package com.mobiledevelopment.www.artistlist.save;

import com.mobiledevelopment.www.artistlist.ArtistInfoActivity;
import com.mobiledevelopment.www.artistlist.list.ArtistListActivity;
import com.mobiledevelopment.www.artistlist.reader.Data;

import java.util.List;

/**
 * Контейнер для хранения данных.
 */
public class Container {

    public ArtistListActivity.DownloadJsonTask downloadJsonTask;
    public ArtistListActivity.DownloadCoversTask downloadCoversTask;
    public List<Data> data;

    public ArtistInfoActivity.DownloadCoverTask downloadCoverTask;
    public Data element;

    /**
     * Создаем новый контейнер для хранение данных <class>ArtistListActivity</class>.
     *
     * @param downloadJsonTask   загрузчик для Json.
     * @param downloadCoversTask загрузчик для обложек исполнителей.
     * @param data               данные исполнителей.
     */
    public Container(ArtistListActivity.DownloadJsonTask downloadJsonTask,
                     ArtistListActivity.DownloadCoversTask downloadCoversTask, List<Data> data) {
        this.downloadJsonTask = downloadJsonTask;
        this.downloadCoversTask = downloadCoversTask;
        this.data = data;
    }

    /**
     * Создаем новый контейнер для хранение данных <class>ArtistInfoActivity</class>.
     *
     * @param downloadCoverTask загрузчик для большой обложки исполнителя.
     * @param element           данные выбраного исполнителя.
     */
    public Container(ArtistInfoActivity.DownloadCoverTask downloadCoverTask, Data element) {
        this.downloadCoverTask = downloadCoverTask;
        this.element = element;
    }
}
