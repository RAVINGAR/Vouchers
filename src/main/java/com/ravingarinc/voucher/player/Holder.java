package com.ravingarinc.voucher.player;

import com.ravingarinc.api.gui.BaseGui;
import com.ravingarinc.api.gui.builder.GuiBuilder;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.Voucher;
import com.ravingarinc.voucher.storage.VoucherSettings;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Holder {
    private final Player player;
    private final Map<String, Voucher> vouchers;

    private BaseGui gui = null;

    public Holder(final Player player) {
        this.player = player;
        this.vouchers = new HashMap<>();
    }

    public BaseGui getGui(RavinPlugin plugin) {
        if(gui == null) {
            gui = buildGui(plugin).build();
        }
        return gui;
    }

    public void dispose() {
        if(gui != null) {
            gui.destroy();
            gui = null;
        }
    }

    private GuiBuilder<BaseGui> buildGui(RavinPlugin plugin) {
        final VoucherTracker tracker = plugin.getModule(VoucherTracker.class);
        final GuiBuilder<BaseGui> builder = new GuiBuilder<>(plugin, "Vouchers", BaseGui.class, 45);
        builder.setPrimaryBorder(VoucherSettings.border1);
        builder.setSecondaryBorder(VoucherSettings.border2);
        builder.setBackIconIndex(40);
        builder.createMenu("MAIN", null)
                .addStaticIcon("TITLE", ChatColor.GRAY + "Vouchers", ChatColor.DARK_GRAY + "This is where you can view all vouchers!", Material.FILLED_MAP, 4).finalise()
                .addPage("VOUCHER_PAGE", 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
                .addNextPageIcon(26).finalise()
                .addPreviousPageIcon(18).finalise()
                .addPageFiller("VOUCHER_FILLER_UNLOCKED", tracker::getVouchers)
                .setDisplayNameProvider(Voucher::getDisplayName)
                .setLoreProvider(v -> v.getLore() + "\n\n" + VoucherSettings.unlockedMessage)
                .setIdentifierProvider(Voucher::getKey)
                .setMaterialProvider(Voucher::getIcon)
                .setPredicateProvider((voucher) -> (gui, player) -> isUnlocked(voucher.getKey())).finalise()
                .addPageFiller("VOUCHER_FILLER_LOCKED", tracker::getVouchers)
                .setDisplayNameProvider(Voucher::getLockedDisplayName)
                .setLoreProvider(v -> v.getLore() + "\n\n" + VoucherSettings.lockedMessage)
                .setIdentifierProvider(Voucher::getKey)
                .setMaterialProvider(v -> Material.PAPER)
                .setPredicateProvider((voucher) -> (gui, player) -> !isUnlocked(voucher.getKey())).finalise();
        return builder;
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
