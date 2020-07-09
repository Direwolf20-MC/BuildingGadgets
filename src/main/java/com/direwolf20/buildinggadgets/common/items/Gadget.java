package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import com.direwolf20.buildinggadgets.common.modes.Mode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class Gadget extends Item {

    public Gadget() {
        super(ModItems.ITEM_GROUP.maxStackSize(1).maxDamage(0).setNoRepair());
    }

    public abstract void action();

    public abstract void undo();

    /**
     * Used to unify all gadgets to use the same mode logic
     */
    public abstract List<Mode> getModes();

    /**
     * Find the gadget from a players main hands
     */
    public static Optional<ItemStack> findGadget(PlayerEntity entity) {
        if (entity.getHeldItemMainhand().getItem() instanceof Gadget)
            return Optional.of(entity.getHeldItemMainhand());

        if (entity.getHeldItemOffhand().getItem() instanceof Gadget)
            return Optional.of(entity.getHeldItemOffhand());

        return Optional.empty();
    }

    // NBT
    public Optional<BlockState> getBlock(ItemStack stack) {
        if( stack.getOrCreateTag().contains("set-block") )
            return Optional.of(NBTUtil.readBlockState(stack.getOrCreateTag().getCompound("set-block")));

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

    // clamp the range.
    // TODO: 09/07/2020 Add this to the config to make it simple to extend the max range
    public void setRange(ItemStack stack, int range) {
        // if range > 15 then set to 15, otherwise, if range < 0 then set to 1, otherwise set to the configured range
        int localRange = range > 15 ? 15 : (range <= 0 ? 1 : range);

        stack.getOrCreateTag().putInt("range", localRange);
    }

    // TODO: 09/07/2020 add max range config
    public void cycleRange(ItemStack gadget, PlayerEntity entity) {
        int currentRange = ((Gadget) gadget.getItem()).getRange(gadget);
        int range = currentRange + (entity.isSneaking() ? -1 : 1);

        range = range > 15 ? 15 : (range <= 0 ? 1 : range);
        this.setRange(gadget, range);

        // notify the player
        entity.sendStatusMessage(LangHelper.compMessage("message", "range-updated", range), true);
    }
}
