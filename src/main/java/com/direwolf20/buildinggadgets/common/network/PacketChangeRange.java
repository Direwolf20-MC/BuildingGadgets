package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.*;
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
            AbstractGadget.getGadget(playerEntity).ifPresent(gadget -> {
                if (message.range >= 0)
                    GadgetUtils.setToolRange(gadget, message.range);
                else if (gadget.getItem() instanceof BuildingGadget) {
                    BuildingGadget buildingGadget = (BuildingGadget) (gadget.getItem());
                    buildingGadget.rangeChange(playerEntity, gadget);
                } else if (gadget.getItem() instanceof ExchangingGadget) {
                    ExchangingGadget exchangingGadget = (ExchangingGadget) (gadget.getItem());
                    exchangingGadget.rangeChange(playerEntity, gadget);
                } else if (gadget.getItem() instanceof DestructionGadget) {
                    DestructionGadget destructionGadget = (DestructionGadget) (gadget.getItem());
                    destructionGadget.switchOverlay(playerEntity, gadget);
                }
            });
        }
    }
}