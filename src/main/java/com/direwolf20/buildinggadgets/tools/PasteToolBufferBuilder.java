package com.direwolf20.buildinggadgets.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class PasteToolBufferBuilder {

    private static ToolDireBuffer bufferBuilder = new ToolDireBuffer(2097152);
    private static int counter = 0;


    public static BufferBuilder getBuffer() {
        return bufferBuilder;
    }

    public static void addMapToBuffer(ArrayList<BlockMap> sortedMapList, ItemStack stack) {

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        bufferBuilder.reset();
        //bufferBuilder.setTranslation(doubleX, doubleY, doubleZ);
        //bufferBuilder.setTranslation(0,1,0);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (BlockMap blockMap : sortedMapList) {
            BlockPos coordinate = blockMap.pos;
            IBlockState renderBlockState = blockMap.state;
            //
            IBakedModel model = dispatcher.getModelForState(renderBlockState);
            dispatcher.getBlockModelRenderer().renderModelFlat(Minecraft.getMinecraft().world, model, renderBlockState, new BlockPos(blockMap.xOffset, blockMap.yOffset, blockMap.zOffset), bufferBuilder, true, 0L);

        }
        //bufferBuilder.setTranslation(0,0,0);
        bufferBuilder.finishDrawing();
    }


    public static void draw(EntityPlayer player, double x, double y, double z, BlockPos startPos) {
        bufferBuilder.sortVertexData((float) (x - startPos.getX()), (float) ((y + player.getEyeHeight()) - startPos.getY()), (float) (z - startPos.getZ()));
        if (bufferBuilder.getVertexCount() > 0) {
            VertexFormat vertexformat = bufferBuilder.getVertexFormat();
            int i = vertexformat.getNextOffset();
            ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();

            for (int j = 0; j < list.size(); ++j) {
                VertexFormatElement vertexformatelement = list.get(j);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int k = vertexformatelement.getType().getGlConstant();
                int l = vertexformatelement.getIndex();
                bytebuffer.position(vertexformat.getOffset(j));

                // moved to VertexFormatElement.preDraw
                vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
            }

            GlStateManager.glDrawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
            int i1 = 0;

            for (int j1 = list.size(); i1 < j1; ++i1) {
                VertexFormatElement vertexformatelement1 = list.get(i1);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
                int k1 = vertexformatelement1.getIndex();

                // moved to VertexFormatElement.postDraw
                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
            }
        }
    }

}
