package com.davidrdz.vitallog.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.data.db.DoseLogDao;
import com.davidrdz.vitallog.data.db.MedicationDao;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.Medication;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MedicationRepositoryTest {

    private FakeMedicationDao medicationDao;
    private FakeDoseLogDao doseLogDao;
    private MedicationRepository repository;

    @Before
    public void setUp() {
        medicationDao = new FakeMedicationDao();
        doseLogDao = new FakeDoseLogDao();
        repository = new MedicationRepository(medicationDao, doseLogDao,
                new RepositoryExecutors(Runnable::run, Runnable::run));
    }

    @Test
    public void save_insertsInDao_andReturnsMedicationWithId() {
        final List<SaveResult<Medication>> results = new ArrayList<>();

        repository.save(new Medication(0, "Metformina", 1, DoseUnit.TABLET, 8, 30, true),
                results::add);

        assertEquals(1, medicationDao.inserted.size());
        assertEquals(1, results.size());
        final Medication saved = results.get(0).getSaved();
        assertEquals(FakeMedicationDao.ASSIGNED_ID, saved.getId());
        assertEquals("Metformina", saved.getName());
        assertEquals(DoseUnit.TABLET, saved.getDoseUnit());
        assertEquals(8, saved.getHour());
        assertEquals(30, saved.getMinute());
        assertTrue(saved.isActive());
    }

    @Test
    public void delete_deactivatesInDao_insteadOfRemovingRow() {
        final Medication medication =
                new Medication(5, "Losartán", 1, DoseUnit.TABLET, 20, 0, true);
        final List<SaveResult<Medication>> results = new ArrayList<>();

        repository.delete(medication, results::add);

        assertEquals(List.of(5L), medicationDao.deactivatedIds);
        assertEquals(1, results.size());
        assertFalse(results.get(0).getSaved().isActive());
        assertEquals(5, results.get(0).getSaved().getId());
    }

    @Test
    public void saveDoseLog_insertsInDao_andReturnsLogWithId() {
        final List<SaveResult<DoseLog>> results = new ArrayList<>();

        repository.saveDoseLog(new DoseLog(0, 5, 1_000L, 2_000L), results::add);

        assertEquals(1, doseLogDao.inserted.size());
        assertEquals(1, results.size());
        assertEquals(FakeDoseLogDao.ASSIGNED_ID, results.get(0).getSaved().getId());
        assertEquals(5, results.get(0).getSaved().getMedicationId());
    }

    private static final class FakeMedicationDao implements MedicationDao {
        static final long ASSIGNED_ID = 3;
        final List<Medication> inserted = new ArrayList<>();
        final List<Long> deactivatedIds = new ArrayList<>();

        @Override
        public long insert(final Medication medication) {
            inserted.add(medication);
            return ASSIGNED_ID;
        }

        @Override
        public LiveData<List<Medication>> observeActive() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int deactivate(final long id) {
            deactivatedIds.add(id);
            return 1;
        }
    }

    private static final class FakeDoseLogDao implements DoseLogDao {
        static final long ASSIGNED_ID = 9;
        final List<DoseLog> inserted = new ArrayList<>();

        @Override
        public long insert(final DoseLog doseLog) {
            inserted.add(doseLog);
            return ASSIGNED_ID;
        }

        @Override
        public LiveData<List<DoseLog>> observeTakenBetween(final long startInclusive,
                                                           final long endExclusive) {
            throw new UnsupportedOperationException();
        }
    }
}
