package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestBlockMap {

    private static int uuidLength = 128;

    private String UUID;
    private boolean isTemplate;

    public PacketRequestBlockMap(String ID, boolean isTemplate) {
        this.UUID = ID;
        this.isTemplate = isTemplate;
    }

    public static void encode(PacketRequestBlockMap msg, PacketBuffer buffer) {
        buffer.writeString(msg.UUID);
        buffer.writeBoolean(msg.isTemplate);
    }

    public static PacketRequestBlockMap decode(PacketBuffer buffer) {
        return new PacketRequestBlockMap(buffer.readString(uuidLength), buffer.readBoolean());
    }

    public static class Handler {
        public static void handle(final PacketRequestBlockMap msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if( player == null ) return;

                NBTTagCompound tagCompound = (msg.isTemplate ? WorldSave.getTemplateWorldSave(player.world) : WorldSave.getWorldSave(player.world)).getCompoundFromUUID(msg.UUID);
                if (tagCompound != null)
                    PacketHandler.sendTo(new PacketBlockMap(tagCompound), player);

            });

            ctx.get().setPacketHandled(true);
        }
    }
}
