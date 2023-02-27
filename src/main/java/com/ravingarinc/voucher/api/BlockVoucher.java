package com.ravingarinc.voucher.api;

import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.VoucherSettings;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockVoucher extends ItemVoucher {
    public BlockVoucher(final Material material, final HolderManager manager) {
        super(material, manager);

        subscribe(BlockPlaceEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (event.getBlockPlaced().getType() == material) {
                event.setCancelled(true);
                event.setBuild(false);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });

        subscribe(BlockBreakEvent.class, (event) -> {
            if (isUnlocked(event.getPlayer())) {
                return;
            }
            if (event.getBlock().getType() == material) {
                event.setCancelled(true);
                VoucherSettings.sendDenyMessage(event.getPlayer());
            }
        });
    }
}
