package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.UUID;
import java.util.function.Supplier;

public final class PacketTemplateIdAllocated extends UUIDPacket {
    public PacketTemplateIdAllocated(PacketBuffer buffer) {
        super(buffer);
    }

    public PacketTemplateIdAllocated(UUID id) {
        super(id);
    }

    public void handle(Supplier<Context> contextSupplier) {
        Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
                ClientProxy.CACHE_TEMPLATE_PROVIDER.onRemoteIdAllocated(getId());
            else
                SaveManager.INSTANCE.getTemplateProvider().onRemoteIdAllocated(getId());
        });
        ctx.setPacketHandled(true);
    }
}
