package com.ravingarinc.voucher.api;

import javax.annotation.concurrent.ThreadSafe;
import java.util.function.Supplier;

@ThreadSafe
public class Lazy<T> {
    private final Supplier<T> supplier;
    private volatile T instance = null;

    public Lazy(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        final T val = instance;
        if (val != null) {
            return val;
        }
        synchronized (supplier) {
            final T val2 = instance;
            if (val2 == null) {
                final T val3 = supplier.get();
                instance = val3;
                return val3;
            } else {
                return val2;
            }
        }
    }
}

