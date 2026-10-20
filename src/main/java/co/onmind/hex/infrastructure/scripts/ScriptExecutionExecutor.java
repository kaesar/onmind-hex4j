package co.onmind.hex.infrastructure.scripts;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs JavaScript scripts on a bounded pool of <em>platform</em> threads.
 * <p>
 * Script execution is CPU-bound. Running it on the request thread was fine while
 * Tomcat capped concurrency, but on a virtual thread it is actively harmful: a
 * CPU-bound task occupies a <em>carrier</em> thread (the JVM's shared pool of
 * platform threads) for its entire duration, so a handful of concurrent scripts can
 * starve every other virtual thread in the process. The request thread would then
 * sit unmounted and the whole service would appear to hang.
 * <p>
 * Isolating the work on a dedicated, bounded pool of platform threads keeps the
 * carrier pool free and puts a hard ceiling on how much CPU the scripting feature
 * can consume. The waiting request thread is a virtual thread, so it unmounts while
 * the script runs and costs nothing.
 * <p>
 * The MDC of the calling thread is copied into the worker so that the request id
 * remains visible in the script logs — virtual threads do not inherit
 * {@link ThreadLocal} state.
 */
@Component
public class ScriptExecutionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutionExecutor.class);

    private final ThreadPoolExecutor executor;
    private final long timeoutSeconds;

    @org.springframework.beans.factory.annotation.Autowired
    public ScriptExecutionExecutor(
            @Value("${app.scripts.pool-size:0}") int poolSize,
            @Value("${app.scripts.timeout-seconds:30}") long timeoutSeconds) {
        this(poolSize, timeoutSeconds, 0);
    }

    ScriptExecutionExecutor(int poolSize, long timeoutSeconds, int queueCapacity) {
        int size = poolSize > 0 ? poolSize : Math.max(2, Runtime.getRuntime().availableProcessors());
        int queue = queueCapacity > 0 ? queueCapacity : size * 4;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 30;
        this.executor = new ThreadPoolExecutor(
                size,
                size,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queue),
                Thread.ofPlatform().name("script-", 0).factory(),
                new ThreadPoolExecutor.AbortPolicy());
        logger.info("Script execution executor initialized: poolSize={}, queueCapacity={}, timeout={}s",
                size, queue, this.timeoutSeconds);
    }

    /**
     * Executes the given task on a script worker and waits for its result.
     *
     * @param task the script to run
     * @param <T>  the result type
     * @return the value produced by the task
     * @throws ScriptExecutionException if the task fails, times out or cannot be scheduled
     */
    public <T> T execute(Callable<T> task) {
        Map<String, String> callerContext = MDC.getCopyOfContextMap();
        Future<T> future;
        try {
            future = executor.submit(() -> {
                if (callerContext != null) {
                    MDC.setContextMap(callerContext);
                }
                try {
                    return task.call();
                } finally {
                    MDC.clear();
                }
            });
        } catch (RejectedExecutionException e) {
            throw new ScriptExecutionException(
                    "Script execution rejected: all script workers are busy", e);
        }

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ScriptExecutionException(
                    "Script execution exceeded the " + timeoutSeconds + "s timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            throw new ScriptExecutionException("Script execution interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new ScriptExecutionException(cause.getMessage(), cause);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Number of tasks currently running on a script worker.
     *
     * @return the active task count
     */
    int activeCount() {
        return executor.getActiveCount();
    }

    /**
     * Number of tasks waiting for a free script worker.
     *
     * @return the queued task count
     */
    int queueSize() {
        return executor.getQueue().size();
    }
}
