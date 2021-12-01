package com.direwolf20.buildinggadgets.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class UUIDPacket {
    private final UUID id;

    public UUIDPacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID());
    }

    public UUIDPacket(UUID id) {
        this.id = id;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
    }

    public UUID getId() {
        return id;
    }
}
