package com.direwolf20.buildinggadgets.entities;

import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.items.FakeBuilderWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ConstructionBlockEntityRender extends Render<ConstructionBlockEntity> {

    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public ConstructionBlockEntityRender(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(ConstructionBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        int teCounter = entity.getTicksExisted();
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {
            teCounter = maxLife;
        }
        float scale = (float) (maxLife - teCounter) / maxLife;
        GlStateManager.translate(x, y, z);
        //GlStateManager.translate(trans, trans, trans);
        GlStateManager.translate(-0.005f, -0.005f, -0.005f);
        GlStateManager.scale(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);

        GL14.glBlendColor(1F, 1F, 1F, scale); //Set the alpha of the blocks we are rendering
        //IBlockState renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        IBlockState renderBlockState = ModBlocks.constructionBlock.getDefaultState();
        if (renderBlockState == null) {
            renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        }

        Set<BlockPos> coords = new HashSet<BlockPos>();
        BlockPos pos = entity.getPosition();
        coords.add(pos);
        IBlockState state = renderBlockState;
        fakeWorld.setWorldAndState(entity.world, renderBlockState, coords);
        if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
            try {
                state = renderBlockState.getActualState(fakeWorld, pos);
            } catch (Exception var8) {
            }
        }
        blockrendererdispatcher.renderBlockBrightness(state, 1.0f);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (entity.despawning == 1) {
            if (entity.getPosition() != null) {
                TileEntity te = entity.getEntityWorld().getTileEntity(entity.getPosition());
                if (te instanceof ConstructionBlockTileEntity) {
                    ((ConstructionBlockTileEntity) te).updateLighting();
                    //((ConstructionBlockTileEntity) te).markDirtyClient();
                }
            }
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(ConstructionBlockEntity entity) {
        return null;
    }


    public static class Factory implements IRenderFactory<ConstructionBlockEntity> {

        @Override
        public Render<? super ConstructionBlockEntity> createRenderFor(RenderManager manager) {
            return new ConstructionBlockEntityRender(manager);
        }

    }
}
