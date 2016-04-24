package com.mobiledevelopment.www.artistlist.file;

import android.content.Context;
import android.util.Pair;

import java.io.File;
import java.io.IOException;

/**
 * Создание файла.
 */
public class CreateFile {
    /**
     * Создаем файл в Cache (или проверяем что он существует).
     *
     * @param context контекст приложения.
     * @param prefix  имя создаваемого файла.
     * @return пару (Созданый (или проверенный) файл, Флаг - существовал файл по данному пути или нет).
     * @throws IOException в случае если файл (или директории на пути к файлу) нельзя создать, или
     * обьект по данному пути не является файлом.
     */
    public static Pair<File, Boolean> createCacheFile(Context context, String prefix) throws IOException {
        // Получаем путь к cache директории
        File dir = new File(context.getCacheDir(), "Cache");
        // Проверяем что по данному пути существует именно директория, иначе кидаем ошибку.
        if (dir.exists() && !dir.isDirectory())
            throw new IOException("Not a directory: " + dir);
        // Пытаемся создать директорию, если она ранее не существовала.
        // Если не получилось создать - кидаем ошибку.
        if (!dir.exists() && !dir.mkdirs())
            throw new IOException("Failed to create directory: " + dir);
        // получаем путь к нашему файлу
        File result = new File(dir, prefix + ".tmp");
        // Если он существует - возвращаем его
        if (result.exists())
            return new Pair<>(result, true);
        // Если не существует пытаемся создать, и в случае успеха возвращаем его.
        else if (result.createNewFile())
            return new Pair<>(result, false);
        // Кидаем ошибку если не получилось создать файл.
        else
            throw new IOException("Failed to create file: " + result);
    }
}
