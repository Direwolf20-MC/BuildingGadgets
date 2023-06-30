package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;
import com.direwolf20.buildinggadgets.common.world.MockTileEntityRenderWorld;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseRenderer {
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private static final MockTileEntityRenderWorld tileEntityWorld = new MockTileEntityRenderWorld();
    private static final MockBuilderWorld builderWorld = new MockBuilderWorld();
    private static final Set<BlockEntity> invalidTileEntities = new HashSet<>();

    private static final RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);

    public void render(RenderLevelStageEvent evt, Player player, ItemStack heldItem) {
        // FIXME: might be wrong
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        // This is necessary to prevent issues with not rendering the overlay's at all (when Botania being present) - See #329 for more information
        bindBlocks();

        if( this.isLinkable() )
            BaseRenderer.renderLinkedInventoryOutline(evt, heldItem, player);
    }

    private void bindBlocks() {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
    }

    private static void renderLinkedInventoryOutline(RenderLevelStageEvent evt, ItemStack item, Player player) {
        Pair<BlockPos, ResourceKey<Level>> dataFromStack = InventoryLinker.getDataFromStack(item);
        if (dataFromStack == null) {
            return;
        }

        if (!player.level().dimension().equals(dataFromStack.getValue())) {
            return;
        }

        BlockPos pos = dataFromStack.getKey();
        Vec3 renderPos = getMc().gameRenderer.getMainCamera().getPosition()
                .subtract(pos.getX(), pos.getY(), pos.getZ())
                .add(.005f, .005f, .005f);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        PoseStack stack = evt.getPoseStack();
        stack.pushPose();
        stack.translate(-renderPos.x(), -renderPos.y(), -renderPos.z());
        stack.scale(1.01f, 1.01f, 1.01f);

        renderBoxSolid(stack.last().pose(), buffer.getBuffer(OurRenderTypes.BlockOverlay), BlockPos.ZERO, 0, 1, 0, .35f);

        stack.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch(); // @mcp: finish (mcp) = draw (yarn)
    }


    int getEnergy(Player player, ItemStack heldItem) {
        LazyOptional<IEnergyStorage> energy = heldItem.getCapability(ForgeCapabilities.ENERGY);
        if (player.isCreative() || !energy.isPresent())
            return Integer.MAX_VALUE;

        return energy.map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    protected static void renderMissingBlock(Matrix4f matrix, VertexConsumer builder, BlockPos pos) {
        renderBoxSolid(matrix, builder, pos, 1f, 0f, 0f, 0.35f);
    }

    protected static void renderBoxSolid(Matrix4f matrix, VertexConsumer builder, BlockPos pos, float r, float g, float b, float alpha) {
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;

        renderBoxSolid(matrix, builder, x, y, z, xEnd, yEnd, zEnd, r, g, b, alpha);
    }

    protected static void renderBoxSolid(Matrix4f matrix, VertexConsumer builder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        float startX = (float) x;
        float startY = (float) y;
        float startZ = (float) z;
        float endX = (float) xEnd;
        float endY = (float) yEnd;
        float endZ = (float) zEnd;

//        float startX = 0, startY = 0, startZ = -1, endX = 1, endY = 1, endZ = 0;

        //down
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
    }

    /**
     * Can the gadget be linked to other inventories?
     */
    public boolean isLinkable() {
        return false;
    }

    static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    static MockBuilderWorld getBuilderWorld() {
        return builderWorld;
    }

    static RemoteInventoryCache getCacheInventory() {
        return cacheInventory;
    }

    public static void setInventoryCache(Multiset<UniqueItem> cache) {
        BaseRenderer.cacheInventory.setCache(cache);
    }

    public static void updateInventoryCache() {
        cacheInventory.forceUpdate();
    }
}
