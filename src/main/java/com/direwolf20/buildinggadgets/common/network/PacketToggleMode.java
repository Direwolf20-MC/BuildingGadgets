package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangerGadget;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleMode implements IMessage {

    private int mode;

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(mode);
    }

    public PacketToggleMode() {
    }

    public PacketToggleMode(int modeInt) {
        mode = modeInt;
    }

    public static class Handler implements IMessageHandler<PacketToggleMode, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleMode message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketToggleMode message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = AbstractGadget.getGadget(playerEntity);
            if (heldItem.isEmpty())
                return;

            if (heldItem.getItem() instanceof BuildingGadget) {
                BuildingGadget buildingGadget = (BuildingGadget) (heldItem.getItem());
                buildingGadget.setMode(playerEntity, heldItem, message.mode);
            } else if (heldItem.getItem() instanceof ExchangerGadget) {
                ExchangerGadget exchangerGadget = (ExchangerGadget) (heldItem.getItem());
                exchangerGadget.setMode(playerEntity, heldItem, message.mode);
            } else if (heldItem.getItem() instanceof CopyGadget) {
                CopyGadget copyGadget = (CopyGadget) (heldItem.getItem());
                copyGadget.setMode(playerEntity, heldItem, message.mode);
            }
        }
    }
}
