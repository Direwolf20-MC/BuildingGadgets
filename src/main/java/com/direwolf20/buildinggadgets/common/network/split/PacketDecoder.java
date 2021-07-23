package com.direwolf20.buildinggadgets.common.network.split;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

final class PacketDecoder<MSG> {
    private final Cache<Short, PendingPacket> pendingPackets;
    private final Function<FriendlyByteBuf, MSG> decoder;

    PacketDecoder(Function<FriendlyByteBuf, MSG> decoder) {
        pendingPackets = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
        this.decoder = decoder;
    }

    Optional<MSG> decode(SplitPacket packet) {
        try {
            return pendingPackets.get(packet.getSessionId(), PendingPacket::new).add(packet);
        } catch (Exception e) {
            BuildingGadgets.LOG.error("An error occurred whilst assembling packet {} ins session {} with index {} which {}. Discarding.",
                    packet.getId(), packet.getSessionId(), packet.getIndex(), packet.hasMore() ? "has follow up packets" : "no follow up packets", e);
            pendingPackets.invalidate(packet.getSessionId());
            return Optional.empty();
        }
    }

    private final class PendingPacket {
        private final List<SplitPacket> partialPackets;
        private boolean receivedLast;

        private PendingPacket() {
            partialPackets = new LinkedList<>();
        }

        private Optional<MSG> add(SplitPacket packet) {
            if (! packet.hasMore())
                receivedLast = true;
            ListIterator<SplitPacket> it = partialPackets.listIterator();
            while (it.hasNext() && packet != null) {
                SplitPacket curPacket = it.next();
                if (packet.getIndex() < curPacket.getIndex()) {
                    it.set(packet);
                    it.add(curPacket);
                    packet = null;
                }
            }
            if (packet != null)
                partialPackets.add(packet);
            if (receivedLast)
                return checkComplete();
            return Optional.empty();
        }

        private Optional<MSG> checkComplete() {
            int index = 0;
            for (SplitPacket packet : partialPackets) {
                if (packet.getIndex() != index++)
                    return Optional.empty();
            }
            return assemble();
        }

        private Optional<MSG> assemble() {
            FriendlyByteBuf payloadBuffer = new FriendlyByteBuf(Unpooled.buffer(Short.MAX_VALUE, Integer.MAX_VALUE));
            for (SplitPacket packet : partialPackets) {
                FriendlyByteBuf payload = packet.getPayload();
                payloadBuffer.writeBytes(payload);
            }
            return Optional.of(decoder.apply(payloadBuffer));
        }

    }
}
