package com.davidrdz.vitallog.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.data.entity.GlucoseReading;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GlucoseReadingDaoTest {

    private AppDatabase database;
    private GlucoseReadingDao dao;

    @Before
    public void setUp() {
        database = InMemoryDatabase.create();
        dao = database.glucoseReadingDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void observeAll_isSortedNewestFirst() throws InterruptedException {
        dao.insert(new GlucoseReading(0, 100, 2_000L));
        dao.insert(new GlucoseReading(0, 140, 3_000L));
        dao.insert(new GlucoseReading(0, 90, 1_000L));

        final List<GlucoseReading> all = LiveDataTestUtil.getOrAwaitValue(dao.observeAll());

        assertEquals(3, all.size());
        assertEquals(140, all.get(0).getValueMgDl());
        assertEquals(100, all.get(1).getValueMgDl());
        assertEquals(90, all.get(2).getValueMgDl());
    }

    @Test
    public void observeLatest_returnsMostRecentReading() throws InterruptedException {
        dao.insert(new GlucoseReading(0, 100, 2_000L));
        dao.insert(new GlucoseReading(0, 90, 1_000L));

        final GlucoseReading latest = LiveDataTestUtil.getOrAwaitValue(dao.observeLatest());

        assertEquals(100, latest.getValueMgDl());
        assertEquals(2_000L, latest.getTakenAt());
    }

    @Test
    public void observeLatest_withoutReadings_isNull() throws InterruptedException {
        assertNull(LiveDataTestUtil.getOrAwaitValue(dao.observeLatest()));
    }
}
