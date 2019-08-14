package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.client.renderer.FakeTERWorld;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseRenderer {
    public static final BlockState AIR = Blocks.AIR.getDefaultState();

    private static final FakeTERWorld tileEntityWorld = new FakeTERWorld();
    private static final FakeBuilderWorld builderWorld = new FakeBuilderWorld();
    private static final Set<TileEntity> invalidTileEntities = new HashSet<>();

    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);

    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        // This is necessary to prevent issues with not rendering the overlay's at all (when Botania being present) - See #329 for more information
        getMc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        if( this.isLinkable() )
            BaseRenderer.renderLinkedInventoryOutline(heldItem, player);
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

    long playerHasBlocks(ItemStack stack, PlayerEntity player, BlockState state) {
        long hasBlocks = InventoryHelper.countItem(stack, player, getCacheInventory());

        if ( !state.hasTileEntity() )
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
