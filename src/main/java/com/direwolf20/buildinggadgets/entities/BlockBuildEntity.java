package com.direwolf20.buildinggadgets.entities;

import com.direwolf20.buildinggadgets.ModBlocks;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBuildEntity extends Entity{

    private static final DataParameter<Integer> toolMode = EntityDataManager.<Integer>createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<IBlockState>> SET_BLOCK = EntityDataManager.<Optional<IBlockState>>createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);

    public int despawning = -1;
    public int maxLife = 20;
    private int mode;
    private IBlockState setBlock;
    private IBlockState originalSetBlock;
    private BlockPos setPos;
    private EntityLivingBase spawnedBy;

    World world;

    public BlockBuildEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);

        // We get an instance of the world (though this can be achieved with getEntityWorld)
        world = worldIn;
    }

    public BlockBuildEntity(World worldIn, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock, int toolMode) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        IBlockState currentBlock = worldIn.getBlockState(spawnPos);
        setPos = spawnPos;
        setBlock = spawnBlock;
        originalSetBlock = spawnBlock;
        setSetBlock(spawnBlock);
        if (toolMode == 3) {
            if (currentBlock != null) {
                setBlock = currentBlock;
                setSetBlock(setBlock);
            } else {
                setBlock = Blocks.AIR.getDefaultState();
                setSetBlock(setBlock);
            }
        }
        world = worldIn;
        mode = toolMode;
        setToolMode(toolMode);
        spawnedBy = player;
        world.setBlockState(spawnPos, ModBlocks.effectBlock.getDefaultState());
    }

    public int getToolMode() {
        return this.dataManager.get(toolMode);
    }

    public void setToolMode(int mode) {
        this.dataManager.set(toolMode, mode);
    }

    @Nullable
    public IBlockState getSetBlock() {
        return (IBlockState) ((Optional) this.dataManager.get(SET_BLOCK)).orNull();
    }

    public void setSetBlock(@Nullable IBlockState state) {
        this.dataManager.set(SET_BLOCK, Optional.fromNullable(state));
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0; //After tr
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (ticksExisted > maxLife) {
            setDespawning();
        }

        if (!isDespawning()) {

        } else {
            despawnTick();
        }
    }

    public boolean isDespawning() {
        return despawning != -1;
    }

    private void setDespawning() {
        if (despawning == -1) {
            despawning = 0;
            if (setPos != null && setBlock != null && (getToolMode() == 1)) {
                world.setBlockState(setPos, setBlock);
            } else if (setPos != null && setBlock != null && getToolMode() == 2) {
                world.setBlockState(setPos, Blocks.AIR.getDefaultState());
            } else if (setPos != null && setBlock != null && getToolMode() == 3) {
                world.spawnEntity(new BlockBuildEntity(world, setPos, spawnedBy, originalSetBlock, 1));
            }
        }
    }

    private void despawnTick() {
        despawning++;
        if (despawning > 1) {
            setDead();
        }
    }

    @Override
    public void setDead() {
        this.isDead = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("despawning", despawning);
        // This is usually not saved, but it is important here, so we will
        compound.setInteger("ticksExisted", ticksExisted);

        compound.setIntArray("setPos", new int[]{setPos.getX(), setPos.getY(), setPos.getZ()});

        compound.setInteger("setBlock", Block.getStateId(setBlock));
        compound.setInteger("originalSetBlock", Block.getStateId(originalSetBlock));

        compound.setInteger("mode", mode);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        despawning = compound.getInteger("despawning");
        ticksExisted = compound.getInteger("ticksExisted");

        int[] pos = compound.getIntArray("setPos");
        setPos = new BlockPos(pos[0], pos[1], pos[2]);

        setBlock = Block.getStateById(compound.getInteger("setBlock"));
        originalSetBlock = Block.getStateById(compound.getInteger("originalSetBlock"));

        mode = compound.getInteger("mode");

        setSetBlock(setBlock);
        setToolMode(mode);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(FIXED, BlockPos.ORIGIN);
        this.dataManager.register(toolMode, 1);
        this.dataManager.register(SET_BLOCK, Optional.absent());
    }
}
