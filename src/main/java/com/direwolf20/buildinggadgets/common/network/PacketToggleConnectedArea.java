package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;
import com.direwolf20.buildinggadgets.common.gadgets.DestructionGadget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleConnectedArea extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketToggleConnectedArea, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleConnectedArea message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;

                AbstractGadget.getGadget(player).ifPresent(gadget -> {
                    AbstractGadget item = (AbstractGadget) gadget.getItem();
                    if (item instanceof ExchangingGadget || item instanceof BuildingGadget || item instanceof DestructionGadget)
                        AbstractGadget.toggleConnectedArea(player, gadget);
                });

            });
            return null;
        }
    }
}