package com.ravingarinc.voucher.api;

import com.ravingarinc.api.Pair;
import com.ravingarinc.voucher.item.ItemType;
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

import java.util.*;

public class ItemVoucher extends Voucher {
    protected ItemType type;

    protected final String craftTime;
    protected final int energyCost;
    protected final int moneyCost;
    protected final List<Pair<ItemType, Integer>> itemCosts;

    public ItemVoucher(final ItemType type, final String tier, final String craftTime, final int energyCost, final int moneyCost, final HolderManager manager) {
        super(type.getKey(), tier, manager);
        this.type = type;
        this.craftTime = craftTime;
        this.energyCost = energyCost;
        this.moneyCost = moneyCost;
        this.itemCosts = new ArrayList<>();

        if(VoucherSettings.preventItemCrafting) {
            subscribe(PrepareItemCraftEvent.class, (event) -> {
                final CraftingInventory inventory = event.getInventory();
                final ItemStack result = inventory.getResult();
                if (result != null && type.isSameAs(result)) {
                    if (isUnlocked((Player) event.getViewers().get(0))) {
                        return;
                    }
                    inventory.setResult(null);
                }
            });

            subscribe(PrepareSmithingEvent.class, (event) -> {
                final SmithingInventory inventory = event.getInventory();
                final ItemStack result = inventory.getResult();
                if (result != null && type.isSameAs(result)) {
                    if (isUnlocked((Player) event.getViewers().get(0))) {
                        return;
                    }
                    inventory.setResult(null);
                    event.setResult(null);
                }
            });
        }

        if(VoucherSettings.preventItemInteraction) {
            subscribe(PlayerInteractEvent.class, (event) -> {
                if (type.isSameAs(event.getItem())) {
                    if (isUnlocked(event.getPlayer())) {
                        return;
                    }
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    VoucherSettings.sendDenyMessage(event.getPlayer());
                }
            });
        }

        if(VoucherSettings.preventItemDamage) {
            subscribe(EntityDamageByEntityEvent.class, (event) -> {
                if ((event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
                    if (event.getDamager() instanceof Player player) {
                        if (type.isSameAs(player.getInventory().getItemInMainHand())) {
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

    public void addItemCost(final ItemType type, final int quantity) {
        this.itemCosts.add(new Pair<>(type, quantity));
    }

    public String getCraftTime() {
        return craftTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public int getMoneyCost() {
        return moneyCost;
    }

    public ItemType getType() {
        return type;
    }

    public List<Pair<ItemType, Integer>> getItemCosts() {
        return Collections.unmodifiableList(itemCosts);
    }

    @Override
    public String getItemDisplayName() {
        return type.getDisplayName();
    }

    @Override
    public Material getIcon() {
        return type.getMaterial();
    }

    @Override
    public String getLore() {
        if (lore == null) {
            final String format = fullyCapitalise(getItemDisplayName());
            final StringBuilder lore = new StringBuilder();
            final Iterator<String> iterator = Arrays.stream(VoucherSettings.voucherLoreFormat).iterator();
            while (iterator.hasNext()) {
                lore.append(iterator.next()
                        .replace("{item}", format)
                        .replace("{tier}", getTier()));
                if (iterator.hasNext()) {
                    lore.append("\n");
                }
            }
            this.lore = lore.toString();
        }
        return lore;
    }
}
