package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBuildEntity extends Entity {

    private static final DataParameter<Integer> toolMode = EntityDataManager.<Integer>createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<IBlockState>> SET_BLOCK = EntityDataManager.<Optional<IBlockState>>createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> usePaste = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BOOLEAN);


    private int despawning = -1;
    public int maxLife = 20;
    private int mode;
    private IBlockState setBlock;
    private IBlockState originalSetBlock;
    private IBlockState actualSetBlock;
    private BlockPos setPos;
    private EntityLivingBase spawnedBy;
    private boolean useConstructionPaste;

    private World world;

    public BlockBuildEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
    }

    public BlockBuildEntity(World worldIn, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock, int toolMode, IBlockState actualSpawnBlock, boolean constrPaste) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        IBlockState currentBlock = worldIn.getBlockState(spawnPos);
        TileEntity te = worldIn.getTileEntity(spawnPos);
        setPos = spawnPos;
        if (te instanceof ConstructionBlockTileEntity) {
            setBlock = ((ConstructionBlockTileEntity) te).getBlockState();
            if (setBlock == null) {
                setBlock = spawnBlock;
            }
        } else {
            setBlock = spawnBlock;
        }
        originalSetBlock = spawnBlock;
        setSetBlock(setBlock);
        if (toolMode == 3) {
            if (currentBlock != null) {
                if (te instanceof ConstructionBlockTileEntity) {
                    setBlock = ((ConstructionBlockTileEntity) te).getBlockState();
                    if (setBlock == null) {
                        setBlock = currentBlock;
                    }
                } else {
                    setBlock = currentBlock;
                }
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
        actualSetBlock = actualSpawnBlock;
        world.setBlockState(spawnPos, ModBlocks.effectBlock.getDefaultState());
        setUsingConstructionPaste(constrPaste);
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

    public void setUsingConstructionPaste(Boolean paste) {
        this.dataManager.set(usePaste, paste);
    }

    public boolean getUsingConstructionPaste() {
        return this.dataManager.get(usePaste);
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
                if (getUsingConstructionPaste()) {
                    world.setBlockState(setPos, ModBlocks.constructionBlock.getDefaultState());
                    TileEntity te = world.getTileEntity(setPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(setBlock);
                        ((ConstructionBlockTileEntity) te).setActualBlockState(actualSetBlock);
                    }
                    world.spawnEntity(new ConstructionBlockEntity(world, setPos, false));
                } else {
                    world.setBlockState(setPos, setBlock);
                    world.getBlockState(setPos).getBlock().neighborChanged(setBlock, world, setPos, world.getBlockState(setPos.up()).getBlock(), setPos.up());
                }
            } else if (setPos != null && setBlock != null && getToolMode() == 2) {
                world.setBlockState(setPos, Blocks.AIR.getDefaultState());
            } else if (setPos != null && setBlock != null && getToolMode() == 3) {
                world.spawnEntity(new BlockBuildEntity(world, setPos, spawnedBy, originalSetBlock, 1, actualSetBlock, getUsingConstructionPaste()));
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
        compound.setInt("despawning", despawning);
        compound.setInt("ticksExisted", ticksExisted);
        compound.setTag("setPos", NBTUtil.createPosTag(setPos));
        NBTTagCompound blockStateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(blockStateTag, setBlock);
        compound.setTag("setBlock", blockStateTag);
        NBTTagCompound actualBlockStateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(actualBlockStateTag, actualSetBlock);
        compound.setTag("actualSetBlock", actualBlockStateTag);
        NBTUtil.writeBlockState(blockStateTag, originalSetBlock);
        compound.setTag("originalBlock", blockStateTag);
        compound.setInt("mode", mode);
        compound.setBoolean("paste", useConstructionPaste);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        despawning = compound.getInt("despawning");
        ticksExisted = compound.getInt("ticksExisted");
        setPos = NBTUtil.getPosFromTag(compound.getCompound("setPos"));
        setBlock = NBTUtil.readBlockState(compound.getCompound("setBlock"));
        originalSetBlock = NBTUtil.readBlockState(compound.getCompound("originalBlock"));
        actualSetBlock = NBTUtil.readBlockState(compound.getCompound("actualSetBlock"));
        mode = compound.getInt("mode");
        useConstructionPaste = compound.getBoolean("paste");
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(FIXED, BlockPos.ORIGIN);
        this.dataManager.register(toolMode, 1);
        this.dataManager.register(SET_BLOCK, Optional.absent());
        this.dataManager.register(usePaste, useConstructionPaste);
    }

}
