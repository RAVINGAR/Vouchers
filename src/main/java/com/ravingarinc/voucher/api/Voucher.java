package com.ravingarinc.voucher.api;

import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

public abstract class Voucher {
    protected final String key;

    protected final HolderManager manager;

    protected Map<Class<?>, Subscriber<?>> subscribers;

    protected String lore = null;

    public Voucher(final String key, final HolderManager manager) {
        this.key = key;
        this.subscribers = new HashMap<>();
        this.manager = manager;
    }

    public <T extends Event> void subscribe(final Class<T> event, final Consumer<T> consumer) {
        subscribers.put(event, new Subscriber<>(event, consumer));
    }

    public void handle(final Event event) {
        Optional.ofNullable(subscribers.get(event.getClass())).ifPresent((subscriber) -> subscriber.accept(event));
    }

    public abstract Material getIcon();

    public String getKey() {
        return key;
    }

    public boolean isUnlocked(final Player player) {
        return manager.getHolder(player).isUnlocked(key);
    }

    public String getDisplayName() {
        return VoucherSettings.voucherNameFormat.replace("{item}", fullyCapitalise(key));
    }

    public String getLockedDisplayName() {
        return VoucherSettings.voucherNameFormatLocked.replace("{item}", fullyCapitalise(key));
    }

    public abstract String getItemDisplayName();

    public abstract String getLore();

    public ItemStack getVoucherItem(RavinPlugin plugin) {
        final ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            meta = plugin.getServer().getItemFactory().getItemMeta(getIcon());
        }
        meta.setDisplayName(getDisplayName());
        meta.setLore(Arrays.stream(getLore().split("\n")).toList());
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "voucher_id"), PersistentDataType.STRING, key);
        stack.setItemMeta(meta);
        return stack;
    }

    protected String fullyCapitalise(final String word) {
        final StringBuilder builder = new StringBuilder();
        final String[] split = word.toLowerCase().split("[_ -]");
        int i = 0;
        for (final String s : split) {
            builder.append(s.toUpperCase().charAt(0));
            builder.append(s.substring(1));
            if (++i < split.length) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}
