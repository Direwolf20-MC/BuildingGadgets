package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.client.renderer.FakeTERWorld;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class BaseRenderer {
    public static final BlockState AIR = Blocks.AIR.getDefaultState();

    private static final FakeTERWorld tileEntityWorld = new FakeTERWorld();
    private static final FakeBuilderWorld builderWorld = new FakeBuilderWorld();
    private static final Set<TileEntity> invalidTileEntities = new HashSet<>();

    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);


    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        // This is necessary to prevent issues with not rendering the overlay's at all (when Botania being present) - See #329 for more information
        bindBlocks();

        if( this.isLinkable() )
            BaseRenderer.renderLinkedInventoryOutline(heldItem, player);
    }

    protected void bindBlocks() {
        getMc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
    }

    private static void renderLinkedInventoryOutline(ItemStack item, PlayerEntity player) {
        // This is problematic as you use REMOTE_INVENTORY_POS to get the dimension instead of REMOTE_INVENTORY_DIM
        ResourceLocation dim = GadgetUtils.getDIMFromNBT(item, NBTKeys.REMOTE_INVENTORY_POS);
        BlockPos pos = GadgetUtils.getPOSFromNBT(item, NBTKeys.REMOTE_INVENTORY_POS);

        if (dim == null || pos == null)
            return;

        DimensionType dimension = DimensionType.byName(dim);
        if (dimension == null || player.dimension != dimension)
            return;

        Vec3d renderPos = BaseRenderer.getPlayerPos().subtract(pos.getX(), pos.getY(), pos.getZ()).add(.005f, .005f, -.005f);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        GlStateManager.translated(-renderPos.getX(), -renderPos.getY(), -renderPos.getZ());
        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scalef(1.01f, 1.01f, 1.01f);
        GL14.glBlendColor(1F, 1F, 1F, 0.35f);

        getMc().getBlockRendererDispatcher().renderBlockBrightness(Blocks.YELLOW_STAINED_GLASS.getDefaultState(), 1f);
        GlStateManager.popMatrix();
    }

    int getEnergy(PlayerEntity player, ItemStack heldItem) {
        LazyOptional<IEnergyStorage> energy = CapabilityUtil.EnergyUtil.getCap(heldItem);
        if (player.isCreative() || !energy.isPresent())
            return Integer.MAX_VALUE;

        return energy.orElseThrow(CapabilityNotPresentException::new).getEnergyStored();
    }

    protected BufferBuilder setupMissingRender() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        GL14.glBlendColor(1F, 1F, 1F, 0.3f); //Set the alpha of the blocks we are rendering
        bufferBuilder.begin(GL14.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        return bufferBuilder;
    }

    protected void teardownMissingRender() {
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected void renderBlock(BlockRendererDispatcher dispatcher, BufferBuilder builder, BlockState state, BlockPos pos, World world, Random random) {
        try {
            dispatcher.renderBlock(state, pos, world, builder, random, EmptyModelData.INSTANCE);
        } catch (Throwable t) {
            BuildingGadgets.LOG.trace("Block at {} with state {} threw exception, whilst rendering", pos, state, t);
        }
    }

    protected void renderBlockTile(BlockRendererDispatcher dispatcher, BlockState state) {
        try {
            dispatcher.renderBlockBrightness(state, 1f);
        } catch (Throwable t) {
            BuildingGadgets.LOG.trace("Block Tile with state {} threw exception, whilst rendering", state, t);
        }
    }

    protected void renderBlockOverlay(BufferBuilder builder, BlockPos pos, float red, float green, float blue, float alpha) {
        double x = pos.getX() - 0.01;
        double y = pos.getY() - 0.01;
        double z = pos.getZ() - 0.01;
        double xEnd = pos.getX() + 1.01;
        double yEnd = pos.getY() + 1.01;
        double zEnd = pos.getZ() + 1.01;

        renderBoxSolid(builder, x, y, z, xEnd, yEnd, zEnd, red, green, blue, alpha);
    }

    protected void renderMissingBlock(BufferBuilder bufferBuilder, BlockPos pos) {
        float red = .69f;
        float green = .18f;
        float blue = .14f;
        float alpha = 0.55f;

        renderBlockOverlay(bufferBuilder, pos, red, green, blue, alpha);
    }

    protected void renderBoxSolid(BufferBuilder bufferBuilder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, z).color(red, green, blue, alpha).endVertex();
        //left-side
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, z).color(red, green, blue, alpha).endVertex();
        //bottom
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, zEnd).color(red, green, blue, alpha).endVertex();
        //top
        bufferBuilder.pos(xEnd, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        //right-side
        bufferBuilder.pos(xEnd, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, yEnd, z).color(red, green, blue, alpha).endVertex();
        //back-side
        bufferBuilder.pos(xEnd, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, zEnd).color(red, green, blue, alpha).endVertex();
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

    static FakeBuilderWorld getBuilderWorld() {
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

    /**
     * Pulls the static player pos from TileEntityRender (updated from the worldRender)
     * into a Vec3d to be used to get the players pos.
     */
    static Vec3d getPlayerPos() {
        return new Vec3d(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
    }
}
