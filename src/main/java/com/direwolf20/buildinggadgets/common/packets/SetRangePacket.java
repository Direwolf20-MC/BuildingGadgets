package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.common.items.Gadget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class SetRangePacket {

    private final int range;

    public SetRangePacket() {
        this.range = - 1;
    }

    public SetRangePacket(int range) {
        this.range = range;
    }

    public static SetRangePacket decode(PacketBuffer buffer) {
        return new SetRangePacket(buffer.readInt());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(range);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;

            Gadget.findGadget(player).ifPresent(e -> {
                if (range < 0)
                    e.getGadget().cycleRange(e.getStack(), player);
                else
                    e.getGadget().setRange(e.getStack(), range);
            });

            // TODO: 09/07/2020 Add this back
            //                else if (stack.getItem() instanceof GadgetDestruction)
            //                    GadgetDestruction.switchOverlay(player, stack);
        });

        ctx.get().setPacketHandled(true);
    }
}
