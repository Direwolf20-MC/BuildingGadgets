package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.common.util.exceptions.PaletteOverflowException;
import com.direwolf20.buildinggadgets.common.util.helpers.LambdaHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiPredicate;

public class RegionSnapshot {

    private static final String DIMENSION = "dim";
    private static final String BLOCK_FRAMES = "frames";
    private static final String BLOCK_PALETTES = "palettes";
    private static final String TILE_DATA = "block_snapshots";
    private static final String TILE_POS = "block_pos";
    private static final String TILE_NBT = "block_nbt";

    private static CompoundNBT serialize(CompoundNBT tag, RegionSnapshot snapshot) {
        tag.setString(DIMENSION, snapshot.world.getDimension().getType().getRegistryName().toString());
        snapshot.region.serializeTo(tag);

        // Palette serialization begin
        // The serialized palettes only include actual block states, empty block state should be added, regardlessly, during deserialization
        NBTTagList palettes = new NBTTagList();
        Object2IntMap<BlockState> mapPalettes = new Object2IntOpenHashMap<>();
        {
            // ID 0 is preoccupied for "nothing should be placed here", which is represented as Optional.empty()
            // This way whenever we access the size of this map, we always get the correct state ID to be used next
            mapPalettes.put(null, 0);
        }
        Set<BlockState> recordedPalettes = new HashSet<>();
        // Palette serialization end

        // Streaking: a way to record a number of repeating block states, using only 1 integer
        // Storage frame: an integer indicating a sequence of block states and the streaking block state ID; the first 24 bits are used to stored the ID, and the former 8 bits are used to store the number of repeating block states.
        //   If there are more than 256, or 2^8 repeating block states, it will force start a new storage frame

        // Complete storage frames, will be serialized into a NBTTagList later
        IntList frames = new IntArrayList();

        // The repeating block state, if any
        // - similar logic as counter resetting for initializing as the first block state
        Optional<BlockState> lastStreak = snapshot.blockStates.get(0);
        // Number of repeating block states
        int streak = 0;

        for (Optional<BlockState> state : snapshot.blockStates) {
            // Palette serialization begin
            if (state.isPresent()) {
                // This statement should never throw an exception
                BlockState nonnullState = state.orElseThrow(RuntimeException::new);
                if (!recordedPalettes.contains(nonnullState)) {
                    recordedPalettes.add(nonnullState);
                    mapPalettes.put(nonnullState, mapPalettes.size());
                    palettes.add(NBTUtil.writeBlockState(nonnullState));
                }
            }
            // Palette serialization ends

            // If the same block state showed up again, increase the number of streaks
            // If we have more than 255 streaks (maximum integer that can be stored in 8 bits), we force start a new streak, because the current storage frame won't fit that many of them
            if (state.equals(lastStreak) && streak < 255) {
                streak++;
                continue;
            }

            // Note: if the program reached here, it means the current storage frame is completed

            // Number of streaks and the ID of the block state that is streaking
            // - no need to differentiate between single and repeating block states, since the number at the larger endian represents how many repeating block states are there
            // - if the streaking block state is empty (returns `null` when `get()` is called), it will be mapped to ID 0.
            //   - see initialization of `mapPalettes`
            // - when serializing, the stored number will be ONE FEWER then the actual amount
            //   - this is for squeezing space efficiency of the serialized format (even though it is not very necessary)
            frames.add((((streak - 1) & 0xff) << 24) | (mapPalettes.getInt(lastStreak.orElse(null)) & 0xffffff));

            // Regardless of which situation, we reset the counter
            streak = 0;
            lastStreak = state;
        }
        // Force add a frame here -- even if we pushed a new frame on the last block state, itself hasn't been stored yet
        frames.add((((streak - 1) & 0xff) << 24) | (mapPalettes.getInt(lastStreak.orElse(null)) & 0xffffff));

        tag.setIntArray(BLOCK_FRAMES, frames.toIntArray());
        tag.setTag(BLOCK_PALETTES, palettes);

        NBTTagList tileData = new NBTTagList();
        for (Pair<BlockPos, CompoundNBT> data : snapshot.tileData) {
            CompoundNBT serializedData = new CompoundNBT();
            serializedData.setTag(TILE_POS, NBTUtil.writeBlockPos(data.getLeft()));
            serializedData.setTag(TILE_NBT, data.getRight());
            tileData.add(serializedData);
        }
        tag.setTag(TILE_DATA, tileData);

        return tag;
    }

    private static RegionSnapshot deserialize(CompoundNBT tag) {
        ResourceLocation dimension = new ResourceLocation(tag.getString(DIMENSION));
        World world = ServerLifecycleHooks.getCurrentServer().getWorld(Objects.requireNonNull(DimensionType.byName(dimension)));

        Region region = Region.deserializeFrom(tag);

        List<Optional<BlockState>> palettes = new ArrayList<>();
        {
            // Empty block state, indicating "do not replace here"
            palettes.add(Optional.empty());
        }
        NBTTagList palettesNBT = tag.getList(BLOCK_PALETTES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < palettesNBT.size(); i++) {
            palettes.add(Optional.of(NBTUtil.readBlockState(palettesNBT.getCompound(i))));
        }
        assert palettes.size() == palettesNBT.size() + 1;

        // See the serialization algorithm for vocabulary definitions, including "frame", "streak", etc.
        ImmutableList.Builder<Optional<BlockState>> blockStates = ImmutableList.builder();
        int[] frames = tag.getIntArray(BLOCK_FRAMES);
        for (int frame : frames) {
            int stateID = frame & 0xffffff;
            // The serialized number will be ONE FEWER then the actual amount
            // - see the serialization for +1
            int streaks = (frame >> 24) + 1;

            Optional<BlockState> state = palettes.get(stateID);
            // Add `streaks` amount of the same block state
            for (int j = 0; j < streaks; j++) {
                blockStates.add(state);
            }
        }

        NBTTagList tileDataNBT = tag.getList(TILE_DATA, Constants.NBT.TAG_COMPOUND);
        ImmutableList.Builder<Pair<BlockPos, CompoundNBT>> tileData = ImmutableList.builder();
        for (int i = 0; i < tileDataNBT.size(); i++) {
            CompoundNBT serializedData = tileDataNBT.getCompound(i);
            tileData.add(Pair.of(
                    NBTUtil.readBlockPos(serializedData.getCompound(TILE_POS)),
                    serializedData.getCompound(TILE_NBT)));
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
    public static RegionSnapshot take(World world, Region region) throws PaletteOverflowException {
        return select(world, region).build();
    }

    private World world;
    private Region region;

    private ImmutableList<Optional<BlockState>> blockStates;
    private ImmutableList<Pair<BlockPos, CompoundNBT>> tileData;

    /**
     * Cached serialized form. This is reliable because {@link RegionSnapshot} is <i>immutable</i>.
     */
    private CompoundNBT serializedForm;

    private RegionSnapshot(World world, Region region, ImmutableList<Optional<BlockState>> blockStates, ImmutableList<Pair<BlockPos, CompoundNBT>> tileData) {
        this.world = world;
        this.region = region;
        this.blockStates = blockStates;
        this.tileData = tileData;
    }

    public void restore() {
        int index = 0;
        for (BlockPos pos : region) {
            blockStates.get(index).ifPresent(state -> world.setBlockState(pos, state));
            index++;
        }

        for (Pair<BlockPos, CompoundNBT> data : tileData) {
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
    public ImmutableList<Optional<BlockState>> getBlockStates() {
        return blockStates;
    }

    /**
     * The indices of the entries of this list is unrelated to any positions, instead, the entry stores the affected
     * coordinate.
     */
    public ImmutableList<Pair<BlockPos, CompoundNBT>> getTileData() {
        return tileData;
    }

    public CompoundNBT serialize() {
        return serializeTo(new CompoundNBT());
    }

    public CompoundNBT serializeTo(CompoundNBT tag) {
        if (serializedForm == null)
            serializedForm = serialize(tag, this);
        return serializedForm;
    }

    public static final class Builder {

        private World world;
        private Region region;
        private BiPredicate<BlockPos, BlockState> normalValidator;
        private TriPredicate<BlockPos, BlockState, TileEntity> tileValidator;

        private boolean built = false;

        private Builder(World world, Region region) {
            this.world = world;
            this.region = region;
        }

        /**
         * Set validator for normal blocks (non tile entities).
         *
         * @return this
         * @throws IllegalStateException When trying to invoke this method when this builder has build someone already.
         */
        public Builder checkBlocks(BiPredicate<BlockPos, BlockState> normalValidator) {
            Preconditions.checkState(!built);
            this.normalValidator = LambdaHelper.and(this.normalValidator, normalValidator);
            return this;
        }

        /**
         * @throws IllegalStateException When trying to invoke this method when this builder has build someone already.
         */
        public Builder exclude(BlockState... statesToExclude) {
            ImmutableSet<BlockState> statesSet = ImmutableSet.copyOf(statesToExclude);
            return checkBlocks((pos, state) -> !statesSet.contains(state));
        }

        /**
         * @throws IllegalStateException When trying to invoke this method when this builder has build someone already.
         */
        public Builder excludeAir() {
            return checkBlocks((pos, state) -> state.isAir(world, pos));
        }

        /**
         * Set validator for tile entities. If want to record tile entities regardless of its condition, use
         *
         * @return this
         * @throws IllegalStateException When trying to invoke this method when this builder has build someone already.
         */
        public Builder checkTiles(TriPredicate<BlockPos, BlockState, TileEntity> tileValidator) {
            Preconditions.checkState(!built);
            this.tileValidator = LambdaHelper.and(this.tileValidator, tileValidator);
            return this;
        }

        /**
         * Set whether record tile entities or not. Will force override the predicate.
         *
         * @throws IllegalStateException When trying to invoke this method when this builder has build someone already.
         * @see #checkTiles(TriPredicate)
         */
        public Builder recordTiles(boolean flag) {
            Preconditions.checkState(!built);
            tileValidator = flag ? (pos, state, tile) -> true : null;
            return this;
        }

        /**
         * @throws IllegalStateException    When trying to invoke this method when this builder has build someone
         *                                  already.
         * @throws PaletteOverflowException When there are more than 16777216, or {@code 2^24}, unique block states in
         *                                  the given region. This is because the serialization format only uses the
         *                                  first 24 bits of an integer to store the palette ID.
         */
        public RegionSnapshot build() throws IllegalStateException, PaletteOverflowException {
            Preconditions.checkState(!built);

            ImmutableList.Builder<Optional<BlockState>> blockStatesBuilder = ImmutableList.builder();
            ImmutableList.Builder<Pair<BlockPos, CompoundNBT>> tileDataBuilder = ImmutableList.builder();
            for (BlockPos pos : region) {
                TileEntity tile = world.getTileEntity(pos);
                BlockState state = world.getBlockState(pos);
                if (tile != null && tileValidator.test(pos, state, tile)) {
                    tileDataBuilder.add(Pair.of(pos, tile.serializeNBT()));
                    blockStatesBuilder.add(Optional.of(state));
                } else if (normalValidator.test(pos, state))
                    blockStatesBuilder.add(Optional.of(state));
                else
                    blockStatesBuilder.add(Optional.empty());
            }

            ImmutableList<Optional<BlockState>> blockStates = blockStatesBuilder.build();

            ImmutableSet<Optional<BlockState>> uniqueBlocksStates = ImmutableSet.copyOf(blockStates);
            // 16777216 == 2^24
            if (uniqueBlocksStates.size() >= 16777216)
                throw new PaletteOverflowException(region, uniqueBlocksStates.size());

            built = true;
            return new RegionSnapshot(world, region, blockStates, tileDataBuilder.build());
        }

    }

}
