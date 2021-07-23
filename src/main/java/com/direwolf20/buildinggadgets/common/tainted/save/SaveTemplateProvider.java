package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.packets.SplitPacketUpdateTemplate;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider.IUpdateListener;

public final class SaveTemplateProvider implements ITemplateProvider {
    private final Supplier<TemplateSave> save;
    private final Set<IUpdateListener> updateListeners;

    public SaveTemplateProvider(Supplier<TemplateSave> save) {
        this.save = save;
        this.updateListeners = Collections.newSetFromMap(new WeakHashMap<>());
    }

    public TemplateSave getSave() {
        return save.get();
    }

    @Override
    public Template getTemplateForKey(ITemplateKey key) {
        UUID id = getId(key);
        return getSave().getTemplate(id);
    }

    @Override
    public void setTemplate(ITemplateKey key, Template template) {
        getSave().setTemplate(key.getTemplateId(this::getFreeId), template);
        notifyListeners(key, template, l -> l::onTemplateUpdate);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return false;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key) {
        UUID id = getId(key);
        Template template = getSave().getTemplate(id);
        notifyListeners(key, template, l -> l::onTemplateUpdateSend);
        PacketHandler.getSplitManager().send(new SplitPacketUpdateTemplate(id, template), PacketDistributor.ALL.noArg());
        return true;
    }

    @Override
    public void registerUpdateListener(IUpdateListener listener) {
        updateListeners.add(listener);
    }

    @Override
    public void removeUpdateListener(IUpdateListener listener) {
        updateListeners.remove(listener);
    }

    @Override
    public UUID getId(ITemplateKey key) {
        return key.getTemplateId(this::getFreeId);
    }

    public boolean requestRemoteUpdate(ITemplateKey key, ServerPlayer playerEntity) {
        return requestRemoteUpdate(key, PacketDistributor.PLAYER.with(() -> playerEntity));
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, PacketTarget target) {
        UUID id = getId(key);
        Template template = getSave().getTemplate(id);
        PacketHandler.getSplitManager().send(new SplitPacketUpdateTemplate(id, template), target);
        return true;
    }

    @Override
    public boolean requestUpdate(ITemplateKey key, PacketTarget target) {
        UUID id = getId(key);
        PacketHandler.HANDLER.send(target, new PacketRequestTemplate(id));
        return true;
    }

    public void onRemoteIdAllocated(UUID allocated) {
        getSave().getTemplate(allocated);
    }

    private UUID getFreeId() {
        UUID freeId = getSave().getFreeUUID();
        return freeId;
    }

    private void notifyListeners(ITemplateKey key, Template template, Function<IUpdateListener, TriConsumer<ITemplateProvider, ITemplateKey, Template>> function) {
        for (IUpdateListener listener : updateListeners) {
            try {
                function.apply(listener).accept(this, key, template);
            } catch (Exception e) {
                BuildingGadgets.LOG.error("Update listener threw an exception!", e);
            }
        }
    }
}
