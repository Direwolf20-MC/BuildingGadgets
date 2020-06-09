package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.modes.Mode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class Gadget extends Item {

    public Gadget() {
        super(ModItems.ITEM_GROUP.maxStackSize(1).maxDamage(0).setNoRepair());
    }

    /**
     * Used to unify all gadgets to use the same mode logic
     */
    protected abstract List<Mode> getModes();

    /**
     * Will be removed before release. Just a testing method to handle cycling though each gadgets modes
     */
    public String rotateModes(ItemStack stack) {
        int currentIndex = this.getModes().indexOf(this.getMode(stack));
        int newIndex = (currentIndex + 1) > this.getModes().size() - 1 ? 0 : (currentIndex + 1);

        this.setMode(stack, this.getModes().get(newIndex).getName());
        return this.getModes().get(newIndex).getName();
    }

    /**
     * Finds and returns the closest block to the player within X amount of blocks
     *
     * @param worldIn           Current world
     * @param playerEntity      Current Player Entity
     * @return                  Optional BlockState of what block the player is looking ats
     */
    public static Optional<BlockState> getLookingAt(World worldIn, PlayerEntity playerEntity, int range) {
        RayTraceResult trace = playerEntity.pick(range, 0, false);

        if( trace.getType() != RayTraceResult.Type.BLOCK ) {
            return Optional.empty();
        }

        BlockPos lookAt = ((BlockRayTraceResult) trace).getPos();
        BlockState state = worldIn.getBlockState(lookAt);
        if( state.isAir(worldIn, lookAt) ) {
            return Optional.empty();
        }

        return Optional.of(state);
    }

    // NBT
    public Optional<BlockState> getBlock(ItemStack stack) {
        if( stack.getOrCreateTag().contains("set-block") )
            return Optional.of(NBTUtil.readBlockState(stack.getOrCreateTag()));

        return Optional.empty();
    }

    public void setBlock(ItemStack stack, @Nonnull BlockState state) {
        stack.getOrCreateTag().put("set-block", NBTUtil.writeBlockState(state));
    }

    public Mode getMode(ItemStack stack) {
        if( stack.getOrCreateTag().contains("mode") ) {
            String modeId = Objects.requireNonNull(stack.getOrCreateTag().get("mode")).getString();

            // Find the mode based on it's name or return the default one.
            return this.getModes().stream()
                    .filter(e -> e.getName().equals(modeId))
                    .findFirst()
                    .orElse(this.getModes().get(0));
        }

        // Not found, return default
        return this.getModes().get(0);
    }

    public void setMode(ItemStack stack, String mode) {
        stack.getOrCreateTag().putString("mode", mode);
    }
}
