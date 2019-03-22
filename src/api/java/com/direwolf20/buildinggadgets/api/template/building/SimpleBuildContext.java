package com.direwolf20.buildinggadgets.api.template.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Simple implementation of {@link IBuildContext} providing a {@link Builder} for creation.
 */
@Immutable
public final class SimpleBuildContext implements IBuildContext {
    /**
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param context The context to copy.
     * @return A new {@link Builder} with all values copied from the specified {@link IBuildContext}.
     */
    public static Builder builderOf(IBuildContext context) {
        return builder().world(context.getWorld()).buildingPlayer(context.getBuildingPlayer());
    }

    @Nullable
    private final IWorld world;
    @Nullable
    private final EntityPlayer buildingPlayer;

    private SimpleBuildContext(@Nullable IWorld world, @Nullable EntityPlayer buildingPlayer) {
        this.world = world;
        this.buildingPlayer = buildingPlayer;
    }

    /**
     * @return The {@link IWorld} of this {@code SimpleBuildContext}. Will not be null.
     */
    @Override
    public IWorld getWorld() {
        return world;
    }

    /**
     * @return The {@link EntityPlayer} performing the build. May be null if unknown.
     */
    @Nullable
    @Override
    public EntityPlayer getBuildingPlayer() {
        return buildingPlayer;
    }

    /**
     * {@code Builder} for creating new instances of {@link SimpleBuildContext}
     */
    public static final class Builder {
        @Nonnull
        private IWorld world;
        @Nullable
        private EntityPlayer buildingPlayer;

        private Builder() {
            this.world = null;
            this.buildingPlayer = null;
        }

        /**
         * Sets the {@link IWorld} of the resulting {@link SimpleBuildContext}.
         * @param world The {@link IWorld} of the resulting {@link SimpleBuildContext}.
         * @return The {@code Builder} itself
         * @see SimpleBuildContext#getWorld()
         */
        public Builder world(IWorld world) {
            this.world = Objects.requireNonNull(world);
            return this;
        }

        /**
         * Sets the {@link EntityPlayer} of the resulting {@link SimpleBuildContext}.
         * @param buildingPlayer The {@link EntityPlayer} of the resulting {@link SimpleBuildContext}.
         * @return The {@code Builder} itself
         * @see SimpleBuildContext#getBuildingPlayer()
         */
        public Builder buildingPlayer(@Nullable EntityPlayer buildingPlayer) {
            this.buildingPlayer = buildingPlayer;
            return this;
        }

        /**
         * Creates a new {@link SimpleBuildContext} using the world previously set on this {@code Builder}.
         * @return A new {@link SimpleBuildContext} with the values specified in this {@code Builder}.
         * @see #build(IWorld)
         */
        public SimpleBuildContext build() {
            return build(null);
        }

        /**
         * Creates a new {@link SimpleBuildContext} using the specified world. If the given world is null, the world in this {@code Builder} will be used.
         * @param world The {@link IWorld} to use. If null this {@code Builder}'s world will be used.
         * @return A new {@link SimpleBuildContext} with the values specified in this {@code Builder}.
         * @throws NullPointerException if both the {@link IWorld} passed in and the {@link IWorld} of this {@code Builder} are null.
         */
        public SimpleBuildContext build(@Nullable IWorld world) {
            return new SimpleBuildContext(world != null ? world : Objects.requireNonNull(this.world), buildingPlayer);
        }
    }
}
