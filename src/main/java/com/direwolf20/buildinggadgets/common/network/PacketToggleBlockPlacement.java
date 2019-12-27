package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleBlockPlacement extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketToggleBlockPlacement, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleBlockPlacement message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                ItemStack stack = AbstractGadget.getGadget(player);
                if (stack.getItem() instanceof BuildingGadget)
                    BuildingGadget.togglePlaceAtop(player, stack);
            });
            return null;
        }
    }
}