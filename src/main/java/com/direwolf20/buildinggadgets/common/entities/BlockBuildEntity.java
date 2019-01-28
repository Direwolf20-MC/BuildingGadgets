package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.BuildingObjects;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;

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
import java.util.Optional;

public class BlockBuildEntity extends Entity {

    private static final DataParameter<Integer> toolMode = EntityDataManager.<Integer>createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<IBlockState>> SET_BLOCK = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> usePaste = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BOOLEAN);

    private IBlockState setBlock;
    private IBlockState originalSetBlock;
    private IBlockState actualSetBlock;
    private BlockPos setPos;
    private EntityLivingBase spawnedBy;
    private World world;

    private int mode;
    private boolean useConstructionPaste;
    private int despawning = -1;

    int maxLife = 20;

    public BlockBuildEntity(World world) {
        super(BuildingObjects.BLOCK_BUILD, world);

        setSize(0.1F, 0.1F);
        this.world = world;
    }

    public BlockBuildEntity(World worldIn, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock, int toolMode, IBlockState actualSpawnBlock, boolean constrPaste) {
        super(BuildingObjects.BLOCK_BUILD, worldIn);
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
        world.setBlockState(spawnPos, BuildingObjects.effectBlock.getDefaultState());
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
        return (IBlockState) ((Optional) this.dataManager.get(SET_BLOCK)).orElse(null);
    }

    public void setSetBlock(@Nullable IBlockState state) {
        this.dataManager.set(SET_BLOCK, Optional.ofNullable(state));
    }

    public void setUsingConstructionPaste(Boolean paste) {
        this.dataManager.set(usePaste, paste);
    }

    public boolean getUsingConstructionPaste() {
        return this.dataManager.get(usePaste);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(FIXED, BlockPos.ORIGIN);
        this.dataManager.register(toolMode, 1);
        this.dataManager.register(SET_BLOCK, Optional.empty());
        this.dataManager.register(usePaste, useConstructionPaste);
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        despawning = compound.getInt("despawning");
        ticksExisted = compound.getInt("ticksExisted");
        setPos = NBTUtil.readBlockPos(compound.getCompound("setPos"));
        setBlock = NBTUtil.readBlockState(compound.getCompound("setBlock"));
        originalSetBlock = NBTUtil.readBlockState(compound.getCompound("originalBlock"));
        actualSetBlock = NBTUtil.readBlockState(compound.getCompound("actualSetBlock"));
        mode = compound.getInt("mode");
        useConstructionPaste = compound.getBoolean("paste");
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        compound.setInt("despawning", despawning);
        compound.setInt("ticksExisted", ticksExisted);
        compound.setTag("setPos", NBTUtil.writeBlockPos(setPos));

        NBTTagCompound blockStateTag = NBTUtil.writeBlockState(setBlock);
        compound.setTag("setBlock", blockStateTag);

        NBTTagCompound actualBlockStateTag = NBTUtil.writeBlockState(actualSetBlock);
        compound.setTag("actualSetBlock", actualBlockStateTag);

        blockStateTag = NBTUtil.writeBlockState(originalSetBlock);

        compound.setTag("originalBlock", blockStateTag);
        compound.setInt("mode", mode);
        compound.setBoolean("paste", useConstructionPaste);
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0; //After tr
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    @Override
    public void tick() {
        super.tick();

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
                    world.setBlockState(setPos, BuildingObjects.constructionBlock.getDefaultState());
                    TileEntity te = world.getTileEntity(setPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(setBlock, actualSetBlock);
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
            this.remove();
        }
    }
}
