package com.direwolf20.buildinggadgets.api.template.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public final class SimpleBuildContext implements IBuildContext {
    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    private final IWorld world;
    @Nullable
    private final EntityPlayer buildingPlayer;

    public SimpleBuildContext(@Nullable IWorld world, @Nullable EntityPlayer buildingPlayer) {
        this.world = world;
        this.buildingPlayer = buildingPlayer;
    }

    @Nullable
    @Override
    public IWorld getWorld() {
        return world;
    }

    @Nullable
    @Override
    public EntityPlayer getBuildingPlayer() {
        return buildingPlayer;
    }

    public static class Builder {
        @Nullable
        private IWorld world;
        @Nullable
        private EntityPlayer buildingPlayer;

        public Builder() {
            world = null;
            buildingPlayer = null;
        }

        public Builder setWorld(@Nullable IWorld world) {
            this.world = world;
            return this;
        }

        public Builder setBuildingPlayer(@Nullable EntityPlayer buildingPlayer) {
            this.buildingPlayer = buildingPlayer;
            return this;
        }

        public SimpleBuildContext build() {
            return new SimpleBuildContext(world, buildingPlayer);
        }
    }
}
