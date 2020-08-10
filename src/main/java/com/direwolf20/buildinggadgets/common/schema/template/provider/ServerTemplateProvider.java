package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.common.packets.UpdateTemplatePacket;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class ServerTemplateProvider implements ITemplateProvider {
    private final WeakReference<ServerWorld> world;

    public ServerTemplateProvider(ServerWorld world) {
        this.world = new WeakReference<>(world);
    }

    @Override
    public Optional<Template> getIfAvailable(UUID uuid) {
        return Optional.ofNullable(world.get())
                .map(TemplateWorldSave::getInstance)
                .flatMap(save -> save.getTemplate(uuid));
    }

    @Override
    public void getWhenAvailable(UUID id, Consumer<Optional<Template>> callback) {
        callback.accept(getIfAvailable(id));
    }

    @Override
    public void setAndUpdateRemote(UUID id, @Nullable Template template, @Nullable PacketTarget sendTarget) {
        ServerWorld theWorld = world.get();
        if (theWorld != null) {
            TemplateWorldSave.getInstance(theWorld).setTemplate(id, template);
            if (sendTarget != null)
                UpdateTemplatePacket.send(id, template, sendTarget);
        }
    }
}
