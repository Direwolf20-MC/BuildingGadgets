package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangerGadget;
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
                ItemStack stack = AbstractGadget.getGadget(player);
                AbstractGadget item = (AbstractGadget) stack.getItem();
                if (item instanceof ExchangerGadget || item instanceof BuildingGadget
                        || (item instanceof DestructionGadget && SyncedConfig.nonFuzzyEnabledDestruction))
                    item.toggleFuzzy(player, stack);
            });
            return null;
        }
    }
}