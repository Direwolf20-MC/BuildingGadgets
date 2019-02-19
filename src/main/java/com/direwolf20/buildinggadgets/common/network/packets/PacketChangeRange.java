package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeRange {

    public static void encode(PacketChangeRange msg, PacketBuffer buffer) {}
    public static PacketChangeRange decode(PacketBuffer buffer) { return new PacketChangeRange(); }

    public static class Handler {
        public static void handle(final PacketChangeRange msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = GadgetGeneric.getGadget(player);
                if (stack.getItem() instanceof GadgetBuilding)
                    GadgetBuilding.rangeChange(player, stack);
                else if (stack.getItem() instanceof GadgetExchanger)
                    GadgetExchanger.rangeChange(player, stack);
                else if (stack.getItem() instanceof GadgetCopyPaste)
                    GadgetCopyPaste.rotateBlocks(stack, player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}