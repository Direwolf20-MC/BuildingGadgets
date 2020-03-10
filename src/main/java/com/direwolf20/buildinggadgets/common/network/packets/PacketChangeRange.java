package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeRange {
    private int range;

    public PacketChangeRange() {
        range = -1;
    }

    public PacketChangeRange(int range) {
        this.range = range;
    }

    public static void encode(PacketChangeRange msg, PacketBuffer buffer) {
        buffer.writeInt(msg.range);
    }
    public static PacketChangeRange decode(PacketBuffer buffer) {
        return new PacketChangeRange(buffer.readInt());
    }

    public static class Handler {
        public static void handle(final PacketChangeRange msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (msg.range >= 0)
                    GadgetUtils.setToolRange(stack, msg.range);
                else if (stack.getItem() instanceof GadgetBuilding)
                    GadgetBuilding.rangeChange(player, stack);
                else if (stack.getItem() instanceof GadgetExchanger)
                    GadgetExchanger.rangeChange(player, stack);
                else if (stack.getItem() instanceof GadgetDestruction)
                    GadgetDestruction.switchOverlay(player, stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}