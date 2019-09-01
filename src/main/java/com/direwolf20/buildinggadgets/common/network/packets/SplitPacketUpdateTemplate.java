package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.TemplateIO;
import com.direwolf20.buildinggadgets.api.template.provider.SimpleTemplateKey;
import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

public final class SplitPacketUpdateTemplate extends UUIDPacket {
    private final ITemplate template;

    public SplitPacketUpdateTemplate(PacketBuffer buffer) {
        super(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        try {
            template = TemplateIO.readTemplate(new ByteArrayInputStream(bytes), null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Template from buffer!", e);
        }
    }

    public SplitPacketUpdateTemplate(UUID id, ITemplate template) {
        super(id);
        this.template = template;
    }

    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            TemplateIO.writeTemplate(template, stream);
            buffer.writeBytes(stream.toByteArray());
        } catch (IOException e) {
            BuildingGadgets.LOG.error("Failed to write Template during Packet Encoding!", e);
        }
    }

    public void handle(Supplier<Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
                ClientProxy.CACHE_TEMPLATE_PROVIDER.setTemplate(new SimpleTemplateKey(getId()), template);
            else
                SaveManager.INSTANCE.getTemplateProvider().setTemplate(new SimpleTemplateKey(getId()), template);
        });
    }
}
