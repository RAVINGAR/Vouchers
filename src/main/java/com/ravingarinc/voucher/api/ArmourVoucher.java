package com.ravingarinc.voucher.api;

import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArmourVoucher extends ItemVoucher {
    private final List<Integer> armourSlots = Arrays.asList(36, 37, 38, 39);

    private final Map<UUID, Long> lastDamageCheck;

    public ArmourVoucher(final Material material, final HolderManager manager) {
        super(material, manager);

        lastDamageCheck = new HashMap<>();

        subscribe(InventoryDragEvent.class, (event) -> {
            if (event.getRawSlots().isEmpty()) {
                return;
            }
            final ItemStack cursor = event.getOldCursor();
            if (cursor.getType() != material) {
                return;
            }
            if (armourSlots.stream().anyMatch(i -> event.getInventorySlots().stream().findFirst().orElse(-1).equals(i))) {
                if (!isUnlocked((Player) event.getWhoClicked())) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
        });

        subscribe(BlockDispenseArmorEvent.class, (event) -> {
            if (isUnlocked((Player) event.getTargetEntity())) {
                return;
            }
            if (event.getItem().getType() == material) {
                event.setCancelled(true);
            }
        });

        subscribe(InventoryClickEvent.class, (event) -> {
            final InventoryAction action = event.getAction();
            final InventoryType.SlotType slot = event.getSlotType();
            final ClickType click = event.getClick();
            final Player player = (Player) event.getWhoClicked();

            ItemStack stack = event.getCurrentItem();
            if (stack != null && stack.getType() == material && !isUnlocked(player)) {
                if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT)) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
            if (click == ClickType.NUMBER_KEY) {
                stack = player.getInventory().getItem(event.getHotbarButton());
                if (stack != null && stack.getType() == material) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
            stack = event.getCursor();
            if (stack != null && stack.getType() == material && !isUnlocked(player)) {
                if (slot == InventoryType.SlotType.ARMOR && (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE)) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
        });

        subscribe(EntityDamageEvent.class, (event) -> {
            final Player player = (Player) event.getEntity(); //assert this is a player
            final long currentTime = System.currentTimeMillis();
            final long lastCheck = lastDamageCheck.computeIfAbsent(player.getUniqueId(), (p) -> currentTime);
            if (System.currentTimeMillis() > lastCheck + 7000L) {
                lastDamageCheck.put(player.getUniqueId(), currentTime);
                boolean message = false;
                final PlayerInventory inventory = player.getInventory();
                final ItemStack[] armour = inventory.getArmorContents();
                for (int i = 0; i < armour.length; i++) {
                    final ItemStack piece = armour[i];
                    if (piece != null && piece.getType() == material && !isUnlocked(player)) {
                        message = true;
                        inventory.setItem(armourSlots.get(i), null);
                        final int empty = inventory.firstEmpty();
                        if (empty == -1) {
                            player.getWorld().dropItemNaturally(player.getLocation(), piece);
                        } else {
                            inventory.setItem(empty, piece);
                        }
                    }
                }

                if (message) {
                    VoucherSettings.sendDenyMessage(player);
                }
            }
        });
    }
}
