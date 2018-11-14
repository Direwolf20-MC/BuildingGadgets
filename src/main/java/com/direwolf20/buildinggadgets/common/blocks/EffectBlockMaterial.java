package com.direwolf20.buildinggadgets.common.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class EffectBlockMaterial extends Material {
    public static final Material EFFECTBLOCKMATERIAL = new EffectBlockMaterial(MapColor.AIR);

    public EffectBlockMaterial(MapColor color) {
        super(color);
        this.setNoPushMobility();
    }

    /**
     * Returns true if the block is a considered solid. This is true by default.
     */
    @Override
    public boolean isSolid() {
        return false;
    }
}
