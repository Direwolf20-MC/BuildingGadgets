package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.common.items.Gadget;
import com.direwolf20.buildinggadgets.common.construction.modes.Mode;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class SetModePacket {
    private int modeIndex;

    public SetModePacket() {
        this.modeIndex = -1;
    }
    public SetModePacket(int modeIndex) {
        this.modeIndex = modeIndex;
    }

    public static void encode(SetModePacket msg, PacketBuffer buffer) {
        buffer.writeInt(msg.modeIndex);
    }
    public static SetModePacket decode(PacketBuffer buffer) {
        return new SetModePacket(buffer.readInt());
    }

    public static class Handler {
        public static void handle(final SetModePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player == null)
                    return;

                Optional<ItemStack> stack = Gadget.findGadget(player);
                stack.ifPresent(itemStack -> {
                    Gadget gadget = ((Gadget) itemStack.getItem());

                    if (msg.modeIndex < 0) {
                        gadget.cycleMode(itemStack, player);
                        return;
                    }

                    // Try and ensure we never attempt to get a mode out of it's enum bounds
                    Mode mode;
                    try {
                        mode = gadget.getModes().get(msg.modeIndex);
                    } catch (IndexOutOfBoundsException ignored) {
                        mode = gadget.getModes().get(0);
                    }

                    gadget.setMode(itemStack, mode.getName());
                });

                // TODO: 09/07/2020 Add this back
//                else if (stack.getItem() instanceof GadgetDestruction)
//                    GadgetDestruction.switchOverlay(player, stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
