package com.davidrdz.vitallog.data.db;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.platform.app.InstrumentationRegistry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Lee el primer valor de un {@link LiveData} de Room observándolo en el hilo principal. */
final class LiveDataTestUtil {

    private static final long TIMEOUT_SECONDS = 2;

    private LiveDataTestUtil() {
    }

    static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] holder = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        final Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(final T value) {
                holder[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(() -> liveData.observeForever(observer));
        if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            InstrumentationRegistry.getInstrumentation()
                    .runOnMainSync(() -> liveData.removeObserver(observer));
            throw new AssertionError("LiveData never emitted a value");
        }
        @SuppressWarnings("unchecked") final T value = (T) holder[0];
        return value;
    }
}
