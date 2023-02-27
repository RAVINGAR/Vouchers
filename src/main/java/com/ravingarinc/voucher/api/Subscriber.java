package com.ravingarinc.voucher.api;

import com.ravingarinc.api.I;
import org.bukkit.event.Event;

import java.util.function.Consumer;
import java.util.logging.Level;

public class Subscriber<T extends Event> {
    private final Class<T> type;
    private final Consumer<T> consumer;

    public Subscriber(final Class<T> event, final Consumer<T> consumer) {
        this.type = event;
        this.consumer = consumer;
    }

    public void accept(final Event event) {
        try {
            consumer.accept(type.cast(event));
        } catch (final ClassCastException exception) {
            I.log(Level.SEVERE, "Failed to cast subscriber event to given type!", exception);
        }
    }
}
