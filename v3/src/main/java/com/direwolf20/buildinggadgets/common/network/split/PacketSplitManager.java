package com.direwolf20.buildinggadgets.common.network.split;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PacketSplitManager {
    public static final int SPLIT_BORDER = 30000;
    private int id;
    private final Map<Class<?>, PacketSplitHandler<?>> classToHandlerMap;
    private final Int2ObjectMap<PacketSplitHandler<?>> idToHandlerMap;

    public PacketSplitManager() {
        this.classToHandlerMap = new IdentityHashMap<>();
        this.idToHandlerMap = new Int2ObjectOpenHashMap<>();
    }

    public <MSG> void registerSplitPacket(Class<MSG> msgClass, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<Context>> handler) {
        PacketEncoder<MSG> splitEncoder = new PacketEncoder<>(encoder, id);
        PacketDecoder<MSG> splitDecoder = new PacketDecoder<>(decoder);
        PacketSplitHandler<MSG> splitHandler = new PacketSplitHandler<>(splitEncoder, splitDecoder, handler);
        classToHandlerMap.put(msgClass, splitHandler);
        idToHandlerMap.put(id++, splitHandler);
    }

    public void sendTo(Object message, ServerPlayerEntity player) {
        send(message, packet -> PacketHandler.sendTo(packet, player));
    }

    public void sendToServer(Object message) {
        send(message, PacketHandler::sendToServer);
    }

    public void send(Object message, PacketTarget target) {
        send(message, packet -> PacketHandler.HANDLER.send(target, packet));
    }

    private void send(Object message, Consumer<SplitPacket> packetConsumer) {
        @SuppressWarnings("unchecked") //it will only ever have been inserted for the correct class!
                PacketSplitHandler<Object> handler = (PacketSplitHandler<Object>) classToHandlerMap.get(message.getClass());
        Preconditions.checkArgument(handler != null, "Cannot send unknown packet " + message + "!");
        handler.splitPackets(message, packetConsumer);
    }

    public void encode(SplitPacket msg, PacketBuffer buf) {
        msg.writeTo(buf);
    }

    public SplitPacket decode(PacketBuffer buf) {
        return SplitPacket.readFrom(buf);
    }

    public void handle(SplitPacket msg, Supplier<Context> ctx) {
        PacketSplitHandler<?> handler = idToHandlerMap.get(msg.getId());
        Preconditions.checkArgument(handler != null, "Cannot handler packet with unknown id " + msg.getId() + "!");
        handler.handleSplit(msg, ctx);
    }

    private static final class PacketSplitHandler<MSG> {
        private final PacketEncoder<MSG> encoder;
        private final PacketDecoder<MSG> decoder;
        private final BiConsumer<MSG, Supplier<Context>> handler;

        private PacketSplitHandler(PacketEncoder<MSG> encoder, PacketDecoder<MSG> decoder, BiConsumer<MSG, Supplier<Context>> handler) {
            this.encoder = encoder;
            this.decoder = decoder;
            this.handler = handler;
        }

        private void handleSplit(SplitPacket msg, Supplier<Context> ctx) {
            Optional<MSG> msgOpt = decoder.decode(msg);
            msgOpt.ifPresent(packet -> {
                handler.accept(packet, ctx);
                ctx.get().setPacketHandled(true);
            });
        }

        private void splitPackets(MSG msg, Consumer<SplitPacket> consumer) {
            encoder.encode(msg).forEach(consumer);
        }
    }

}
