package com.direwolf20.buildinggadgets.common.network.split;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public final class SplitPacket {
    private final int id;
    private final int index;
    private final short sessionId;
    private final boolean hasMore;
    private final FriendlyByteBuf payload;

    static SplitPacket readFrom(FriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        int index = buffer.readVarInt();
        short sessionId = buffer.readShort();
        boolean hasMore = buffer.readBoolean();
        FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer(buffer.readableBytes(), Integer.MAX_VALUE));
        buffer.readBytes(payload);
        return new SplitPacket(id, index, sessionId, hasMore, payload);
    }

    SplitPacket(int id, int index, short sessionId, boolean hasMore, FriendlyByteBuf payload) {
        this.id = id;
        this.index = index;
        this.sessionId = sessionId;
        this.hasMore = hasMore;
        this.payload = payload;
    }

    void writeTo(FriendlyByteBuf buffer) {
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

    public FriendlyByteBuf getPayload() {
        return payload;
    }
}
