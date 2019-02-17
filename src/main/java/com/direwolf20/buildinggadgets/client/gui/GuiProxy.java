package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.network.FMLPlayMessages;

public class GuiProxy {

    public static final int CopyPasteID = 0;
    public static final int DestructionID = 1;
    public static final int PasteID = 2;

    public static GuiScreen openGui(FMLPlayMessages.OpenContainer msg) {
        EntityPlayer player = Minecraft.getInstance().player;

        BuildingGadgets.LOG.debug(msg.getId().getPath());
        return null;
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TemplateManagerTileEntity) {
            return new TemplateManagerContainer(player.inventory, (TemplateManagerTileEntity) te);
        }


        return null;
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TemplateManagerTileEntity) {
            TemplateManagerTileEntity containerTileEntity = (TemplateManagerTileEntity) te;
            return new TemplateManagerGUI(containerTileEntity, new TemplateManagerContainer(player.inventory, containerTileEntity));
        }
        if (ID == CopyPasteID) {
            if (player.getHeldItemMainhand().getItem() instanceof GadgetCopyPaste)
                return new CopyPasteGUI(player.getHeldItemMainhand());
            else if (player.getHeldItemOffhand().getItem() instanceof GadgetCopyPaste)
                return new CopyPasteGUI(player.getHeldItemOffhand());
            else
                return null;
        } else if (ID == DestructionID) {
            if (player.getHeldItemMainhand().getItem() instanceof GadgetDestruction)
                return new DestructionGUI(player.getHeldItemMainhand());
            else if (player.getHeldItemOffhand().getItem() instanceof GadgetDestruction)
                return new DestructionGUI(player.getHeldItemOffhand());
            else
                return null;
        } else if (ID == PasteID) {
            if (player.getHeldItemMainhand().getItem() instanceof GadgetCopyPaste)
                return new PasteGUI(player.getHeldItemMainhand());
            else if (player.getHeldItemOffhand().getItem() instanceof GadgetCopyPaste)
                return new PasteGUI(player.getHeldItemOffhand());
            else
                return null;
        }
        return null;
    }
}
