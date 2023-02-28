package com.ravingarinc.voucher.listener;

import com.ravingarinc.api.module.ModuleListener;
import com.ravingarinc.api.module.ModuleLoadException;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.Voucher;
import com.ravingarinc.voucher.player.Holder;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener extends ModuleListener {
    private static final long CHECK_INTERVAL = 6000L;

    private VoucherTracker tracker;

    private HolderManager manager;

    public PlayerListener(final RavinPlugin plugin) {
        super(PlayerListener.class, plugin, VoucherTracker.class);
    }

    @Override
    public void load() throws ModuleLoadException {
        tracker = plugin.getModule(VoucherTracker.class);
        manager = plugin.getModule(HolderManager.class);
        super.load();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        manager.loadHolder(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        manager.saveHolder(manager.removeHolder(event.getPlayer()));
    }

    @EventHandler
    public void onEntityDamageEvent(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            tracker.handleEvent(event);
        }
    }

    @EventHandler
    public void onBreakBlockEvent(final BlockBreakEvent event) {
        tracker.handleEvent(event);
    }

    @EventHandler
    public void onPrepareItemCraftEvent(final PrepareItemCraftEvent event) {
        tracker.handleEvent(event);
    }

    @EventHandler
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        if (handleVoucherClick(event)) {
            tracker.handleEvent(event);
        }
    }

    public boolean handleVoucherClick(final PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                return true;
            }
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return true;
            }
            final String key = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "voucher_id"), PersistentDataType.STRING);
            if (key == null) {
                return true;
            }
            final Player player = event.getPlayer();
            final Holder holder = manager.getHolder(player);
            if (holder.isUnlocked(key)) {
                player.sendMessage(ChatColor.RED + "You have already unlocked this voucher!");
                return false;
            }
            final Voucher voucher = tracker.getVoucher(key);
            if (voucher == null) {
                player.sendMessage(ChatColor.RED + "This voucher is no longer valid!");
                return false;
            }

            holder.unlock(voucher);
            player.sendMessage(ChatColor.GREEN + "You unlocked the " + voucher.getDisplayName());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.7F, 1.0F);
            player.getInventory().setItemInMainHand(null);
            return false;
        }
        return true;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        tracker.handleEvent(event);
    }

    @EventHandler()
    public void onInventoryDrag(final InventoryDragEvent event) {
        tracker.handleEvent(event);
    }
}
