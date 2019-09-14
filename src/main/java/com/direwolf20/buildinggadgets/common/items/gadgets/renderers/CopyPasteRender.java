package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.util.blocks.BlockMap;
import com.direwolf20.buildinggadgets.common.util.buffers.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.util.buffers.ToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ChestRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.List;

public class CopyPasteRender extends BaseRenderer {
    private ChestRenderer chestRenderer;

    public ChestRenderer getChestRenderer() {
        if (chestRenderer == null)
            chestRenderer = new ChestRenderer();
        return chestRenderer;
    }

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);

        if (OurItems.gadgetCopyPaste.getStartPos(heldItem) == null || OurItems.gadgetCopyPaste.getEndPos(heldItem) == null)
            return;

        Vec3d playerPos = getPlayerPos();
        String UUID = OurItems.gadgetCopyPaste.getUUID(heldItem);

        if(GadgetCopyPaste.getToolMode(heldItem) == GadgetCopyPaste.ToolMode.Copy)
            renderCopy(evt, player, heldItem, playerPos, UUID);
        else
            renderPaste(evt, player, heldItem, playerPos, UUID);
    }

    private void renderCopy(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem, Vec3d playerPos, String UUID) {
        BlockPos startPos = OurItems.gadgetCopyPaste.getStartPos(heldItem);
        BlockPos endPos = OurItems.gadgetCopyPaste.getEndPos(heldItem);
        BlockPos blankPos = new BlockPos(0, 0, 0);
        if (startPos == null || endPos == null || startPos.equals(blankPos) || endPos.equals(blankPos)) {
            return;
        }

        List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
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
        GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0

        GlStateManager.disableLighting();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        renderBox(tessellator, bufferbuilder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

        GlStateManager.lineWidth(1.0F);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

    private void renderPaste(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem, Vec3d playerPos, String UUID) {
        World world = player.world;

        //First check if we have an anchor, if not check if we're looking at a block, if not, exit
        BlockPos startPos = GadgetCopyPaste.getAnchor(heldItem);
        if (startPos == null) {
            startPos = VectorHelper.getPosLookingAt(player, heldItem);

            if (world.getBlockState(startPos) == AIR)
                return;

            startPos = startPos.up(GadgetCopyPaste.getY(heldItem));
            startPos = startPos.east(GadgetCopyPaste.getX(heldItem));
            startPos = startPos.south(GadgetCopyPaste.getZ(heldItem));
        } else {
            startPos = startPos.up(GadgetCopyPaste.getY(heldItem));
            startPos = startPos.east(GadgetCopyPaste.getX(heldItem));
            startPos = startPos.south(GadgetCopyPaste.getZ(heldItem));
        }

        //We store our buffers in PasteToolBufferBuilder (A client only class) -- retrieve the buffer from this locally cache'd map
        ToolBufferBuilder toolBufferBuilder = PasteToolBufferBuilder.getBufferFromMap(UUID);
        if (toolBufferBuilder == null) {
            return;
        }
        //Also get the blockMapList from the local cache - If either the buffer or the blockmap list are empty, exit.
        List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
        if (toolBufferBuilder.getVertexCount() == 0 || blockMapList.size() == 0) {
            return;
        }

        //Don't draw on top of blocks being built by our tools.
        BlockState startBlock = world.getBlockState(startPos);
        if (startBlock == OurBlocks.effectBlock.getDefaultState()) return;

        //Prepare the block rendering
        //BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

        //Save the current position that is being rendered
        GlStateManager.pushMatrix();

        //Enable Blending (So we can have transparent effect)
        GlStateManager.enableBlend();
        //This blend function allows you to use a constant alpha, which is defined later
        GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

        GlStateManager.pushMatrix();//Push matrix again just because
        GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
        GlStateManager.translatef(startPos.getX(), startPos.getY(), startPos.getZ()); //Move the render to the startingBlockPos

        GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
        //GlStateManager.translate(-0.0005f, -0.0005f, 0.0005f);
        //GlStateManager.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
        GlStateManager.translatef(0.0005f, 0.0005f, -0.0005f);
        GlStateManager.scalef(0.999f, 0.999f, 0.999f);//Slightly Larger block to avoid z-fighting.
        //GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        PasteToolBufferBuilder.draw(player, playerPos.getX(), playerPos.getY(), playerPos.getZ(), startPos, UUID); //Draw the cached buffer in the world.

        GlStateManager.popMatrix();
        //Set blending back to the default mode

        for (BlockMap blockMap : blockMapList) {
            BlockState state = blockMap.state.getState();
            if (state.hasTileEntity()) {
                TileEntity te = getTileEntityWorld().getTE(state, world);
                TileEntityRenderer<TileEntity> teRender = getTileEntityWorld().getTER(state, world);
                if (teRender != null) {
                    GlStateManager.pushMatrix();
                    GlStateManager.color4f(1F, 1F, 1F, 1F);
                    GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
                    GlStateManager.translatef(startPos.getX(), startPos.getY(), startPos.getZ());
                    GlStateManager.translatef(blockMap.xOffset, blockMap.yOffset, blockMap.zOffset);
                    GlStateManager.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                    GlStateManager.enableBlend(); //We have to do this in the loop because the TE Render removes blend when its done
                    GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
                    ItemStack renderStack = new ItemStack(state.getBlock());
                    if (renderStack.getItem().getTileEntityItemStackRenderer() != ItemStackTileEntityRenderer.instance || isVanillaISTER(renderStack)) {
                        GlStateManager.rotatef(- 90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                        getChestRenderer().renderChestBrightness(state.getBlock(), 1f);//Render the defined block
                    } else {
                        if (! getInvalidTileEntities().contains(te)) {
                            try {
                                TileEntityRendererDispatcher.instance.render(te, 0, 0, 0, evt.getPartialTicks(), - 1, true);
                            } catch (Exception e) {
                                System.out.println("TER Exception with block type: " + state);
                                getInvalidTileEntities().add(te);
                                GlStateManager.disableFog();
                                GlStateManager.popMatrix();
                            }
                        }
                    }
                    GlStateManager.disableFog();
                    GlStateManager.popMatrix();
                }
            }
        }


        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //Disable blend
        GlStateManager.disableBlend();
        //Pop from the original push in this method
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isLinkable() {
        return true;
    }

    // Todo: replace with something simpler
    private static boolean isVanillaISTER(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BedBlock)
            return true;
        else if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock)
            return true;
        else if (item instanceof BlockItem && ((BlockItem) item).getBlock() == Blocks.CONDUIT)
            return true;
        else if (item == Blocks.ENDER_CHEST.asItem())
            return true;
        else if (item == Blocks.TRAPPED_CHEST.asItem())
            return true;
        else if (Block.getBlockFromItem(item) instanceof ShulkerBoxBlock)
            return true;
        else if (Block.getBlockFromItem(item) instanceof ChestBlock)
            return true;
        else
            return false;
    }

    private static void renderBox(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, int R, int G, int B) {
        GlStateManager.lineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
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
        GlStateManager.lineWidth(1.0F);
    }
}
