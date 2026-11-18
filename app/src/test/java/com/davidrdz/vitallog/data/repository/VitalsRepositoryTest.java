package com.davidrdz.vitallog.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.data.db.BloodPressureReadingDao;
import com.davidrdz.vitallog.data.db.GlucoseReadingDao;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.entity.GlucoseReading;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VitalsRepositoryTest {

    private static final long TAKEN_AT = 1_763_390_000_000L;

    private FakeGlucoseDao glucoseDao;
    private FakePressureDao pressureDao;
    private VitalsRepository repository;

    @Before
    public void setUp() {
        glucoseDao = new FakeGlucoseDao();
        pressureDao = new FakePressureDao();
        repository = new VitalsRepository(glucoseDao, pressureDao,
                new RepositoryExecutors(Runnable::run, Runnable::run));
    }

    @Test
    public void saveGlucose_insertsInDao_andReturnsSavedReadingWithId() {
        final List<SaveResult<GlucoseReading>> results = new ArrayList<>();

        repository.saveGlucose(new GlucoseReading(0, 120, TAKEN_AT), results::add);

        assertEquals(1, glucoseDao.inserted.size());
        assertEquals(120, glucoseDao.inserted.get(0).getValueMgDl());
        assertEquals(1, results.size());
        final GlucoseReading saved = results.get(0).getSaved();
        assertEquals(FakeGlucoseDao.ASSIGNED_ID, saved.getId());
        assertEquals(120, saved.getValueMgDl());
        assertEquals(TAKEN_AT, saved.getTakenAt());
    }

    @Test
    public void saveBloodPressure_insertsInDao_andReturnsSavedReadingWithId() {
        final List<SaveResult<BloodPressureReading>> results = new ArrayList<>();

        repository.saveBloodPressure(new BloodPressureReading(0, 130, 85, TAKEN_AT), results::add);

        assertEquals(1, pressureDao.inserted.size());
        assertEquals(1, results.size());
        final BloodPressureReading saved = results.get(0).getSaved();
        assertEquals(FakePressureDao.ASSIGNED_ID, saved.getId());
        assertEquals(130, saved.getSystolic());
        assertEquals(85, saved.getDiastolic());
        assertEquals(TAKEN_AT, saved.getTakenAt());
    }

    @Test
    public void save_deliversResultThroughMainExecutor() {
        final List<Runnable> mainQueue = new ArrayList<>();
        final VitalsRepository deferred = new VitalsRepository(glucoseDao, pressureDao,
                new RepositoryExecutors(Runnable::run, mainQueue::add));
        final List<SaveResult<GlucoseReading>> results = new ArrayList<>();

        deferred.saveGlucose(new GlucoseReading(0, 95, TAKEN_AT), results::add);

        assertEquals("El DAO se llama en el executor de E/S", 1, glucoseDao.inserted.size());
        assertEquals("El resultado espera al hilo principal", 0, results.size());
        mainQueue.get(0).run();
        assertEquals(1, results.size());
    }

    @Test
    public void observeLatest_delegatesToDao() {
        assertNotNull(repository.observeLatestGlucose());
        assertSame(glucoseDao.latest, repository.observeLatestGlucose());
        assertSame(pressureDao.latest, repository.observeLatestBloodPressure());
    }

    private static final class FakeGlucoseDao implements GlucoseReadingDao {
        static final long ASSIGNED_ID = 7;
        final List<GlucoseReading> inserted = new ArrayList<>();
        final LiveData<GlucoseReading> latest = new LiveData<GlucoseReading>() { };

        @Override
        public long insert(final GlucoseReading reading) {
            inserted.add(reading);
            return ASSIGNED_ID;
        }

        @Override
        public LiveData<List<GlucoseReading>> observeAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<GlucoseReading> observeLatest() {
            return latest;
        }
    }

    private static final class FakePressureDao implements BloodPressureReadingDao {
        static final long ASSIGNED_ID = 11;
        final List<BloodPressureReading> inserted = new ArrayList<>();
        final LiveData<BloodPressureReading> latest = new LiveData<BloodPressureReading>() { };

        @Override
        public long insert(final BloodPressureReading reading) {
            inserted.add(reading);
            return ASSIGNED_ID;
        }

        @Override
        public LiveData<List<BloodPressureReading>> observeAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<BloodPressureReading> observeLatest() {
            return latest;
        }
    }
}
