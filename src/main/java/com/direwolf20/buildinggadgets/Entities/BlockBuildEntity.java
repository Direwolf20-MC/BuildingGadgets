package com.direwolf20.buildinggadgets.Entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class BlockBuildEntity extends Entity implements IEntityAdditionalSpawnData {

    public int despawning = -1;
    public int maxLife = 80;
    private IBlockState setBlock;
    private BlockPos setPos;
    World world;
    private static DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);

    public BlockBuildEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
    }

    public BlockBuildEntity(World worldIn, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        setPos = spawnPos;
        setBlock = spawnBlock;
        world = worldIn;
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
    public boolean shouldRenderInPass(int pass) {
        return pass == 1; //After tr
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

        }else {
            despawnTick();
        }
    }

    public boolean isDespawning() {
        return despawning != -1;
    }

    private void setDespawning() {
        if(despawning == -1) {
            despawning = 0;
            if (setPos != null && setBlock != null) {
                world.setBlockState(setPos, setBlock);
            }
        }
    }

    private void despawnTick() {
        despawning++;
        if(despawning > 1) {
            setDead();
            //System.out.println(setPos+":"+setBlock);
        }
    }

    @Override
    public void setDead()
    {
        this.isDead = true;
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

}
