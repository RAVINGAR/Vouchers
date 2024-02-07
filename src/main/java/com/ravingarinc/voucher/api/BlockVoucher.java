package com.ravingarinc.voucher.api;

import com.ravingarinc.voucher.item.ItemType;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockVoucher extends ItemVoucher {
    public BlockVoucher(final ItemType type, final HolderManager manager) {
        super(type, manager);

        subscribe(BlockPlaceEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (type.isSameAs(event.getBlockPlaced().getBlockData())) {
                event.setCancelled(true);
                event.setBuild(false);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(BlockBreakEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (type.isSameAs(event.getBlock().getBlockData())) {
                event.setCancelled(true);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });
    }
}
