package com.mobiledevelopment.www.artistlist.reader;

import android.util.JsonReader;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Извлекает данные из Json файла.
 */
public class Reader {

    /**
     * Загружаем Json файл.
     *
     * @param jsonUrl ссылка на Json файл.
     * @return список с данными о исполнителях.
     * @throws IOException если не можем открыть/закрыть соединения, или ответ от сервера отличен от <code>HttpURLConnection.HTTP_OK</code>.
     */
    public static List<Data> downloadJson(URL jsonUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) jsonUrl.openConnection();
        InputStream in = null;

        try {
            // Проверяем HTTP код ответа. Ожидаем только ответ 200 (ОК).
            // Остальные коды считаем ошибкой.
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "URL: " + jsonUrl);
            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            in = conn.getInputStream();
            // Передаем поток для чтения
            return readJsonStream(in);
        } finally {
            // Закрываем все потоки и соедиениние
            if (in != null) {
                in.close();
            }
            conn.disconnect();
        }
    }

    /**
     * Открываем <class>JsonReader</class> для дальнейшего чтения.
     *
     * @param in поток на загрузку Json файла.
     * @return список с данными о исполнителях.
     * @throws IOException если не можем открыть/закрыть соединения.
     */
    private static List<Data> readJsonStream(InputStream in) throws IOException {
        // Открываем JsonReader и передаем его.
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readData(reader);
        } finally {
            // Закрываем reader.
            reader.close();
        }
    }

    /**
     * Читаем данные по каждому исполнителю и добавляем информацию в список.
     *
     * @param reader обьект <class>JsonReader</class>.
     * @return список с данными о исполнителях.
     * @throws IOException если во время чтения разрывается соединение.
     */
    private static List<Data> readData(JsonReader reader) throws IOException {
        List<Data> data = new ArrayList<>();
        String fieldName;
        
        reader.beginArray();
        // Цикл по исполнителяем внутри json файла.
        while (reader.hasNext()) {
            // Создаем пустой элемент и заполняем его по ходу чтения.
            Data element = new Data();
            reader.beginObject();
            // Цикл по данным исполнителя.
            while (reader.hasNext()) {
                fieldName = reader.nextName();
                switch (fieldName) {
                    case "id":
                        element.id = reader.nextInt();
                        break;
                    case "name":
                        element.name = reader.nextString();
                        break;
                    case "genres":
                        reader.beginArray();
                        element.genres = new ArrayList<>();
                        // Считываем жанры пока они есть.
                        while (reader.hasNext())
                            element.genres.add(reader.nextString());
                        reader.endArray();
                        break;
                    case "tracks":
                        element.tracks = reader.nextInt();
                        break;
                    case "albums":
                        element.albums = reader.nextInt();
                        break;
                    case "link":
                        element.link = new URL(reader.nextString());
                        break;
                    case "description":
                        element.description = reader.nextString();
                        break;
                    case "cover":
                        reader.beginObject();
                        while (reader.hasNext()) {
                            fieldName = reader.nextName();
                            switch (fieldName) {
                                case "small":
                                    element.smallCover = new URL(reader.nextString());
                                    break;
                                case "big":
                                    element.bigCover = new URL(reader.nextString());
                                    break;
                                default:
                                    // Пропускаем лишние данные.
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                        break;
                    default:
                        // Пропускаем лишние данные.
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            data.add(element);
        }
        reader.endArray();
        return data;
    }

    private static final String TAG = "Reader";
}
