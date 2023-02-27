package com.ravingarinc.voucher.command.sub;

import com.ravingarinc.api.command.CommandOption;
import com.ravingarinc.api.gui.BaseGui;
import com.ravingarinc.api.gui.builder.GuiBuilder;
import com.ravingarinc.api.gui.builder.GuiProvider;
import com.ravingarinc.api.gui.builder.PageBuilder;
import com.ravingarinc.api.gui.component.action.NextPageAction;
import com.ravingarinc.api.gui.component.action.PreviousPageAction;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewCommand extends CommandOption {
    private final VoucherTracker tracker;
    private final GuiProvider provider;

    private final HolderManager manager;

    private final Map<UUID, GuiBuilder<BaseGui>> builders;

    public ViewCommand(final RavinPlugin plugin, final CommandOption parent) {
        super("view", parent, "vouchers.view", "- View your locked and unlocked vouchers", 1, (sender, args) -> false);
        tracker = plugin.getModule(VoucherTracker.class);
        manager = plugin.getModule(HolderManager.class);
        provider = GuiProvider.getInstance(plugin);
        builders = new HashMap<>();
        setFunction((sender, args) -> {
            if (sender instanceof Player player) {
                final GuiBuilder<BaseGui> builder = builders.computeIfAbsent(player.getUniqueId(), (u) -> getBuilder(plugin));
                provider.openCustomGui(builder, player);
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
            }
            return true;
        });
    }

    public GuiBuilder<BaseGui> getBuilder(final RavinPlugin plugin) {
        final GuiBuilder<BaseGui> builder = new GuiBuilder<>(plugin, "Vouchers", BaseGui.class, () -> Bukkit.createInventory(null, 45, "Vouchers"));
        builder.setPrimaryBorder(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        builder.setSecondaryBorder(Material.PURPLE_STAINED_GLASS_PANE);
        builder.setBackIconIndex(40);
        /*
        0  1  2  3  4  5  6  7  8
        9  10 11 12 13 14 15 16 17
        18 19 20 21 22 23 24 25 26
        27 28 29 30 31 32 33 34 35
        36 37 38 39 40 41 42 43 44
         */

        final PageBuilder pageBuilder = builder.createMenu("MAIN", null)
                .addStaticIcon("NEXT_PAGE", ChatColor.YELLOW + "Next Page", ChatColor.GRAY + "Click for the next page!", Material.ARROW, 26)
                .getActionBuilder().addMiscAction(new NextPageAction("VOUCHER_PAGE", "MAIN"))
                .finalise().finalise()
                .addStaticIcon("PREVIOUS_PAGE", ChatColor.YELLOW + "Previous Page", ChatColor.GRAY + "Click for the previous page!", Material.ARROW, 18)
                .getActionBuilder().addMiscAction(new PreviousPageAction("VOUCHER_PAGE", "MAIN"))
                .finalise().finalise()
                .addPage("VOUCHER_PAGE", 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

        tracker.getVouchers().forEach(voucher -> {
            pageBuilder.addPageIcon(voucher.getKey().toUpperCase(), voucher.getDisplayName(), voucher.getLore() + "\n\n" + VoucherSettings.unlockedMessage,
                    voucher.getIcon(), (gui) -> manager.getHolder(gui.getPlayer()).isUnlocked(voucher.getKey()));

            pageBuilder.addPageIcon(voucher.getKey().toUpperCase() + "_LOCKED", voucher.getDisplayName(), voucher.getLore() + "\n\n" + VoucherSettings.lockedMessage,
                    Material.IRON_BARS, (gui) -> !manager.getHolder(gui.getPlayer()).isUnlocked(voucher.getKey()));
        });

        return builder;
    }
}
