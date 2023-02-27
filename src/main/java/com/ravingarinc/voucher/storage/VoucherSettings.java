package com.ravingarinc.voucher.storage;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class VoucherSettings {
    public static String voucherNameFormat = ChatColor.GOLD + "Voucher | {item}";
    public static String[] voucherLoreFormat = new String[]{
            ChatColor.GRAY + "Grants the ability to craft",
            ChatColor.GRAY + "and use " + ChatColor.DARK_GRAY + "{item}"
    };
    public static String[] farmLoreFormat = new String[]{
            ChatColor.GRAY + "Grants the ability to plant",
            ChatColor.DARK_GRAY + "{seed}" + ChatColor.GRAY + " to grow " + ChatColor.DARK_GRAY + "{block}",
            ChatColor.GRAY + "and harvest &8{item}.",
            ChatColor.GRAY + "Grants the ability to craft",
            ChatColor.GRAY + "and use " + ChatColor.DARK_GRAY + "{food}"
    };

    public static String unlockedMessage = ChatColor.GREEN + "<Unlocked>";
    public static String lockedMessage = ChatColor.DARK_GRAY + "<Locked>";
    public static String denyMessage = ChatColor.RED + "You must unlock the voucher for that item to use it!";

    public static boolean preventBlockPlacement = true;
    public static boolean preventBlockMining = true;
    public static boolean blockFoodMaterialCraft = false;
    public static boolean blockFoodMaterialConsume = false;

    public static void sendDenyMessage(final Player player) {
        if (!denyMessage.isEmpty()) {
            player.sendMessage(denyMessage);
        }
    }
}
