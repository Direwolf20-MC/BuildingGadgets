package com.direwolf20.buildinggadgets.common.tainted.building.view;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Simple implementation of {@link BuildContext} providing a {@link Builder} for creation.
 */
@Immutable
public final class BuildContext {
    /**
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    private final LevelAccessor world;
    @Nullable
    private final Player player;

    private final ItemStack stack;

    public BuildContext(@Nonnull LevelAccessor world, @Nullable Player player, @Nonnull ItemStack stack) {
        this.world = world;
        this.player = player;
        this.stack = stack;
    }

    /**
     * @return The {@link IWorld} of this {@code SimpleBuildContext}. Will not be null.
     */
    public LevelAccessor getWorld() {
        return world;
    }

    /**
     * @return The {@link PlayerEntity} performing the build. May be null if unknown.
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ServerLevel getServerWorld() {
        return ((ServerLevelAccessor) world).getLevel();
    }

    /**
     * {@code SimpleBuilder} for creating new instances of {@link BuildContext}
     */
    public static final class Builder {
        @Nullable
        private LevelAccessor world;
        @Nullable
        private Player buildingPlayer;
        @Nonnull
        private ItemStack stack;

        private Builder() {
            this.world = null;
            this.buildingPlayer = null;
            this.stack = ItemStack.EMPTY;
        }

        /**
         * Sets the {@link IWorld} of the resulting {@link BuildContext}.
         * @param world The {@link IWorld} of the resulting {@link BuildContext}.
         * @return The {@code Builder} itself
         * @see BuildContext#getWorld()
         */
        public Builder world(@Nonnull LevelAccessor world) {
            this.world = world;
            return this;
        }

        /**
         * Sets the {@link PlayerEntity} of the resulting {@link BuildContext}. Notice that this also set's the world
         * for the resulting {@code SimpleBuildContext} if the player is non-null and a world hasn't been set yet.
         * <p>
         * This defaults to null.
         * @param buildingPlayer The {@link PlayerEntity} of the resulting {@link BuildContext}.
         * @return The {@code Builder} itself
         * @see BuildContext#getPlayer()
         */
        public Builder player(@Nullable Player buildingPlayer) {
            this.buildingPlayer = buildingPlayer;
            if (world == null && buildingPlayer != null)
                this.world = buildingPlayer.level;
            return this;
        }

        /**
         * Sets the {@link ItemStack} of the resulting {@link BuildContext}.
         * <p>
         * Defaults to {@link ItemStack#EMPTY}.
         *
         * @param stack The {@link ItemStack} of the resulting {@code SimpleBuildContext}
         * @return The {@code Builder} itself
         * @see BuildContext#getStack()
         */
        public Builder stack(@Nonnull ItemStack stack) {
            this.stack = stack;
            return this;
        }

        /**
         * Creates a new {@link BuildContext} using the world previously set on this {@code Builder}.
         * @return A new {@link BuildContext} with the values specified in this {@code Builder}.
         * @see #build(IWorld)
         */
        public BuildContext build() {
            return build(null);
        }

        /**
         * Creates a new {@link BuildContext} using the specified world. If the given world is null, the world in this {@code Builder} will be used.
         * @param world The {@link IWorld} to use. If null this {@code SimpleBuilder}'s world will be used.
         * @return A new {@link BuildContext} with the values specified in this {@code SimpleBuilder}.
         * @throws NullPointerException if both the {@link World} passed in and the {@link World} of this {@code Builder} are null.
         */
        public BuildContext build(@Nullable LevelAccessor world) {
            return new BuildContext(world != null ? world : Objects.requireNonNull(this.world), buildingPlayer, stack);
        }
    }
}
