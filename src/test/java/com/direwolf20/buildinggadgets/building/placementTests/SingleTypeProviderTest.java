package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.building.placement.SingleTypeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("requires custom runner with minecraft started")
public class SingleTypeProviderTest {

    //TODO implement custom runner and add tests
    //this will not work since initializing a block instance requires sound engine and texture engine
    private final IBlockState state = new Block(Material.AIR).getDefaultState();
    private final SingleTypeProvider provider = new SingleTypeProvider(state);
    private final Random random = new Random();

    @Test
    public void stateAfterSerializationShouldRemainSameAsBeforeSerialization() {
        NBTTagCompound serialized = provider.serializeNBT();
        SingleTypeProvider deserialized = new SingleTypeProvider(null).deserializeNBT(serialized);
        assertEquals(provider.at(BlockPos.ORIGIN), deserialized.at(BlockPos.ORIGIN));
    }

    @Test
    public void accessResultsShouldRemainConstantHardcoded() {
        assertEquals(state, provider.at(new BlockPos(0, 0, 0)));
        assertEquals(state, provider.at(new BlockPos(-0, -0, -0)));
        assertEquals(state, provider.at(new BlockPos(64, 64, 64)));
        assertEquals(state, provider.at(new BlockPos(512, -512, 512)));
        assertEquals(state, provider.at(new BlockPos(-512, 512, -512)));
    }

    @RepeatedTest(16)
    public void accessResultsShouldRemainConstantRandom() {
        int x = random.nextInt(Integer.MAX_VALUE);
        int y = random.nextInt(Integer.MAX_VALUE);
        int z = random.nextInt(Integer.MAX_VALUE);
        assertEquals(state, provider.at(new BlockPos(x, y, z)));
    }

}
