package com.direwolf20.buildinggadgets.client.gui;

import java.util.function.Consumer;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages.OpenContainer;
import net.minecraftforge.fml.network.NetworkHooks;

public enum GuiMod {
    COPY_PASTE(tool -> tool.getItem() instanceof GadgetCopyPaste ? new CopyPasteGUI(tool) : null),
    DESTRUCTION(tool -> tool.getItem() instanceof GadgetDestruction ? new DestructionGUI(tool) : null),
    PASTE(tool -> tool.getItem() instanceof GadgetCopyPaste ? new PasteGUI(tool) : null),
    TEMPLATE_MANAGER("template_manager", message -> {
        TileEntity te = Minecraft.getInstance().world.getTileEntity(message.getAdditionalData().readBlockPos());
        return te instanceof TemplateManagerTileEntity ? new TemplateManagerGUI((TemplateManagerTileEntity) te,
                getTemplateManagerContainer(Minecraft.getInstance().player, te)) : null;
    }, (id, player, world, pos) -> {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TemplateManagerTileEntity) {
            openGuiContainer(id, player, getTemplateManagerContainer(player, te), buffer -> buffer.writeBlockPos(pos));
            return true;
        }
        return false;
    });

    private static interface IContainerOpener {
        boolean open(String id, EntityPlayerMP player, World world, BlockPos pos);
    }

    private Function<ItemStack, GuiScreen> clientScreenProvider;
    private Function<OpenContainer, GuiScreen> commonScreenProvider;
    private IContainerOpener containerOpener;
    private String id;

    private GuiMod(Function<ItemStack, GuiScreen> clientScreenProvider) {
        this.clientScreenProvider = clientScreenProvider;
    }

    private GuiMod(String id, Function<OpenContainer, GuiScreen> commonScreenProvider, IContainerOpener containerOpener) {
        this.id = id;
        this.commonScreenProvider = commonScreenProvider;
        this.containerOpener = containerOpener;
    }

    public boolean openScreen(EntityPlayer player) {
        if (clientScreenProvider == null)
            return false;

        GuiScreen screen = clientScreenProvider.apply(GadgetGeneric.getGadget(player));
        Minecraft.getInstance().displayGuiScreen(screen);
        return screen == null;
    }

    public boolean openContainer(EntityPlayer player, World world, BlockPos pos) {
        return containerOpener == null || !(player instanceof EntityPlayerMP) ? false : containerOpener.open(id, (EntityPlayerMP) player, world, pos);
    }

    public static GuiScreen openScreen(OpenContainer message) {
        if (message.getId().getPath().equals(TEMPLATE_MANAGER.id))
            Minecraft.getInstance().displayGuiScreen(TEMPLATE_MANAGER.commonScreenProvider.apply(message));

        return null;
    }

    private static TemplateManagerContainer getTemplateManagerContainer(EntityPlayer player, TileEntity te) {
        return new TemplateManagerContainer(player.inventory, (TemplateManagerTileEntity) te);
    }

    private static void openGuiContainer(String id, EntityPlayerMP player, Container container, Consumer<PacketBuffer> extraDataWriter) {
        NetworkHooks.openGui(player, new IInteractionObject() {
            @Override
            public boolean hasCustomName() {
                return false;
            }

            @Override
            public ITextComponent getName() {
                return null;
            }

            @Override
            public ITextComponent getCustomName() {
                return null;
            }

            @Override
            public String getGuiID() {
                return String.format("%s:%s", BuildingGadgets.MODID, id);
            }

            @Override
            public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                return container;
            }
        }, extraDataWriter);
    }
}
