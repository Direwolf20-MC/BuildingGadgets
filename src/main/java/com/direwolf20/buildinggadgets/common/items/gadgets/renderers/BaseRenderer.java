package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.client.renderer.FakeTERWorld;
import com.direwolf20.buildinggadgets.client.renderer.MyRenderType;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseRenderer {
    public static final BlockState AIR = Blocks.AIR.getDefaultState();

    private static final FakeTERWorld tileEntityWorld = new FakeTERWorld();
    private static final MockBuilderWorld builderWorld = new MockBuilderWorld();
    private static final Set<TileEntity> invalidTileEntities = new HashSet<>();

    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);

    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        // This is necessary to prevent issues with not rendering the overlay's at all (when Botania being present) - See #329 for more information
        bindBlocks();

        if( this.isLinkable() )
            BaseRenderer.renderLinkedInventoryOutline(evt, heldItem, player);
    }

    private void bindBlocks() {
        getMc().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
    }

    private static void renderLinkedInventoryOutline(RenderWorldLastEvent evt, ItemStack item, PlayerEntity player) {
        // This is problematic as you use REMOTE_INVENTORY_POS to get the dimension instead of REMOTE_INVENTORY_DIM
        ResourceLocation dim = GadgetUtils.getDIMFromNBT(item, NBTKeys.REMOTE_INVENTORY_POS);
        BlockPos pos = GadgetUtils.getPOSFromNBT(item, NBTKeys.REMOTE_INVENTORY_POS);

        if (dim == null || pos == null)
            return;

        DimensionType dimension = DimensionType.byName(dim);
        if (dimension == null || player.dimension != dimension)
            return;

        Vec3d renderPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView()
                .subtract(pos.getX(), pos.getY(), pos.getZ())
                .add(.005f, .005f, .005f);

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

        MatrixStack stack = evt.getMatrixStack();
        stack.push();
        stack.translate(-renderPos.getX(), -renderPos.getY(), -renderPos.getZ());
        stack.scale(1.01f, 1.01f, 1.01f);

        renderBoxSolid(stack.getLast().getMatrix(), buffer.getBuffer(MyRenderType.BlockOverlay), BlockPos.ZERO, 0, 1, 0, .35f);

        stack.pop();
        RenderSystem.disableDepthTest();
        buffer.finish();
    }


    int getEnergy(PlayerEntity player, ItemStack heldItem) {
        LazyOptional<IEnergyStorage> energy = heldItem.getCapability(CapabilityEnergy.ENERGY);
        if (player.isCreative() || !energy.isPresent())
            return Integer.MAX_VALUE;

        return energy.map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    /*protected BufferBuilder setupMissingRender() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.0001F);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        GL14.glBlendColor(1F, 1F, 1F, 0.3f); //Set the alpha of the blocks we are rendering
        bufferBuilder.begin(GL14.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        return bufferBuilder;
    }

    protected void teardownMissingRender() {
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }*/

    protected static void renderMissingBlock(Matrix4f matrix, IVertexBuilder builder, BlockPos pos) {
        renderBoxSolid(matrix, builder, pos, 1, 0, 0, 0.35f);
    }

    protected static void renderBoxSolid(Matrix4f matrix, IVertexBuilder builder, BlockPos pos, int r, int g, int b, float alpha) {
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;

        renderBoxSolid(matrix, builder, x, y, z, xEnd, yEnd, zEnd, r, g, b, alpha);
    }

    protected static void renderBoxSolid(Matrix4f matrix, IVertexBuilder builder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
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
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        builder.pos(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        builder.pos(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
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

    static FakeTERWorld getTileEntityWorld() {
        return tileEntityWorld;
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

    static Set<TileEntity> getInvalidTileEntities() {
        return invalidTileEntities;
    }
}
