package com.direwolf20.buildinggadgets.entities;

import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlockTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConstructionBlockEntity extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);

    public int despawning = -1;
    public int maxLife = 80;
    private BlockPos setPos;
    private EntityLivingBase spawnedBy;
    World world;

    public ConstructionBlockEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
    }

    public ConstructionBlockEntity(World worldIn, BlockPos spawnPos) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        setPos = spawnPos;
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
            if (setPos != null) {
                TileEntity te = world.getTileEntity(setPos);
                if (te instanceof ConstructionBlockTileEntity) {
                    IBlockState tempState = ((ConstructionBlockTileEntity) te).getBlockState();
                    int opacity = tempState.getBlock().getLightOpacity(tempState, world, setPos);
                    if (opacity == 255) {
                        IBlockState tempSetBlock = ((ConstructionBlockTileEntity) te).getBlockState();
                        IBlockState tempActualSetBlock = ((ConstructionBlockTileEntity) te).getActualBlockState();
                        world.setBlockState(setPos, ModBlocks.constructionBlock.getDefaultState().withProperty(ConstructionBlock.BRIGHT, false));
                        te = world.getTileEntity(setPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            ((ConstructionBlockTileEntity) te).setBlockState(tempSetBlock);
                            ((ConstructionBlockTileEntity) te).setActualBlockState(tempActualSetBlock);
                        }
                    }

                    //((ConstructionBlockTileEntity) te).updateLighting();
                    //((ConstructionBlockTileEntity) te).markDirtyClient();
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

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        int id = additionalData.readInt();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        int id = -1;
        buffer.writeInt(id);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void entityInit() {
        this.dataManager.register(FIXED, BlockPos.ORIGIN);
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0; //After tr
    }

    /*@SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (setPos != null) {
            int xCoord = setPos.getX();
            int yCoord = setPos.getY();
            int zCoord = setPos.getZ();

            return new AxisAlignedBB(xCoord - 7, yCoord - 7, zCoord - 7, xCoord + 8, yCoord + 7, zCoord + 8);
        } else {
            return super.getRenderBoundingBox();
        }
    }*/

}
