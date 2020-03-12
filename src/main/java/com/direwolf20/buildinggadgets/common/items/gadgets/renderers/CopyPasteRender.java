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
import com.direwolf20.buildinggadgets.common.template.Template;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper.RenderSorter;
import com.direwolf20.buildinggadgets.common.world.MockDelegationWorld;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
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
        if (heldItem.isEmpty())
            return;
        super.render(evt, player, heldItem);
        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
        if (GadgetCopyPaste.getToolMode(heldItem) == GadgetCopyPaste.ToolMode.COPY) {
            GadgetCopyPaste.getSelectedRegion(heldItem).ifPresent(region -> {
                renderCopy(evt, playerPos, region);
            });
        } else
            renderPaste(evt, player, heldItem, playerPos);
        //System.out.println("Done");
    }

    private void renderCopy(RenderWorldLastEvent evt, Vec3d playerPos, Region region) {
        BlockPos startPos = region.getMin();
        BlockPos endPos = region.getMax();
        BlockPos blankPos = new BlockPos(0, 0, 0);
        if (startPos.equals(blankPos) || endPos.equals(blankPos))
            return;

        //We want to draw from the starting position to the (ending position)+1
        int x = Math.min(startPos.getX(), endPos.getX());
        int y = Math.min(startPos.getY(), endPos.getY());
        int z = Math.min(startPos.getZ(), endPos.getZ());
        int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
        int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
        int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder;
        builder = buffer.getBuffer(MyRenderType.CopyGadgetLines);
        MatrixStack stack = evt.getMatrixStack();
        stack.push();
        stack.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());

        renderCopyOutline(stack.getLast().getMatrix(), builder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

        stack.pop();
        buffer.finish();
    }

    private void renderPaste(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem, Vec3d playerPos) {
        World world = player.world;

        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            heldItem.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                UUID id = provider.getId(key);
                GadgetCopyPaste.getActivePos(player, heldItem).ifPresent(startPos -> {
//                    this.render(evt, id, startPos, world, player, heldItem, provider.getTemplateForKey(key));
                    //try {
                    //RenderInfo info = renderCache.get(new RenderKey(id, startPos), () -> {
                    // todo: fix - DireNote, What even is this
                    //int displayList = GLAllocation.generateDisplayLists(1);
                    //GlStateManager.newList(displayList, GL11.GL_COMPILE);
                    this.performRender(world, player, heldItem, startPos, provider.getTemplateForKey(key), evt.getPartialTicks(), evt);
                    //GlStateManager.endList();
                    //return new RenderInfo( 0 );//displayList);
                    //});
                    //info.render(playerPos);
                    //} catch (ExecutionException e) {
                    //    BuildingGadgets.LOG.error("Failed to create Render!", e);
                    //}
                });
            });
        });
    }


    private void performRender(World world, PlayerEntity player, ItemStack stack, BlockPos startPos, Template template, float partialTicks, RenderWorldLastEvent evt) {
        MockDelegationWorld fakeWorld = new MockDelegationWorld(world);
        IBuildContext context = SimpleBuildContext.builder()
                .buildingPlayer(player)
                .usedStack(stack)
                .build(fakeWorld);
        IBuildView view = template.createViewInContext(context);
        view.translateTo(startPos);
        RenderSorter sorter = new RenderSorter(player, view.estimateSize());
        for (PlacementTarget target : view) {
            if (target.placeIn(context))
                sorter.onPlaced(target);
        }
        //Prepare the block rendering
        //BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        renderTargets(context, sorter, partialTicks, evt);

        //ToDo the red render now works but shows over everything regardless of circumstance, uncomment to see what i mean
        //if (! player.isCreative())
        //renderMissing(player, stack, view, sorter, evt);
        //GlStateManager.disableBlend();
    }

//    private void renderTargets(IBuildContext context, RenderSorter sorter, float partialTicks, RenderWorldLastEvent evt) {
//        //Enable Blending (So we can have transparent effect)
//        //GlStateManager.enableBlend();
//        //This blend function allows you to use a constant alpha, which is defined later
//        //GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
//        //GlStateManager.translate(-0.0005f, -0.0005f, 0.0005f);
//        //GlStateManager.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
//        //GlStateManager.translatef(0.0005f, 0.0005f, - 0.0005f);
//
//        if( renderBuffer != null ) {
//            renderBuffer.render(evt.getMatrixStack().getLast().getMatrix());
//            System.out.println("Rendering Cache");
//            return;
//        }
//
//        renderBuffer = MultiVBORenderer.of((buffer) -> {
//            Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
//            IVertexBuilder builder = buffer.getBuffer(MyRenderType.RenderBlock);
//            BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();
//            MatrixStack matrix = evt.getMatrixStack();
//            matrix.push();
//            matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
////            Random rand = new Random();
//
//            for (PlacementTarget target : sorter.getSortedTargets()) {
//                BlockPos targetPos = target.getPos();
//                BlockState state = context.getWorld().getBlockState(target.getPos());
//                TileEntity te = context.getWorld().getTileEntity(target.getPos());
//                matrix.push();//Push matrix again in order to apply these settings individually
//                matrix.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
//                IBakedModel ibakedmodel = dispatcher.getModelForState(state);
//                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//                int color = blockColors.getColor(state, context.getWorld(), targetPos, 0);
//                float f = (float) (color >> 16 & 255) / 255.0F;
//                float f1 = (float) (color >> 8 & 255) / 255.0F;
//                float f2 = (float) (color & 255) / 255.0F;
//                try {
//                    if (state.getRenderType() == BlockRenderType.MODEL)
//                        for (Direction direction : Direction.values()) {
//                            renderModelBrightnessColorQuads(matrix.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
//                        }
//                } catch (Exception e) {
//                    BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
//                }
///*            try {
//            if (te != null && ! erroredCache.get(target.getData(), () -> false)) {
//                TileEntityRenderer<TileEntity> renderer = teDispatcher.getRenderer(te);
//                if (renderer != null) {
//                    if (te.hasFastRenderer()) {
//                        // todo: fix
////                            renderer.render(te, 0, 0, 0, partialTicks, - 1, builder);
//                    } else {
//                        // todo: fix
////                            renderer.render(te, 0, 0, 0, partialTicks, - 1);
//                        bindBlocks(); //some blocks (all vanilla tiles I tested) rebind the atlas!
//                    }
//                }
//            }
//        } catch (Exception e) {
//            erroredCache.put(target.getData(), true);
//        }*/
//                matrix.pop();
//            }
//            matrix.pop();
////            buffer.finish();
//        });
//
////        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
////        IVertexBuilder builder;
//
////        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
////        builder = buffer.getBuffer(MyRenderType.RenderBlock);
////        BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();
////        MatrixStack matrix = evt.getMatrixStack();
////        matrix.push();
////        matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
////        Random rand = new Random();
////
////        for (PlacementTarget target : sorter.getSortedTargets()) {
////            BlockPos targetPos = target.getPos();
////            BlockState state = context.getWorld().getBlockState(target.getPos());
////            TileEntity te = context.getWorld().getTileEntity(target.getPos());
////            matrix.push();//Push matrix again in order to apply these settings individually
////            matrix.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
////            IBakedModel ibakedmodel = dispatcher.getModelForState(state);
////            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
////            int color = blockColors.getColor(state, context.getWorld(), targetPos, 0);
////            float f = (float) (color >> 16 & 255) / 255.0F;
////            float f1 = (float) (color >> 8 & 255) / 255.0F;
////            float f2 = (float) (color & 255) / 255.0F;
////            try {
////                if (state.getRenderType() == BlockRenderType.MODEL)
////                    for (Direction direction : Direction.values()) {
////                        renderModelBrightnessColorQuads(matrix.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
////                    }
////            } catch (Exception e) {
////                BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
////            }
/////*            try {
////            if (te != null && ! erroredCache.get(target.getData(), () -> false)) {
////                TileEntityRenderer<TileEntity> renderer = teDispatcher.getRenderer(te);
////                if (renderer != null) {
////                    if (te.hasFastRenderer()) {
////                        // todo: fix
//////                            renderer.render(te, 0, 0, 0, partialTicks, - 1, builder);
////                    } else {
////                        // todo: fix
//////                            renderer.render(te, 0, 0, 0, partialTicks, - 1);
////                        bindBlocks(); //some blocks (all vanilla tiles I tested) rebind the atlas!
////                    }
////                }
////            }
////        } catch (Exception e) {
////            erroredCache.put(target.getData(), true);
////        }*/
////            matrix.pop();
////        }
////        matrix.pop();
////        buffer.finish();
//    }

//    private void renderMissing(PlayerEntity player, ItemStack stack, IBuildView view, RenderSorter sorter, RenderWorldLastEvent evt) {
//        int energyCost = ((GadgetCopyPaste) stack.getItem()).getEnergyCost(stack);
//        //wrap in a recording index, to prevent a single item of some type from allowing all of that kind.
//        //it sadly makes it very inefficient - we should try to find a faster solution
//        IItemIndex index = new RecordingItemIndex(InventoryHelper.index(stack, player));
//        boolean overwrite = Config.GENERAL.allowOverwriteBlocks.get();
//        BlockItemUseContext useContext = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, VectorHelper.getLookingAt(player, stack)));
//        InvertedPlacementEvaluator evaluator = new InvertedPlacementEvaluator(
//                sorter.getOrderedTargets(),
//                new PlacementChecker(
//                        stack.getCapability(CapabilityEnergy.ENERGY).map(SimulateEnergyStorage::new),
//                        t -> energyCost,
//                        index,
//                        (c, t) -> overwrite ? player.world.getBlockState(t.getPos()).isReplaceable(useContext) : player.world.isAirBlock(t.getPos()),
//                        false),
//                view.getContext());
//
//        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
//        IVertexBuilder builder;
//        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
//        builder = buffer.getBuffer(MyRenderType.MissingBlockOverlay);
//        BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();
//        MatrixStack matrix = evt.getMatrixStack();
//        matrix.push();
//        matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
//        for (PlacementTarget target : evaluator) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
//            renderMissingBlock(matrix.getLast().getMatrix(), builder, target.getPos());
//        }
//        matrix.pop();
//        buffer.finish();
//    }

    @Override
    public boolean isLinkable() {
        return true;
    }

    // Todo: replace with something simpler
    private static boolean isVanillaISTER(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BedBlock)
            return true;
        else if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock)
            return true;
        else if (item instanceof BlockItem && ((BlockItem) item).getBlock() == Blocks.CONDUIT)
            return true;
        else if (item == Blocks.ENDER_CHEST.asItem())
            return true;
        else if (item == Blocks.TRAPPED_CHEST.asItem())
            return true;
        else if (Block.getBlockFromItem(item) instanceof ShulkerBoxBlock)
            return true;
        else if (Block.getBlockFromItem(item) instanceof ChestBlock)
            return true;
        else
            return false;
    }

    private static void renderCopyOutline(Matrix4f matrix, IVertexBuilder builder, float startX, float startY, float startZ, float endX, float endY, float endZ, int R, int G, int B) {

        builder.pos(matrix, startX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        builder.pos(matrix, startX, startY, startZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(G, B, B, R).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, startX, startY, startZ).color(B, B, G, R).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(B, G, B, R).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, startX, endY, startZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, startX, endY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, startX, startY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, startY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, endY, endZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, endY, startZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(G, G, G, R).endVertex();
        builder.pos(matrix, endX, startY, startZ).color(G, G, G, 0.0F).endVertex();
    }

    private void renderTargets(IBuildContext context, RenderSorter sorter, float partialTicks, RenderWorldLastEvent evt) {
        tickTrack ++;
        if (renderBuffer != null && tickTrack < 300) {
            Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

            MatrixStack matrix = evt.getMatrixStack(); //Get current matrix position from the evt call
            matrix.push(); //Save the render position from RenderWorldLast
            matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ()); //Sets render position to 0,0,0
            //matrix.translate(-152, 63, -55); //This is where the draw will occur, change to anchor/player look vector
            if (tickTrack % 30 == 0) {
                try {
                    renderBuffer.sort((float) playerPos.getX(), (float) playerPos.getY(), (float) playerPos.getZ());
                } catch (Exception e) {

                }
            }
            renderBuffer.render(matrix.getLast().getMatrix()); //Actually draw whats in the buffer
            matrix.pop(); //Load the save that we did above, undoing the past 2 translates
            return;
        }
        ArrayList<BlockPos> blockPosList = new ArrayList<>();
        for (PlacementTarget target : sorter.getSortedTargets()) {
            blockPosList.add(target.getPos());
        }
        tickTrack = 0;
        System.out.println("Creating cache");
        if (renderBuffer != null) //Reset Render Buffer before rebuilding
            renderBuffer.close();
        renderBuffer = MultiVBORenderer.of((buffer) -> {
            System.out.println("Building again");

            IVertexBuilder builder = buffer.getBuffer(MyRenderType.RenderBlock);
            BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();

            MatrixStack matrix = new MatrixStack(); //Create a new matrix stack for use in the buffer building process
            matrix.push(); //Save position
            //BlockPos startPos = GadgetUtils.getPOSFromNBT(context.getUsedStack(), "start_pos");

            for (PlacementTarget target : sorter.getSortedTargets()) {
                BlockPos targetPos = target.getPos();
                BlockState state = context.getWorld().getBlockState(target.getPos());

                matrix.push(); //Save position again
                //matrix.translate(-startPos.getX(), -startPos.getY(), -startPos.getZ());
                matrix.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());

                IBakedModel ibakedmodel = dispatcher.getModelForState(state);
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                int color = blockColors.getColor(state, context.getWorld(), targetPos, 0);

                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;
                try {
                    if (state.getRenderType() == BlockRenderType.MODEL)
                        for (Direction direction : Direction.values()) {
                            //if (!blockPosList.contains(targetPos.offset(direction)))
                            renderModelBrightnessColorQuads(matrix.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(targetPos)), EmptyModelData.INSTANCE), 15728640, 655360);
                        }
                } catch (Exception e) {
                    BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
                }

                matrix.pop(); // Load the position we saved earlier
            }
            matrix.pop(); //Load after loop
        });
        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

        MatrixStack matrix = evt.getMatrixStack(); //Get current matrix position from the evt call
        matrix.push(); //Save the render position from RenderWorldLast
        matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ()); //Sets render position to 0,0,0
        //matrix.translate(-152, 63, -55); //This is where the draw will occur, change to anchor/player look vector
        renderBuffer.render(matrix.getLast().getMatrix()); //Actually draw whats in the buffer
        matrix.pop(); //Load the save that we did above, undoing the past 2 translates
    }

    public static class MultiVBORenderer implements Closeable
    {
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
                BlockPos playerPos = getMc().player.getPosition();
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
            buffers.entrySet().forEach(kv -> {
                RenderType rt = kv.getKey();
                DireVertexBuffer vbo = kv.getValue();
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

        public void close()
        {
            buffers.values().forEach(DireVertexBuffer::close);
        }
    }
}
