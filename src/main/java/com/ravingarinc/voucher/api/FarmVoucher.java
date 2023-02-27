package com.ravingarinc.voucher.api;

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
    private final Material item;
    private final Material block;
    private final Material seed;
    private final Material food;

    public FarmVoucher(final String key, final Material item, final Material block, final Material seed, final Material food, final HolderManager manager) {
        super(key, manager);

        this.item = item;
        this.block = block;
        this.seed = seed;
        this.food = food;

        if (VoucherSettings.blockFoodMaterialCraft) {
            subscribe(PrepareItemCraftEvent.class, (event) -> {
                if (isUnlocked((Player) event.getViewers().get(0))) {
                    return;
                }
                final CraftingInventory inventory = event.getInventory();
                final ItemStack result = inventory.getResult();
                if (result != null && result.getType() == food) {
                    inventory.setResult(null);
                }
            });
        }

        if (VoucherSettings.blockFoodMaterialConsume) {
            subscribe(PlayerItemConsumeEvent.class, (event) -> {
                if (isUnlocked(event.getPlayer())) {
                    return;
                }
                if (event.getItem().getType() == food) {
                    event.setCancelled(true);
                    VoucherSettings.sendDenyMessage(event.getPlayer());
                }
            });
        }

        subscribe(PlayerInteractEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (event.getMaterial() == seed || event.getMaterial() == item) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(BlockPlaceEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (event.getBlockPlaced().getType() == block) {
                event.setCancelled(true);
                event.setBuild(false);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(BlockBreakEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (event.getBlock().getType() == block) {
                event.setCancelled(true);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });
    }

    @Override
    public Material getIcon() {
        return item;
    }

    @Override
    public String getLore() {
        if (lore == null) {
            final String formatItem = fullyCapitalise(item.getKey().getKey());
            final String formatBlock = fullyCapitalise(block.getKey().getKey());
            final String formatFood = fullyCapitalise(food.getKey().getKey());
            final String formatSeed = fullyCapitalise(seed.getKey().getKey());
            final StringBuilder lore = new StringBuilder();
            final Iterator<String> iterator = Arrays.stream(VoucherSettings.farmLoreFormat).iterator();
            while (iterator.hasNext()) {
                lore.append(iterator.next()
                        .replace("{item}", formatItem)
                        .replace("{block}", formatBlock)
                        .replace("{food}", formatFood)
                        .replace("{seed}", formatSeed));
                if (iterator.hasNext()) {
                    lore.append("\n");
                }
            }
            this.lore = lore.toString();
        }
        return this.lore;
    }
}
