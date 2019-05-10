package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiPredicate;

public class RegionSnapshot {

    private static final String DIMENSION = "dim";
    private static final String BLOCK_FRAMES = "frames";
    private static final String BLOCK_PALETTES = "palettes";
    private static final String TILE_DATA = "block_snapshots";
    private static final String TILE_POS = "block_pos";
    private static final String TILE_NBT = "block_nbt";

    private static NBTTagCompound serialize(NBTTagCompound tag, RegionSnapshot snapshot) {
        tag.setInt(DIMENSION, snapshot.world.getDimension().getType().getId());
        snapshot.region.serializeTo(tag);

        // Palette serialization begin
        NBTTagList palettes = new NBTTagList();
        Object2IntMap<IBlockState> mapPalettes = new Object2IntOpenHashMap<>();
        Set<IBlockState> recordedPalettes = new HashSet<>();

        // "nothing should be here" block state
        palettes.add(new NBTTagCompound());
        // Palette serialization end

        // Streaking: a way to record a number of repeating block states, using only 2 integers
        // Storage frame: one or two numbers indicating a sequence of block states
        // Regular frame: a type of storage frame; 1 number, representing the ID of the block state recorded
        // Streak frame: a type of storage frame; 2 numbers, where the first one is positive, representing the ID of the repeating block state; the second one is negative, representing the number of repeating block states

        // Complete storage frames, will be serialized into a NBTTagList later
        IntList frames = new IntArrayList();

        // The repeating block state, if any
        IBlockState lastStreak = null;
        // Number of repeating block states
        int streak = 0;

        boolean firstIteration = true;
        for (IBlockState state : snapshot.blockStates) {
            // Palette serialization begin
            if (!recordedPalettes.contains(state)) {
                recordedPalettes.add(state);
                // ID 0 is preoccupied for "nothing should be placed here", which is represented in 'null'
                mapPalettes.put(state, mapPalettes.size() + 1);
                palettes.add(NBTUtil.writeBlockState(state));
            }
            // Palette serialization ends

            // Hence we record the storage frame after it is completed, the inital values of the counter would be used as a storage frame regardless, therefore we need to skip inorder for this algorithm to function correctly
            if (firstIteration)
                firstIteration = false;
            else {
                // If the same block state showed up again, increase the number of streaks
                // If we have more than 255 streaks (maximum integer that can be stored in 8 bits), we force start a new streak, because the current storage frame won't fit that many of them
                if (state == lastStreak && streak < 255) {
                    streak++;
                    continue;
                }

                // Note: if the program reached here, it means the current storage frame is completed

                // Number of streaks and the ID of the block state that is streaking
                // No need to differentiate between single and repeating block states, since the number at the larger endian represents how many repeating block states are there
                int frame = ((streak & 0xff) << 24) | (mapPalettes.getInt(lastStreak) & 0xffffff);
                frames.add(frame);
            }

            // Regardless of which situation, we reset the counter
            streak = 1;
            lastStreak = state;
        }

        tag.setIntArray(BLOCK_FRAMES, frames.toIntArray());
        tag.setTag(BLOCK_PALETTES, palettes);

        NBTTagList tileData = new NBTTagList();
        for (Pair<BlockPos, NBTTagCompound> data : snapshot.tileData) {
            NBTTagCompound serializedData = new NBTTagCompound();
            serializedData.setTag(TILE_POS, NBTUtil.writeBlockPos(data.getLeft()));
            serializedData.setTag(TILE_NBT, data.getRight());
            tileData.add(serializedData);
        }
        tag.setTag(TILE_DATA, tileData);

        return tag;
    }

    private static RegionSnapshot deserialize(NBTTagCompound tag) {
        int dimension = tag.getInt(DIMENSION);
        World world = ServerLifecycleHooks.getCurrentServer().getWorld(Objects.requireNonNull(DimensionType.getById(dimension)));

        Region region = Region.deserializeFrom(tag);

        List<IBlockState> palettes = new ArrayList<>();
        // "nothing should be here" block state, represented with 'null'
        palettes.add(null);
        NBTTagList palettesNBT = tag.getList(BLOCK_PALETTES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < palettesNBT.size(); i++) {
            palettes.add(NBTUtil.readBlockState(palettesNBT.getCompound(i)));
        }
        assert palettes.size() == palettesNBT.size() + 1;

        // See the serialization algorithm for vocabulary definitions, including "frame", "streak", etc.
        ImmutableList.Builder<IBlockState> blockStates = ImmutableList.builder();
        int[] frames = tag.getIntArray(BLOCK_FRAMES);
        for (int frame : frames) {
            int stateID = frame & 0xffffff;
            int streaks = frame >> 24;

            IBlockState state = palettes.get(stateID);
            for (int j = 0; j < streaks; j++) {
                blockStates.add(state);
            }
        }

        NBTTagList tileDataNBT = tag.getList(TILE_DATA, Constants.NBT.TAG_COMPOUND);
        ImmutableList.Builder<Pair<BlockPos, NBTTagCompound>> tileData = ImmutableList.builder();
        for (int i = 0; i < tileDataNBT.size(); i++) {
            NBTTagCompound serializedData = tileDataNBT.getCompound(i);
            tileData.add(Pair.of(NBTUtil.readBlockPos(serializedData.getCompound(TILE_POS)), serializedData.getCompound(TILE_NBT)));
        }

        return new RegionSnapshot(world, region, blockStates.build(), tileData.build());
    }

    /**
     * Create a {@link RegionSnapshot} builder.
     * <p>
     * Predicate for whether record blocks or not can be customized through the builder. If no extra conditions are
     * needed, use {@link #take(World, Region)} instead.
     */
    public static Builder select(World world, Region region) {
        return new Builder(world, region);
    }

    /**
     * Directly take a snapshot of the area, <b>without</b> tile entities.
     *
     * @see #select(World, Region)
     */
    public static RegionSnapshot take(World world, Region region) {
        return select(world, region).build();
    }

    private World world;
    private Region region;

    private ImmutableList<IBlockState> blockStates;
    private ImmutableList<Pair<BlockPos, NBTTagCompound>> tileData;

    /**
     * Cached serialized form. This is reliable because {@link RegionSnapshot} is <i>immutable</i>.
     */
    private NBTTagCompound serializedForm;

    private RegionSnapshot(World world, Region region, ImmutableList<IBlockState> blockStates, ImmutableList<Pair<BlockPos, NBTTagCompound>> tileData) {
        this.world = world;
        this.region = region;
        this.blockStates = blockStates;
        this.tileData = tileData;
    }

    public void restore() {
        int index = 0;
        for (BlockPos pos : region) {
            IBlockState state = blockStates.get(index);
            if (state == DummyBlockState.NOTHING)
                continue;

            world.setBlockState(pos, state);
            index++;
        }

        for (Pair<BlockPos, NBTTagCompound> data : tileData) {
            // Assume the blocks are replaced already, which should be the case
            TileEntity tile = world.getTileEntity(data.getLeft());
            if (tile != null) {
                tile.read(data.getRight());
                tile.markDirty();
            } else
                throw new IllegalStateException("Unable to find expected tile entity at " + data.getLeft() + " that can read " + data.getRight());
        }
    }

    public Region getRegion() {
        return region;
    }

    /**
     * From minimum Z to maximum Z; then from minimum Y to maximum Y; then from minimum X to maximum X; each entry are
     * stored in the above order and represent the block state in that position. If an entry is {@code null}, it means
     * that position should not be replaced regularly: it might be replaced with a {@link BlockSnapshot}, or it should
     * be left untouched.
     */
    public ImmutableList<IBlockState> getBlockStates() {
        return blockStates;
    }

    /**
     * The indices of the entries of this list is unrelated to any positions, instead, the entry stores the affected
     * coordinate.
     */
    public ImmutableList<Pair<BlockPos, NBTTagCompound>> getTileData() {
        return tileData;
    }

    public NBTTagCompound serialize() {
        return serializeTo(new NBTTagCompound());
    }

    public NBTTagCompound serializeTo(NBTTagCompound tag) {
        if (serializedForm == null)
            serializedForm = serialize(tag, this);
        return serializedForm;
    }

    public static final class Builder {

        private static final BiPredicate<BlockPos, IBlockState> AIR_CHECK = (pos, state) -> state.getMaterial() != Material.AIR;

        private World world;
        private Region region;
        private BiPredicate<BlockPos, IBlockState> normalValidator;
        private TriPredicate<BlockPos, IBlockState, TileEntity> tileValidator;

        private boolean built = false;

        private Builder(World world, Region region) {
            this.world = world;
            this.region = region;
        }

        /**
         * Set validator for normal blocks (non tile entities).
         *
         * @return this
         */
        public Builder checkBlocks(BiPredicate<BlockPos, IBlockState> normalValidator) {
            Preconditions.checkState(!built);

            this.normalValidator = normalValidator;
            return this;
        }

        public Builder exclude(IBlockState... statesToExclude) {
            ImmutableSet<IBlockState> statesSet = ImmutableSet.copyOf(statesToExclude);
            if (normalValidator == null)
                return checkBlocks((pos, state) -> !statesSet.contains(state));
            else {
                return checkBlocks(normalValidator.and((pos, state) -> !statesSet.contains(state)));
            }
        }

        public Builder excludeAir() {
            if (normalValidator == null)
                return checkBlocks(AIR_CHECK);
            else {
                return checkBlocks(normalValidator.and(AIR_CHECK));
            }
        }

        /**
         * Set validator for tile entities. If want to record tile entities regardless of its condition, use
         *
         * @return this
         */
        public Builder checkTiles(TriPredicate<BlockPos, IBlockState, TileEntity> tileValidator) {
            Preconditions.checkState(!built);

            this.tileValidator = tileValidator;
            return this;
        }

        /**
         * Set whether record tile entities or not. Will force override the predicate.
         *
         * @see #checkTiles(TriPredicate)
         */
        public Builder recordTiles(boolean flag) {
            if (flag)
                return checkTiles((pos, state, tile) -> true);
            else
                return checkTiles(null);
        }

        public RegionSnapshot build() {
            Preconditions.checkState(!built);

            ImmutableList.Builder<IBlockState> tileData = ImmutableList.builder();
            ImmutableList.Builder<Pair<BlockPos, NBTTagCompound>> tileSnapshots = ImmutableList.builder();
            if (tileValidator != null)
                buildWithTiles(tileData, tileSnapshots);
            else
                buildWithoutTiles(tileData);

            built = true;
            return new RegionSnapshot(world, region, tileData.build(), tileSnapshots.build());
        }

        private void buildWithTiles(ImmutableList.Builder<IBlockState> blockStates, ImmutableList.Builder<Pair<BlockPos, NBTTagCompound>> tileData) {
            for (BlockPos pos : region) {
                TileEntity tile = world.getTileEntity(pos);
                if (tile != null) {
                    IBlockState state = world.getBlockState(pos);
                    if (tileValidator.test(pos, state, tile)) {
                        tileData.add(Pair.of(pos, tile.serializeNBT()));
                        blockStates.add(state);
                    }
                } else {
                    IBlockState state = world.getBlockState(pos);
                    if (normalValidator.test(pos, state))
                        blockStates.add(state);
                    else
                        blockStates.add(DummyBlockState.NOTHING);
                }
            }
        }

        private void buildWithoutTiles(ImmutableList.Builder<IBlockState> blockStates) {
            for (BlockPos pos : region) {
                IBlockState state = world.getBlockState(pos);
                if (normalValidator.test(pos, state))
                    blockStates.add(state);
                else
                    blockStates.add(DummyBlockState.NOTHING);
            }
        }

    }

    /**
     * Dummy, singleton block state class used to represent "nothing here". In other words, coordinate containing this
     * block state will be left untouched.
     * <p>
     * {@code null} were not used because we cannot have {@code null} values in {@link ImmutableList}.
     */
    private static class DummyBlockState implements IBlockState {

        public static final DummyBlockState NOTHING = new DummyBlockState();

        private DummyBlockState() {
        }

        @Nonnull
        @Override
        public Block getBlock() {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

        @Nonnull
        @Override
        public Collection<IProperty<?>> getProperties() {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

        @Override
        public <T extends Comparable<T>> boolean has(@Nonnull IProperty<T> property) {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

        @Nonnull
        @Override
        public <T extends Comparable<T>> T get(@Nonnull IProperty<T> property) {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

        @Nonnull
        @Override
        public <T extends Comparable<T>, V extends T> IBlockState with(@Nonnull IProperty<T> property, @Nonnull V value) {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

        @Override
        @Nonnull
        public <T extends Comparable<T>> IBlockState cycle(@Nonnull IProperty<T> property) {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

        @Nonnull
        @Override
        public ImmutableMap<IProperty<?>, Comparable<?>> getValues() {
            throw new IllegalStateException("Dummy block state, no operation should be applied");
        }

    }

}
