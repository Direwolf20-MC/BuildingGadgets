package com.direwolf20.buildinggadgets.common.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class ConstructionBlockEntityRender extends EntityRenderer<ConstructionBlockEntity> {

    public ConstructionBlockEntityRender(EntityRendererManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void render(ConstructionBlockEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(ConstructionBlockEntity entity) {
        return null;
    }

    // todo: fix
//    @Override
//    public void doRender(ConstructionBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
//        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//        Minecraft mc = Minecraft.getInstance();
//        GlStateManager.pushMatrix();
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
//
//        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//        int teCounter = entity.getTicksExisted();
//        int maxLife = entity.getMaxLife();
//        teCounter = teCounter > maxLife ? maxLife : teCounter;
//        float scale = (float) (maxLife - teCounter) / maxLife;
//        if (entity.getMakingPaste())
//            scale = (float) teCounter / maxLife;
//        GlStateManager.translated(x, y, z);
//        GlStateManager.translatef(-0.0005f, -0.0005f, -0.0005f);
//        GlStateManager.scalef(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
//        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
//
//        GL14.glBlendColor(1F, 1F, 1F, scale); //Set the alpha of the blocks we are rendering
//        BlockState renderBlockState = OurBlocks.constructionBlockDense.getDefaultState();
//        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1f);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.disableBlend();
//        GlStateManager.popMatrix();
//    }
}
