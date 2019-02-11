package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
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
                ItemStack stack = GadgetGeneric.getGadget(player);
                GadgetGeneric item = (GadgetGeneric) stack.getItem();
                if (item instanceof GadgetExchanger || item instanceof GadgetBuilding || item instanceof GadgetDestruction) {
                    item.toggleFuzzy(player, stack);
                }
            });
            return null;
        }
    }
}