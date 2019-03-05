package com.direwolf20.buildinggadgets.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UniqueBlockState implements IBlockState {

    public static final IBlockState AIR = new UniqueBlockState(-1) {
        @Override
        public Material getMaterial() {
            return Material.AIR;
        }
    };

    private static int lastId = -1;

    public static UniqueBlockState createWithId(int id) {
        return new UniqueBlockState(id);
    }

    public static UniqueBlockState createNew() {
        return new UniqueBlockState(++lastId);
    }

    private final int id;

    private UniqueBlockState(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UniqueBlockState{" + id + "}";
    }

    @Override
    public Material getMaterial() {
        return Material.ROCK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueBlockState that = (UniqueBlockState) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //All of these methods should not be used as testing the placement logic should only need to access blockstates

    @Override
    public Collection<IProperty<?>> getPropertyKeys() {
        return null;
    }

    @Override
    public <T extends Comparable<T>> T getValue(IProperty<T> iProperty) {
        return null;
    }

    @Override
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> iProperty, V v) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> iProperty) {
        return null;
    }

    @Override
    public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
        return null;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public boolean onBlockEventReceived(World world, BlockPos blockPos, int i, int i1) {
        return false;
    }

    @Override
    public void neighborChanged(World world, BlockPos blockPos, Block block, BlockPos blockPos1) {

    }

    @Override
    public boolean isFullBlock() {
        return false;
    }

    @Override
    public boolean canEntitySpawn(Entity entity) {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public int getLightOpacity(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return 0;
    }

    @Override
    public int getLightValue() {
        return 0;
    }

    @Override
    public int getLightValue(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return 0;
    }

    @Override
    public boolean isTranslucent() {
        return false;
    }

    @Override
    public boolean useNeighborBrightness() {
        return false;
    }

    @Override
    public MapColor getMapColor(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return null;
    }

    @Override
    public IBlockState withRotation(Rotation rotation) {
        return null;
    }

    @Override
    public IBlockState withMirror(Mirror mirror) {
        return null;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean hasCustomBreakingProgress() {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType() {
        return null;
    }

    @Override
    public int getPackedLightmapCoords(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return 0;
    }

    @Override
    public float getAmbientOcclusionLightValue() {
        return 0;
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean canProvidePower() {
        return false;
    }

    @Override
    public int getWeakPower(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return 0;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return false;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos blockPos) {
        return 0;
    }

    @Override
    public float getBlockHardness(World world, BlockPos blockPos) {
        return 0;
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer entityPlayer, World world, BlockPos blockPos) {
        return 0;
    }

    @Override
    public int getStrongPower(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return 0;
    }

    @Override
    public EnumPushReaction getMobilityFlag() {
        return null;
    }

    @Override
    public IBlockState getActualState(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos blockPos) {
        return null;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return null;
    }

    @Override
    public void addCollisionBoxToList(World world, BlockPos blockPos, AxisAlignedBB axisAlignedBB, List<AxisAlignedBB> list, @Nullable Entity entity, boolean b) {

    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return null;
    }

    @Override
    public RayTraceResult collisionRayTrace(World world, BlockPos blockPos, Vec3d vec3d, Vec3d vec3d1) {
        return null;
    }

    @Override
    public boolean isTopSolid() {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return false;
    }

    @Override
    public boolean doesSideBlockChestOpening(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return false;
    }

    @Override
    public Vec3d getOffset(IBlockAccess iBlockAccess, BlockPos blockPos) {
        return null;
    }

    @Override
    public boolean causesSuffocation() {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess iBlockAccess, BlockPos blockPos, EnumFacing enumFacing) {
        return null;
    }

}
