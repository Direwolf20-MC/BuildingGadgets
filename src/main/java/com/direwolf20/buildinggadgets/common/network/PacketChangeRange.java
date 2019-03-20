package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketChangeRange implements IMessage {
    private int range;

    public PacketChangeRange() {
        range = -1;
    }

    public PacketChangeRange(int range) {
        this.range = range;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        range = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(range);
    }

    public static class Handler implements IMessageHandler<PacketChangeRange, IMessage> {
        @Override
        public IMessage onMessage(PacketChangeRange message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketChangeRange message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = GadgetGeneric.getGadget(playerEntity);
            if (heldItem.isEmpty())
                return;

            if (message.range >= 0)
                GadgetUtils.setToolRange(heldItem, message.range);
            else if (heldItem.getItem() instanceof GadgetBuilding) {
                GadgetBuilding gadgetBuilding = (GadgetBuilding) (heldItem.getItem());
                gadgetBuilding.rangeChange(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetExchanger) {
                GadgetExchanger gadgetExchanger = (GadgetExchanger) (heldItem.getItem());
                gadgetExchanger.rangeChange(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetDestruction) {
                GadgetDestruction gadgetDestruction = (GadgetDestruction) (heldItem.getItem());
                gadgetDestruction.switchOverlay(playerEntity, heldItem);
            }
        }
    }
}