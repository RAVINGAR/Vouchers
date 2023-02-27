package com.ravingarinc.voucher.tracker;

import com.ravingarinc.api.I;
import com.ravingarinc.api.module.Module;
import com.ravingarinc.api.module.ModuleLoadException;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.Subscriber;
import com.ravingarinc.voucher.api.Voucher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class VoucherTracker extends Module {
    private final Map<String, Voucher> vouchers;

    private Map<Class<?>, List<Subscriber<?>>> subscribers;

    public VoucherTracker(final RavinPlugin plugin) {
        super(VoucherTracker.class, plugin);
        vouchers = new LinkedHashMap<>();
    }

    @Override
    protected void load() throws ModuleLoadException {

    }

    public void addVoucher(final Voucher voucher) {
        if (vouchers.containsKey(voucher.getKey())) {
            I.log(Level.WARNING, "Encountered duplicate vouchers with the key '" + voucher.getKey() + "'! Please check your config!");
            return;
        }
        this.vouchers.put(voucher.getKey(), voucher);
    }

    @Nullable
    public Voucher getVoucher(final String key) {
        return vouchers.get(key);
    }

    public <T extends Event> void handleEvent(final T event) {
        vouchers.values().forEach((voucher) -> voucher.handle(event));
    }

    public Collection<Voucher> getVouchers() {
        return Collections.unmodifiableCollection(vouchers.values());
    }

    public List<String> getVoucherKeys() {
        return vouchers.keySet().stream().toList();
    }

    @Override
    public void cancel() {
        vouchers.clear();
    }
}
