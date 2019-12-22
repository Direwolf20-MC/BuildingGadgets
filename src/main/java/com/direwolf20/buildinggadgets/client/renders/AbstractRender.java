package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.RayTraceHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.energy.CapabilityEnergy;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.List;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.getAnchor;

public abstract class AbstractRender {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final BlockRendererDispatcher rendererDispatcher = mc.getBlockRendererDispatcher();
    public static final IBlockState emptyBlockState = Blocks.AIR.getDefaultState();
    private static final IBlockState effectBlockState = ModBlocks.effectBlock.getDefaultState();

    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);

    public abstract void gadgetRender(Tessellator tessellator, BufferBuilder bufferBuilder, RayTraceResult rayTraceResult, ItemStack gadget, List<BlockPos> existingLocations);

    public void render(RenderWorldLastEvent evt, EntityPlayer player, ItemStack gadget) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        Vec3d playerPos = new Vec3d(
                player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks(),
                player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks(),
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks()
        );

        // Setup and translate to the players pos
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.translate(-playerPos.x, -playerPos.y, -playerPos.z);

        // Check if we have a bound inventory
        Integer dim = GadgetUtils.getDIMFromNBT(gadget, "boundTE");
        BlockPos pos = GadgetUtils.getPOSFromNBT(gadget, "boundTE");

        if( dim != null && pos != null && dim == mc.player.dimension )
            renderSingleBlock(tessellator, bufferBuilder, pos, .94f, 1f, 0, .35f);


        // Validate that we should render
        RayTraceResult rayTraceResult = RayTraceHelper.rayTrace(mc.player, GadgetGeneric.shouldRayTraceFluid(gadget));
        List<BlockPos> existingLocations = getAnchor(gadget);

        // Perform a typical render
        if( existingLocations.size() == 0 ) {
            if( rayTraceResult != null && rayTraceResult.typeOfHit != RayTraceResult.Type.MISS) {
                IBlockState traceBlock = mc.player.world.getBlockState(rayTraceResult.getBlockPos());

                if( traceBlock != effectBlockState )
                    this.gadgetRender(tessellator, bufferBuilder, rayTraceResult, gadget, existingLocations);
            }
        }
        else
            this.gadgetRender(tessellator, bufferBuilder, rayTraceResult, gadget, existingLocations); // render using anchor

        // Reset the majority of what we changed
        GlStateManager.translate(0, 0, 0);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
    }

    // Figure out how many of the block we're rendering we have in the inventory of the player.
    // todo: figure out if this actually is working as intended.
    public static ItemStack getItemWithSilk(IBlockState gadgetBlock) {
        ItemStack itemStack;
        if (gadgetBlock.getBlock().canSilkHarvest(mc.player.world, BlockPos.ORIGIN, gadgetBlock, mc.player))
            itemStack = InventoryManipulation.getSilkTouchDrop(gadgetBlock);
        else
            itemStack = gadgetBlock.getBlock().getPickBlock(gadgetBlock, null, mc.player.world, BlockPos.ORIGIN, mc.player);

        if (itemStack.getItem().equals(Items.AIR))
            itemStack = gadgetBlock.getBlock().getPickBlock(gadgetBlock, null, mc.player.world, BlockPos.ORIGIN, mc.player);

        return itemStack;
    }

    public static long getItemCount(ItemStack itemStack) {
        return getItemCount(itemStack, cacheInventory);
    }

    public static long getItemCount(ItemStack itemStack, RemoteInventoryCache cacheInventory) {
        long hasBlocks = InventoryManipulation.countItem(itemStack, mc.player, cacheInventory);
        return hasBlocks + InventoryManipulation.countPaste(mc.player);
    }

    public static int getGadgetEnergy(ItemStack stack) {
        if (mc.player.capabilities.isCreativeMode || (!stack.hasCapability(CapabilityEnergy.ENERGY, null) && !stack.isItemStackDamageable()))
            return 1000000;

        return stack.hasCapability(CapabilityEnergy.ENERGY, null)
                ? CapabilityProviderEnergy.getCap(stack).getEnergyStored()
                : stack.getMaxDamage() - stack.getItemDamage();
    }

    public static void renderSingleBlock(Tessellator tessellator, BufferBuilder bufferBuilder, BlockPos pos, float red, float green, float blue, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        GL14.glBlendColor(1F, 1F, 1F, alpha);

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderBoundingBox(bufferBuilder,
                pos.getX() - 0.01,
                pos.getY() - 0.01,
                pos.getZ() - 0.01,
                pos.getX() + 1.01,
                pos.getY() + 1.01,
                pos.getZ() + 1.01,
                red, green, blue, alpha
        );
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.popMatrix();
    }

    private static void renderBoundingBox(BufferBuilder bufferBuilder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
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
}
