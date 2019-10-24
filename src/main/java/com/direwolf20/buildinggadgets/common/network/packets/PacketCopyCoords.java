package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
            ServerPlayerEntity playerEntity = ctx.get().getSender();
            if( playerEntity == null ) return;

            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetCopyPaste.getGadget(playerEntity);
                if (heldItem.isEmpty()) return;

                BlockPos startPos = msg.start;
                BlockPos endPos = msg.end;
                GadgetCopyPaste tool = OurItems.gadgetCopyPaste;
                if (startPos.equals(BlockPos.ZERO) && endPos.equals(BlockPos.ZERO)) {
                    tool.setStartPos(heldItem, null);
                    tool.setEndPos(heldItem, null);
                    playerEntity.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.areareset").getUnformattedComponentText()), true);
                } else {
                    tool.setStartPos(heldItem, startPos);
                    tool.setEndPos(heldItem, endPos);
                    OurItems.gadgetCopyPaste.copyBlocks(heldItem, playerEntity, playerEntity.world, tool.getStartPos(heldItem), tool.getEndPos(heldItem));
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}