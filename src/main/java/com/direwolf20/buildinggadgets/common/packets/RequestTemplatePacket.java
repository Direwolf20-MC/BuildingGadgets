package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.common.capbility.TemplateProviderCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestTemplatePacket {
    private final UUID id;

    public RequestTemplatePacket(@Nonnull UUID id) {
        this.id = id;
    }

    public RequestTemplatePacket(PacketBuffer buffer) {
        this(buffer.readUniqueId());
    }

    public void encode(PacketBuffer target) {
        target.writeUniqueId(id);
    }

    public void handle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            assert ctx.get().getDirection().getReceptionSide().isServer();
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null)
                return;

            sender.getServerWorld().getCapability(TemplateProviderCapability.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(p ->
                    p.setAndUpdateRemote(getId(), p.getIfAvailable(getId()).orElse(null),
                            PacketDistributor.PLAYER.with(() -> sender)));
        });
        ctx.get().setPacketHandled(true);
    }

    public UUID getId() {
        return id;
    }
}
