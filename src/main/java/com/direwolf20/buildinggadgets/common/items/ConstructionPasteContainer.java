package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.PasteContainerCapabilityProvider;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

public class ConstructionPasteContainer extends Item {
    public static final ResourceLocation LEVEL = new ResourceLocation("level");

    private final IntSupplier maxCapacity;
    private final boolean isCreative;

    public ConstructionPasteContainer(boolean isCreative, IntSupplier maxCapacity) {
        super(OurItems.nonStackableItemProperties());

        this.isCreative = isCreative;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return super.getRenderPropertiesInternal();
    }

    public ConstructionPasteContainer(boolean isCreative) {
        this(isCreative, () -> Integer.MAX_VALUE);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        player.startUsingItem(hand);
        Inventory inv = player.getInventory();
        if (!world.isClientSide) {
            for (int i = 0; i < 36; ++i) { // todo: this is awful. hardcoded int
                ItemStack itemStack = inv.getItem(i);
                if (itemStack.getItem() instanceof ConstructionPaste) {
                    InventoryHelper.addPasteToContainer(player, itemStack);
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, heldItem);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        MutableComponent key = isCreative
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

        return 0;
    }

    public static int getMaxPasteAmount(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ConstructionPasteContainer)
            return ((ConstructionPasteContainer) item).getMaxCapacity();

        return 0;
    }

    public int getMaxCapacity() {
        return isCreative ? Integer.MAX_VALUE : maxCapacity.getAsInt();
    }

    public boolean isCreative() {
        return isCreative;
    }
}
