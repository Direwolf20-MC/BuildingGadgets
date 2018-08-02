package com.direwolf20.buildinggadgets.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class ConstructionBlockEntity extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);

    public int despawning = -1;
    public int maxLife = 20;
    private BlockPos setPos;
    private EntityLivingBase spawnedBy;
    World world;

    public ConstructionBlockEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
        System.out.println("ConstructionBlockEntity is alive!");
    }

    public ConstructionBlockEntity(World worldIn, BlockPos spawnPos) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        System.out.println("ConstructionBlockEntity is alive!");
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
            System.out.println("ConstructionBlockEntity is dead!");
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


}
