package com.ravingarinc.voucher.item;

import com.ravingarinc.voucher.api.Util;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class VanillaItemType implements ItemType {
    private final Material material;

    private final String name;
    public VanillaItemType(Material material) {
        this.material = material;
        this.name = Util.fullyCapitalise(material.getKey().getKey());
    }
    @Override
    public boolean isSameAs(ItemStack item) {
        if(item == null) {
            return false;
        }
        return item.getType() == material;
    }

    @Override
    public boolean isSameAs(BlockData block) {
        return block.getMaterial() == material;
    }

    @Override
    public String getId() {
        return material.getKey().getKey().toLowerCase();
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getKey() {
        return "vanilla_" + getId();
    }

    @Override
    public String toString() {
        return "vanilla:" + material.getKey().getKey();
    }
}
