package co.onmind.hex.transverse.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;

import java.util.function.Supplier;

public final class BulkheadGeneric {

    private BulkheadGeneric() {
    }

    public static <T> T withBulkhead(Supplier<T> action, Bulkhead bulkhead) {
        return bulkhead.executeSupplier(action);
    }

    public static void withBulkhead(Runnable action, Bulkhead bulkhead) {
        bulkhead.executeRunnable(action);
    }
}
