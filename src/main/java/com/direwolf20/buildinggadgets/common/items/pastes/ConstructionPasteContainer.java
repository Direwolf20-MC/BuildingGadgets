package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

public class ConstructionPasteContainer extends GenericPasteContainer {

    private IntSupplier maxCapacity;

    public ConstructionPasteContainer(Properties builder, IntSupplier maxCapacity) {
        super(builder);
        this.maxCapacity = maxCapacity;
        addPropertyOverride(Reference.PROPERTY_OVERRIDE_LEVEL, (stack, world, entity) -> {
            float percent = ConstructionPasteContainer.getPasteAmount(stack) / (float) this.maxCapacity.getAsInt();
            return MathHelper.floor(percent * 4) / 4F;
        });
    }

    @Override
    public void setPasteCount(ItemStack stack, int amount) {
        NBTHelper.getOrNewTag(stack).putInt(NBTKeys.PASTE_COUNT, amount);
    }

    @Override
    public int getPasteCount(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt(NBTKeys.PASTE_COUNT);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        player.setActiveHand(hand);
        PlayerInventory inv = player.inventory;
        if (!world.isRemote) {
            for (int i = 0; i < 36; ++i) {
                ItemStack itemStack = inv.getStackInSlot(i);
                if (itemStack.getItem() instanceof ConstructionPaste) {
                    InventoryHelper.addPasteToContainer(player, itemStack);
                }
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        list.add(TooltipTranslation.PASTECONTAINER_AMOUNT.componentTranslation(getPasteCount(stack))
                         .setStyle(Styles.WHITE));
    }

    @Override
    public int getMaxCapacity() {
        return maxCapacity.getAsInt();
    }

}
