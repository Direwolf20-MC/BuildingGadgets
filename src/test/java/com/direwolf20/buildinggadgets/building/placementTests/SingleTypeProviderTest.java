package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import com.direwolf20.buildinggadgets.util.UniqueBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SingleTypeProviderTest {

    private final IBlockState state = UniqueBlockState.createNew();
    private final SingleTypeProvider provider = new SingleTypeProvider(state);
    private final Random random = new Random();

    //TODO implement custom runner that launches minecraft
    //  this does not work since NBTUtil.readBlockState(IBlockState) accesses block registry, and using
    //  UniqueBlockState losses the point of testing
    @Disabled("requires custom runner with minecraft started")
    @Test
    void stateAfterSerializationShouldRemainSameAsBeforeSerialization() {
        NBTTagCompound serialized = provider.serialize();
        SingleTypeProvider deserialized = new SingleTypeProvider(null).deserialize(serialized);
        assertEquals(provider.at(BlockPos.ORIGIN), deserialized.at(BlockPos.ORIGIN));
    }

    @Test
    void accessResultsShouldRemainConstantHardcoded() {
        assertEquals(state, provider.at(new BlockPos(0, 0, 0)));
        assertEquals(state, provider.at(new BlockPos(-0, -0, -0)));
        assertEquals(state, provider.at(new BlockPos(64, 64, 64)));
        assertEquals(state, provider.at(new BlockPos(512, -512, 512)));
        assertEquals(state, provider.at(new BlockPos(-512, 512, -512)));
    }

    @RepeatedTest(16)
    void accessResultsShouldRemainConstantRandom() {
        int x = random.nextInt();
        int y = random.nextInt();
        int z = random.nextInt();
        assertEquals(state, provider.at(new BlockPos(x, y, z)));
    }

}



