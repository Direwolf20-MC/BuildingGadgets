package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class PasteToolBufferBuilder {

    private static Map<String, NBTTagCompound> tagMap = new HashMap<String, NBTTagCompound>();
    private static Map<String, ToolBufferBuilder> bufferMap = new HashMap<String, ToolBufferBuilder>();


    private static int getCopyCounter(String UUID) {
        if (tagMap.containsKey(UUID)) {
            return tagMap.get(UUID).getInt("copycounter");
        }
        return -1;
    }

    public static void clearMaps() {
        tagMap = new HashMap<String, NBTTagCompound>();
        bufferMap = new HashMap<String, ToolBufferBuilder>();
    }

    public static void addToMap(String UUID, NBTTagCompound tag) {
        tagMap.put(UUID, tag);
    }

    @Nullable
    public static NBTTagCompound getTagFromUUID(String UUID) {
        if (tagMap.containsKey(UUID)) {
            return tagMap.get(UUID);
        }
        return null;
    }

    @Nullable
    public static ToolBufferBuilder getBufferFromMap(String UUID) {
        if (bufferMap.containsKey(UUID)) {
            return bufferMap.get(UUID);
        }
        return null;
    }

    public static void addMapToBuffer(String UUID) {
//        long time = System.nanoTime();
        List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(tagMap.get(UUID));
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        ToolBufferBuilder bufferBuilder = new ToolBufferBuilder(2097152);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (BlockMap blockMap : blockMapList) {
            IBlockState renderBlockState = blockMap.state;
            if (!(renderBlockState.equals(Blocks.AIR.getDefaultState()))) {
                IBakedModel model = dispatcher.getModelForState(renderBlockState);
                dispatcher.getBlockModelRenderer().renderModelFlat(Minecraft.getInstance().world, model, renderBlockState, new BlockPos(blockMap.xOffset, blockMap.yOffset, blockMap.zOffset), bufferBuilder, false, new Random(), 0L);
            }
        }
        bufferBuilder.finishDrawing();
        bufferMap.put(UUID, bufferBuilder);
        //System.out.printf("Created %d Vertexes for %d blocks in %.2f ms%n", bufferBuilder.getVertexCount(), blockMapList.size(), (System.nanoTime() - time) * 1e-6);
    }

    public static void draw(EntityPlayer player, double x, double y, double z, BlockPos startPos, String UUID) {
//        long time = System.nanoTime();
        ToolBufferBuilder bufferBuilder = bufferMap.get(UUID);
        bufferBuilder.sortVertexData((float) (x - startPos.getX()), (float) ((y + player.getEyeHeight()) - startPos.getY()), (float) (z - startPos.getZ()));
        //System.out.printf("Sorted %d Vertexes in %.2f ms%n", bufferBuilder.getVertexCount(), (System.nanoTime() - time) * 1e-6);
        if (bufferBuilder.getVertexCount() > 0) {
            VertexFormat vertexformat = bufferBuilder.getVertexFormat();
            int i = vertexformat.getSize();
            ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();

            for (int j = 0; j < list.size(); ++j) {
                VertexFormatElement vertexformatelement = list.get(j);
//                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
//                int k = vertexformatelement.getType().getGlConstant();
//                int l = vertexformatelement.getIndex();
                bytebuffer.position(vertexformat.getOffset(j));

                // moved to VertexFormatElement.preDraw
                vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
            }

            GlStateManager.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
            int i1 = 0;

            for (int j1 = list.size(); i1 < j1; ++i1) {
                VertexFormatElement vertexformatelement1 = list.get(i1);
//                VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
//                int k1 = vertexformatelement1.getIndex();

                // moved to VertexFormatElement.postDraw
                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
            }
        }
    }

    public static boolean isUpdateNeeded(String UUID, ItemStack stack) {
        return ((((GadgetCopyPaste) BGItems.gadgetCopyPaste).getCopyCounter(stack) != getCopyCounter(UUID) || PasteToolBufferBuilder.getTagFromUUID(UUID) == null));
    }
}
