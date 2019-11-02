package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.items.TemplateItem;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.registry.OurContainers;
import com.direwolf20.buildinggadgets.common.template.Template;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.world.FakeDelegationWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class TemplateManagerContainer extends BaseContainer {
    public static final String TEXTURE_LOC_SLOT_TOOL = Reference.MODID + ":gui/slot_copy_paste_gadget";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = Reference.MODID + ":gui/slot_template";

    private TemplateManagerTileEntity te;
    private Optional<TemplatePrebuilt> viewCache = Optional.empty();
    private FakeDelegationWorld delegationWorld = new FakeDelegationWorld(Minecraft.getInstance().world);

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER, windowId);
        BlockPos pos = extraData.readBlockPos();

        this.te = (TemplateManagerTileEntity) Minecraft.getInstance().world.getTileEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory, TemplateManagerTileEntity tileEntity) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER, windowId);
        this.te = Objects.requireNonNull(tileEntity);

        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return getTe().canInteractWith(playerIn);
    }

    private void addOwnSlots() {
        IItemHandler itemHandler = this.getTe().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
        int x = 132;
        addSlot(new WatchedSlot(itemHandler, 0, x, 18, TEXTURE_LOC_SLOT_TOOL, this::updateViewCache));
        addSlot(new SlotTemplateManager(itemHandler, 1, x, 63, TEXTURE_LOC_SLOT_TEMPLATE));
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack currentStack = slot.getStack();
            itemstack = currentStack.copy();

            if (index < TemplateManagerTileEntity.SIZE) {
                if (! this.mergeItemStack(currentStack, TemplateManagerTileEntity.SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.mergeItemStack(currentStack, 0, TemplateManagerTileEntity.SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    private void updateViewCache(int slot) {
        ItemStack stack = this.getSlot(slot).getStack();
        if( stack.isEmpty() ) {
            if( this.viewCache.isPresent() )
                this.viewCache = Optional.empty();

            return;
        }

        if(!(stack.getItem() instanceof GadgetCopyPaste) && !(stack.getItem() instanceof TemplateItem))
            return;

        Minecraft.getInstance().world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
            Template template = provider.getTemplateForKey(key);
            this.viewCache = Optional.of(new TemplatePrebuilt(template, template.createViewInContext(
                    SimpleBuildContext.builder().
                            buildingPlayer(Minecraft.getInstance().player).
                            build(delegationWorld)
            )));
        }));
    }

    public Optional<TemplatePrebuilt> getViewCache() {
        return viewCache;
    }

    public TemplateManagerTileEntity getTe() {
        return te;
    }

    private class WatchedSlot extends SlotTemplateManager {
        private Consumer<Integer> onChange;

        public WatchedSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, String backgroundLoc, Consumer<Integer> onChange) {
            super(itemHandler, index, xPosition, yPosition, backgroundLoc);
            this.onChange = onChange;
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            this.onChange.accept(this.getSlotIndex());
        }
    }

    public class TemplatePrebuilt {
        private Template template;
        private IBuildView buildView;

        public TemplatePrebuilt(Template template, IBuildView buildView) {
            this.template = template;
            this.buildView = buildView;
        }

        public Template getTemplate() {
            return template;
        }

        public IBuildView getBuildView() {
            return buildView;
        }
    }

}
