package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.gui.materiallist.MaterialListGUI;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.utils.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
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

import java.awt.Color;
import java.util.Iterator;
import java.util.function.Consumer;

public enum GuiMod {
    COPY(GadgetCopyPaste::getGadget, CopyGUI::new),
    PASTE(GadgetCopyPaste::getGadget, PasteGUI::new),
    DESTRUCTION(GadgetDestruction::getGadget, DestructionGUI::new),
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
    }),
    MATERIAL_LIST(Template::getTemplate, MaterialListGUI::new);

    private static interface IContainerOpener {
        boolean open(String id, EntityPlayerMP player, World world, BlockPos pos);
    }

    private Function<EntityPlayer, ItemStack> stackReader;
    private Function<ItemStack, GuiScreen> clientScreenProvider;
    private Function<OpenContainer, GuiScreen> commonScreenProvider;
    private IContainerOpener containerOpener;
    private String id;

    private GuiMod(Function<EntityPlayer, ItemStack> stackReader, Function<ItemStack, GuiScreen> clientScreenProvider) {
        this.stackReader = stackReader;
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

        GuiScreen screen = clientScreenProvider.apply(stackReader.apply(player));
        Minecraft.getInstance().displayGuiScreen(screen);
        return screen == null;
    }

    public boolean openContainer(EntityPlayer player, World world, BlockPos pos) {
        return containerOpener == null || !(player instanceof EntityPlayerMP) ? false : containerOpener.open(id, (EntityPlayerMP) player, world, pos);
    }

    public static GuiScreen openScreen(OpenContainer message) {
        if (message.getId().getPath().equals(TEMPLATE_MANAGER.id))
            return TEMPLATE_MANAGER.commonScreenProvider.apply(message);

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
                return String.join(":", Reference.MODID, id);
            }

            @Override
            public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                return container;
            }
        }, extraDataWriter);
    }

    public static String getLangKeyField(String prefix, String name) {
        return getLangKey(prefix, "field", name);
    }

    public static String getLangKeyButton(String prefix, String name) {
        return getLangKey(prefix, "button", name);
    }

    public static String getLangKeyArea(String prefix, String name) {
        return getLangKey(prefix, "area", name);
    }

    public static String getLangKey(String prefix, String type, String name) {
        return LangUtil.getLangKey("gui", prefix, type, name);
    }

    public static void setEmptyField(GuiTextFieldBase field, Supplier<Integer> value) {
        if (field.getText().isEmpty())
            field.setText(String.valueOf(value.get()));
    }

    public static boolean sizeCheckBoxes(Iterator<GuiTextFieldBase> fields, int min, int max) {
        while (fields.hasNext()) {
            int n = fields.next().getInt();
            if (n < min || n > max)
                return false;
        }
        return true;
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
