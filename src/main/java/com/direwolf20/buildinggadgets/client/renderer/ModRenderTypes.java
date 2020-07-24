package com.direwolf20.buildinggadgets.client.renderer;


import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class ModRenderTypes extends RenderType {
    // Dummy
    public ModRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    private static final LineState THICK_LINES = new LineState(OptionalDouble.of(3.0D));

    public static final RenderType RenderBlock = makeType("GadgetRenderBlock",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .shadeModel(SHADE_ENABLED)
                    .lightmap(LIGHTMAP_ENABLED)
                    .texture(BLOCK_SHEET_MIPPED)
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .depthTest(DEPTH_LEQUAL)
                    .cull(CULL_DISABLED)
                    .writeMask(COLOR_DEPTH_WRITE)
                    .build(false));

    public static final RenderType MissingBlockOverlay = makeType("GadgetMissingBlockOverlay",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_LEQUAL)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false));

    public static final RenderType CopyGadgetLines = makeType("GadgetCopyLines",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256,
            RenderType.State.getBuilder()
                    .line(new LineState(OptionalDouble.of(2.0D)))
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_LEQUAL)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false));

    public static final RenderType CopyPasteRenderBlock = makeType("CopyPasteRenderBlock",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .shadeModel(SHADE_ENABLED)
                    .lightmap(LIGHTMAP_ENABLED)
                    .texture(BLOCK_SHEET_MIPPED)
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .depthTest(DEPTH_LEQUAL)
                    .cull(CULL_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false));

    public static final RenderType BlockOverlay = makeType("BGBlockOverlay",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .layer(PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_LEQUAL)
                    .cull(CULL_ENABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_DEPTH_WRITE)
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
                ResourceLocation texture = ((Type) localType).renderState.texture.texture
                        .orElse(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

                localType = getEntityTranslucentCull(texture);
            }
            else if (localType.toString().equals(Atlases.getCutoutBlockType().toString())) {
                localType = Atlases.getTranslucentCullBlockType();
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
            public IVertexBuilder pos(double x, double y, double z)
            {
                return inner.pos(x,y,z);
            }

            @Override
            public IVertexBuilder pos(Matrix4f matrixIn, float x, float y, float z)
            {
                return inner.pos(matrixIn, x, y, z);
            }

            @Override
            public IVertexBuilder color(int red, int green, int blue, int alpha)
            {
                return inner.color(red,green,blue, (int) (alpha * constantAlpha));
            }

            @Override
            public IVertexBuilder tex(float u, float v)
            {
                return inner.tex(u, v);
            }

            @Override
            public IVertexBuilder overlay(int u, int v)
            {
                return inner.overlay(u, v);
            }

            @Override
            public IVertexBuilder lightmap(int u, int v)
            {
                return inner.lightmap(u, v);
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
