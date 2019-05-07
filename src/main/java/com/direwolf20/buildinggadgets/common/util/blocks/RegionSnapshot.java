package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.api.building.Region;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.BiPredicate;

public class RegionSnapshot {

    private static final String DIMENSION = "dim";
    private static final String BLOCK_FRAMES = "frames";
    private static final String BLOCK_PALETTES = "palettes";
    private static final String BLOCK_SNAPSHOTS = "block_snapshots";

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
        // Storage frame: one or two integers indicating a sequence of block states
        // Regular frame: a type of storage frame; 1 integer, representing the ID of the block state recorded
        // Streak frame: a type of storage frame; 2 integers, where the first one is positive, representing the ID of the repeating block state; the second one is negative, representing the number of repeating block states

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
                if (state == lastStreak) {
                    streak++;
                    continue;
                }

                // Note: if the program reached here, it means the current storage frame is completed

                // If it didn't show up and this block is not by itself, record the streak frame
                // The first one represents the ID of the streaking block state, the second one, which is negative, represents the amount of streaking block states
                if (streak > 1) {
                    // ID of the block state that is streaking
                    frames.add(mapPalettes.getInt(lastStreak));
                    // Number of streaks
                    // Stored in negative to differentiate with a block state ID
                    frames.add(-streak);
                } else
                    // Otherwise just add the block state ID, since there is only one of it
                    frames.add(mapPalettes.getInt(lastStreak));
            }

            // Regardless of which situation, we reset the counter
            streak = 1;
            lastStreak = state;
        }

        tag.setIntArray(BLOCK_FRAMES, frames.toIntArray());
        tag.setTag(BLOCK_PALETTES, palettes);

        NBTTagList blockSnapshots = new NBTTagList();
        for (BlockSnapshot blockSnapshot : snapshot.tileSnapshots) {
            NBTTagCompound serializedSnapshot = new NBTTagCompound();
            blockSnapshot.writeToNBT(serializedSnapshot);
            blockSnapshots.add(serializedSnapshot);
        }
        tag.setTag(BLOCK_SNAPSHOTS, blockSnapshots);

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
        List<IBlockState> blockStates = new ArrayList<>();
        int[] frames = tag.getIntArray(BLOCK_FRAMES);
        for (int i = 0; i < frames.length; i++) {
            int stateID = frames[i];
            // If this is the last item, there will be no more items to indicate this is a streak
            // Therefore we do the streak-scanning process only if this is not the last item
            if (i != frames.length - 1) {
                int nextItem = frames[i + 1];
                if (nextItem < 0) {
                    // Streak is represented in a negative number. See the serialization algorithm for more information
                    int streak = -nextItem;

                    IBlockState state = palettes.get(stateID);
                    for (int j = 0; j < streak; j++) {
                        blockStates.add(state);
                    }

                    // We already used the item afterwards, so we can skip it
                    i++;
                    continue;
                }
            }
            IBlockState state = palettes.get(stateID);
            blockStates.add(state);
        }

        NBTTagList blockSnapshotsNBT = tag.getList(BLOCK_SNAPSHOTS, Constants.NBT.TAG_COMPOUND);
        List<BlockSnapshot> blockSnapshots = new ArrayList<>();
        for (int i = 0; i < blockSnapshotsNBT.size(); i++) {
            blockSnapshots.add(BlockSnapshot.readFromNBT(blockSnapshotsNBT.getCompound(i)));
        }

        return new RegionSnapshot(world, region, blockStates, blockSnapshots);
    }

    public static RegionSnapshot takeWithoutTiles(World world, Region region, BiPredicate<BlockPos, IBlockState> validator) {
        return take(world, region, validator, false);
    }

    public static RegionSnapshot take(World world, Region region, BiPredicate<BlockPos, IBlockState> validator, boolean recordTileEntities) {
        List<IBlockState> blockStates = new ArrayList<>();
        List<BlockSnapshot> tileSnapshots = new ArrayList<>();
        for (BlockPos pos : region) {
            TileEntity tile = world.getTileEntity(pos);
            if (recordTileEntities && tile != null) {
                IBlockState state = world.getBlockState(pos);
                if (validator.test(pos, state))
                    tileSnapshots.add(new BlockSnapshot(world, pos, state));
            } else {
                IBlockState state = world.getBlockState(pos);
                if (validator.test(pos, state))
                    blockStates.add(state);
                else
                    blockStates.add(null);
            }
        }
        return new RegionSnapshot(world, region, blockStates, tileSnapshots);
    }

    private World world;
    private Region region;

    /**
     * From minimum Z to maximum Z; then from minimum Y to maximum Y; then from minimum X to maximum X; each entry are
     * stored in the above order and represent the block state in that position. If an entry is {@code null}, it means
     * that position should not be replaced regularly: it might be replaced with a {@link BlockSnapshot}, or it should
     * be left untouched.
     */
    private List<IBlockState> blockStates;

    /**
     * The indices of the entries of this list is unrelated to any positions, instead, the entry stores the affected
     * coordinate.
     */
    private List<BlockSnapshot> tileSnapshots;

    private NBTTagCompound serializedForm;

    public RegionSnapshot(World world, Region region, List<IBlockState> blockStates, List<BlockSnapshot> tileSnapshots) {
        this.world = world;
        this.region = region;
        this.blockStates = blockStates;
        this.tileSnapshots = tileSnapshots;
    }

    public boolean restore() {
        boolean statesSucceeded = true;
        int index = 0;
        for (BlockPos pos : region) {
            IBlockState state = blockStates.get(index);
            if (state == null)
                continue;

            statesSucceeded &= world.setBlockState(pos, state);

            index++;
        }

        boolean snapshotsSucceeded = true;
        for (BlockSnapshot snapshot : tileSnapshots) {
            snapshotsSucceeded &= snapshot.restore();
        }

        return statesSucceeded && snapshotsSucceeded;
    }

    public Region getRegion() {
        return region;
    }

    public NBTTagCompound serialize() {
        return serializeTo(new NBTTagCompound());
    }

    public NBTTagCompound serializeTo(NBTTagCompound tag) {
        if (serializedForm == null)
            serializedForm = serialize(tag, this);
        return serializedForm;
    }

}
