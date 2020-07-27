package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;

public class ConstructionBlockEntityRender extends EntityRenderer<ConstructionBlockEntity> {

    public ConstructionBlockEntityRender(EntityRendererManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void render(ConstructionBlockEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getBufferBuilders().getEntityVertexConsumers();
        IVertexBuilder builder;
        Minecraft mc = Minecraft.getInstance();
        builder = buffer.getBuffer(OurRenderTypes.RenderBlock);
        BlockState renderBlockState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().getDefaultState();
        int teCounter = entityIn.ticksExisted;
        int maxLife = entityIn.getMaxLife();
        teCounter = Math.min(teCounter, maxLife);
        float scale = (float) (maxLife - teCounter) / maxLife;
        if (entityIn.getMakingPaste())
            scale = (float) teCounter / maxLife;
        matrixStackIn.push();
        matrixStackIn.translate(-0.0005f, -0.0005f, -0.0005f);
        matrixStackIn.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(renderBlockState, mc.player.world, entityIn.getBlockPos(), 0);
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(renderBlockState);
        for (Direction direction : Direction.values()) {
            renderModelBrightnessColorQuads(matrixStackIn.peek(), builder, f, f1, f2, scale, ibakedmodel.getQuads(renderBlockState, direction, new Random(MathHelper.getPositionRandom(entityIn.getBlockPos())), EmptyModelData.INSTANCE), 15728640, 655360);
        }
        matrixStackIn.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(ConstructionBlockEntity entity) {
        return null;
    }
}
