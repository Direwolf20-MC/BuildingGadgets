package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.client.renderer.FakeTERWorld;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

        //if( this.isLinkable() )
        //BaseRenderer.renderLinkedInventoryOutline(evt, heldItem, player);
    }

    protected void bindBlocks() {
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
                .add(.005f, .005f, -.005f);

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();

        RenderSystem.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        RenderSystem.translated(-renderPos.getX(), -renderPos.getY(), -renderPos.getZ());
        RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.scalef(1.01f, 1.01f, 1.01f);
        GL14.glBlendColor(1F, 1F, 1F, 0.35f);

        getMc().getBlockRendererDispatcher().renderBlock(Blocks.YELLOW_STAINED_GLASS.getDefaultState(), evt.getMatrixStack(), IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer()), 0, 0);

        RenderSystem.popMatrix();
    }

    int getEnergy(PlayerEntity player, ItemStack heldItem) {
        LazyOptional<IEnergyStorage> energy = heldItem.getCapability(CapabilityEnergy.ENERGY);
        if (player.isCreative() || !energy.isPresent())
            return Integer.MAX_VALUE;

        return energy.orElseThrow(CapabilityNotPresentException::new).getEnergyStored();
    }

    long playerHasBlocks(ItemStack stack, PlayerEntity player, BlockState state) {
        long hasBlocks = InventoryHelper.countItem(stack, player, getCacheInventory());

        if (!state.hasTileEntity())
            hasBlocks = hasBlocks + InventoryHelper.countPaste(player);

        return hasBlocks;
    }

    ItemStack getItemStackForRender(BlockState state, PlayerEntity player, IWorld world) {
        ItemStack itemStack = ItemStack.EMPTY;

        // Todo: use blockdata apparently
//        if (state.getBlock().canSilkHarvest(state, world, BlockPos.ZERO, player))
//            itemStack = InventoryHelper.getSilkTouchDrop(state);
//        else
        itemStack = state.getBlock().getPickBlock(state, null, world, BlockPos.ZERO, player);


        return !itemStack.isEmpty() ? itemStack : state.getBlock().getPickBlock(state, null, world, BlockPos.ZERO, player);
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

    protected void renderMissingBlock(Matrix4f matrix, IVertexBuilder builder, BlockPos pos) {
        float red = 1;
        float green = 0;
        float blue = 0;
        float alpha = 0.35f;
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;
        renderBoxSolid(matrix, builder, x, y, z, xEnd, yEnd, zEnd, red, green, blue, alpha);
    }

    protected void renderBoxSolid(Matrix4f matrix, IVertexBuilder builder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        float xf = (float) x;
        float yf = (float) y;
        float zf = (float) z;
        float xEndf = (float) xEnd;
        float yEndf = (float) yEnd;
        float zEndf = (float) zEnd;

        builder.pos(matrix, xf, yf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yEndf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yEndf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yf, zf).color(red, green, blue, alpha).endVertex();
        //left-side
        builder.pos(matrix, xf, yf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yEndf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yEndf, zf).color(red, green, blue, alpha).endVertex();
        //bottom
        builder.pos(matrix, xf, yf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yf, zEndf).color(red, green, blue, alpha).endVertex();
        //top
        builder.pos(matrix, xEndf, yEndf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yEndf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yEndf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yEndf, zEndf).color(red, green, blue, alpha).endVertex();
        //right-side
        builder.pos(matrix, xEndf, yEndf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yf, zf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yEndf, zf).color(red, green, blue, alpha).endVertex();
        //back-side
        builder.pos(matrix, xEndf, yEndf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yEndf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xf, yf, zEndf).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, xEndf, yf, zEndf).color(red, green, blue, alpha).endVertex();
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
