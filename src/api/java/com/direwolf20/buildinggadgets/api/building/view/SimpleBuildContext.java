package com.direwolf20.buildinggadgets.api.building.view;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
     * @param context The context to copy. If null, then this Method acts as if calling {@link #builder()}.
     * @return A new {@link Builder} with all values copied from the specified {@link IBuildContext}.
     */
    public static Builder builderOf(@Nullable IBuildContext context) {
        Builder builder = builder();
        if (context == null)
            return builder;
        return builder
                .world(context.getWorld())
                .buildingPlayer(context.getBuildingPlayer())
                .usedStack(context.getUsedStack());
    }

    public static SimpleBuildContext copyOf(@Nullable IBuildContext context) {
        return builderOf(context).build();
    }

    @Nonnull
    private final IWorld world;
    @Nullable
    private final PlayerEntity buildingPlayer;

    private final ItemStack stack;

    private SimpleBuildContext(@Nonnull IWorld world, @Nullable PlayerEntity buildingPlayer, @Nonnull ItemStack stack) {
        this.world = world;
        this.buildingPlayer = buildingPlayer;
        this.stack = Objects.requireNonNull(stack);
    }

    /**
     * @return The {@link IWorld} of this {@code SimpleBuildContext}. Will not be null.
     */
    @Override
    public IWorld getWorld() {
        return world;
    }

    /**
     * @return The {@link PlayerEntity} performing the build. May be null if unknown.
     */
    @Nullable
    @Override
    public PlayerEntity getBuildingPlayer() {
        return buildingPlayer;
    }

    @Override
    public ItemStack getUsedStack() {
        return stack;
    }

    /**
     * {@code SimpleBuilder} for creating new instances of {@link SimpleBuildContext}
     */
    public static final class Builder {
        @Nullable
        private IWorld world;
        @Nullable
        private PlayerEntity buildingPlayer;
        @Nonnull
        private ItemStack stack;

        private Builder() {
            this.world = null;
            this.buildingPlayer = null;
            this.stack = ItemStack.EMPTY;
        }

        /**
         * Sets the {@link IWorld} of the resulting {@link SimpleBuildContext}.
         * @param world The {@link IWorld} of the resulting {@link SimpleBuildContext}.
         * @return The {@code SimpleBuilder} itself
         * @see SimpleBuildContext#getWorld()
         */
        public Builder world(IWorld world) {
            this.world = Objects.requireNonNull(world);
            return this;
        }

        /**
         * Sets the {@link PlayerEntity} of the resulting {@link SimpleBuildContext}. Notice that this also set's the world
         * for the resulting {@code SimpleBuildContext} if the player is non-null and a world hasn't been set yet.
         * <p>
         * This defaults to null.
         * @param buildingPlayer The {@link PlayerEntity} of the resulting {@link SimpleBuildContext}.
         * @return The {@code SimpleBuilder} itself
         * @see SimpleBuildContext#getBuildingPlayer()
         */
        public Builder buildingPlayer(@Nullable PlayerEntity buildingPlayer) {
            this.buildingPlayer = buildingPlayer;
            if (world == null && buildingPlayer != null)
                this.world = buildingPlayer.world;
            return this;
        }

        /**
         * Sets the {@link ItemStack} of the resulting {@link SimpleBuildContext}.
         * <p>
         * Defaults to {@link ItemStack#EMPTY}.
         *
         * @param stack The {@link ItemStack} of the resulting {@code SimpleBuildContext}
         * @return The {@code SimpleBuilder} itself
         * @see SimpleBuildContext#getUsedStack()
         */
        public Builder usedStack(@Nonnull ItemStack stack) {
            this.stack = Objects.requireNonNull(stack);
            return this;
        }

        /**
         * Creates a new {@link SimpleBuildContext} using the world previously set on this {@code SimpleBuilder}.
         * @return A new {@link SimpleBuildContext} with the values specified in this {@code SimpleBuilder}.
         * @see #build(IWorld)
         */
        public SimpleBuildContext build() {
            return build(null);
        }

        /**
         * Creates a new {@link SimpleBuildContext} using the specified world. If the given world is null, the world in this {@code SimpleBuilder} will be used.
         * @param world The {@link IWorld} to use. If null this {@code SimpleBuilder}'s world will be used.
         * @return A new {@link SimpleBuildContext} with the values specified in this {@code SimpleBuilder}.
         * @throws NullPointerException if both the {@link IWorld} passed in and the {@link IWorld} of this {@code SimpleBuilder} are null.
         */
        public SimpleBuildContext build(@Nullable IWorld world) {
            return new SimpleBuildContext(world != null ? world : Objects.requireNonNull(this.world), buildingPlayer, stack);
        }
    }
}
