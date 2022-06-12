package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;

public class ConstructionBlockEntityRender extends EntityRenderer<ConstructionBlockEntity> {

    public ConstructionBlockEntityRender(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.shadowRadius = 0F;
    }

    @Override
    public void render(ConstructionBlockEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder;
        Minecraft mc = Minecraft.getInstance();
        builder = buffer.getBuffer(OurRenderTypes.RenderBlock);
        BlockState renderBlockState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState();
        int teCounter = entityIn.tickCount;
        int maxLife = entityIn.getMaxLife();
        teCounter = Math.min(teCounter, maxLife);
        float scale = (float) (maxLife - teCounter) / maxLife;
        if (entityIn.getMakingPaste())
            scale = (float) teCounter / maxLife;
        matrixStackIn.pushPose();
        matrixStackIn.translate(-0.0005f, -0.0005f, -0.0005f);
        matrixStackIn.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(renderBlockState, mc.player.level, entityIn.blockPosition(), 0);
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BakedModel ibakedmodel = blockrendererdispatcher.getBlockModel(renderBlockState);
        for (Direction direction : Direction.values()) {
            renderModelBrightnessColorQuads(matrixStackIn.last(), builder, f, f1, f2, scale, ibakedmodel.getQuads(renderBlockState, direction, RandomSource.create(Mth.getSeed(entityIn.blockPosition())), EmptyModelData.INSTANCE), 15728640, 655360);
        }
        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ConstructionBlockEntity entity) {
        return null;
    }
}
