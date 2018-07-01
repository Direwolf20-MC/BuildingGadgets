package com.direwolf20.buildinggadgets.Network;

import com.direwolf20.buildinggadgets.Items.BuildingTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleMode implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketToggleMode() {
    }

    public static class Handler implements IMessageHandler<PacketToggleMode, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleMode message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketToggleMode message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof BuildingTool) {
                BuildingTool buildingTool = (BuildingTool) (heldItem.getItem());
                buildingTool.toggleMode(playerEntity, heldItem);
            }
        }
    }
}
