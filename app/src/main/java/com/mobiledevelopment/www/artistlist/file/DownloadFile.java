package com.mobiledevelopment.www.artistlist.file;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Загрузка файла.
 */
public class DownloadFile {

    /**
     * Загружаем файл по его URL.
     *
     * @param downloadUrl ссылка на загружаемый файл.
     * @param destFile    Файл в который будем записывать.
     * @throws IOException если не можем открыть/закрыть соединения, или ответ от сервера отличен
     *                     от <code>HttpURLConnection.HTTP_OK</code>.
     */
    public static void downloadFile(URL downloadUrl, File destFile) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
        InputStream in = null;
        OutputStream out = null;
        int contentLength;

        try {
            // Проверяем HTTP код ответа. Ожидаем только ответ 200 (ОК).
            // Остальные коды считаем ошибкой.
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "URL: " + downloadUrl);
            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            // Узнаем размер файла, который мы собираемся скачать
            // (приходит в ответе в HTTP заголовке Content-Length)
            contentLength = conn.getContentLength();
            Log.d(TAG, "Content Length: " + contentLength);

            // Создаем временный буффер для I/O операций размером 128кб
            byte[] buffer = new byte[1024 * 128];

            // Размер полученной порции в байтах
            int receivedBytes;
            // Сколько байт всего получили (и записали).
            int receivedLength = 0;

            // Начинаем читать ответ и открываем файл для записи
            in = conn.getInputStream();
            out = new FileOutputStream(destFile);

            // В цикле читаем данные порциями в буффер, и из буффера пишем в файл.
            // Заканчиваем по признаку конца файла - in.read(buffer) возвращает -1
            while ((receivedBytes = in.read(buffer)) >= 0) {
                out.write(buffer, 0, receivedBytes);
                receivedLength += receivedBytes;
            }
            // Сверяем количество полученых байт с размером файла
            if (receivedLength != contentLength) {
                Log.w(TAG, "Received " + receivedLength + " bytes, but expected " + contentLength);
            } else {
                Log.d(TAG, "Received " + receivedLength + " bytes");
            }
            Log.d(TAG, "destFile " + destFile);

        } finally {
            // Закрываем все потоки и соедиениние
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            conn.disconnect();
        }
    }

    private static final String TAG = "Download";
}
