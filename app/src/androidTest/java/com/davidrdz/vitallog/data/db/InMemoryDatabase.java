package com.davidrdz.vitallog.data.db;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

/** Base de datos en memoria para pruebas: se borra al cerrarla. */
final class InMemoryDatabase {

    private InMemoryDatabase() {
    }

    static AppDatabase create() {
        return Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class).build();
    }
}
