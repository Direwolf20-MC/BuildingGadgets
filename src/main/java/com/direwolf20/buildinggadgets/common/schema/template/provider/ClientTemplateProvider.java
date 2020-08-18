package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.common.packets.Packets;
import com.direwolf20.buildinggadgets.common.packets.RequestTemplatePacket;
import com.direwolf20.buildinggadgets.common.packets.UpdateTemplatePacket;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class ClientTemplateProvider extends AbsTemplateProvider {
    //caches don't allow null values... We need to remember that a template is not present, so that the render
    //does not request one all the time
    private final Cache<UUID, Optional<Template>> cachedTemplates;

    public ClientTemplateProvider() {
        this.cachedTemplates = CacheBuilder.newBuilder()
                .concurrencyLevel(1) //Only the client Thread will ever modify this
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Optional<Template> getIfAvailable(UUID uuid) {
        Optional<Template> res = getTemplate(uuid);

        if (! res.isPresent())
            Packets.sendToServer(new RequestTemplatePacket(uuid));

        return res;
    }

    @Override
    protected Optional<Template> getTemplate(UUID uuid) {
        return Optional.ofNullable(cachedTemplates.getIfPresent(uuid)).flatMap(Function.identity());
    }

    @Override
    public void setAndUpdateRemote(UUID id, @Nullable Template template, @Nullable PacketTarget sendTarget) {
        if (template != null)
            cachedTemplates.put(id, Optional.of(template));
        else
            cachedTemplates.put(id, Optional.empty());

        if (sendTarget != null)
            UpdateTemplatePacket.send(id, template, sendTarget);

        super.setAndUpdateRemote(id, template, sendTarget);

        cleanUp();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();
        cachedTemplates.cleanUp();
    }
}
