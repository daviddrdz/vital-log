package com.davidrdz.vitallog.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.data.db.AppointmentDao;
import com.davidrdz.vitallog.data.entity.Appointment;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRepositoryTest {

    private static final long NOW = 1_000_000L;

    private FakeAppointmentDao appointmentDao;
    private FakeReminderScheduler scheduler;
    private AppointmentRepository repository;

    @Before
    public void setUp() {
        appointmentDao = new FakeAppointmentDao();
        scheduler = new FakeReminderScheduler();
        repository = new AppointmentRepository(appointmentDao,
                new RepositoryExecutors(Runnable::run, Runnable::run), scheduler);
    }

    @Test
    public void save_insertsAndSchedulesReminderForSavedAppointment() {
        final List<SaveResult<Appointment>> results = new ArrayList<>();

        repository.save(new Appointment(0, "Dra. López", NOW, true), results::add);

        assertEquals(FakeAppointmentDao.ASSIGNED_ID, results.get(0).getSaved().getId());
        assertEquals(List.of(FakeAppointmentDao.ASSIGNED_ID), scheduler.scheduledAppointmentIds);
    }

    @Test
    public void delete_deactivatesAndCancelsReminder() {
        final List<SaveResult<Appointment>> results = new ArrayList<>();

        repository.delete(new Appointment(6, "Clínica", NOW, true), results::add);

        assertEquals(List.of(6L), appointmentDao.deactivatedIds);
        assertEquals(List.of("APPOINTMENT:6"), scheduler.cancelled);
        assertFalse(results.get(0).getSaved().isActive());
    }

    @Test
    public void rescheduleReminders_schedulesUpcoming_thenRunsDone() {
        appointmentDao.upcoming.add(new Appointment(1, "Dra. López", NOW + 1, true));
        appointmentDao.upcoming.add(new Appointment(2, "Clínica", NOW + 2, true));
        final List<String> done = new ArrayList<>();

        repository.rescheduleReminders(NOW, () -> done.add("done"));

        assertEquals(NOW, appointmentDao.lastUpcomingQuery);
        assertEquals(List.of(1L, 2L), scheduler.scheduledAppointmentIds);
        assertEquals(List.of("done"), done);
    }

    private static final class FakeAppointmentDao implements AppointmentDao {
        static final long ASSIGNED_ID = 8;
        final List<Long> deactivatedIds = new ArrayList<>();
        final List<Appointment> upcoming = new ArrayList<>();
        long lastUpcomingQuery = -1;

        @Override
        public long insert(final Appointment appointment) {
            return ASSIGNED_ID;
        }

        @Override
        public LiveData<List<Appointment>> observeUpcoming(final long now) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Appointment> getUpcoming(final long now) {
            lastUpcomingQuery = now;
            return upcoming;
        }

        @Override
        public Appointment findActiveById(final long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int deactivate(final long id) {
            deactivatedIds.add(id);
            return 1;
        }
    }
}
