package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.packets.UpdateTemplatePacket;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

public final class ServerTemplateProvider extends AbsTemplateProvider {
    private final WeakReference<ServerWorld> world;

    public ServerTemplateProvider(ServerWorld world) {
        super();
        this.world = new WeakReference<>(world);
    }

    @Override
    public Optional<Template> getIfAvailable(UUID uuid) {
        return getTemplate(uuid);
    }

    @Override
    protected Optional<Template> getTemplate(UUID uuid) {
        return Optional.ofNullable(world.get())
                .map(TemplateWorldSave::getInstance)
                .flatMap(save -> save.getTemplate(uuid));
    }

    @Override
    public void setAndUpdateRemote(UUID id, @Nullable Template template, @Nullable PacketTarget sendTarget) {
        ServerWorld theWorld = world.get();
        if (theWorld != null) {
            TemplateWorldSave.getInstance(theWorld).setTemplate(id, template);
            if (sendTarget != null)
                UpdateTemplatePacket.send(id, template, sendTarget);
            super.setAndUpdateRemote(id, template, sendTarget);
        } else
            BuildingGadgets.LOGGER.warn("Attempted to set template with id {} on an unloaded world!!!", id);
    }
}
