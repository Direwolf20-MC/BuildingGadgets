package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.DireBufferBuilder;
import com.direwolf20.buildinggadgets.client.renderer.DireVertexBuffer;
import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.world.MockDelegationWorld;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;

public class CopyPasteRender extends BaseRenderer {
    private MultiVBORenderer renderBuffer;
    private int tickTrack = 0;


    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        // We can completely trust that heldItem isn't empty and that it's a copy paste gadget.
        super.render(evt, player, heldItem);

        // Provide this as both renders require the data.
        Vector3d cameraView = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

        // translate the matric to the projected view
        MatrixStack stack = evt.getMatrixStack(); //Get current matrix position from the evt call
        stack.push(); //Save the render position from RenderWorldLast
        stack.translate(-cameraView.getX(), -cameraView.getY(), -cameraView.getZ()); //Sets render position to 0,0,0

        if (GadgetCopyPaste.getToolMode(heldItem) == GadgetCopyPaste.ToolMode.COPY) {
            GadgetCopyPaste.getSelectedRegion(heldItem).ifPresent(region ->
                    renderCopy(stack, region));
        } else
            renderPaste(stack, cameraView, player, heldItem);

        stack.pop();
    }

    private void renderCopy(MatrixStack matrix, Region region) {
        BlockPos startPos = region.getMin();
        BlockPos endPos = region.getMax();
        BlockPos blankPos = new BlockPos(0, 0, 0);

        if (startPos.equals(blankPos) || endPos.equals(blankPos))
            return;

        //We want to draw from the starting position to the (ending position)+1
        int x = Math.min(startPos.getX(), endPos.getX()), y = Math.min(startPos.getY(), endPos.getY()), z = Math.min(startPos.getZ(), endPos.getZ());

        int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
        int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
        int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

        int R = 255, G = 223, B = 127;

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getBufferBuilders().getEntityVertexConsumers();
        IVertexBuilder builder = buffer.getBuffer(OurRenderTypes.CopyGadgetLines);

        Matrix4f matrix4f = matrix.peek().getModel();
        builder.vertex(matrix4f, x, y, z).color(G, G, G, 0.0F).endVertex();
        builder.vertex(matrix4f, x, y, z).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, y, z).color(G, B, B, R).endVertex();
        builder.vertex(matrix4f, dx, y, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, x, y, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, x, y, z).color(B, B, G, R).endVertex();
        builder.vertex(matrix4f, x, dy, z).color(B, G, B, R).endVertex();
        builder.vertex(matrix4f, dx, dy, z).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, dy, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, x, dy, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, x, dy, z).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, x, dy, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, x, y, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, y, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, dy, dz).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, dy, z).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, y, z).color(G, G, G, R).endVertex();
        builder.vertex(matrix4f, dx, y, z).color(G, G, G, 0.0F).endVertex();

        buffer.draw(); // @mcp: draw = finish
    }

    private void renderPaste(MatrixStack matrices, Vector3d cameraView, PlayerEntity player, ItemStack heldItem) {
        World world = player.world;

        // Check the template cap from the world
        // Fetch the template key (because for some reason this is it's own cap)
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> heldItem.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
            // Finally get the data from the render.
            GadgetCopyPaste.getActivePos(player, heldItem).ifPresent(startPos -> {
                MockDelegationWorld fakeWorld = new MockDelegationWorld(world);

                BuildContext context = BuildContext.builder().player(player).stack(heldItem).build(fakeWorld);

                // Get the template and move it to the start pos (player.pick())
                IBuildView view = provider.getTemplateForKey(key).createViewInContext(context);

                // Sort the render
                List<PlacementTarget> targets = new ArrayList<>(view.estimateSize());
                for (PlacementTarget target : view) {
                    if (target.placeIn(context)) {
                        targets.add(target);
                    }
                }

                renderTargets(matrices, cameraView, context, targets, startPos);
            });
        }));
    }

    private void renderTargets(MatrixStack matrix, Vector3d projectedView, BuildContext context, List<PlacementTarget> targets, BlockPos startPos) {
        tickTrack++;
        if (renderBuffer != null && tickTrack < 300) {
            if (tickTrack % 30 == 0) {
                try {
                    Vector3d projectedView2 = projectedView;
                    Vector3d startPosView = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
                    projectedView2 = projectedView2.subtract(startPosView);
                    renderBuffer.sort((float) projectedView2.getX(), (float) projectedView2.getY(), (float) projectedView2.getZ());
                } catch (Exception ignored) {
                }
            }

            matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
            renderBuffer.render(matrix.peek().getModel()); //Actually draw whats in the buffer
            return;
        }

//        List<BlockPos> blockPosList = sorter.getSortedTargets().stream().map(PlacementTarget::getPos).collect(Collectors.toList());

        tickTrack = 0;
//        System.out.println("Creating cache");
        if (renderBuffer != null) //Reset Render Buffer before rebuilding
            renderBuffer.close();

        renderBuffer = MultiVBORenderer.of((buffer) -> {
//            System.out.println("Building again");

            IVertexBuilder builder = buffer.getBuffer(OurRenderTypes.RenderBlock);
            IVertexBuilder noDepthbuilder = buffer.getBuffer(OurRenderTypes.CopyPasteRenderBlock);

            BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();

            MatrixStack stack = new MatrixStack(); //Create a new matrix stack for use in the buffer building process
            stack.push(); //Save position

            for (PlacementTarget target : targets) {
                BlockPos targetPos = target.getPos();
                BlockState state = context.getWorld().getBlockState(target.getPos());

                stack.push(); //Save position again
                //matrix.translate(-startPos.getX(), -startPos.getY(), -startPos.getZ());
                stack.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());

                IBakedModel ibakedmodel = dispatcher.getModelForState(state);
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                int color = blockColors.getColor(state, context.getWorld(), targetPos, 0);

                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;
                try {
                    if (state.getRenderType() == BlockRenderType.MODEL) {
                        for (Direction direction : Direction.values()) {
                            if (Block.shouldSideBeRendered(state, context.getWorld(), targetPos, direction) && !(context.getWorld().getBlockState(targetPos.offset(direction)).getBlock().equals(state.getBlock()))) {
                                if (state.getMaterial().isOpaque()) {
                                    renderModelBrightnessColorQuads(stack.peek(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                                } else {
                                    renderModelBrightnessColorQuads(stack.peek(), noDepthbuilder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                                }
                            }
                        }
                        if (state.getMaterial().isOpaque())
                            renderModelBrightnessColorQuads(stack.peek(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                        else
                            renderModelBrightnessColorQuads(stack.peek(), noDepthbuilder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                    }
                } catch (Exception e) {
                    BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
                }

                stack.pop(); // Load the position we saved earlier
            }
            stack.pop(); //Load after loop
        });
//        try {
            Vector3d projectedView2 = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
            Vector3d startPosView = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
            projectedView2 = projectedView2.subtract(startPosView);
            renderBuffer.sort((float) projectedView2.getX(), (float) projectedView2.getY(), (float) projectedView2.getZ());
//        } catch (Exception ignored) {
//        }
        matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
        renderBuffer.render(matrix.peek().getModel()); //Actually draw whats in the buffer
    }

    @Override
    public boolean isLinkable() {
        return true;
    }

    /**
     * Vertex Buffer Object for caching the render. Pretty similar to how the chunk caching works
     */
    public static class MultiVBORenderer implements Closeable {
        private static final int BUFFER_SIZE = 2 * 1024 * 1024 * 3;

        public static MultiVBORenderer of(Consumer<IRenderTypeBuffer> vertexProducer) {
            final Map<RenderType, DireBufferBuilder> builders = Maps.newHashMap();

            vertexProducer.accept(rt -> builders.computeIfAbsent(rt, (_rt) -> {
                DireBufferBuilder builder = new DireBufferBuilder(BUFFER_SIZE);
                builder.begin(_rt.getDrawMode(), _rt.getVertexFormat());

                return builder;
            }));

            Map<RenderType, DireBufferBuilder.State> sortCaches = Maps.newHashMap();
            Map<RenderType, DireVertexBuffer> buffers = Maps.transformEntries(builders, (rt, builder) -> {
                Objects.requireNonNull(rt);
                Objects.requireNonNull(builder);
                sortCaches.put(rt, builder.getVertexState());

                builder.finishDrawing();
                VertexFormat fmt = rt.getVertexFormat();
                DireVertexBuffer vbo = new DireVertexBuffer(fmt);

                vbo.upload(builder);
                return vbo;
            });

            return new MultiVBORenderer(buffers, sortCaches);
        }

        private final ImmutableMap<RenderType, DireVertexBuffer> buffers;
        private final ImmutableMap<RenderType, DireBufferBuilder.State> sortCaches;

        protected MultiVBORenderer(Map<RenderType, DireVertexBuffer> buffers, Map<RenderType, DireBufferBuilder.State> sortCaches) {
            this.buffers = ImmutableMap.copyOf(buffers);
            this.sortCaches = ImmutableMap.copyOf(sortCaches);
        }

        public void sort(float x, float y, float z) {
            for (Map.Entry<RenderType, DireBufferBuilder.State> kv : sortCaches.entrySet()) {
                RenderType rt = kv.getKey();
                DireBufferBuilder.State state = kv.getValue();
                DireBufferBuilder builder = new DireBufferBuilder(BUFFER_SIZE);
                builder.begin(rt.getDrawMode(), rt.getVertexFormat());
                builder.setVertexState(state);
                builder.sortVertexData(x, y, z);
                builder.finishDrawing();

                DireVertexBuffer vbo = buffers.get(rt);
                vbo.upload(builder);
            }
        }

        public void render(Matrix4f matrix) {
            buffers.forEach((rt, vbo) -> {
                VertexFormat fmt = rt.getVertexFormat();

                rt.startDrawing();
                vbo.bindBuffer();
                fmt.startDrawing(0L);
                vbo.draw(matrix, rt.getDrawMode());
                DireVertexBuffer.unbindBuffer();
                fmt.endDrawing();
                rt.endDrawing();
            });
        }

        public void close() {
            buffers.values().forEach(DireVertexBuffer::close);
        }
    }
}
