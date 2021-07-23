package com.direwolf20.buildinggadgets.client.renderer;


import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import java.util.OptionalDouble;


public class OurRenderTypes extends RenderType {


    private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

    public static final RenderType RenderBlock = create("GadgetRenderBlock",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
//                    .setShadeModelState(SMOOTH_SHADE)
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public static final RenderType MissingBlockOverlay = create("GadgetMissingBlockOverlay",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType CopyGadgetLines = create("GadgetCopyLines",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINE_STRIP, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.of(2.0D)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType CopyPasteRenderBlock = create("CopyPasteRenderBlock",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.BLOCK_SHADER)
//                    .setShadeModelState(SMOOTH_SHADE)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED) //BLOCK_SHEET_MIPPED (mcp) = BLOCK_SHEET_MIPPED (yarn)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType BlockOverlay = create("BGBlockOverlay",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_SOLID_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING) // view_offset_z_layering
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));

    public OurRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    /**
     * This is used for rendering blocks with an alpha value as the alpha currently isn't
     * supported by minecraft.
     *
     * Literally just raps the buffer so we can render a different RenderType
     */
    public static class MultiplyAlphaRenderTypeBuffer implements MultiBufferSource
    {
        private final MultiBufferSource inner;
        private final float constantAlpha;

        public MultiplyAlphaRenderTypeBuffer(MultiBufferSource inner, float constantAlpha)
        {
            this.inner = inner;
            this.constantAlpha = constantAlpha;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type)
        {
            RenderType localType = type;
            if (localType instanceof CompositeRenderType) {
                // all of this requires a lot of AT's so be aware of that on ports
                ResourceLocation texture = ((TextureStateShard) ((CompositeRenderType) localType).state.textureState).texture
                        .orElse(InventoryMenu.BLOCK_ATLAS);

                localType = entityTranslucentCull(texture);
            }
            else if (localType.toString().equals(Sheets.translucentCullBlockSheet().toString())) {
                localType = Sheets.translucentCullBlockSheet();
            }

            return new MultiplyAlphaVertexBuilder(inner.getBuffer(localType), this.constantAlpha);
        }

        /**
         * Required for modifying the alpha value.
         */
        public static class MultiplyAlphaVertexBuilder implements VertexConsumer
        {
            private final VertexConsumer inner;
            private final float constantAlpha;

            public MultiplyAlphaVertexBuilder(VertexConsumer inner, float constantAlpha)
            {
                this.inner = inner;
                this.constantAlpha = constantAlpha;
            }

            @Override
            public VertexConsumer vertex(double x, double y, double z)
            {
                return inner.vertex(x,y,z);
            }

            @Override
            public VertexConsumer vertex(Matrix4f matrixIn, float x, float y, float z)
            {
                return inner.vertex(matrixIn, x, y, z);
            }

            @Override
            public VertexConsumer color(int red, int green, int blue, int alpha)
            {
                return inner.color(red,green,blue, (int) (alpha * constantAlpha));
            }

            @Override
            public VertexConsumer uv(float u, float v) {
                return inner.uv(u, v);
            }

            @Override
            public VertexConsumer overlayCoords(int u, int v)
            {
                return inner.overlayCoords(u, v);
            }


            @Override
            public VertexConsumer uv2(int u, int v)
            {
                return inner.uv2(u, v);
            }

            @Override
            public VertexConsumer normal(float x, float y, float z)
            {
                return inner.normal(x,y,z);
            }

            @Override
            public VertexConsumer normal(Matrix3f matrixIn, float x, float y, float z)
            {
                return inner.normal(matrixIn, x, y, z);
            }

            @Override
            public void endVertex()
            {
                inner.endVertex();
            }

            @Override
            public void defaultColor(int p_166901_, int p_166902_, int p_166903_, int p_166904_) {
                inner.defaultColor(p_166901_, p_166902_, p_166903_, p_166904_);
            }

            @Override
            public void unsetDefaultColor() {
                inner.unsetDefaultColor();
            }
        }
    }
}
