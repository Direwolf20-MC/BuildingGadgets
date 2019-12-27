package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangingGadget;
import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleFuzzy extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketToggleFuzzy, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleFuzzy message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;

                AbstractGadget.getGadget(player).ifPresent(gadget -> {
                    AbstractGadget item = (AbstractGadget) gadget.getItem();
                    if (item instanceof ExchangingGadget || item instanceof BuildingGadget
                            || (item instanceof DestructionGadget && SyncedConfig.nonFuzzyEnabledDestruction))
                        AbstractGadget.toggleFuzzy(player, gadget);
                });
            });
            return null;
        }
    }
}