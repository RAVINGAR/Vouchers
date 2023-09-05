package com.ravingarinc.voucher.player;

import com.ravingarinc.api.module.Module;
import com.ravingarinc.api.module.ModuleLoadException;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.storage.sql.VoucherDatabase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HolderManager extends Module {
    private final Map<UUID, Holder> holders;

    private VoucherDatabase database;

    public HolderManager(final RavinPlugin plugin) {
        super(HolderManager.class, plugin);
        holders = new HashMap<>();
    }

    @Override
    public void load() {
        database = plugin.getModule(VoucherDatabase.class);
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            loadHolder(player);
        }
    }

    @Override
    public void cancel() {
        holders.values().forEach(holder -> {
            holder.dispose();
            database.saveHolder(holder);
        });
        holders.clear();
    }

    @NotNull
    public Holder getHolder(final Player player) {
        final Holder holder = holders.get(player.getUniqueId());
        if (holder == null) {
            throw new IllegalStateException("Holder was not loaded yet!");
        }
        return holder;
    }

    public void loadHolder(final Player player) {
        database.loadHolder(player).ifPresent(holder -> holders.put(player.getUniqueId(), holder));
    }

    public void saveHolder(final Holder holder) {
        database.saveHolder(holder);
    }

    public Holder removeHolder(final Player player) {
        return holders.remove(player.getUniqueId());
    }
}
