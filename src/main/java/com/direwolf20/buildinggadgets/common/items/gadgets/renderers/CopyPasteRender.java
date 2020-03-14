package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.renderer.DireBufferBuilder;
import com.direwolf20.buildinggadgets.client.renderer.DireVertexBuffer;
import com.direwolf20.buildinggadgets.client.renderer.MyRenderType;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.world.MockDelegationWorld;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;

public class CopyPasteRender extends BaseRenderer {
    private MultiVBORenderer renderBuffer;
    private int tickTrack = 0;

    private final Cache<BlockData, Boolean> erroredCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        // We can completely trust that heldItem isn't empty and that it's a copy paste gadget.
        super.render(evt, player, heldItem);

        // Provide this as both renders require the data.
        Vec3d projectedView = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

        // translate the matric to the projected view
        MatrixStack stack = evt.getMatrixStack(); //Get current matrix position from the evt call
        stack.push(); //Save the render position from RenderWorldLast
        stack.translate(-projectedView.getX(), -projectedView.getY(), -projectedView.getZ()); //Sets render position to 0,0,0

        if (GadgetCopyPaste.getToolMode(heldItem) == GadgetCopyPaste.ToolMode.COPY) {
            GadgetCopyPaste.getSelectedRegion(heldItem).ifPresent(region ->
                    renderCopy(stack, region));
        } else
            renderPaste(stack, projectedView, player, heldItem);

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

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(MyRenderType.CopyGadgetLines);

        Matrix4f lastMatrix = matrix.getLast().getMatrix();
        builder.pos(lastMatrix, x, y, z).color(G, G, G, 0.0F).endVertex();
        builder.pos(lastMatrix, x, y, z).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, y, z).color(G, B, B, R).endVertex();
        builder.pos(lastMatrix, dx, y, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, x, y, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, x, y, z).color(B, B, G, R).endVertex();
        builder.pos(lastMatrix, x, dy, z).color(B, G, B, R).endVertex();
        builder.pos(lastMatrix, dx, dy, z).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, x, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, x, dy, z).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, x, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, x, y, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, y, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, dy, dz).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, dy, z).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, y, z).color(G, G, G, R).endVertex();
        builder.pos(lastMatrix, dx, y, z).color(G, G, G, 0.0F).endVertex();

        buffer.finish();
    }

    /**
     * todo: eval how much load the caps add template fetching put on the render
     *
     * @implNote @michaelhillcox
     * @since 1.14 - 3.2.0b
     */
    private void renderPaste(MatrixStack matrix, Vec3d projectedView, PlayerEntity player, ItemStack heldItem) {
        World world = player.world;

        // Check the template cap from the world
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {

            // Fetch the template key (because for some reason this is it's own cap)
            heldItem.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {

                // Finally get the data from the render.
                GadgetCopyPaste.getActivePos(player, heldItem).ifPresent(startPos -> {
                    MockDelegationWorld fakeWorld = new MockDelegationWorld(world);

                    IBuildContext context = SimpleBuildContext.builder()
                            .buildingPlayer(player)
                            .usedStack(heldItem)
                            .build(fakeWorld);

                    // Get the template and move it to the start pos (player.pick())
                    IBuildView view = provider.getTemplateForKey(key).createViewInContext(context);
                    //view.translateTo(startPos);

                    // Sort the render
                    //RenderSorter sorter = new RenderSorter(player, view.estimateSize());
                    List<PlacementTarget> targets = new ArrayList<>(view.estimateSize());
                    for (PlacementTarget target : view) {
                        if (target.placeIn(context))
                            targets.add(target);
                    }

                    renderTargets(matrix, projectedView, context, targets, startPos);
                });
            });
        });
    }

    private void renderTargets(MatrixStack matrix, Vec3d projectedView, IBuildContext context, List<PlacementTarget> targets, BlockPos startPos) {
        tickTrack++;
        if (renderBuffer != null && tickTrack < 300) {
            if (tickTrack % 30 == 0) {
                try {
                    Vec3d projectedView2 = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
                    Vec3d startPosView = new Vec3d(startPos.getX(), startPos.getY(), startPos.getZ());
                    projectedView2 = projectedView2.subtract(startPosView);
                    renderBuffer.sort((float) projectedView2.getX(), (float) projectedView2.getY(), (float) projectedView2.getZ());
                } catch (Exception ignored) {
                }
            }

            matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
            renderBuffer.render(matrix.getLast().getMatrix()); //Actually draw whats in the buffer
            return;
        }

//        List<BlockPos> blockPosList = sorter.getSortedTargets().stream().map(PlacementTarget::getPos).collect(Collectors.toList());

        tickTrack = 0;
        System.out.println("Creating cache");
        if (renderBuffer != null) //Reset Render Buffer before rebuilding
            renderBuffer.close();

        renderBuffer = MultiVBORenderer.of((buffer) -> {
            System.out.println("Building again");

            IVertexBuilder builder = buffer.getBuffer(MyRenderType.CopyPasteRenderBlock);
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
                    if (state.getRenderType() == BlockRenderType.MODEL)
                        for (Direction direction : Direction.values()) {
                            if (Block.shouldSideBeRendered(state, context.getWorld(), targetPos, direction))
                                renderModelBrightnessColorQuads(stack.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                        }
                    renderModelBrightnessColorQuads(stack.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                } catch (Exception e) {
                    BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
                }

                stack.pop(); // Load the position we saved earlier
            }
            stack.pop(); //Load after loop
        });
        try {
            Vec3d projectedView2 = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
            Vec3d startPosView = new Vec3d(startPos.getX(), startPos.getY(), startPos.getZ());
            projectedView2 = projectedView2.subtract(startPosView);
            renderBuffer.sort((float) projectedView2.getX(), (float) projectedView2.getY(), (float) projectedView2.getZ());
        } catch (Exception ignored) {
        }
        matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
        renderBuffer.render(matrix.getLast().getMatrix()); //Actually draw whats in the buffer
    }

    @Override
    public boolean isLinkable() {
        return true;
    }

    public static class MultiVBORenderer implements Closeable {
        private static final int BUFFER_SIZE = 2 * 1024 * 1024 * 3;

        public static MultiVBORenderer of(Consumer<IRenderTypeBuffer> vertexProducer) {
            final Map<RenderType, DireBufferBuilder> builders = Maps.newHashMap();

            vertexProducer.accept(rt -> builders.computeIfAbsent(rt, (_rt) -> {
                DireBufferBuilder builder = new DireBufferBuilder(BUFFER_SIZE);
                builder.begin(_rt.getDrawMode(), _rt.getVertexFormat());
                System.out.println("Created new builder for RT=" + _rt);
                return builder;
            }));

            Map<RenderType, DireBufferBuilder.State> sortCaches = Maps.newHashMap();
            Map<RenderType, DireVertexBuffer> buffers = Maps.transformEntries(builders, (rt, builder) -> {
                Objects.requireNonNull(rt);
                Objects.requireNonNull(builder);
                sortCaches.put(rt, builder.getVertexState());
                System.out.println("Finishing builder for RT=" + rt);
                builder.finishDrawing();
                VertexFormat fmt = rt.getVertexFormat();
                DireVertexBuffer vbo = new DireVertexBuffer(fmt);
//                BlockPos playerPos = getMc().player.getPosition();
                //builder.sortVertexData((float) playerPos.getX(), (float) playerPos.getY(), (float) playerPos.getZ());//This sorts the buffer relative to the player's camera

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

                rt.setupRenderState();
                vbo.bindBuffer();
                fmt.setupBufferState(0L);
                vbo.draw(matrix, rt.getDrawMode());
                DireVertexBuffer.unbindBuffer();
                fmt.clearBufferState();
                rt.clearRenderState();
            });
        }

        public void close() {
            buffers.values().forEach(DireVertexBuffer::close);
        }
    }

// todo: unused: check for need
// @since 1.14
//    private static boolean isVanillaISTER(ItemStack stack) {
//        Item item = stack.getItem();
//        if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BedBlock)
//            return true;
//        else if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock)
//            return true;
//        else if (item instanceof BlockItem && ((BlockItem) item).getBlock() == Blocks.CONDUIT)
//            return true;
//        else if (item == Blocks.ENDER_CHEST.asItem())
//            return true;
//        else if (item == Blocks.TRAPPED_CHEST.asItem())
//            return true;
//        else if (Block.getBlockFromItem(item) instanceof ShulkerBoxBlock)
//            return true;
//        else if (Block.getBlockFromItem(item) instanceof ChestBlock)
//            return true;
//        else
//            return false;
//    }
}
