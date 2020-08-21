package com.direwolf20.buildinggadgets.client.renderer;


import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class OurRenderTypes extends RenderType {
    // Dummy
    public OurRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    private static final LineState THICK_LINES = new LineState(OptionalDouble.of(3.0D));

    public static final RenderType RenderBlock = of("GadgetRenderBlock",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .shadeModel(SMOOTH_SHADE_MODEL)
                    .lightmap(ENABLE_LIGHTMAP)
                    .texture(MIPMAP_BLOCK_ATLAS_TEXTURE) //BLOCK_SHEET_MIPPED (mcp) = MIPMAP_BLOCK_ATLAS_TEXTURE (yarn)
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .cull(DISABLE_CULLING)
                    .writeMaskState(ALL_MASK)
                    .build(false));

    public static final RenderType MissingBlockOverlay = of("GadgetMissingBlockOverlay",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .cull(DISABLE_CULLING)
                    .lightmap(DISABLE_LIGHTMAP)
                    .writeMaskState(COLOR_MASK)
                    .build(false));

    public static final RenderType CopyGadgetLines = of("GadgetCopyLines",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256,
            RenderType.State.builder()
                    .lineWidth(new LineState(OptionalDouble.of(2.0D)))
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .cull(DISABLE_CULLING)
                    .lightmap(DISABLE_LIGHTMAP)
                    .writeMaskState(COLOR_MASK)
                    .build(false));

    public static final RenderType CopyPasteRenderBlock = of("CopyPasteRenderBlock",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .shadeModel(SMOOTH_SHADE_MODEL)
                    .lightmap(ENABLE_LIGHTMAP)
                    .texture(MIPMAP_BLOCK_ATLAS_TEXTURE) //BLOCK_SHEET_MIPPED (mcp) = MIPMAP_BLOCK_ATLAS_TEXTURE (yarn)
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .cull(DISABLE_CULLING)
                    .writeMaskState(COLOR_MASK)
                    .build(false));

    public static final RenderType BlockOverlay = of("BGBlockOverlay",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(LEQUAL_DEPTH_TEST)
                    .cull(ENABLE_CULLING)
                    .lightmap(DISABLE_LIGHTMAP)
                    .writeMaskState(ALL_MASK)
                    .build(false));

    /**
     * This is used for rendering blocks with an alpha value as the alpha currently isn't
     * supported by minecraft.
     *
     * Literally just raps the buffer so we can render a different RenderType
     */
    public static class MultiplyAlphaRenderTypeBuffer implements IRenderTypeBuffer
    {
        private final IRenderTypeBuffer inner;
        private final float constantAlpha;

        public MultiplyAlphaRenderTypeBuffer(IRenderTypeBuffer inner, float constantAlpha)
        {
            this.inner = inner;
            this.constantAlpha = constantAlpha;
        }

        @Override
        public IVertexBuilder getBuffer(RenderType type)
        {
            RenderType localType = type;
            if (localType instanceof Type) {
                // all of this requires a lot of AT's so be aware of that on ports
                ResourceLocation texture = ((Type) localType).phases.texture.id
                        .orElse(PlayerContainer.BLOCK_ATLAS_TEXTURE);

                localType = getEntityTranslucentCull(texture);
            }
            else if (localType.toString().equals(Atlases.getEntityCutout().toString())) {
                localType = Atlases.getEntityTranslucentCull();
            }

            return new MultiplyAlphaVertexBuilder(inner.getBuffer(localType), this.constantAlpha);
        }

        /**
         * Required for modifying the alpha value.
         */
        public static class MultiplyAlphaVertexBuilder implements IVertexBuilder
        {
            private final IVertexBuilder inner;
            private final float constantAlpha;

            public MultiplyAlphaVertexBuilder(IVertexBuilder inner, float constantAlpha)
            {
                this.inner = inner;
                this.constantAlpha = constantAlpha;
            }

            @Override
            public IVertexBuilder vertex(double x, double y, double z)
            {
                return inner.vertex(x,y,z);
            }

            @Override
            public IVertexBuilder vertex(Matrix4f matrixIn, float x, float y, float z)
            {
                return inner.vertex(matrixIn, x, y, z);
            }

            @Override
            public IVertexBuilder color(int red, int green, int blue, int alpha)
            {
                return inner.color(red,green,blue, (int) (alpha * constantAlpha));
            }

            @Override
            public IVertexBuilder texture(float u, float v) {
                return inner.texture(u, v);
            }

            @Override
            public IVertexBuilder overlay(int u, int v)
            {
                return inner.overlay(u, v);
            }


            @Override
            public IVertexBuilder light(int u, int v)
            {
                return inner.light(u, v);
            }

            @Override
            public IVertexBuilder normal(float x, float y, float z)
            {
                return inner.normal(x,y,z);
            }

            @Override
            public IVertexBuilder normal(Matrix3f matrixIn, float x, float y, float z)
            {
                return inner.normal(matrixIn, x, y, z);
            }

            @Override
            public void endVertex()
            {
                inner.endVertex();
            }
        }
    }
}
