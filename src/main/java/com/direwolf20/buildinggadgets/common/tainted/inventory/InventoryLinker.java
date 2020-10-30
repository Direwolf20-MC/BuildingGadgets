package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Optional;

public class InventoryLinker {
    /**
     * Perform the link to the inventory
     */
    public static Result linkInventory(World world, ItemStack stack, BlockRayTraceResult trace) {
        TileEntity tileEntity = world.getTileEntity(trace.getPos());
        if (tileEntity == null || !tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            return Result.fail(MessageTranslation.INVALID_BOUND_TILE);
        }

        // remove if the existing linked inventory is the same block we're setting now.
        boolean removed = getLinkedInventory(world, stack)
                .map(e -> removeIfSame(stack, trace.getPos()))
                .orElse(false);

        if (removed) {
            return Result.removed();
        }

        // Set the relevant data
        CompoundNBT compound = stack.getOrCreateTag();
        compound.putString(NBTKeys.REMOTE_INVENTORY_DIM, world.getDimensionKey().getLocation().toString());
        compound.put(NBTKeys.REMOTE_INVENTORY_POS, NBTUtil.writeBlockPos(trace.getPos()));
        return Result.success();
    }

    /**
     * Directly fetch the linked inventory if the tile exists (removes if not) and if the tile holds
     * a capability.
     */
    public static LazyOptional<IItemHandler> getLinkedInventory(World world, BlockPos pos, RegistryKey<World> registry, @Nullable ItemStack stack) {
        if (!world.getDimensionKey().equals(registry)) {
            return LazyOptional.empty();
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            // Unlink if the tile entity no longer exists
            if (stack != null) {
                removeDataFromStack(stack);
            }

            return LazyOptional.empty();
        }

        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public static LazyOptional<IItemHandler> getLinkedInventory(World world, ItemStack stack) {
        Pair<BlockPos, RegistryKey<World>> dataFromStack = getDataFromStack(stack);
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
        Pair<BlockPos, RegistryKey<World>> dataFromStack = getDataFromStack(stack);
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
        CompoundNBT compound = stack.getOrCreateTag();
        compound.remove(NBTKeys.REMOTE_INVENTORY_POS);
        compound.remove(NBTKeys.REMOTE_INVENTORY_DIM);
    }

    /**
     * Retrieves the link data from the ItemStack
     */
    @Nullable
    public static Pair<BlockPos, RegistryKey<World>> getDataFromStack(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        if (!compound.contains(NBTKeys.REMOTE_INVENTORY_POS) || !compound.contains(NBTKeys.REMOTE_INVENTORY_DIM)) {
            return null;
        }

        RegistryKey<World> dimKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(compound.getString(NBTKeys.REMOTE_INVENTORY_DIM)));
        return Pair.of(
            NBTUtil.readBlockPos(compound.getCompound(NBTKeys.REMOTE_INVENTORY_POS)),
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
