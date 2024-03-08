package com.ravingarinc.voucher.storage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class VoucherSettings {
    public static String voucherNameFormat = ChatColor.GOLD + "{item} Voucher";
    public static String voucherNameFormatLocked = ChatColor.DARK_GRAY + "{item} Voucher";
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

    public static boolean preventItemCrafting = true;

    public static boolean preventItemDamage = true;
    public static boolean preventItemEquipping = true;
    public static boolean preventItemInteraction = true;

    public static Material border1 = Material.BLACK_STAINED_GLASS_PANE;
    public static Material border2 = Material.GRAY_STAINED_GLASS_PANE;

    public static void sendDenyMessage(final Player player) {
        if (!denyMessage.isEmpty()) {
            player.sendMessage(denyMessage);
        }
    }
}
