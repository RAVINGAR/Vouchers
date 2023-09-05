package com.ravingarinc.voucher.api;

import com.ravingarinc.api.I;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.logging.Level;

public class ItemVoucher extends Voucher {
    protected Material material;

    public ItemVoucher(final Material material, final HolderManager manager) {
        super(material.name().toLowerCase(), manager);
        this.material = material;

        subscribe(PrepareItemCraftEvent.class, (event) -> {
            final CraftingInventory inventory = event.getInventory();
            final ItemStack result = inventory.getResult();
            if (result != null && result.getType() == material) {
                if (isUnlocked((Player) event.getViewers().get(0))) {
                    return;
                }
                inventory.setResult(null);
            }
        });

        subscribe(PrepareSmithingEvent.class, (event) -> {
            final SmithingInventory inventory = event.getInventory();
            final ItemStack result = inventory.getResult();
            if (result != null && result.getType() == material) {
                if (isUnlocked((Player) event.getViewers().get(0))) {
                    return;
                }
                inventory.setResult(null);
                event.setResult(null);
            }
        });

        subscribe(PlayerInteractEvent.class, (event) -> {
            if (event.getMaterial() == material) {
                I.log(Level.WARNING,"DEBUG -> Handling Interact Event for Item");
                if (isUnlocked(event.getPlayer())) {
                    return;
                }
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(EntityDamageByEntityEvent.class, (event) -> {
            if ((event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
                if (event.getDamager() instanceof Player player) {
                    if (player.getInventory().getItemInMainHand().getType() == material) {
                        if (isUnlocked(player)) {
                            return;
                        }
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
}
