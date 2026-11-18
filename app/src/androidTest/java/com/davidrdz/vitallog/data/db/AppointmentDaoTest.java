package com.davidrdz.vitallog.data.db;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.data.entity.Appointment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AppointmentDaoTest {

    private static final long NOW = 1_000_000L;

    private AppDatabase database;
    private AppointmentDao dao;

    @Before
    public void setUp() {
        database = InMemoryDatabase.create();
        dao = database.appointmentDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void observeUpcoming_returnsOnlyFutureAndActive_soonestFirst()
            throws InterruptedException {
        dao.insert(new Appointment(0, "Cita pasada", NOW - 1, true));
        dao.insert(new Appointment(0, "Cita lejana", NOW + 5_000, true));
        dao.insert(new Appointment(0, "Cita próxima", NOW + 1_000, true));
        dao.insert(new Appointment(0, "Cita borrada", NOW + 2_000, false));

        final List<Appointment> upcoming =
                LiveDataTestUtil.getOrAwaitValue(dao.observeUpcoming(NOW));

        assertEquals(2, upcoming.size());
        assertEquals("Cita próxima", upcoming.get(0).getDoctorOrPlace());
        assertEquals("Cita lejana", upcoming.get(1).getDoctorOrPlace());
    }

    @Test
    public void deactivate_hidesAppointment() throws InterruptedException {
        final long id = dao.insert(new Appointment(0, "Doctora López", NOW + 1_000, true));

        assertEquals(1, dao.deactivate(id));

        assertEquals(0, LiveDataTestUtil.getOrAwaitValue(dao.observeUpcoming(NOW)).size());
    }
}
