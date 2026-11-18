package com.davidrdz.vitallog.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.Medication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DoseLogDaoTest {

    private static final long DAY_START = 86_400_000L;
    private static final long DAY_END = 2 * DAY_START;
    private static final long HOUR = 3_600_000L;

    private AppDatabase database;
    private DoseLogDao dao;
    private long medicationId;

    @Before
    public void setUp() {
        database = InMemoryDatabase.create();
        dao = database.doseLogDao();
        medicationId = database.medicationDao().insert(
                new Medication(0, "Metformina", 1, DoseUnit.TABLET, 8, 0, true));
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void insert_withUnknownMedication_violatesForeignKey() {
        assertThrows(SQLiteConstraintException.class,
                () -> dao.insert(new DoseLog(0, medicationId + 100, DAY_START, DAY_START)));
    }

    @Test
    public void deletingMedicationWithDoses_isRestricted() {
        dao.insert(new DoseLog(0, medicationId, DAY_START, DAY_START));

        assertThrows(SQLiteConstraintException.class, () -> database.getOpenHelper()
                .getWritableDatabase()
                .execSQL("DELETE FROM medications WHERE id = " + medicationId));
    }

    @Test
    public void observeTakenBetween_returnsOnlyThatDay_inOrder() throws InterruptedException {
        dao.insert(new DoseLog(0, medicationId, DAY_START - HOUR, DAY_START - 1));
        dao.insert(new DoseLog(0, medicationId, DAY_START + 20 * HOUR, DAY_START + 20 * HOUR));
        dao.insert(new DoseLog(0, medicationId, DAY_START + 8 * HOUR, DAY_START + 8 * HOUR));
        dao.insert(new DoseLog(0, medicationId, DAY_END, DAY_END));

        final List<DoseLog> day =
                LiveDataTestUtil.getOrAwaitValue(dao.observeTakenBetween(DAY_START, DAY_END));

        assertEquals(2, day.size());
        assertEquals(DAY_START + 8 * HOUR, day.get(0).getTakenAt());
        assertEquals(DAY_START + 20 * HOUR, day.get(1).getTakenAt());
    }
}
