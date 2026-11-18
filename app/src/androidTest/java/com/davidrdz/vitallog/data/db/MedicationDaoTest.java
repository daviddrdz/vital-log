package com.davidrdz.vitallog.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.Medication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MedicationDaoTest {

    private AppDatabase database;
    private MedicationDao dao;

    @Before
    public void setUp() {
        database = InMemoryDatabase.create();
        dao = database.medicationDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void insert_keepsAllFields() throws InterruptedException {
        final long id = dao.insert(
                new Medication(0, "Insulina", 12, DoseUnit.UNIT, 21, 15, true));

        final Medication stored = LiveDataTestUtil.getOrAwaitValue(dao.observeActive()).get(0);

        assertEquals(id, stored.getId());
        assertEquals("Insulina", stored.getName());
        assertEquals(12, stored.getDoseAmount());
        assertEquals(DoseUnit.UNIT, stored.getDoseUnit());
        assertEquals(21, stored.getHour());
        assertEquals(15, stored.getMinute());
        assertTrue(stored.isActive());
    }

    @Test
    public void observeActive_isSortedByTimeOfDay() throws InterruptedException {
        dao.insert(new Medication(0, "Losartán", 1, DoseUnit.TABLET, 20, 0, true));
        dao.insert(new Medication(0, "Metformina", 1, DoseUnit.TABLET, 8, 30, true));
        dao.insert(new Medication(0, "Aspirina", 1, DoseUnit.TABLET, 8, 0, true));

        final List<Medication> active = LiveDataTestUtil.getOrAwaitValue(dao.observeActive());

        assertEquals("Aspirina", active.get(0).getName());
        assertEquals("Metformina", active.get(1).getName());
        assertEquals("Losartán", active.get(2).getName());
    }

    @Test
    public void deactivate_hidesMedicationFromActiveList() throws InterruptedException {
        final long kept = dao.insert(
                new Medication(0, "Metformina", 1, DoseUnit.TABLET, 8, 30, true));
        final long removed = dao.insert(
                new Medication(0, "Losartán", 1, DoseUnit.TABLET, 20, 0, true));

        assertEquals(1, dao.deactivate(removed));

        final List<Medication> active = LiveDataTestUtil.getOrAwaitValue(dao.observeActive());
        assertEquals(1, active.size());
        assertEquals(kept, active.get(0).getId());
    }
}
