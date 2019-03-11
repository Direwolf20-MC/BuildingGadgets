package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
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
    public int maxLife = 80;
    private BlockPos setPos;
    //    private EntityLivingBase spawnedBy;
    private World world;

    public ConstructionBlockEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
    }

    public ConstructionBlockEntity(World worldIn, BlockPos spawnPos, boolean makePaste) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        setPos = spawnPos;
        setMakingPaste(makePaste);
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
        if (setPos != null) {
            if (!(world.getBlockState(setPos).getBlock() instanceof ConstructionBlock) && !(world.getBlockState(setPos).getBlock() instanceof ConstructionBlockPowder)) {
                setDespawning();
            }
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
            if (setPos != null) {
                if (!getMakingPaste()) {
                    TileEntity te = world.getTileEntity(setPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        IBlockState tempState = ((ConstructionBlockTileEntity) te).getBlockState();
                        if (tempState == null)
                            return;

                        int opacity = tempState.getBlock().getLightOpacity(tempState, world, setPos);
                        boolean neighborBrightness = tempState.getBlock().getUseNeighborBrightness(tempState);
                        if (opacity == 255 || neighborBrightness) {
                            IBlockState tempSetBlock = ((ConstructionBlockTileEntity) te).getBlockState();
                            IBlockState tempActualSetBlock = ((ConstructionBlockTileEntity) te).getActualBlockState();
                            world.setBlockState(setPos, ModBlocks.constructionBlock.getDefaultState()
                                    .withProperty(ConstructionBlock.BRIGHT, opacity != 255)
                                    .withProperty(ConstructionBlock.NEIGHBOR_BRIGHTNESS, neighborBrightness));
                            te = world.getTileEntity(setPos);
                            if (te instanceof ConstructionBlockTileEntity) {
                                ((ConstructionBlockTileEntity) te).setBlockState(tempSetBlock);
                                ((ConstructionBlockTileEntity) te).setActualBlockState(tempActualSetBlock);
                            }
                        }
                    }
                } else if (world.getBlockState(setPos) == ModBlocks.constructionBlockPowder.getDefaultState()) {
                    world.setBlockState(setPos, ModBlocks.constructionBlockDense.getDefaultState());
                }
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

    public void setMakingPaste(Boolean paste) {
        this.dataManager.set(MAKING, paste);
    }

    public boolean getMakingPaste() {
        return this.dataManager.get(MAKING);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("despawning", despawning);
        compound.setInteger("ticksExisted", ticksExisted);
        compound.setTag("setPos", NBTUtil.createPosTag(setPos));
        compound.setBoolean("makingPaste", getMakingPaste());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        //System.out.println(compound);
        despawning = compound.getInteger("despawning");
        ticksExisted = compound.getInteger("ticksExisted");
        setPos = NBTUtil.getPosFromTag(compound.getCompoundTag("setPos"));
        setMakingPaste(compound.getBoolean("makingPaste"));
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(FIXED, BlockPos.ORIGIN);
        this.dataManager.register(MAKING, false);
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0; //After tr
    }

}
