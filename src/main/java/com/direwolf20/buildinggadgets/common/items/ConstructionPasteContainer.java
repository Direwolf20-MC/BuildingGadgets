package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.PasteContainerCapabilityProvider;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

public class ConstructionPasteContainer extends Item {
    private static final ResourceLocation LEVEL = new ResourceLocation("level");

    private final IntSupplier maxCapacity;
    private final boolean isCreative;

    public ConstructionPasteContainer(boolean isCreative, IntSupplier maxCapacity) {
        super(OurItems.nonStackableItemProperties());

        this.isCreative = isCreative;
        this.maxCapacity = maxCapacity;

        // This is used for setting up the texture change :D
        // TODO ADD BACK 1.16
//        addPropertyOverride(LEVEL, (stack, world, entity) -> {
//            float percent = ConstructionPasteContainer.getPasteAmount(stack) / (float) this.maxCapacity.getAsInt();
//            return MathHelper.floor(percent * 4) / 4F;
//        });
    }

    public ConstructionPasteContainer(boolean isCreative) {
        this(isCreative, () -> Integer.MAX_VALUE);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new PasteContainerCapabilityProvider(stack);
    }

    public void setPasteCount(ItemStack stack, int amount) {
        if (isCreative) {
            return;
        }

        stack.getOrCreateTag().putInt(NBTKeys.PASTE_COUNT, amount);
    }

    public int getPasteCount(ItemStack stack) {
        if (isCreative) {
            return Integer.MAX_VALUE;
        }

        return !stack.hasTag() ? 0 : stack.getTag().getInt(NBTKeys.PASTE_COUNT);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        player.setActiveHand(hand);
        PlayerInventory inv = player.inventory;
        if (!world.isRemote) {
            for (int i = 0; i < 36; ++i) { // todo: this is awful. hardcoded int
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
        TranslationTextComponent key = isCreative
                ? TooltipTranslation.PASTECONTAINER_CREATIVE_AMOUNT.componentTranslation()
                : TooltipTranslation.PASTECONTAINER_AMOUNT.componentTranslation(getPasteCount(stack), getMaxCapacity());

        list.add(key.setStyle(Styles.WHITE));
    }

    /**
     * Helper method. Delegates to {@link ConstructionPasteContainer#setPasteCount(ItemStack, int)}.
     *
     * @param stack  current gadget
     * @param amount amount
     */
    public static void setPasteAmount(ItemStack stack, int amount) {
        Item item = stack.getItem();
        if (item instanceof ConstructionPasteContainer)
            ((ConstructionPasteContainer) item).setPasteCount(stack, amount);
        else
            BuildingGadgets.LOG.warn("Potential abuse of ConstructionPasteContainer#setPasteAmount(ItemStack, int) where the given ItemStack does not contain a ConstructionPasteContainer.");
    }

    /**
     * Helper method. Delegates to {@link ConstructionPasteContainer#getPasteCount(ItemStack)}}.
     */
    public static int getPasteAmount(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ConstructionPasteContainer)
            return ((ConstructionPasteContainer) item).getPasteCount(stack);
        BuildingGadgets.LOG.warn("Potential abuse of ConstructionPasteContainer#getPasteAmount(ItemStack) where the given ItemStack does not contain a ConstructionPasteContainer.");
        return 0;
    }

    public int getMaxCapacity() {
        return isCreative ? Integer.MAX_VALUE : maxCapacity.getAsInt();
    }

    public boolean isCreative() {
        return isCreative;
    }
}
