package com.mobiledevelopment.www.artistlist.save;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Создаем фрагмет который не унижтожается при перевороте экрана.
 */
public class SaveFragment extends Fragment {

    private Container container;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setRetainInstance(true); // защита от уничтожения
    }

    /**
     * Отдаем сохраненый контейнер.
     *
     * @return контейнер с данными.
     */
    public Container getModel() {
        return container;
    }

    /**
     * Получаем контейнер на хранение.
     *
     * @param container контейнер с данными.
     */
    public void setModel(Container container) {
        this.container = container;
    }
}
