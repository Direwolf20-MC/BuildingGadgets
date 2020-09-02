package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.common.construction.modes.Mode;
import com.direwolf20.buildinggadgets.common.items.Gadget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.function.Supplier;

public final class SetModePacket {
    private final int modeIndex;

    public SetModePacket() {
        this.modeIndex = -1;
    }

    public SetModePacket(int modeIndex) {
        this.modeIndex = modeIndex;
    }

    public static SetModePacket decode(PacketBuffer buffer) {
        return new SetModePacket(buffer.readInt());
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(modeIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;

            Gadget.findGadget(player).ifPresent(e -> {
                if (modeIndex < 0) {
                    e.getGadget().cycleMode(e.getStack(), player);
                    return;
                }

                // Try and ensure we never attempt to get a mode out of it's enum bounds
                Mode mode = e.getGadget().getModes().get(modeIndex > e.getGadget().getModes().size() ? 0 : modeIndex);
                e.getGadget().setMode(e.getStack(), mode.getName());
            });
        });

        ctx.get().setPacketHandled(true);
    }
}
