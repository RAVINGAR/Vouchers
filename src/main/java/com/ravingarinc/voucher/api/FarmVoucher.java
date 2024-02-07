package com.ravingarinc.voucher.api;

import com.ravingarinc.voucher.item.ItemType;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Iterator;

public class FarmVoucher extends Voucher {
    private final ItemType item;
    private final ItemType block;
    private final ItemType seed;
    private final ItemType food;

    public FarmVoucher(final String key, final ItemType item, final ItemType block, final ItemType seed, final ItemType food, final HolderManager manager) {
        super(key, manager);

        this.item = item;
        this.block = block;
        this.seed = seed;
        this.food = food;

        if (VoucherSettings.blockFoodMaterialCraft) {
            subscribe(PrepareItemCraftEvent.class, (event) -> {
                final CraftingInventory inventory = event.getInventory();
                final ItemStack result = inventory.getResult();
                if (result != null && food.isSameAs(result)) {
                    if (isUnlocked((Player) event.getViewers().get(0))) {
                        return;
                    }
                    inventory.setResult(null);
                }
            });
        }

        if (VoucherSettings.blockFoodMaterialConsume) {
            subscribe(PlayerItemConsumeEvent.class, (event) -> {
                if (food.isSameAs(event.getItem())) {
                    if (isUnlocked(event.getPlayer())) {
                        return;
                    }
                    event.setCancelled(true);
                    VoucherSettings.sendDenyMessage(event.getPlayer());
                }
            });
        }

        subscribe(PlayerInteractEvent.class, (event) -> {
            final ItemStack eventItem = event.getItem();
            if (seed.isSameAs(eventItem) || item.isSameAs(eventItem)) {
                if (isUnlocked(event.getPlayer())) {
                    return;
                }
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(BlockPlaceEvent.class, (event) -> {
            if (block.isSameAs(event.getBlockPlaced().getBlockData())) {
                if (isUnlocked(event.getPlayer())) {
                    return;
                }
                event.setCancelled(true);
                event.setBuild(false);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(BlockBreakEvent.class, (event) -> {
            if (block.isSameAs(event.getBlock().getBlockData())) {
                if (isUnlocked(event.getPlayer())) {
                    return;
                }
                event.setCancelled(true);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });
    }

    @Override
    public Material getIcon() {
        return item.getMaterial();
    }

    @Override
    public String getLore() {
        if (lore == null) {
            final StringBuilder lore = new StringBuilder();
            final Iterator<String> iterator = Arrays.stream(VoucherSettings.farmLoreFormat).iterator();
            while (iterator.hasNext()) {
                lore.append(iterator.next()
                        .replace("{tier}", item.getTier())
                        .replace("{item}", item.getDisplayName())
                        .replace("{block}", block.getDisplayName())
                        .replace("{food}", food.getDisplayName())
                        .replace("{seed}", seed.getDisplayName()));
                if (iterator.hasNext()) {
                    lore.append("\n");
                }
            }
            this.lore = lore.toString();
        }
        return this.lore;
    }

    @Override
    public String getItemDisplayName() {
        return item.getDisplayName();
    }
}
