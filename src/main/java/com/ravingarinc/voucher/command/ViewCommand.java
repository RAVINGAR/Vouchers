package com.ravingarinc.voucher.command;

import com.ravingarinc.api.command.CommandOption;
import com.ravingarinc.api.gui.BaseGui;
import com.ravingarinc.api.gui.builder.GuiBuilder;
import com.ravingarinc.api.gui.builder.GuiProvider;
import com.ravingarinc.api.gui.builder.MenuBuilder;
import com.ravingarinc.api.gui.builder.PageBuilder;
import com.ravingarinc.api.gui.component.Page;
import com.ravingarinc.api.gui.component.action.NextPageAction;
import com.ravingarinc.api.gui.component.action.PreviousPageAction;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.Voucher;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ViewCommand extends CommandOption {

    private final HolderManager manager;

    public ViewCommand(final RavinPlugin plugin, final CommandOption parent) {
        super("view", parent, "vouchers.view", "- View your locked and unlocked vouchers", 1, (sender, args) -> false);
        manager = plugin.getModule(HolderManager.class);
        setFunction((sender, args) -> {
            if (sender instanceof Player player) {
                player.openInventory(manager.getHolder(player).getGui(plugin).getInventory());
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
            }
            return true;
        });
    }
}
