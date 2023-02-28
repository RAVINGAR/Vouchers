package com.ravingarinc.voucher.command;

import com.ravingarinc.api.command.BaseCommand;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.Voucher;
import com.ravingarinc.voucher.command.sub.ViewCommand;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class VoucherCommand extends BaseCommand {
    public VoucherCommand(final RavinPlugin plugin) {
        super("voucher", null);

        final HolderManager manager = plugin.getModule(HolderManager.class);
        final VoucherTracker tracker = plugin.getModule(VoucherTracker.class);

        addOption("reload", "vouchers.reload", "Reloads the Voucher plugin!", 1, (sender, args) -> {
            plugin.reload();
            sender.sendMessage(ChatColor.GRAY + "The plugin has been reloaded!");
            return true;
        });

        addOption("admin", "vouchers.admin", "- Admin command for Vouchers ", 2, (p, s) -> false)
                .addOption("item", null, ChatColor.GREEN + "<voucher> <player>" + ChatColor.GRAY + "- Give a voucher item to a player!", 3, (sender, args) -> {
                    final Voucher voucher = tracker.getVoucher(args[2]);
                    if (voucher == null) {
                        sender.sendMessage(ChatColor.RED + "Could not find voucher with key " + args[2]);
                        return true;
                    }
                    Player player = null;
                    if (args.length > 3) {
                        player = plugin.getServer().getPlayer(args[3]);
                    } else if (sender instanceof Player p) {
                        player = p;
                    }
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Could not find a valid player!");
                        return true;
                    }
                    final Player finalPlayer = player;
                    player.getInventory().addItem(voucher.getItem()).values().forEach(i -> finalPlayer.getWorld().dropItemNaturally(finalPlayer.getLocation(), i));
                    sender.sendMessage(ChatColor.GREEN + "You have given the voucher '" + args[2] + "' to " + player.getName());
                    return true;
                }).buildTabCompletions((sender, args) -> {
                    if (args.length == 4) {
                        return null;
                    } else if (args.length == 3) {
                        return tracker.getVoucherKeys();
                    }
                    return new ArrayList<>();
                }).getParent()
                .addOption("unlock", null, ChatColor.GREEN + "<voucher> <player> " + ChatColor.GRAY + "- Force unlock a specific voucher for a player!", 3,
                        (sender, args) -> {
                            final Voucher voucher = tracker.getVoucher(args[2]);
                            if (voucher == null) {
                                sender.sendMessage(ChatColor.RED + "Could not find voucher with key " + args[2]);
                                return true;
                            }
                            Player player = null;
                            if (args.length > 3) {
                                player = plugin.getServer().getPlayer(args[3]);
                            } else if (sender instanceof Player p) {
                                player = p;
                            }
                            if (player == null) {
                                sender.sendMessage(ChatColor.RED + "Could not find a valid player!");
                                return true;
                            }

                            manager.getHolder(player).unlock(voucher);
                            sender.sendMessage(ChatColor.GREEN + "You have unlocked the voucher '" + args[2] + "' for " + player.getName());
                            return true;
                        }).buildTabCompletions((sender, args) -> {
                    if (args.length == 4) {
                        return null;
                    } else if (args.length == 3) {
                        return tracker.getVoucherKeys();
                    }
                    return new ArrayList<>();
                }).getParent()
                .addOption("lock", null, ChatColor.GREEN + "<voucher> <player> " + ChatColor.GRAY + "- Force lock a specific voucher for a player!", 3,
                        (sender, args) -> {
                            final Voucher voucher = tracker.getVoucher(args[2]);
                            if (voucher == null) {
                                sender.sendMessage(ChatColor.RED + "Could not find voucher with key " + args[2]);
                                return true;
                            }
                            Player player = null;
                            if (args.length > 3) {
                                player = plugin.getServer().getPlayer(args[3]);
                            } else if (sender instanceof Player p) {
                                player = p;
                            }
                            if (player == null) {
                                sender.sendMessage(ChatColor.RED + "Could not find a valid player!");
                                return true;
                            }

                            manager.getHolder(player).lock(args[2]);
                            sender.sendMessage(ChatColor.GREEN + "You have locked the voucher '" + args[2] + "' for " + player.getName());
                            return true;
                        }).buildTabCompletions((sender, args) -> {
                    if (args.length == 4) {
                        return null;
                    } else if (args.length == 3) {
                        return tracker.getVoucherKeys();
                    }
                    return new ArrayList<>();
                }).getParent().addHelpOption(ChatColor.DARK_GREEN, ChatColor.GREEN);

        new ViewCommand(plugin, this).register();

        addHelpOption(ChatColor.DARK_GREEN, ChatColor.GREEN);
    }
}
