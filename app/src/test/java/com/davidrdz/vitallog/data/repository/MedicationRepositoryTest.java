package com.davidrdz.vitallog.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
    private FakeReminderScheduler scheduler;
    private MedicationRepository repository;

    @Before
    public void setUp() {
        medicationDao = new FakeMedicationDao();
        doseLogDao = new FakeDoseLogDao();
        scheduler = new FakeReminderScheduler();
        repository = new MedicationRepository(medicationDao, doseLogDao,
                new RepositoryExecutors(Runnable::run, Runnable::run), scheduler);
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
    public void save_schedulesReminderForSavedMedication() {
        repository.save(new Medication(0, "Metformina", 1, DoseUnit.TABLET, 8, 30, true),
                result -> { });

        assertEquals(List.of(FakeMedicationDao.ASSIGNED_ID), scheduler.scheduledMedicationIds);
    }

    @Test
    public void delete_cancelsReminder() {
        repository.delete(new Medication(5, "Losartán", 1, DoseUnit.TABLET, 20, 0, true),
                result -> { });

        assertEquals(List.of("MEDICATION:5"), scheduler.cancelled);
    }

    @Test
    public void rescheduleReminders_schedulesEveryActiveMedication_thenRunsDone() {
        medicationDao.active.add(new Medication(1, "Metformina", 1, DoseUnit.TABLET, 8, 0, true));
        medicationDao.active.add(new Medication(2, "Losartán", 1, DoseUnit.TABLET, 20, 0, true));
        final List<String> done = new ArrayList<>();

        repository.rescheduleReminders(() -> done.add("done"));

        assertEquals(List.of(1L, 2L), scheduler.scheduledMedicationIds);
        assertEquals(List.of("done"), done);
    }

    @Test
    public void findActive_deliversDaoResult() {
        medicationDao.active.add(new Medication(4, "Metformina", 1, DoseUnit.TABLET, 8, 0, true));
        final List<Medication> found = new ArrayList<>();

        repository.findActive(4, found::add);
        repository.findActive(99, found::add);

        assertEquals(2, found.size());
        assertEquals(4, found.get(0).getId());
        assertNull(found.get(1));
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
        final List<Medication> active = new ArrayList<>();

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

        @Override
        public List<Medication> getActive() {
            return active;
        }

        @Override
        public Medication findActiveById(final long id) {
            for (final Medication medication : active) {
                if (medication.getId() == id) {
                    return medication;
                }
            }
            return null;
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
