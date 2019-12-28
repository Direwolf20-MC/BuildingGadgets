package com.direwolf20.buildinggadgets.common.utils;

import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.gadgets.CopyGadget;
import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.ModItems;
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
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.List;

import static net.minecraft.block.BlockStainedGlass.COLOR;

public class ToolRenders {
    public static void renderPasteOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        Integer dim = GadgetUtils.getDIMFromNBT(stack, "boundTE");
        BlockPos pos = GadgetUtils.getPOSFromNBT(stack, "boundTE");

        //Calculate the players current position, which is needed later
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

        Minecraft mc = Minecraft.getMinecraft();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (dim != null && pos != null) {
            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translate(-0.005f, -0.005f, 0.005f);
            GlStateManager.scale(1.01f, 1.01f, 1.01f);
            GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
            dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR, EnumDyeColor.YELLOW), 1f);
            GlStateManager.popMatrix();
        }

        String UUID = ModItems.copyGadget.getUUID(stack);
        World world = player.world;
        if (ModItems.copyGadget.getStartPos(stack) == null) return;
        if (ModItems.copyGadget.getEndPos(stack) == null) return;
        if (CopyGadget.getToolMode(stack) == CopyGadget.ToolMode.Paste) {
            //First check if we have an anchor, if not check if we're looking at a block, if not, exit
            BlockPos startPos = CopyGadget.getAnchor(stack);
            if (startPos == null) {
                RayTraceResult trace = RayTraceHelper.rayTrace(player, AbstractGadget.shouldRayTraceFluid(stack));
                if (trace == null)
                    return;

                startPos = trace.getBlockPos();
                startPos = startPos.up(CopyGadget.getY(stack));
                startPos = startPos.east(CopyGadget.getX(stack));
                startPos = startPos.south(CopyGadget.getZ(stack));
            } else {
                startPos = startPos.up(CopyGadget.getY(stack));
                startPos = startPos.east(CopyGadget.getX(stack));
                startPos = startPos.south(CopyGadget.getZ(stack));
            }

            //We store our buffers in PasteToolBufferBuilder (A client only class) -- retrieve the buffer from this locally cache'd map
            ReverseBufferBuilder toolDireBuffer = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (toolDireBuffer == null) {
                return;
            }
            //Also get the blockMapList from the local cache - If either the buffer or the blockmap list are empty, exit.
            List<BlockMap> blockMapList = CopyGadget.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (toolDireBuffer.getVertexCount() == 0 || blockMapList.size() == 0) {
                return;
            }

            //Don't draw on top of blocks being built by our utils.
            IBlockState startBlock = world.getBlockState(startPos);
            if (startBlock == ModBlocks.effectBlock.getDefaultState()) return;

            //Prepare the block rendering
            //BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            //Save the current position that is being rendered
            GlStateManager.pushMatrix();

            //Enable Blending (So we can have transparent effect)
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translate(startPos.getX(), startPos.getY(), startPos.getZ()); //Move the render to the startingBlockPos
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
            //GlStateManager.translate(-0.0005f, -0.0005f, 0.0005f);
            //GlStateManager.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
            GlStateManager.translate(0.0005f, 0.0005f, -0.0005f);
            GlStateManager.scale(0.999f, 0.999f, 0.999f);//Slightly Larger block to avoid z-fighting.
            //GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            PasteToolBufferBuilder.draw(player, doubleX, doubleY, doubleZ, startPos, UUID); //Draw the cached buffer in the world.

            GlStateManager.popMatrix();
            //Set blending back to the default mode

            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //Disable blend
            GlStateManager.disableBlend();
            //Pop from the original push in this method
            GlStateManager.popMatrix();

        } else {
            BlockPos startPos = ModItems.copyGadget.getStartPos(stack);
            BlockPos endPos = ModItems.copyGadget.getEndPos(stack);
            BlockPos blankPos = BlockPos.ORIGIN;
            if (startPos == null || endPos == null || startPos.equals(blankPos) || endPos.equals(blankPos)) {
                return;
            }


            List<BlockMap> blockMapList = CopyGadget.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (blockMapList.size() == 0) {
                //return;
            }

            //We want to draw from the starting position to the (ending position)+1
            int x = (startPos.getX() <= endPos.getX()) ? startPos.getX() : endPos.getX();
            int y = (startPos.getY() <= endPos.getY()) ? startPos.getY() : endPos.getY();
            int z = (startPos.getZ() <= endPos.getZ()) ? startPos.getZ() : endPos.getZ();
            int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
            int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
            int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0

            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            renderBox(tessellator, bufferbuilder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

            GlStateManager.glLineWidth(1.0F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);

            GlStateManager.popMatrix();

        }
    }

    private static void renderBox(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, int R, int G, int B) {
        GlStateManager.glLineWidth(2.0F);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, B, B, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(B, B, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(B, G, B, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.glLineWidth(1.0F);
    }
}
