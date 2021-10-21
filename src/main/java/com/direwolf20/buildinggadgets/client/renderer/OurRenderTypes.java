package com.direwolf20.buildinggadgets.client.renderer;


import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class OurRenderTypes extends RenderType {
    public static final RenderType RenderBlock = makeType("GadgetRenderBlock",
        DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
        RenderType.State.getBuilder()
            .shadeModel(SHADE_ENABLED)
            .lightmap(LIGHTMAP_ENABLED)
            .texture(BLOCK_SHEET_MIPPED)
            .layer(VIEW_OFFSET_Z_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .depthTest(DEPTH_LEQUAL)
            .cull(CULL_DISABLED)
            .writeMask(COLOR_DEPTH_WRITE)
            .build(false)
    );
    public static final RenderType MissingBlockOverlay = makeType("GadgetMissingBlockOverlay",
        DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
        RenderType.State.getBuilder()
            .layer(VIEW_OFFSET_Z_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .texture(NO_TEXTURE)
            .depthTest(DEPTH_LEQUAL)
            .cull(CULL_DISABLED)
            .lightmap(LIGHTMAP_DISABLED)
            .writeMask(COLOR_WRITE)
            .build(false)
    );
    public static final RenderType CopyGadgetLines = makeType("GadgetCopyLines",
        DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256,
        RenderType.State.getBuilder()
            .line(new LineState(OptionalDouble.of(2.0D)))
            .layer(VIEW_OFFSET_Z_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .texture(NO_TEXTURE)
            .depthTest(DEPTH_LEQUAL)
            .cull(CULL_DISABLED)
            .lightmap(LIGHTMAP_DISABLED)
            .writeMask(COLOR_WRITE)
            .build(false)
    );
    public static final RenderType CopyPasteRenderBlock = makeType("CopyPasteRenderBlock",
        DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
        RenderType.State.getBuilder()
            .shadeModel(SHADE_ENABLED)
            .lightmap(LIGHTMAP_ENABLED)
            .texture(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
            .layer(VIEW_OFFSET_Z_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .depthTest(DEPTH_LEQUAL)
            .cull(CULL_DISABLED)
            .writeMask(COLOR_WRITE)
            .build(false)
    );
    public static final RenderType BlockOverlay = makeType("BGBlockOverlay",
        DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
        RenderType.State.getBuilder()
            .layer(VIEW_OFFSET_Z_LAYERING)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .texture(NO_TEXTURE)
            .depthTest(DEPTH_LEQUAL)
            .cull(CULL_ENABLED)
            .lightmap(LIGHTMAP_DISABLED)
            .writeMask(COLOR_DEPTH_WRITE)
            .build(false)
    );

    private static final LineState THICK_LINES = new LineState(OptionalDouble.of(3.0D));

    public OurRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    /**
     * This is used for rendering blocks with an alpha value as the alpha currently isn't
     * supported by minecraft.
     * <p>
     * Literally just raps the buffer so we can render a different RenderType
     */
    public static class MultiplyAlphaRenderTypeBuffer implements IRenderTypeBuffer {
        private final IRenderTypeBuffer inner;
        private final float constantAlpha;

        public MultiplyAlphaRenderTypeBuffer(IRenderTypeBuffer inner, float constantAlpha) {
            this.inner = inner;
            this.constantAlpha = constantAlpha;
        }

        @Override
        public IVertexBuilder getBuffer(RenderType type) {
            RenderType localType = type;
            if (localType instanceof Type) {
                // all of this requires a lot of AT's so be aware of that on ports
                ResourceLocation texture = ((Type) localType).renderState.texture.texture
                    .orElse(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

                localType = getEntityTranslucentCull(texture);
            } else if (localType.toString().equals(Atlases.getTranslucentCullBlockType().toString())) {
                localType = Atlases.getTranslucentCullBlockType();
            }

            return new MultiplyAlphaVertexBuilder(this.inner.getBuffer(localType), this.constantAlpha);
        }

        /**
         * Required for modifying the alpha value.
         */
        public static class MultiplyAlphaVertexBuilder implements IVertexBuilder {
            private final IVertexBuilder inner;
            private final float constantAlpha;

            public MultiplyAlphaVertexBuilder(IVertexBuilder inner, float constantAlpha) {
                this.inner = inner;
                this.constantAlpha = constantAlpha;
            }

            @Override
            public IVertexBuilder pos(double x, double y, double z) {
                return this.inner.pos(x, y, z);
            }

            @Override
            public IVertexBuilder pos(Matrix4f matrixIn, float x, float y, float z) {
                return this.inner.pos(matrixIn, x, y, z);
            }

            @Override
            public IVertexBuilder color(int red, int green, int blue, int alpha) {
                return this.inner.color(red, green, blue, (int) (alpha * this.constantAlpha));
            }

            @Override
            public IVertexBuilder tex(float u, float v) {
                return this.inner.tex(u, v);
            }

            @Override
            public IVertexBuilder overlay(int u, int v) {
                return this.inner.overlay(u, v);
            }


            @Override
            public IVertexBuilder lightmap(int u, int v) {
                return this.inner.lightmap(u, v);
            }

            @Override
            public IVertexBuilder normal(float x, float y, float z) {
                return this.inner.normal(x, y, z);
            }

            @Override
            public IVertexBuilder normal(Matrix3f matrixIn, float x, float y, float z) {
                return this.inner.normal(matrixIn, x, y, z);
            }

            @Override
            public void endVertex() {
                this.inner.endVertex();
            }
        }
    }
}
