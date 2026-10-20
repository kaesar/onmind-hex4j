package co.onmind.hex.infrastructure.scripts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScriptExecutionExecutorTest {

    @Test
    @DisplayName("runs the task on a platform thread, never on a virtual thread")
    void runsOnPlatformThread() {
        ScriptExecutionExecutor executor = new ScriptExecutionExecutor(2, 30);

        Boolean virtual = executor.execute(() -> Thread.currentThread().isVirtual());

        assertNotNull(virtual);
        assertFalse(virtual, "CPU-bound script work must not occupy a carrier thread");
        executor.shutdown();
    }

    @Test
    @DisplayName("propagates the MDC of the caller to the worker thread")
    void propagatesMdc() {
        ScriptExecutionExecutor executor = new ScriptExecutionExecutor(2, 30);
        MDC.put("requestId", "abc-123");
        try {
            String requestId = executor.execute(() -> MDC.get("requestId"));

            assertEquals("abc-123", requestId);
        } finally {
            MDC.clear();
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("does not leak the MDC of one task into the next one")
    void doesNotLeakMdcBetweenTasks() {
        ScriptExecutionExecutor executor = new ScriptExecutionExecutor(1, 30);
        MDC.put("requestId", "abc-123");
        try {
            executor.execute(() -> null);
            MDC.clear();

            String requestId = executor.execute(() -> MDC.get("requestId"));

            assertNull(requestId);
        } finally {
            MDC.clear();
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("fails fast when the worker pool is saturated instead of queueing forever")
    void rejectsWhenSaturated() throws Exception {
        // One worker, one queue slot: the second submission fills the queue, the third is rejected.
        ScriptExecutionExecutor executor = new ScriptExecutionExecutor(1, 30, 1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            Thread blocker = new Thread(() -> executor.execute(() -> {
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }));
            blocker.setDaemon(true);
            blocker.start();

            // Wait until the single worker is busy.
            long deadline = System.currentTimeMillis() + 5_000;
            while (executor.activeCount() == 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(1, executor.activeCount(), "the worker should be busy");

            // Fill the single queue slot from another thread (it blocks until the worker frees).
            Thread queued = new Thread(() -> executor.execute(() -> null));
            queued.setDaemon(true);
            queued.start();
            while (executor.queueSize() == 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(1, executor.queueSize(), "the queue should be full");

            ScriptExecutionException ex = assertThrows(ScriptExecutionException.class,
                    () -> executor.execute(() -> "never"));
            assertTrue(ex.getMessage().contains("busy"), ex.getMessage());
        } finally {
            release.countDown();
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("times out a runaway script and releases the caller")
    void timesOutRunawayScript() {
        ScriptExecutionExecutor executor = new ScriptExecutionExecutor(1, 1);
        try {
            ScriptExecutionException ex = assertThrows(ScriptExecutionException.class,
                    () -> executor.execute(() -> {
                        try {
                            Thread.sleep(30_000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "never";
                    }));
            assertTrue(ex.getMessage().contains("timeout"), ex.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
