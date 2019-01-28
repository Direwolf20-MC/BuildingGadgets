package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCopyCoords {

    private final BlockPos start;
    private final BlockPos end;

    public PacketCopyCoords(BlockPos start, BlockPos end) {
        this.start = start;
        this.end = end;
    }

    public static void encode(PacketCopyCoords msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.start);
        buffer.writeBlockPos(msg.end);
    }

    public static PacketCopyCoords decode(PacketBuffer buffer) {
        return new PacketCopyCoords(buffer.readBlockPos(), buffer.readBlockPos());
    }

    public static class Handler {
//        @Override
//        public IMessage onMessage(PacketCopyCoords message, MessageContext ctx) {
//            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
//            return null;
//        }

        public static void handle(final PacketCopyCoords msg, Supplier<NetworkEvent.Context> ctx) {
            EntityPlayerMP playerEntity = ctx.get().getSender();
            if( playerEntity == null ) return;

            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetCopyPaste.getGadget(playerEntity);
                if (heldItem.isEmpty()) return;

                BlockPos startPos = msg.start;
                BlockPos endPos = msg.end;
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
            });

            ctx.get().setPacketHandled(true);
        }
    }
}