package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGEntities;
import com.direwolf20.buildinggadgets.common.utils.ref.NBTKeys;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConstructionBlockEntity extends Entity {

    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(ConstructionBlockEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> MAKING = EntityDataManager.createKey(ConstructionBlockEntity.class, DataSerializers.BOOLEAN);

    private int despawning = -1;

    private BlockPos setPos;
    private World world;

    int maxLife = 80;

    public ConstructionBlockEntity(World worldIn) {
        super(BGEntities.CONSTRUCTION_BLOCK, worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
    }

    public ConstructionBlockEntity(World worldIn, BlockPos spawnPos, boolean makePaste) {
        super(BGEntities.CONSTRUCTION_BLOCK, worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        setPos = spawnPos;
        setMakingPaste(makePaste);
    }

    int getTicksExisted() {
        return ticksExisted;
    }

    @Override
    protected void registerData() {
        this.dataManager.register(FIXED, BlockPos.ORIGIN);
        this.dataManager.register(MAKING, false);
    }

    /**
     * Gets called every tick from main Entity class
     */
    @Override
    public void baseTick() {
        super.baseTick();
        if (ticksExisted > maxLife) {
            setDespawning();
        }
        if (setPos != null) {
            if (!(world.getBlockState(setPos).getBlock() instanceof ConstructionBlock) && !(world.getBlockState(setPos).getBlock() instanceof ConstructionBlockPowder)) {
                setDespawning();
            }
        }

        if (isDespawning()) {
            despawnTick();
        }
    }

    private boolean isDespawning() {
        return despawning != -1;
    }

    private void setDespawning() {
        if (despawning == -1) {
            despawning = 0;
            if (setPos != null) {
                if (!getMakingPaste()) {
                    TileEntity te = world.getTileEntity(setPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        IBlockState tempState = te.getBlockState();

                        int opacity = tempState.getOpacity(world, setPos);
                        boolean neighborBrightness = tempState.useNeighborBrightness(world, setPos);
                        if (opacity > 0 || neighborBrightness) {
                            IBlockState tempSetBlock = te.getBlockState();
                            IBlockState tempActualSetBlock = ((ConstructionBlockTileEntity) te).getActualBlockState();
                            world.setBlockState(setPos, BGBlocks.constructionBlock.getDefaultState()
                                    .with(ConstructionBlock.BRIGHT, opacity == 0)
                                    .with(ConstructionBlock.NEIGHBOR_BRIGHTNESS, neighborBrightness));
                            te = world.getTileEntity(setPos);
                            if (te instanceof ConstructionBlockTileEntity) {
                                ((ConstructionBlockTileEntity) te).setBlockState(tempSetBlock, tempActualSetBlock);
                            }
                        }
                    }
                } else if (world.getBlockState(setPos) == BGBlocks.constructionBlockPowder.getDefaultState()) {
                    world.setBlockState(setPos, BGBlocks.constructionBlockDense.getDefaultState());
                }
            }
        }
    }

    private void despawnTick() {
        despawning++;
        if (despawning > 1) {
            this.remove();
        }
    }

    public void setMakingPaste(Boolean paste) {
        this.dataManager.set(MAKING, paste);
    }

    public boolean getMakingPaste() {
        return this.dataManager.get(MAKING);
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        despawning = compound.getInt(NBTKeys.ENTITY_DESPAWNING);
        ticksExisted = compound.getInt(NBTKeys.ENTITY_TICKS_EXISTED);
        setPos = NBTUtil.readBlockPos(compound.getCompound(NBTKeys.ENTITY_SET_POS));
        setMakingPaste(compound.getBoolean("makingPaste"));
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        compound.setInt(NBTKeys.ENTITY_DESPAWNING, despawning);
        compound.setInt(NBTKeys.ENTITY_TICKS_EXISTED, ticksExisted);
        compound.setTag(NBTKeys.ENTITY_SET_POS, NBTUtil.writeBlockPos(setPos));
        compound.setBoolean("makingPaste", getMakingPaste());
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0; //After tr
    }

}
