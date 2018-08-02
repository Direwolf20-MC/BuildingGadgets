package com.direwolf20.buildinggadgets.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;

public class ConstructionBlockEntityRender extends Render<ConstructionBlockEntity> {



    public ConstructionBlockEntityRender(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(ConstructionBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        int teCounter = entity.getTicksExisted();
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {
            teCounter = maxLife;
        }
        float scale = (float) (teCounter) / (float) maxLife;
        if (scale >= 1.0f) {
            scale = 0.99f;
        }
        float trans = (1 - scale) / 2;
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(trans, trans, trans);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1, 1, 1);

        IBlockState renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        if (renderBlockState == null) {
            renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        }
        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1.0f);
        GlStateManager.popMatrix();
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
