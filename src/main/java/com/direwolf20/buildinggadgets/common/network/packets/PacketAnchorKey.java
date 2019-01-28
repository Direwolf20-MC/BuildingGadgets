package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketAnchorKey {

    public static void encode(PacketAnchorKey msg, PacketBuffer buf) {}
    public static PacketAnchorKey decode(PacketBuffer buf) { return null; }

    public static class Handler
    {
        public static void handle(final PacketAnchorKey msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP playerEntity = ctx.get().getSender();
                if( playerEntity == null ) return;

                ItemStack heldItem = GadgetGeneric.getGadget(playerEntity);
                if (heldItem.isEmpty())
                    return;

                if (heldItem.getItem() instanceof GadgetBuilding) {
                    GadgetUtils.anchorBlocks(playerEntity, heldItem);
                } else if (heldItem.getItem() instanceof GadgetExchanger) {
                    GadgetUtils.anchorBlocks(playerEntity, heldItem);
                } else if (heldItem.getItem() instanceof GadgetCopyPaste) {
                    GadgetCopyPaste.anchorBlocks(playerEntity, heldItem);
                } else if (heldItem.getItem() instanceof GadgetDestruction) {
                    GadgetDestruction.anchorBlocks(playerEntity, heldItem);
                }
            });
        }
    }

}