package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCopyCoords implements IMessage {

    private BlockPos start;
    private BlockPos end;

    @Override
    public void fromBytes(ByteBuf buf) {
        start = BlockPos.fromLong(buf.readLong());
        end = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(start.toLong());
        buf.writeLong(end.toLong());
    }

    public PacketCopyCoords() {

    }

    public PacketCopyCoords(BlockPos startPos, BlockPos endPos) {
        start = startPos;
        end = endPos;
    }

    public static class Handler implements IMessageHandler<PacketCopyCoords, IMessage> {
        @Override
        public IMessage onMessage(PacketCopyCoords message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCopyCoords message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            ItemStack heldItem = GadgetCopyPaste.getGadget(playerEntity);
            if (heldItem.isEmpty()) return;

            BlockPos startPos = message.start;
            BlockPos endPos = message.end;
            GadgetCopyPaste tool = ModItems.gadgetCopyPaste;
            if (startPos.equals(BlockPos.ORIGIN) && endPos.equals(BlockPos.ORIGIN)) {
                tool.setStartPos(heldItem, null);
                tool.setEndPos(heldItem, null);
                playerEntity.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.areareset").getUnformattedComponentText()), true);
            } else {
                tool.setStartPos(heldItem, startPos);
                tool.setEndPos(heldItem, endPos);
                GadgetCopyPaste.copyBlocks(heldItem, playerEntity, playerEntity.world, tool.getStartPos(heldItem), tool.getEndPos(heldItem));
            }
        }
    }
}