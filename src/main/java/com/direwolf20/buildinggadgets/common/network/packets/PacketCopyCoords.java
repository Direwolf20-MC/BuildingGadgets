package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
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
        public static void handle(final PacketCopyCoords msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity playerEntity = ctx.get().getSender();
            if( playerEntity == null ) return;

            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetCopyPaste.getGadget(playerEntity);
                if (heldItem.isEmpty()) return;

                BlockPos startPos = msg.start;
                BlockPos endPos = msg.end;
                if (startPos.equals(BlockPos.ZERO) && endPos.equals(BlockPos.ZERO)) {
                    GadgetCopyPaste.setSelectedRegion(heldItem, null);
                    playerEntity.sendStatusMessage(MessageTranslation.AREA_RESET.componentTranslation().setStyle(Styles.AQUA), true);
                } else {
                    GadgetCopyPaste.setSelectedRegion(heldItem, new Region(startPos, endPos));
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}