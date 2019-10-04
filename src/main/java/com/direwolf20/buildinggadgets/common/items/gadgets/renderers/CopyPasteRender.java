package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.SimpleBuildOpenOptions;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper.RenderSorter;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.util.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.tools.SimulateEnergyStorage;
import com.direwolf20.buildinggadgets.common.util.tools.building.InvertedPlacementEvaluator;
import com.direwolf20.buildinggadgets.common.util.tools.building.PlacementChecker;
import com.direwolf20.buildinggadgets.common.world.FakeDelegationWorld;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.energy.CapabilityEnergy;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CopyPasteRender extends BaseRenderer {
    private ChestRenderer chestRenderer;
    private final Cache<RenderKey, RenderInfo> renderCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .removalListener((RemovalListener<RenderKey, RenderInfo>) notification -> notification.getValue().onRemove())
            .build();

    private final Cache<BlockData, Boolean> erroredCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public ChestRenderer getChestRenderer() {
        if (chestRenderer == null)
            chestRenderer = new ChestRenderer();
        return chestRenderer;
    }

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        if (heldItem.isEmpty())
            return;
        super.render(evt, player, heldItem);
        Vec3d playerPos = getPlayerPos();
        if (GadgetCopyPaste.getToolMode(heldItem) == GadgetCopyPaste.ToolMode.COPY) {
            GadgetCopyPaste.getSelectedRegion(heldItem).ifPresent(region -> {
                renderCopy(playerPos, region);
            });
        } else
            renderPaste(evt, player, heldItem, playerPos);
    }

    private void renderCopy(Vec3d playerPos, Region region) {
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

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translated(- playerPos.getX(), - playerPos.getY(), - playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0

        GlStateManager.disableLighting();
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        renderBox(tessellator, bufferbuilder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

        GlStateManager.lineWidth(1.0F);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();

    }

    private void renderPaste(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem, Vec3d playerPos) {
        World world = player.world;
        AbstractGadget item = (AbstractGadget) heldItem.getItem();
        UUID id = item.getUUID(heldItem);
        float partialTicks = evt.getPartialTicks();
        GadgetCopyPaste.getActivePos(player, heldItem).ifPresent(startPos -> {
            try {
                RenderInfo info = renderCache.get(new RenderKey(id, startPos), () -> {
                    int displayList = GLAllocation.generateDisplayLists(1);
                    GlStateManager.newList(displayList, GL11.GL_COMPILE);
                    this.performRender(world, player, heldItem, startPos, partialTicks);
                    GlStateManager.endList();
                    return new RenderInfo(displayList);
                });
                info.render(playerPos);
            } catch (ExecutionException e) {
                BuildingGadgets.LOG.error("Failed to create Render!", e);
            }
        });
    }

    private void performRender(World world, PlayerEntity player, ItemStack stack, BlockPos startPos, float partialTicks) {
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                ITemplate template = provider.getTemplateForKey(key);
                FakeDelegationWorld fakeWorld = new FakeDelegationWorld(world);
                IBuildContext context = SimpleBuildContext.builder()
                        .buildingPlayer(player)
                        .usedStack(stack)
                        .build(fakeWorld);
                IBuildView view = template.createViewInContext(SimpleBuildOpenOptions.withContext(context));
                if (view == null) {
                    BuildingGadgets.LOG.warn("Expected Template to be able to create a build view! Aborting render!");
                    return;
                }
                view.translateTo(startPos);
                RenderSorter sorter = new RenderSorter(player, view.estimateSize());
                for (PlacementTarget target : view) {
                    if (target.placeIn(context))
                        sorter.onPlaced(target);
                }
                //Prepare the block rendering
                //BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                renderTargets(context, sorter, partialTicks);

                if (! player.isCreative())
                    renderMissing(player, stack, view, sorter);
                GlStateManager.disableBlend();
            });
        });

    }

    private void renderTargets(IBuildContext context, RenderSorter sorter, float partialTicks) {
        //Enable Blending (So we can have transparent effect)
        GlStateManager.enableBlend();
        //This blend function allows you to use a constant alpha, which is defined later
        GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        //GlStateManager.translate(-0.0005f, -0.0005f, 0.0005f);
        //GlStateManager.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
        //GlStateManager.translatef(0.0005f, 0.0005f, - 0.0005f);
        BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();
        TileEntityRendererDispatcher teDispatcher = TileEntityRendererDispatcher.instance;
        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(GL14.GL_QUADS, DefaultVertexFormats.BLOCK);
        Random rand = new Random();
        for (PlacementTarget target : sorter.getSortedTargets()) {
            BlockPos targetPos = target.getPos();
            BlockState state = context.getWorld().getBlockState(target.getPos());
            TileEntity te = context.getWorld().getTileEntity(target.getPos());
            GlStateManager.pushMatrix();//Push matrix again in order to apply these settings individually
            GlStateManager.translatef(targetPos.getX(), targetPos.getY(), targetPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.enableBlend();
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
            try {
                switch (state.getRenderType()) {
                    case MODEL:
                        dispatcher.renderBlock(state, targetPos, context.getWorld(), builder, rand, te != null ? te.getModelData() : EmptyModelData.INSTANCE);
                        break;
                    case ENTITYBLOCK_ANIMATED: {
                        getChestRenderer().renderChestBrightness(state.getBlock(), 1f);
                    }
                }
            } catch (Exception e) {
                BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
            }
            try {
                if (te != null && ! erroredCache.get(target.getData(), () -> false)) {
                    TileEntityRenderer<TileEntity> renderer = teDispatcher.getRenderer(te);
                    if (renderer != null) {
                        if (te.hasFastRenderer())
                            renderer.renderTileEntityFast(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, 0, builder);
                        else
                            renderer.render(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, 0);
                    }
                }
            } catch (Exception e) {
                erroredCache.put(target.getData(), true);
            }
            GlStateManager.popMatrix();
        }
        Tessellator.getInstance().draw();
        GL14.glBlendColor(1F, 1F, 1F, 1f); //Set the alpha of the blocks we are rendering
    }

    private void renderMissing(PlayerEntity player, ItemStack stack, IBuildView view, RenderSorter sorter) {
        int energyCost = ((GadgetCopyPaste) stack.getItem()).getEnergyCost(stack);
        IItemIndex index = InventoryHelper.index(stack, player);
        boolean overwrite = Config.GENERAL.allowOverwriteBlocks.get();
        BlockItemUseContext useContext = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, VectorHelper.getLookingAt(player, stack)));
        InvertedPlacementEvaluator evaluator = new InvertedPlacementEvaluator(
                sorter.getOrderedTargets(),
                new PlacementChecker(
                        stack.getCapability(CapabilityEnergy.ENERGY).map(SimulateEnergyStorage::new),
                        t -> energyCost,
                        index,
                        (c, t) -> overwrite ? c.getWorld().getBlockState(t.getPos()).isReplaceable(useContext) : c.getWorld().isAirBlock(t.getPos()),
                        false),
                view.getContext());
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        GlStateManager.enableBlend();
        GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        bufferBuilder.begin(GL14.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (PlacementTarget target : evaluator) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
            renderBox(bufferBuilder, target.getPos());
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GL14.glBlendColor(1F, 1F, 1F, 1f); //Set the alpha of the blocks we are rendering
    }

    private void renderBox(BufferBuilder bufferBuilder, BlockPos pos) {
        float red = 1;
        float green = 0;
        float blue = 0;
        float alpha = 0.55f;
        double x = pos.getX() - 0.01;
        double y = pos.getY() - 0.01;
        double z = pos.getZ() - 0.01;
        double xEnd = pos.getX() + 1.01;
        double yEnd = pos.getY() + 1.01;
        double zEnd = pos.getZ() + 1.01;
        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        //front-side
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, z).color(red, green, blue, alpha).endVertex();
        //left-side
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, z).color(red, green, blue, alpha).endVertex();
        //bottom
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, zEnd).color(red, green, blue, alpha).endVertex();
        //top
        bufferBuilder.pos(xEnd, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        //right-side
        bufferBuilder.pos(xEnd, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, yEnd, z).color(red, green, blue, alpha).endVertex();
        //back-side
        bufferBuilder.pos(xEnd, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, yEnd, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, zEnd).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(xEnd, y, zEnd).color(red, green, blue, alpha).endVertex();
    }

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

    private static void renderBox(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, int R, int G, int B) {
        GlStateManager.lineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, B, B, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(B, B, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(B, G, B, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.lineWidth(1.0F);
    }

    /**
     * We use both the id and the target pos as keys, so that it re-render's once the player has looks at a different Block.
     * We cache the hashcode, as renders should be as fast as possible.
     */
    private static final class RenderKey {
        @Nonnull
        private final UUID id;
        @Nonnull
        private final BlockPos targetPos;
        private int hash;

        private RenderKey(UUID id, BlockPos targetPos) {
            this.id = Objects.requireNonNull(id, "Cannot create RenderKey without ID!");
            this.targetPos = Objects.requireNonNull(targetPos, "Cannot create RenderKey for " + id + " without target Pos!");
            this.hash = 0;
        }

        @Nonnull
        private UUID getId() {
            return id;
        }

        @Nonnull
        private BlockPos getTargetPos() {
            return targetPos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (! (o instanceof RenderKey)) return false;

            RenderKey renderKey = (RenderKey) o;

            if (! getId().equals(renderKey.getId())) return false;
            return getTargetPos().equals(renderKey.getTargetPos());
        }

        @Override
        public int hashCode() {
            if (hash == 0) {//very unlikely that we hash to 0 - no need to add an evaluated boolean
                hash = getId().hashCode();
                hash = 31 * hash + getTargetPos().hashCode();
                return hash;
            }
            return hash;
        }
    }


    private static final class RenderInfo {
        private final int callList;

        private RenderInfo(int callList) {
            this.callList = callList;
        }

        private void render(Vec3d playerPos) {
            //Save the current position that is being rendered
            GlStateManager.pushMatrix();
            GlStateManager.translated(- playerPos.getX(), - playerPos.getY(), - playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.callList(callList);
            GlStateManager.popMatrix();
        }

        private void onRemove() {
            GLAllocation.deleteDisplayLists(callList);
        }
    }
}
