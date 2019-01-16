package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.api.BlockMap;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class PasteToolBufferBuilder {

    private static Map<UUID, NBTTagCompound> tagMap = new HashMap<>();
    private static Map<UUID, ToolDireBuffer> bufferMap = new HashMap<>();


    private static int getCopyCounter(UUID uuid) {
        if (tagMap.containsKey(uuid)) {
            return tagMap.get(uuid).getInteger("copycounter");
        }
        return -1;
    }

    public static void clearMaps() {
        tagMap.clear();
        bufferMap.clear();
    }

    public static void addToMap(UUID uuid, NBTTagCompound tag) {
        tagMap.put(uuid, tag);
        addMapToBuffer(uuid,tag);
    }

    @Nullable
    public static NBTTagCompound getTagFromUUID(UUID uuid) {
        if (tagMap.containsKey(uuid)) {
            return tagMap.get(uuid);
        }
        return null;
    }

    @Nullable
    public static ToolDireBuffer getBufferFromMap(UUID uuid) {
        if (bufferMap.containsKey(uuid)) {
            return bufferMap.get(uuid);
        }
        return null;
    }

    private static void addMapToBuffer(UUID uuid,NBTTagCompound compound) {
//        long time = System.nanoTime();
        List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(compound);
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        ToolDireBuffer bufferBuilder = new ToolDireBuffer(2097152);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (BlockMap blockMap : blockMapList) {
            IBlockState renderBlockState = blockMap.getState();
            if (!(renderBlockState.equals(Blocks.AIR.getDefaultState()))) {
                IBakedModel model = dispatcher.getModelForState(renderBlockState);
                dispatcher.getBlockModelRenderer().renderModelFlat(Minecraft.getMinecraft().world, model, renderBlockState, new BlockPos(blockMap.getXOffset(), blockMap.getYOffset(), blockMap.getZOffset()), bufferBuilder, false, 0L);
            }
        }
        bufferBuilder.finishDrawing();
        bufferMap.put(uuid, bufferBuilder);
        //System.out.printf("Created %d Vertexes for %d blocks in %.2f ms%n", bufferBuilder.getVertexCount(), blockMapList.size(), (System.nanoTime() - time) * 1e-6);
    }

    public static void draw(EntityPlayer player, double x, double y, double z, BlockPos startPos, UUID uuid) {
//        long time = System.nanoTime();
        ToolDireBuffer bufferBuilder = bufferMap.get(uuid);
        //System.out.printf("Sorted %d Vertexes in %.2f ms%n", bufferBuilder.getVertexCount(), (System.nanoTime() - time) * 1e-6);
        if (bufferBuilder.getVertexCount() > 0) {
            bufferBuilder.sortVertexData((float) (x - startPos.getX()), (float) ((y + player.getEyeHeight()) - startPos.getY()), (float) (z - startPos.getZ()));
            VertexFormat vertexformat = bufferBuilder.getVertexFormat();
            int i = vertexformat.getNextOffset();
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

            GlStateManager.glDrawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
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

    public static boolean isUpdateNeeded(UUID uuid, ItemStack stack) {
        return ((ModItems.gadgetCopyPaste.getCopyCounter(stack) != getCopyCounter(uuid) || PasteToolBufferBuilder.getTagFromUUID(uuid) == null));
    }
}
