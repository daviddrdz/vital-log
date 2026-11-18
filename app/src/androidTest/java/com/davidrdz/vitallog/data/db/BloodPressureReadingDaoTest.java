package com.davidrdz.vitallog.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.data.entity.BloodPressureReading;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class BloodPressureReadingDaoTest {

    private AppDatabase database;
    private BloodPressureReadingDao dao;

    @Before
    public void setUp() {
        database = InMemoryDatabase.create();
        dao = database.bloodPressureReadingDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void observeAll_isSortedNewestFirst() throws InterruptedException {
        dao.insert(new BloodPressureReading(0, 120, 80, 1_000L));
        dao.insert(new BloodPressureReading(0, 135, 88, 3_000L));
        dao.insert(new BloodPressureReading(0, 128, 82, 2_000L));

        final List<BloodPressureReading> all =
                LiveDataTestUtil.getOrAwaitValue(dao.observeAll());

        assertEquals(3, all.size());
        assertEquals(135, all.get(0).getSystolic());
        assertEquals(128, all.get(1).getSystolic());
        assertEquals(120, all.get(2).getSystolic());
    }

    @Test
    public void observeLatest_returnsMostRecentReading() throws InterruptedException {
        dao.insert(new BloodPressureReading(0, 120, 80, 1_000L));
        dao.insert(new BloodPressureReading(0, 135, 88, 3_000L));

        final BloodPressureReading latest =
                LiveDataTestUtil.getOrAwaitValue(dao.observeLatest());

        assertEquals(135, latest.getSystolic());
        assertEquals(88, latest.getDiastolic());
    }

    @Test
    public void observeLatest_withoutReadings_isNull() throws InterruptedException {
        assertNull(LiveDataTestUtil.getOrAwaitValue(dao.observeLatest()));
    }
}
