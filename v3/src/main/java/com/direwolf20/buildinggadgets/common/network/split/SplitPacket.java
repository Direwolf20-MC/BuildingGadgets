package com.direwolf20.buildinggadgets.common.network.split;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public final class SplitPacket {
    private final int id;
    private final int index;
    private final short sessionId;
    private final boolean hasMore;
    private final PacketBuffer payload;

    static SplitPacket readFrom(PacketBuffer buffer) {
        int id = buffer.readVarInt();
        int index = buffer.readVarInt();
        short sessionId = buffer.readShort();
        boolean hasMore = buffer.readBoolean();
        PacketBuffer payload = new PacketBuffer(Unpooled.buffer(buffer.readableBytes(), Integer.MAX_VALUE));
        buffer.readBytes(payload);
        return new SplitPacket(id, index, sessionId, hasMore, payload);
    }

    SplitPacket(int id, int index, short sessionId, boolean hasMore, PacketBuffer payload) {
        this.id = id;
        this.index = index;
        this.sessionId = sessionId;
        this.hasMore = hasMore;
        this.payload = payload;
    }

    void writeTo(PacketBuffer buffer) {
        buffer.writeVarInt(id);
        buffer.writeVarInt(index);
        buffer.writeShort(sessionId);
        buffer.writeBoolean(hasMore);
        buffer.writeBytes(payload);
    }

    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public short getSessionId() {
        return sessionId;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public PacketBuffer getPayload() {
        return payload;
    }
}
