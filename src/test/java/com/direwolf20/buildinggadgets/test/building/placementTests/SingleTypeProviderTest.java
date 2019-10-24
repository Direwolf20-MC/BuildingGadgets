package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.SingleTypeProvider;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.test.util.UniqueBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingleTypeProviderTest {

    private final BlockState state = UniqueBlockState.createNew();
    private final SingleTypeProvider provider = new SingleTypeProvider(new BlockData(state, TileSupport.dummyTileEntityData()));
    private final Random random = new Random();

    //TODO implement custom runner that launches minecraft
    //  this does not work since NBTUtil.readBlockState(BlockState) accesses block registry, and using
    //  UniqueBlockState looses the point of testing
    @Disabled("requires custom runner with minecraft started")
    @Test
    void stateAfterSerializationShouldRemainSameAsBeforeSerialization() {
        CompoundNBT serialized = provider.serialize();
        SingleTypeProvider deserialized = new SingleTypeProvider(null).deserialize(serialized);
        assertEquals(provider.at(BlockPos.ZERO), deserialized.at(BlockPos.ZERO));
    }

    @Test
    void accessResultsShouldRemainConstantHardcoded() {
        assertEquals(state, provider.at(new BlockPos(0, 0, 0)).getState());
        assertEquals(state, provider.at(new BlockPos(- 0, - 0, - 0)).getState());
        assertEquals(state, provider.at(new BlockPos(64, 64, 64)).getState());
        assertEquals(state, provider.at(new BlockPos(512, - 512, 512)).getState());
        assertEquals(state, provider.at(new BlockPos(- 512, 512, - 512)).getState());
    }

    @RepeatedTest(16)
    @Disabled
    void accessResultsShouldRemainConstantRandom() {
        int x = random.nextInt();
        int y = random.nextInt();
        int z = random.nextInt();
        assertEquals(state, provider.at(new BlockPos(x, y, z)).getState());
    }

}



