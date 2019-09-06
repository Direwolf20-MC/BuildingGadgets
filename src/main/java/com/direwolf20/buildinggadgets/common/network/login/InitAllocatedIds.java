package com.direwolf20.buildinggadgets.common.network.login;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class InitAllocatedIds extends LoginIndexedMessage {
    private final Set<UUID> ids;

    public InitAllocatedIds(Set<UUID> ids) {
        super();
        this.ids = ids;
    }

    public InitAllocatedIds(PacketBuffer buffer) {
        this(new HashSet<>());
        while (buffer.isReadable())
            ids.add(buffer.readUniqueId());
    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        for (UUID id : ids) {
            buffer.writeUniqueId(id);
        }
    }

    public Set<UUID> getIds() {
        return ids;
    }

    public void handleOnClient(Supplier<Context> supplier) {
        supplier.get().enqueueWork(() -> {
            for (UUID id : ids) {
                ClientProxy.CACHE_TEMPLATE_PROVIDER.onRemoteIdAllocated(id);
            }
        });
        supplier.get().setPacketHandled(true);
    }
}
