package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capbility.TemplateProviderCapability;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import com.direwolf20.buildinggadgets.common.schema.template.TemplateIO;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class UpdateTemplatePacket extends RequestTemplatePacket {
    private static final int SPLIT_SIZE = 30000;//split every 30000 bytes - this is below the client limit
    private static final AtomicInteger curSession = new AtomicInteger();
    //null UUID indicates server-origin assembly
    private static final LoadingCache<Optional<UUID>, PacketAssembly> assemblyByPlayerId = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<Optional<UUID>, PacketAssembly>() {
                @Override
                public PacketAssembly load(@Nonnull Optional<UUID> key) {
                    return new PacketAssembly();
                }
            });

    public static void send(UUID id, @Nullable Template template, PacketTarget target) {
        Optional.ofNullable(template)
                .flatMap(TemplateIO::getCompressedBytes)
                .map(b -> {
                    int remainingBytes = b.length;
                    int seqNumber = 0;
                    while (remainingBytes > 0) {
                        int size = Math.min(remainingBytes, SPLIT_SIZE);
                        remainingBytes -= size;
                        byte[] payload = new byte[size];
                        System.arraycopy(b, seqNumber * SPLIT_SIZE, payload, 0, size);
                        Packets.INSTANCE.send(target, new UpdateTemplatePacket(id, curSession.getAndIncrement(),
                                payload, seqNumber++, remainingBytes > 0));
                    }
                    return null;
                })
                .orElseGet(() -> {
                    Packets.INSTANCE.send(target,
                            new UpdateTemplatePacket(id, curSession.getAndIncrement(), null, 0, false));
                    return null;
                });
    }

    @Nullable
    private final byte[] payload;
    private final boolean hasMore;
    private final int session;
    private final int seqNumber;

    public UpdateTemplatePacket(UUID id, int curSession, @Nullable byte[] payload, int seqNumber, boolean hasMore) {
        super(id);
        this.session = curSession;
        this.payload = payload;
        this.seqNumber = seqNumber;
        this.hasMore = hasMore;
    }

    public UpdateTemplatePacket(PacketBuffer buffer) {
        super(buffer);
        //ensure that it actually reads the size it wrote - the no-arg version reads the whole buffer!!!
        session = buffer.readInt();
        if (buffer.readBoolean()) {
            payload = buffer.readByteArray(SPLIT_SIZE);
            seqNumber = buffer.readVarInt();
        } else {
            payload = null;
            seqNumber = - 1;
        }
        hasMore = buffer.readBoolean();
    }

    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeInt(session);
        buffer.writeBoolean(payload != null);
        if (payload != null)
            buffer.writeByteArray(payload);
    }

    public void handle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                PacketAssembly assembly = assemblyByPlayerId.get(Optional.ofNullable(ctx.get().getSender()).map(Entity::getUniqueID));
                Supplier<Supplier<World>> loadSafeWorld = ctx.get().getDirection().getReceptionSide().isClient() ?
                        () -> () -> net.minecraft.client.Minecraft.getInstance().world :
                        () -> () -> ctx.get().getSender().getServerWorld();
                assembly.assembleAndSetTemplate(this, loadSafeWorld);
            } catch (Exception e) {
                BuildingGadgets.LOGGER.error("Unexpected exception assembling packet {}.", this, e);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private int getPayloadSize() {
        return payload == null ? - 1 : payload.length;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("session", session)
                .add("seqNumber", seqNumber)
                .add("payloadSize", payload != null ? payload.length : - 1)
                .toString();
    }

    private static final class PacketAssembly {
        private final Int2ObjectMap<List<UpdateTemplatePacket>> toAssemble;

        public PacketAssembly() {
            this.toAssemble = new Int2ObjectOpenHashMap<>();
        }

        public void assembleAndSetTemplate(UpdateTemplatePacket packet, Supplier<Supplier<World>> world) {
            List<UpdateTemplatePacket> inSession = toAssemble.computeIfAbsent(packet.session, i -> new LinkedList<>());
            addPacketAtIndex(packet, inSession);
            if (checkComplete(inSession))
                world.get().get().getCapability(TemplateProviderCapability.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(p ->
                        p.setAndUpdateRemote(inSession.get(0).getId(), reassemble(inSession), null));
        }

        private void addPacketAtIndex(UpdateTemplatePacket packet, List<UpdateTemplatePacket> inSession) {
            ListIterator<UpdateTemplatePacket> it = inSession.listIterator();
            while (it.hasNext() && packet != null) {
                UpdateTemplatePacket curPacket = it.next();
                if (packet.seqNumber < curPacket.seqNumber) {
                    it.set(packet);
                    it.add(curPacket);
                    packet = null;
                }
            }
            if (packet != null)
                inSession.add(packet);
        }

        private boolean checkComplete(List<UpdateTemplatePacket> inSession) {
            if (inSession.get(inSession.size() - 1).hasMore)
                return false;
            int index = 0;
            for (UpdateTemplatePacket packet : inSession) {
                if (packet.seqNumber != index++)
                    return false;
            }
            return true;
        }

        private Template reassemble(List<UpdateTemplatePacket> inSession) {
            int length = inSession.stream().mapToInt(UpdateTemplatePacket::getPayloadSize).sum();
            if (length < 0) //single packet with no payload
                return null;
            byte[] data = new byte[length];
            int read = 0;
            for (UpdateTemplatePacket packet : inSession) {
                assert packet.payload != null;
                System.arraycopy(packet.payload, 0, data, read, packet.payload.length);
                read += packet.payload.length;
            }
            assert read == data.length;
            try {
                CompoundNBT nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(data));
                return Template.deserializeNBT(nbt).orElse(null);
            } catch (IOException e) {
                BuildingGadgets.LOGGER.error("Could not reassemble Template due to unexpected Exception!", e);
                return null;
            }
        }
    }
}
