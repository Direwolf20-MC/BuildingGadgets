package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.List;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.getAnchor;

public abstract class AbstractRender {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final BlockRendererDispatcher rendererDispatcher = mc.getBlockRendererDispatcher();
    public static final IBlockState emptyBlockState = Blocks.AIR.getDefaultState();
    private static final IBlockState effectBlockState = ModBlocks.effectBlock.getDefaultState();

    public abstract void gadgetRender(Tessellator tessellator, BufferBuilder bufferBuilder, RayTraceResult rayTraceResult, IBlockState traceBlock, ItemStack gadget, List<BlockPos> existingLocations);

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
            renderSingleBlock(tessellator, bufferBuilder, pos, .94f, 1f, 0, .55f);

        // Validate that we should render
        RayTraceResult rayTraceResult = player.rayTrace(Config.rayTraceRange, evt.getPartialTicks());
        if( rayTraceResult != null && rayTraceResult.typeOfHit != RayTraceResult.Type.MISS) {
            IBlockState traceBlock = mc.player.world.getBlockState(rayTraceResult.getBlockPos());

            if( traceBlock != effectBlockState ) {
                List<BlockPos> existingLocations = getAnchor(gadget);
                this.gadgetRender(tessellator, bufferBuilder, rayTraceResult, traceBlock, gadget, existingLocations);
            }
        }

        // Reset the majority of what we changed
        GlStateManager.translate(0, 0, 0);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
    }

    public static void renderSingleBlock(Tessellator tessellator, BufferBuilder bufferBuilder, BlockPos pos, float red, float green, float blue, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

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
