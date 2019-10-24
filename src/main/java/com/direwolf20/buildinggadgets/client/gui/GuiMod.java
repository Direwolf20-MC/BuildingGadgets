package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.gui.blocks.ChargingStationGUI;
import com.direwolf20.buildinggadgets.client.gui.blocks.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.client.gui.components.GuiTextFieldBase;
import com.direwolf20.buildinggadgets.client.gui.materiallist.MaterialListGUI;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.containers.ChargingStationContainer;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ContainerReference;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages.OpenContainer;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Iterator;
import java.util.function.Consumer;

public enum GuiMod {
    COPY(GadgetCopyPaste::getGadget, CopyGUI::new),
    PASTE(GadgetCopyPaste::getGadget, PasteGUI::new),
    DESTRUCTION(GadgetDestruction::getGadget, DestructionGUI::new),
    TEMPLATE_MANAGER(ContainerReference.TEMPLATE_MANAGER_CONTAINER, message -> {
        TileEntity te = Minecraft.getInstance().world.getTileEntity(message.getAdditionalData().readBlockPos());
        return te instanceof TemplateManagerTileEntity
                ? new TemplateManagerGUI((TemplateManagerTileEntity) te, getTemplateManagerContainer(Minecraft.getInstance().player, te), Minecraft.getInstance().player.inventory)
                : null;
    }, (id, player, world, pos) -> {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TemplateManagerTileEntity) {
            openGuiContainer(id, player, getTemplateManagerContainer(player, te), buffer -> buffer.writeBlockPos(pos));
            return true;
        }
        return false;
    }),
    CHARGING_STATION(ContainerReference.CHARGING_STATION_CONTAINER, message -> {
        TileEntity te = Minecraft.getInstance().world.getTileEntity(message.getAdditionalData().readBlockPos());
        return te instanceof ChargingStationTileEntity
                ? new ChargingStationGUI((ChargingStationTileEntity) te, getChargingStationContainer(Minecraft.getInstance().player, te), Minecraft.getInstance().player.inventory)
                : null;
    }, (id, player, world, pos) -> {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ChargingStationTileEntity) {
            openGuiContainer(id, player, getChargingStationContainer(player, te), buffer -> buffer.writeBlockPos(pos));
            return true;
        }
        return false;
    }),

    MATERIAL_LIST(player -> {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (mainhand.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent()) {
            return mainhand;
        }

        ItemStack offhand = player.getHeldItemOffhand();
        if (offhand.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return offhand;

        return ItemStack.EMPTY;
    }, MaterialListGUI::new);

    private static interface IContainerOpener {

        boolean open(String id, ServerPlayerEntity player, World world, BlockPos pos);
    }

    private Function<PlayerEntity, ItemStack> stackReader;
    private Function<ItemStack, ? extends Screen> clientScreenProvider;
    private Function<OpenContainer, ? extends Screen> commonScreenProvider;
    private IContainerOpener containerOpener;
    private String id;

    private GuiMod(Function<PlayerEntity, ItemStack> stackReader, Function<ItemStack, ? extends Screen> clientScreenProvider) {
        this.stackReader = stackReader;
        this.clientScreenProvider = clientScreenProvider;
    }

    private GuiMod(String id, Function<OpenContainer, ? extends Screen> commonScreenProvider, IContainerOpener containerOpener) {
        this.id = id;
        this.commonScreenProvider = commonScreenProvider;
        this.containerOpener = containerOpener;
    }

    // fixme: 1.14 requires this but I'm not sure on how to implement it.
    public static Screen openScreen(Minecraft minecraft, Screen screen) {
        return null;
    }

    public boolean openScreen(PlayerEntity player) {
        if (clientScreenProvider == null)
            return false;

        ItemStack stack = stackReader.apply(player);
        if (stack == null || stack.isEmpty())
            return false;

        Screen screen = clientScreenProvider.apply(stack);
        Minecraft.getInstance().displayGuiScreen(screen);
        return screen == null;
    }

    public boolean openContainer(PlayerEntity player, World world, BlockPos pos) {
        return containerOpener != null && player instanceof ServerPlayerEntity && containerOpener.open(id, (ServerPlayerEntity) player, world, pos);
    }

    private static TemplateManagerContainer getTemplateManagerContainer(PlayerEntity player, TileEntity te) {
        //Be warned you need to manually assign the window id on the server
        //if you don't do this, and just return 0 as you can do on the client, it will sync the player inventory instead...!
        return new TemplateManagerContainer(player instanceof ServerPlayerEntity ? ((ServerPlayerEntity) player).currentWindowId + 1 : 0, player.inventory, (TemplateManagerTileEntity) te);
    }

    private static ChargingStationContainer getChargingStationContainer(PlayerEntity player, TileEntity te) {
        return new ChargingStationContainer(player instanceof ServerPlayerEntity ? ((ServerPlayerEntity) player).currentWindowId + 1 : 0, player.world, te.getPos(), player.inventory, player);
    }

    private static void openGuiContainer(String id, ServerPlayerEntity player, Container container, Consumer<PacketBuffer> extraDataWriter) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return null;
            }

            @Nullable
            @Override
            public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
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

    public static String getLangKeySingle(String name) {
        return LangUtil.getLangKey("gui", "single", name);
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
