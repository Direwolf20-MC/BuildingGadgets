package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class InventoryLinker {
    /**
     * Perform the link to the inventory
     */
    public static Result linkInventory(Level world, ItemStack stack, BlockHitResult trace) {
        BlockEntity tileEntity = world.getBlockEntity(trace.getBlockPos());
        if (tileEntity == null || !tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            return Result.fail(MessageTranslation.INVALID_BOUND_TILE);
        }

        // remove if the existing linked inventory is the same block we're setting now.
        boolean removed = getLinkedInventory(world, stack)
                .map(e -> removeIfSame(stack, trace.getBlockPos()))
                .orElse(false);

        if (removed) {
            return Result.removed();
        }

        // Set the relevant data
        CompoundTag compound = stack.getOrCreateTag();
        compound.putString(NBTKeys.REMOTE_INVENTORY_DIM, world.dimension().location().toString());
        compound.put(NBTKeys.REMOTE_INVENTORY_POS, NbtUtils.writeBlockPos(trace.getBlockPos()));
        return Result.success();
    }

    /**
     * Directly fetch the linked inventory if the tile exists (removes if not) and if the tile holds
     * a capability.
     */
    public static LazyOptional<IItemHandler> getLinkedInventory(Level world, BlockPos pos, ResourceKey<Level> registry, @Nullable ItemStack stack) {
        if (!world.dimension().equals(registry)) {
            return LazyOptional.empty();
        }

        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity == null) {
            // Unlink if the tile entity no longer exists
            if (stack != null) {
                removeDataFromStack(stack);
            }

            return LazyOptional.empty();
        }

        return tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
    }

    public static LazyOptional<IItemHandler> getLinkedInventory(Level world, ItemStack stack) {
        Pair<BlockPos, ResourceKey<Level>> dataFromStack = getDataFromStack(stack);
        if (dataFromStack == null) {
            return LazyOptional.empty();
        }

        return getLinkedInventory(world, dataFromStack.getKey(), dataFromStack.getValue(), stack);
    }

    /**
     * Remove the link from the ItemStack if the pos is the same as the target pos. This creates a toggle effect.
     *
     * @implNote Ideally this would not have to get the same data twice but for now, this works fine.
     */
    private static boolean removeIfSame(ItemStack stack, BlockPos pos) {
        // This isn't ideal that we have to do this twice
        Pair<BlockPos, ResourceKey<Level>> dataFromStack = getDataFromStack(stack);
        if (dataFromStack == null) {
            return false;
        }

        if (dataFromStack.getKey().equals(pos)) {
            removeDataFromStack(stack);
            return true;
        }

        return false;
    }

    /**
     * Removes the keys from the stack to allow for lazy contains
     */
    public static void removeDataFromStack(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        compound.remove(NBTKeys.REMOTE_INVENTORY_POS);
        compound.remove(NBTKeys.REMOTE_INVENTORY_DIM);
    }

    /**
     * Retrieves the link data from the ItemStack
     */
    @Nullable
    public static Pair<BlockPos, ResourceKey<Level>> getDataFromStack(ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        if (!compound.contains(NBTKeys.REMOTE_INVENTORY_POS) || !compound.contains(NBTKeys.REMOTE_INVENTORY_DIM)) {
            return null;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString(NBTKeys.REMOTE_INVENTORY_DIM)));
        return Pair.of(
            NbtUtils.readBlockPos(compound.getCompound(NBTKeys.REMOTE_INVENTORY_POS)),
            dimKey
        );
    }

    /**
     * Handles if the Link was successful and a message to go with it.
     */
    public final static class Result {
        private final MessageTranslation i18n;
        private final boolean successful;

        public Result(MessageTranslation i18n, boolean successful) {
            this.i18n = i18n;
            this.successful = successful;
        }

        public static Result fail(MessageTranslation i18n) {
            return new Result(i18n, false);
        }

        public static Result success() {
            return new Result(MessageTranslation.BOUND_TO_TILE, true);
        }

        public static Result removed() {
            return new Result(MessageTranslation.UNBOUND_TO_TILE, true);
        }

        public MessageTranslation getI18n() {
            return i18n;
        }

        public boolean isSuccessful() {
            return successful;
        }
    }
}
