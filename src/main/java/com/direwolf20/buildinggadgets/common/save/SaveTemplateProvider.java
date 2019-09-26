package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateIdAllocated;
import com.direwolf20.buildinggadgets.common.network.packets.SplitPacketUpdateTemplate;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent.GatherLoginPayloadsEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import java.util.UUID;
import java.util.function.Supplier;

public final class SaveTemplateProvider implements ITemplateProvider {
    private final Supplier<TemplateSave> save;

    public SaveTemplateProvider(Supplier<TemplateSave> save) {
        this.save = save;
    }

    public TemplateSave getSave() {
        return save.get();
    }

    @SubscribeEvent
    public void onGatherPayloads(GatherLoginPayloadsEvent event) {
        TemplateSave save = getSave();
        if (save == null) {
            BuildingGadgets.LOG.error("Attempted to gather login payloads, before templates could be loaded! Aborting!");
            return;
        }
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        save.writeAllIds(buffer);
        event.add(buffer, Reference.NETWORK_CHANNEL_ID_LOGIN, Reference.PAYLOAD_CONTEXT_ID_SYNC);
    }

    @Override
    public ITemplate getTemplateForKey(ITemplateKey key) {
        UUID id = key.getTemplateId(this::getFreeId);
        return getSave().getTemplate(id, key::createTemplate);
    }

    @Override
    public void setTemplate(ITemplateKey key, ITemplate template) {
        getSave().setTemplate(key.getTemplateId(this::getFreeId), template);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return false;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key) {
        UUID id = key.getTemplateId(this::getFreeId);
        ITemplate template = getSave().getTemplate(id, key::createTemplate);
        PacketHandler.getSplitManager().send(new SplitPacketUpdateTemplate(id, template), PacketDistributor.ALL.noArg());
        return true;
    }

    public boolean requestRemoteUpdate(ITemplateKey key, ServerPlayerEntity playerEntity) {
        return requestRemoteUpdate(key, PacketDistributor.PLAYER.with(() -> playerEntity));
    }

    public boolean requestRemoteUpdate(ITemplateKey key, PacketTarget target) {
        UUID id = key.getTemplateId(this::getFreeId);
        ITemplate template = getSave().getTemplate(id, key::createTemplate);
        PacketHandler.getSplitManager().send(new SplitPacketUpdateTemplate(id, template), target);
        return true;
    }

    public boolean requestUpdate(ITemplateKey key, ServerPlayerEntity playerEntity) {
        return requestUpdate(key, PacketDistributor.PLAYER.with(() -> playerEntity));
    }

    public boolean requestUpdate(ITemplateKey key, PacketTarget target) {
        UUID id = key.getTemplateId(this::getFreeId);
        PacketHandler.HANDLER.send(target, new PacketRequestTemplate(id));
        return true;
    }

    public void onRemoteIdAllocated(UUID allocated) {
        getSave().getTemplate(allocated);
        onIdAllocated(allocated);//propagate it to everyone
    }

    private UUID getFreeId() {
        UUID freeId = getSave().getFreeUUID();
        onIdAllocated(freeId);
        return freeId;
    }

    private void onIdAllocated(UUID allocatedId) {
        PacketHandler.sendToAllClients(new PacketTemplateIdAllocated(allocatedId));
    }
}
