package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleRayTraceFluid extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketToggleRayTraceFluid, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleRayTraceFluid message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                ItemStack stack = GadgetGeneric.getGadget(player);
                if (!stack.isEmpty())
                    GadgetGeneric.toggleRayTraceFluid(player, stack);
            });
            return null;
        }
    }
}