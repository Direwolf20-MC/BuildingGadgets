package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.items.GenericGadget;
import com.direwolf20.buildinggadgets.tools.GadgetUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketAnchorKey implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketAnchorKey() {
    }

    public static class Handler implements IMessageHandler<PacketAnchorKey, IMessage> {
        @Override
        public IMessage onMessage(PacketAnchorKey message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketAnchorKey message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (!(heldItem.getItem() instanceof GenericGadget)) {
                heldItem = playerEntity.getHeldItemOffhand();
                if (!(heldItem.getItem() instanceof GenericGadget)) {
                    return;
                }
            }
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof BuildingTool) {
                BuildingTool buildingTool = (BuildingTool) (heldItem.getItem());
                GadgetUtils.anchorBlocks(playerEntity, heldItem);
            } else if (!heldItem.isEmpty() && heldItem.getItem() instanceof ExchangerTool) {
                ExchangerTool exchangerTool = (ExchangerTool) (heldItem.getItem());
                GadgetUtils.anchorBlocks(playerEntity, heldItem);
            } else if (!heldItem.isEmpty() && heldItem.getItem() instanceof GenericGadget) {
                CopyPasteTool.anchorBlocks(playerEntity, heldItem);
            }
        }
    }
}