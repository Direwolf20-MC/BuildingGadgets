package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUndoKey implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketUndoKey() {
    }

    public static class Handler implements IMessageHandler<PacketUndoKey, IMessage> {
        @Override
        public IMessage onMessage(PacketUndoKey message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUndoKey message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (!(heldItem.getItem() instanceof BuildingTool) && !(heldItem.getItem() instanceof ExchangerTool)) {
                heldItem = playerEntity.getHeldItemOffhand();
                if (!(heldItem.getItem() instanceof BuildingTool) && !(heldItem.getItem() instanceof  ExchangerTool)) {return;}
            }
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof BuildingTool) {
                BuildingTool buildingTool = (BuildingTool) (heldItem.getItem());
                buildingTool.undoBuild(playerEntity);
            }
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof ExchangerTool) {
                ExchangerTool exchangerTool = (ExchangerTool) (heldItem.getItem());
                exchangerTool.toggleFuzzy(playerEntity, heldItem);
            }
        }
    }
}
