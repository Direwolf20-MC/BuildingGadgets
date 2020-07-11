package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import com.direwolf20.buildinggadgets.common.modes.Mode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class Gadget extends Item {

    public Gadget() {
        super(ModItems.ITEM_GROUP.maxStackSize(1).maxDamage(0).setNoRepair());
    }

    public abstract void action();

    public abstract void undo(ItemStack gadget, World world, PlayerEntity player);

    /**
     * Used to unify all gadgets to use the same mode logic
     */
    public abstract List<Mode> getModes();

    /**
     * Find the gadget from a players main hands,
     *
     * by design a gadget can only be operated one at a time thus the one in the
     * offhand is checked for last to ensure that if there is one in both hands the one
     * in the main hand will be retrieve first.
     */
    public static Optional<ItemStack> findGadget(PlayerEntity entity) {
        if (entity.getHeldItemMainhand().getItem() instanceof Gadget)
            return Optional.of(entity.getHeldItemMainhand());

        if (entity.getHeldItemOffhand().getItem() instanceof Gadget)
            return Optional.of(entity.getHeldItemOffhand());

        return Optional.empty();
    }

    /*
        A bit part of the gadgets is storing data, everything beyond this point is for storing data as
        nbt on the gadget. Nothing else. Please do not put non-nbt based methods below.

        @implNote
        These methods are available on all gadgets but only the Building and Exchanger gadget truly utilise all of them.

        @implNote @MichaelHillcox
        I am also aware that this should likely be a capability, I will move it all to a capability once
        I know how to move it over properly.
    */

    /**
     * Gets the current block to build with from the gadget
     */
    public Optional<BlockState> getBlock(ItemStack stack) {
        if( stack.getOrCreateTag().contains("set-block") )
            return Optional.of(NBTUtil.readBlockState(stack.getOrCreateTag().getCompound("set-block")));

        return Optional.empty();
    }

    public void setBlock(ItemStack stack, @Nonnull BlockState state) {
        stack.getOrCreateTag().put("set-block", NBTUtil.writeBlockState(state));
    }

    /**
     * Used the Modes enum's name to store and retrieve the current mode from the gadget.
     */
    public Mode getMode(ItemStack stack) {
        if( stack.getOrCreateTag().contains("mode") ) {
            // Using requireNonNull here as the IDE does not know how the above contains check works.
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

    /**
     * Cycles the modes through the gadgets mode enums
     */
    public void cycleMode(ItemStack gadget, PlayerEntity entity) {
        int currentIndex = this.getModes().indexOf(this.getMode(gadget));
        int newIndex = (currentIndex + 1) > this.getModes().size() - 1 ? 0 : (currentIndex + 1);

        this.setMode(gadget, this.getModes().get(newIndex).getName());

        // notify the player
        Mode mode = this.getModes().get(newIndex);
        entity.sendStatusMessage(LangHelper.compMessage("message", "mode-updated", ForgeI18n.getPattern(LangHelper.key("mode", mode.getName()))), true);
    }

    public int getRange(ItemStack stack) {
        if( stack.getOrCreateTag().contains("range") )
            return stack.getOrCreateTag().getInt("range");

        return 1;
    }

    /**
     * Sets the range on the gadget but clamps the range based on the max range for all gadgets
     * TODO: 09/07/2020 Add this to the config to make it simple to extend the max range
     */
    public void setRange(ItemStack stack, int range) {
        // if range > 15 then set to 15, otherwise, if range < 0 then set to 1, otherwise set to the configured range
        int localRange = range > 15 ? 15 : (range <= 0 ? 1 : range);

        stack.getOrCreateTag().putInt("range", localRange);
    }

    /**
     * Cycles the range resetting to the first index and the last index on each overflow.
     * TODO: 09/07/2020 add max range config
     */
    public void cycleRange(ItemStack gadget, PlayerEntity entity) {
        int currentRange = ((Gadget) gadget.getItem()).getRange(gadget);
        int range = currentRange + (entity.isSneaking() ? -1 : 1);

        range = range > 15 ? 1 : (range <= 0 ? 15 : range);
        this.setRange(gadget, range);

        // notify the player
        entity.sendStatusMessage(LangHelper.compMessage("message", "range-updated", range), true);
    }

    /**
     * Undo's are stored on the world with a UUID to identify them. This pushes one of those UUID's
     * to the gadget so we know what data we can undo.
     *
     * The UndoStack handles the removal :D
     */
    public boolean pushUndo(ItemStack stack, UUID uuid, DimensionType type) {
        ResourceLocation dimensionName = type.getRegistryName();
        if (dimensionName == null) {
            BuildingGadgets.LOGGER.fatal("Current dimension does not have registry name!");
            return false;
        }

        CompoundNBT compound = stack.getOrCreateTag();

        ListNBT list = compound.getList("undo-list", Constants.NBT.TAG_COMPOUND);
        CompoundNBT data = new CompoundNBT();
        data.putUniqueId("key", uuid);
        data.putString("dimension", dimensionName.toString());

        list.add(data);
        compound.put("undo-list", list);
        return true;
    }
}
