package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public final class PacketRequestTemplate extends UUIDPacket {
    public PacketRequestTemplate(UUID id) {
        super(id);
    }

    public PacketRequestTemplate(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
                ClientProxy.CACHE_TEMPLATE_PROVIDER.requestRemoteUpdate(new TemplateKey(getId()));
            else
                SaveManager.INSTANCE.getTemplateProvider().requestRemoteUpdate(new TemplateKey(getId()));
        });

        contextSupplier.get().setPacketHandled(true);
    }
}
