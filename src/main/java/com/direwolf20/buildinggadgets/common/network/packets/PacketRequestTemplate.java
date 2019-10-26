package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.template.SimpleTemplateKey;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.UUID;
import java.util.function.Supplier;

public final class PacketRequestTemplate extends UUIDPacket {
    public PacketRequestTemplate(UUID id) {
        super(id);
    }

    public PacketRequestTemplate(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
                ClientProxy.CACHE_TEMPLATE_PROVIDER.requestRemoteUpdate(new SimpleTemplateKey(getId()));
            else
                SaveManager.INSTANCE.getTemplateProvider().requestRemoteUpdate(new SimpleTemplateKey(getId()));
        });
    }
}
