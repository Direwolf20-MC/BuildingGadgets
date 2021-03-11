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
    // Dummy
    public OurRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    private static final LineState THICK_LINES = new LineState(OptionalDouble.of(3.0D));

    public static final RenderType RenderBlock = create("GadgetRenderBlock",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setShadeModelState(SMOOTH_SHADE)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public static final RenderType MissingBlockOverlay = create("GadgetMissingBlockOverlay",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType CopyGadgetLines = create("GadgetCopyLines",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_STRIP, 256,
            RenderType.State.builder()
                    .setLineState(new LineState(OptionalDouble.of(2.0D)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType CopyPasteRenderBlock = create("CopyPasteRenderBlock",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setShadeModelState(SMOOTH_SHADE)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType BlockOverlay = create("BGBlockOverlay",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

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
                ResourceLocation texture = ((Type) localType).state.textureState.texture
                        .orElse(PlayerContainer.BLOCK_ATLAS);

                localType = entityTranslucentCull(texture);
            }
            else if (localType.toString().equals(Atlases.translucentCullBlockSheet().toString())) {
                localType = Atlases.translucentCullBlockSheet();
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
            public IVertexBuilder uv(float u, float v) {
                return inner.uv(u, v);
            }

            @Override
            public IVertexBuilder overlayCoords(int u, int v)
            {
                return inner.overlayCoords(u, v);
            }


            @Override
            public IVertexBuilder uv2(int u, int v)
            {
                return inner.uv2(u, v);
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
