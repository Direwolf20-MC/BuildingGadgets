package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.common.construction.UndoStack;
import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.construction.modes.Mode;
import com.direwolf20.buildinggadgets.common.helpers.LookingHelper;
import com.direwolf20.buildinggadgets.common.helpers.MessageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class Gadget extends Item {

    public Gadget() {
        super(ModItems.ITEM_GROUP.maxStackSize(1).maxDamage(0).setNoRepair());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (worldIn == null) {
            return;
        }

        Gadget gadget = ((Gadget) stack.getItem());

        // Selected block
        gadget.getBlock(stack).ifPresent(block ->
                tooltip.add(MessageHelper.translation("tooltip", "selected-block", MessageHelper.blockName(block.getBlock())).setStyle(Style.EMPTY.setItalic(true).applyFormatting(TextFormatting.GREEN))));

        // Current Mode
        tooltip.add(MessageHelper.translation("tooltip", "mode", ForgeI18n.getPattern(MessageHelper.translationKey("mode", gadget.getMode(stack).getName()))).setStyle(Style.EMPTY.applyFormatting(TextFormatting.AQUA)));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if( worldIn.isRemote ) {
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }

        ItemStack gadget = playerIn.getHeldItem(handIn);
        BlockRayTraceResult rayTrace = LookingHelper.getBlockResult(playerIn, false);
        if( playerIn.isSneaking() ) {
            return this.sneakingAction(worldIn, playerIn, gadget, rayTrace);
        }

        return this.action(worldIn, playerIn, gadget, rayTrace)
                ? ActionResult.resultSuccess(gadget)
                : super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public abstract boolean action(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace);

    public abstract ActionResult<ItemStack> sneakingAction(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace);

    public void undo(ItemStack gadget, World world, PlayerEntity player) {
        UndoWorldStore store = UndoWorldStore.get(world);
        UndoStack undoStack = new UndoStack(gadget);

        Optional<UUID> uuid = undoStack.pollBit(world.getDimensionKey());

        if (!uuid.isPresent()) {
            // Not perfect but it's alright for now :D
            List<UUID> bitsByDimension = undoStack.getBitsByDimension(world.getDimensionKey());
            player.sendStatusMessage(MessageHelper.builder("message", bitsByDimension.size() == 0 ? "undo-store-empty" : "undo-fetch-failure").error().build(), true);
        }

        uuid.ifPresent(key -> undoAction(store, key, gadget, world, player));
    }

    public abstract void undoAction(UndoWorldStore store, UUID uuid, ItemStack gadget, World world, PlayerEntity playerEntity);

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
    public static Optional<GadgetWithStack> findGadget(PlayerEntity entity) {
        ItemStack stack = ItemStack.EMPTY;
        if (entity.getHeldItemMainhand().getItem() instanceof Gadget) {
            stack = entity.getHeldItemMainhand();
        }

        if (entity.getHeldItemOffhand().getItem() instanceof Gadget) {
            stack = entity.getHeldItemOffhand();
        }

        return !stack.isEmpty() ? Optional.of(GadgetWithStack.of(stack)) : Optional.empty();
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
        entity.sendStatusMessage(MessageHelper.builder("message", "mode-updated", ForgeI18n.getPattern(MessageHelper.translationKey("mode", mode.getName()))).info().build(), true);
    }

    public int getRange(ItemStack stack) {
        if( stack.getOrCreateTag().contains("range") )
            return stack.getOrCreateTag().getInt("range");

        return 1;
    }

    /**
     * Sets the range on the gadget but clamps the range based on the max range for all gadgets
     */
    public void setRange(ItemStack stack, int range) {
        // if range > 15 then set to 15, otherwise, if range < 0 then set to 1, otherwise set to the configured range
        int maxRange = Config.COMMON_CONFIG.gadgetMaxRange.get();
        int localRange = range > maxRange ? maxRange : (range <= 0 ? 1 : range);

        stack.getOrCreateTag().putInt("range", localRange);
    }

    /**
     * Cycles the range resetting to the first index and the last index on each overflow.
     */
    public void cycleRange(ItemStack gadget, PlayerEntity entity) {
        int maxRange = Config.COMMON_CONFIG.gadgetMaxRange.get();
        int currentRange = ((Gadget) gadget.getItem()).getRange(gadget);
        int range = currentRange + (entity.isSneaking() ? -1 : 1);

        range = range > maxRange ? 1 : (range <= 0 ? maxRange : range);
        this.setRange(gadget, range);

        // notify the player
        entity.sendStatusMessage(MessageHelper.builder("message", "range-updated", range).info().build(), true);
    }

    /**
     * I might remove this, it's just a simple way of passing around not only the gadget but also it's stack
     * representation as well. It's also a bit nicer than a raw pair implementation.
     */
    public static class GadgetWithStack {
        private final Gadget gadget;
        private final ItemStack gadgetStack;

        private GadgetWithStack(Gadget gadget, ItemStack gadgetStack) {
            this.gadget = gadget;
            this.gadgetStack = gadgetStack;
        }

        public static GadgetWithStack of(ItemStack gadgetStack) {
            return new GadgetWithStack((Gadget) gadgetStack.getItem(), gadgetStack);
        }

        public Gadget getGadget() {
            return gadget;
        }

        public ItemStack getStack() {
            return gadgetStack;
        }
    }
}
