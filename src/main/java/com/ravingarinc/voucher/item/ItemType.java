package com.ravingarinc.voucher.item;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public interface ItemType {

    Material getMaterial();

    boolean isSameAs(ItemStack item);

    boolean isSameAs(BlockData block);

    String getId();

    String getDisplayName();

    String getKey();
}
