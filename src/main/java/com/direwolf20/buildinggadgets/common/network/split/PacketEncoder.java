package com.direwolf20.buildinggadgets.common.network.split;

import com.google.common.collect.AbstractIterator;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.BiConsumer;

import static com.direwolf20.buildinggadgets.common.network.split.PacketSplitManager.SPLIT_BORDER;

final class PacketEncoder<MSG> {
    private final BiConsumer<MSG, FriendlyByteBuf> messageEncoder;
    private final int id;
    private short curSession;

    PacketEncoder(BiConsumer<MSG, FriendlyByteBuf> messageEncoder, int id) {
        this.messageEncoder = messageEncoder;
        this.id = id;
        this.curSession = 0;
    }

    Iterable<SplitPacket> encode(MSG msg) {
        return new Iterable<SplitPacket>() { //save some memory by evaluating the copied buffers lazily
            private short curSession = PacketEncoder.this.curSession++;

            @Override
            @Nonnull
            public Iterator<SplitPacket> iterator() {
                return new AbstractIterator<SplitPacket>() {
                    private FriendlyByteBuf messageBuffer = new FriendlyByteBuf(Unpooled.buffer(Short.MAX_VALUE, Integer.MAX_VALUE));
                    private int index = 0;

                    {
                        messageBuffer.markReaderIndex();
                        messageBuffer.markWriterIndex();
                        messageEncoder.accept(msg, messageBuffer);
                        messageBuffer.resetReaderIndex();
                    }

                    @Override
                    protected SplitPacket computeNext() {
                        if (messageBuffer == null)
                            return endOfData();
                        FriendlyByteBuf payload;
                        boolean hasMore = messageBuffer.readableBytes() > SPLIT_BORDER;
                        if (hasMore) {
                            payload = new FriendlyByteBuf(messageBuffer.copy(messageBuffer.readerIndex(), SPLIT_BORDER));
                            messageBuffer.readerIndex(messageBuffer.readerIndex() + SPLIT_BORDER);
                        } else {
                            payload = new FriendlyByteBuf(messageBuffer.copy());
                            messageBuffer = null; //we are finished - hand it to the GC
                        }
                        return new SplitPacket(id, index++, curSession, hasMore, payload);
                    }
                };
            }
        };
    }
}
