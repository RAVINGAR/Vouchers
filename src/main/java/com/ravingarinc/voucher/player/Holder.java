package com.ravingarinc.voucher.player;

import com.ravingarinc.voucher.api.Voucher;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Holder {
    private final Player player;
    private final Map<String, Voucher> vouchers;

    public Holder(final Player player) {
        this.player = player;
        this.vouchers = new HashMap<>();
    }

    public void unlock(final Voucher voucher) {
        vouchers.put(voucher.getKey(), voucher);
    }

    public boolean isUnlocked(final String key) {
        return vouchers.containsKey(key);
    }

    public void lock(final String key) {
        vouchers.remove(key);
    }

    public Set<String> getVoucherKeys() {
        return vouchers.keySet();
    }

    public Player getPlayer() {
        return player;
    }
}
