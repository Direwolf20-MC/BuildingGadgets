/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateManagerTemplateCreated;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.template.*;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider.IUpdateListener;
import com.direwolf20.buildinggadgets.common.tileentities.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateParseException.IllegalMinecraftVersionException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateParseException.UnknownTemplateVersionException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException.CorruptJsonException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateWriteException.DataCannotBeWrittenException;
import com.direwolf20.buildinggadgets.common.util.lang.GuiTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TemplateManagerGUI extends AbstractContainerScreen<TemplateManagerContainer> {
    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/template_manager.png");

    private final Rect2i panel = new Rect2i((8 - 20), 12, 136, 80);
    private boolean panelClicked;
    private int clickButton, clickX, clickY;
    private float initRotX, initRotY, initZoom, initPanX, initPanY;
    private float momentumX, momentumY;
    private float rotX = 0, rotY = 0, zoom = 1;
    private float panX = 0, panY = 0;

    private EditBox nameField;
    private Button buttonSave, buttonLoad, buttonCopy, buttonPaste;

    private final TemplateManagerTileEntity te;
    private final TemplateManagerContainer container;
    private final LazyOptional<ITemplateProvider> templateProvider = getWorld().getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY);

    // It is so stupid I can't get the key from the template.
    private Template template;

    public TemplateManagerGUI(TemplateManagerContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, Component.literal(""));

        this.container = container;
        this.te = container.getTe();
    }

    @Override
    public void init() {
        super.init();
        this.nameField = new EditBox(this.font, (this.leftPos - 20) + 8, topPos - 5, imageWidth - 16, this.font.lineHeight + 3, GuiTranslation.TEMPLATE_NAME_TIP.componentTranslation());

        int x = (leftPos - 20) + 180;
        buttonSave = addRenderableWidget(Button.builder(GuiTranslation.BUTTON_SAVE.componentTranslation(), b -> onSave()).pos(x, topPos + 17).size(60, 20).build());
        buttonLoad = addRenderableWidget(Button.builder(GuiTranslation.BUTTON_LOAD.componentTranslation(), b -> onLoad()).pos(x, topPos + 39).size(60, 20).build());
        buttonCopy = addRenderableWidget(Button.builder(GuiTranslation.BUTTON_COPY.componentTranslation(), b -> onCopy()).pos(x, topPos + 66).size(60, 20).build());
        buttonPaste = addRenderableWidget(Button.builder(GuiTranslation.BUTTON_PASTE.componentTranslation(), b -> onPaste()).pos(x, topPos + 89).size(60, 20).build());

        this.nameField.setMaxLength(50);
        this.nameField.setVisible(true);
        addRenderableWidget(nameField);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(font, "Preview disabled for now...", leftPos - 10, topPos + 40, 0xFFFFFF);
        if (this.template != null) {
            renderRequirement(guiGraphics, mouseX, mouseY);
        }

//        validateCache(partialTicks);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        renderBackground(guiGraphics);

        guiGraphics.blit(background, leftPos - 20, topPos - 12, 0, 0, imageWidth, imageHeight + 25);
        guiGraphics.blit(background, (leftPos - 20) + imageWidth, topPos + 8, imageWidth + 3, 30, 71, imageHeight);

        if (!buttonCopy.isHoveredOrFocused() && !buttonPaste.isHoveredOrFocused()) {
            if (buttonLoad.isHoveredOrFocused())
                guiGraphics.blit(background, (leftPos + imageWidth) - 44, topPos + 38, imageWidth, 0, 17, 24);
            else
                guiGraphics.blit(background, (leftPos + imageWidth) - 44, topPos + 38, imageWidth + 17, 0, 16, 24);
        }

        this.nameField.render(guiGraphics, mouseX, mouseY, partialTicks);
//        fill(matrices, guiLeft + panel.getX() - 1, guiTop + panel.getY() - 1, guiLeft + panel.getX() + panel.getWidth() + 1, guiTop + panel.getY() + panel.getHeight() + 1, 0xFF8A8A8A);

        if (this.template != null) {
            renderPanel();
        }
    }

    private void validateCache(float partialTicks) {
        // Invalidate the render
        if (container.getSlot(0).getItem().isEmpty() && template != null) {
            template = null;
            resetViewport();
            return;
        }

        container.getSlot(0).getItem().getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
            // Make sure we're not re-creating the same cache.
            Template template = provider.getTemplateForKey(key);
            if (this.template == template)
                return;

            this.template = template;

//            IBuildView view = template.createViewInContext(
//                    SimpleBuildContext.builder()
//                            .player(getMinecraft().player)
//                            .stack(container.getSlot(0).getStack())
//                            .build(new MockDelegationWorld(getMinecraft().world)));

//            int displayList = GLAllocation.generateDisplayLists(1);
//            GlStateManager.newList(displayList, GL11.GL_COMPILE);

//            renderStructure(view, partialTicks);

//            GlStateManager.endList();
//            this.displayList = displayList;
        }));
    }

    private void renderStructure(IBuildView view, float partialTicks) {
        Random rand = new Random();
        BlockRenderDispatcher dispatcher = getMinecraft().getBlockRenderer();

        BufferBuilder bufferBuilder = new BufferBuilder(2097152);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        for (PlacementTarget target : view) {
            target.placeIn(view.getContext());
            BlockPos targetPos = target.getPos();
            BlockState renderBlockState = view.getContext().getWorld().getBlockState(targetPos);
            BlockEntity te = view.getContext().getWorld().getBlockEntity(targetPos);

            if (renderBlockState.getRenderShape() == RenderShape.MODEL) {
                BakedModel model = dispatcher.getBlockModel(renderBlockState);
//                dispatcher.getBlockModelRenderer().renderModelFlat()
//                        .renderModelFlat(getWorld(), model, renderBlockState, target.getPos(), bufferBuilder, false,
//                        rand, 0L, te != null ? te.getModelData() : EmptyModelData.INSTANCE);
            }

            if (te != null) {
                try {
                    BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te);
                    if (renderer != null) {
//                        if (te.hasFastRenderer())
//                            renderer.renderTileEntityFast(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, - 1, bufferBuilder);
//                        else
//                            renderer.render(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, - 1);
                    }
                    //remember vanilla Tiles rebinding the TextureAtlas
                    RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                } catch (Exception e) {
                    BuildingGadgets.LOG.error("Error rendering TileEntity", e);
                }
            }
        }

        bufferBuilder.end();

//        if (bufferBuilder.getVertexCount() > 0) {
//            VertexFormat vertexformat = bufferBuilder.getVertexFormat();
//            int i = vertexformat.getSize();
//            ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
//            List<VertexFormatElement> list = vertexformat.getElements();
//
//            for (int j = 0; j < list.size(); ++ j) {
//                VertexFormatElement vertexformatelement = list.get(j);
//                bytebuffer.position(vertexformat.getOffset(j));
//                vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
//            }
//
//            GlStateManager.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
//            int i1 = 0;
//
//            for (int j1 = list.size(); i1 < j1; ++ i1) {
//                VertexFormatElement vertexformatelement1 = list.get(i1);
//                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
//            }
//        }
    }

    private void renderRequirement(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        MaterialList requirements = this.template.getHeaderAndForceMaterials(BuildContext.builder().build(getWorld())).getRequiredItems();
        if (requirements == null)
            return;

        Lighting.setupForFlatItems();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos - 30, topPos - 5, 200);
        guiGraphics.pose().scale(.8f, .8f, .8f);

        String title = "Requirements"; // Todo lang;
        guiGraphics.drawString(getMinecraft().font, title, 5 - (font.width(title)), 0, Color.WHITE.getRGB());

        // The things you have to do to get anything from this system is just stupid.
        MatchResult list = InventoryHelper.CREATIVE_INDEX.tryMatch(requirements);
        ImmutableMultiset<IUniqueObject<?>> foundItems = list.getFoundItems();

        // Reverse sorted list of items required.
        List<Multiset.Entry<IUniqueObject<?>>> sortedEntries = ImmutableList.sortedCopyOf(Comparator
                .<Multiset.Entry<IUniqueObject<?>>, Integer>comparing(Multiset.Entry::getCount)
                .reversed(), list.getChosenOption().entrySet());

        int index = 0, column = 0;
        for (Multiset.Entry<IUniqueObject<?>> e : sortedEntries) {
            ItemStack stack = e.getElement().createStack();
            int x = (-20 - (column * 25)), y = (20 + (index * 25));

            guiGraphics.renderItem(stack, x + 4, y + 4);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + 4, y + 4, GadgetUtils.withSuffix(foundItems.count(e.getElement())));

            int space = (int) (25 - (.2f * 25));
            int zoneX = ((leftPos - 32) + (-15 - (column * space))), zoneY = (topPos - 9) + (20 + (index * space));

            if (mouseX > zoneX && mouseX < (zoneX + space) && mouseY > zoneY && mouseY < (zoneY + space)) {
                guiGraphics.renderTooltip(font, Lists.transform(stack.getTooltipLines(this.getMinecraft().player, TooltipFlag.Default.NORMAL), Component::getVisualOrderText), x + 15, y + 25);
            }

            index++;
            if (index % 8 == 0) {
                column++;
                index = 0;
            }
        }

        Lighting.setupFor3DItems();
        guiGraphics.pose().popPose();
    }

    private void pasteTemplateToStack(Level world, ItemStack stack, Template newTemplate, boolean replaced) {
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider ->
                pasteTemplateToStack(provider, stack, newTemplate, replaced && world.isClientSide()));
    }

    private void pasteTemplateToStack(ITemplateProvider provider, ItemStack stack, Template newTemplate, boolean replaced) {
        stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
            provider.setTemplate(key, newTemplate);
            if (replaced)
                PacketHandler.sendToServer(new PacketTemplateManagerTemplateCreated(provider.getId(key), te.getBlockPos()));
            else
                provider.requestRemoteUpdate(key);
        });
    }

    private boolean replaceStack() {
        ItemStack stack = container.getSlot(1).getItem();
        if (stack.isEmpty())
            return false;

        if (stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return false;

        else if (stack.is(TemplateManagerTileEntity.TEMPLATE_CONVERTIBLES)) {
            container.setItem(1, container.getStateId(), new ItemStack(OurItems.TEMPLATE_ITEM.get()));
            return true;
        }

        return false;
    }

    private void rename(ItemStack stack) {
        if (nameField.getValue().isEmpty())
            return;

        stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
            Template template = provider.getTemplateForKey(key);
            template = template.withName(nameField.getValue());
            provider.setTemplate(key, template);
            provider.requestRemoteUpdate(key);
        }));
    }

    private void renderPanel() {
//        double scale = getMinecraft().getWindow().getGuiScale();
//
//        BlockPos startPos = template.getHeader().getBoundingBox().getMin();
//        BlockPos endPos = template.getHeader().getBoundingBox().getMax();
//
//        double lengthX = Math.abs(startPos.getX() - endPos.getX());
//        double lengthY = Math.abs(startPos.getY() - endPos.getY());
//        double lengthZ = Math.abs(startPos.getZ() - endPos.getZ());
//
//        final double maxW = 6 * 16;
//        final double maxH = 11 * 16;
//
//        double overW = Math.max(lengthX * 16 - maxW, lengthZ * 16 - maxW);
//        double overH = lengthY * 16 - maxH;
//
//        double sc = 1;
//        double zoomScale = 1;
//
//        if (overW > 0 && overW >= overH) {
//            sc = maxW / (overW + maxW);
//            zoomScale = overW / 40;
//        } else if (overH > 0 && overH >= overW) {
//            sc = maxH / (overH + maxH);
//            zoomScale = overH / 40;
//        }
//
//        RenderSystem.pushMatrix();
//        RenderSystem.matrixMode(GL11.GL_PROJECTION);
//        RenderSystem.pushMatrix();
//        RenderSystem.loadIdentity();
//
//        RenderSystem.multMatrix(Matrix4f.perspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000));
//        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//        RenderSystem.viewport((int) Math.round((leftPos + panel.getX()) * scale),
//                (int) Math.round(getMinecraft().getWindow().getHeight() - (topPos + panel.getY() + panel.getHeight()) * scale),
//                (int) Math.round(panel.getWidth() * scale),
//                (int) Math.round(panel.getHeight() * scale));
//
//        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, true);
//
//        sc = (293 * sc) + zoom / zoomScale;
//        RenderSystem.scaled(sc, sc, sc);
//        int moveX = startPos.getX() - endPos.getX();
//
//        RenderSystem.rotatef(30, 0, 1, 0);
//        if (startPos.getX() >= endPos.getX())
//            moveX--;
//
//        RenderSystem.translated((moveX) / 1.75, -Math.abs(startPos.getY() - endPos.getY()) / 1.75, 0);
//        RenderSystem.translated(panX, -panY, 0);
//        RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f) * -1, ((startPos.getY() - endPos.getY()) / 2f) * -1, ((startPos.getZ() - endPos.getZ()) / 2f) * -1);
//        RenderSystem.rotatef(-rotX, 1, 0, 0);
//        RenderSystem.rotatef(rotY, 0, 1, 0);
//        RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f), ((startPos.getY() - endPos.getY()) / 2f), ((startPos.getZ() - endPos.getZ()) / 2f));
//
//        getMinecraft().getTextureManager().bind(InventoryMenu.BLOCK_ATLAS);
//
////        RenderSystem.callList(displayList);
//
//        RenderSystem.popMatrix();
//        RenderSystem.matrixMode(GL11.GL_PROJECTION);
//        RenderSystem.popMatrix();
//        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//        RenderSystem.viewport(0, 0, getMinecraft().getWindow().getWidth(), getMinecraft().getWindow().getHeight());
    }

    private void resetViewport() {
        rotX = 0;
        rotY = 0;
        zoom = 1;
        momentumX = 0;
        momentumY = 0;
        panX = 0;
        panY = 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (panel.contains((int) mouseX - leftPos, (int) mouseY - topPos)) {
            clickButton = mouseButton;
            panelClicked = true;
            clickX = (int) getMinecraft().mouseHandler.xpos();
            clickY = (int) getMinecraft().mouseHandler.ypos();
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        panelClicked = false;
        initRotX = rotX;
        initRotY = rotY;
        initPanX = panX;
        initPanY = panY;
        initZoom = zoom;

        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 256) {
            this.onClose();
            return true;
        }

        return this.nameField.isFocused() ? this.nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (panelClicked) {
            if (clickButton == 0) {
                float prevRotX = rotX;
                float prevRotY = rotY;
                rotX = initRotX - ((int) getMinecraft().mouseHandler.ypos() - clickY);
                rotY = initRotY + ((int) getMinecraft().mouseHandler.xpos() - clickX);
                momentumX = rotX - prevRotX;
                momentumY = rotY - prevRotY;
            } else if (clickButton == 1) {
                panX = initPanX + ((int) getMinecraft().mouseHandler.xpos() - clickX) / 8f;
                panY = initPanY + ((int) getMinecraft().mouseHandler.ypos() - clickY) / 8f;
            }
        }

        rotX += momentumX;
        rotY += momentumY;
        float momentumDampening = 0.98f;
        momentumX *= momentumDampening;
        momentumY *= momentumDampening;

        if (!nameField.isFocused() && nameField.getValue().isEmpty())
            guiGraphics.drawString(font, GuiTranslation.TEMPLATE_PLACEHOLDER.format(), nameField.getX() - leftPos + 4, (nameField.getY() + 2) - topPos, -10197916);

        if (buttonSave.isHoveredOrFocused() || buttonLoad.isHoveredOrFocused() || buttonPaste.isHoveredOrFocused())
            drawSlotOverlay(guiGraphics, buttonLoad.isHoveredOrFocused() ? container.getSlot(0) : container.getSlot(1));
    }

    private void drawSlotOverlay(GuiGraphics guiGraphics, Slot slot) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);
        guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, -1660903937);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        zoom = initZoom + ((float) scrollDelta * 20);
        if (zoom < -200) zoom = -200;
        if (zoom > 5000) zoom = 5000;

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        nameField.tick();
        if (!panelClicked) {
            initRotX = rotX;
            initRotY = rotY;
            initZoom = zoom;
            initPanX = panX;
            initPanY = panY;
        }
    }

//    @Override
//    public void tick() {
//        super.tick();
//        nameField.tick();
//        if (! panelClicked) {
//            initRotX = rotX;
//            initRotY = rotY;
//            initZoom = zoom;
//            initPanX = panX;
//            initPanY = panY;
//        }
//    }

    private Level getWorld() {
        return getMinecraft().level;
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    // Events
    // we need to ensure that the Template we want to look at is recent, before we take any further action
    private void runAfterUpdate(int slot, Runnable runnable) {
        container.getSlot(slot).getItem().getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
            provider.registerUpdateListener(new IUpdateListener() {
                @Override
                public void onTemplateUpdate(ITemplateProvider provider, ITemplateKey updateKey, Template template) {
                    if (provider.getId(updateKey).equals(provider.getId(key))) {
                        runnable.run();
                        provider.removeUpdateListener(this);
                    }
                }
            });
            provider.requestUpdate(key);
        }));
    }

    private void onSave() {
        boolean replaced = replaceStack();
        ItemStack left = container.getSlot(0).getItem();
        ItemStack right = container.getSlot(1).getItem();
        if (left.isEmpty()) {
            rename(right);
            return;
        }

        runAfterUpdate(0, () -> { //we are copying form 0 to 1 => slot 0 needs to be the recent one
            templateProvider.ifPresent(provider -> {
                left.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                    Template templateToSave = provider.getTemplateForKey(key).withName(nameField.getValue());
                    pasteTemplateToStack(provider, right, templateToSave, replaced);
                });
            });
        });
    }

    private void onLoad() {
        boolean replaced = replaceStack();
        ItemStack left = container.getSlot(0).getItem();
        ItemStack right = container.getSlot(1).getItem();
        if (left.isEmpty()) {
            rename(right);
            return;
        }

        runAfterUpdate(1, () -> { //we are copying form 1 to 0 => slot 1 needs to be the recent one
            templateProvider.ifPresent(provider -> {
                right.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                    Template templateToSave = provider.getTemplateForKey(key);
                    pasteTemplateToStack(provider, left, templateToSave, replaced);
                });
            });
        });
    }

    private void onCopy() {
        runAfterUpdate(0, () -> { //we are copying from slot 1 => slot 1 needs to be updated
            ItemStack stack = container.getSlot(0).getItem();
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                templateProvider.ifPresent(provider -> {
                    Player player = getMinecraft().player;
                    assert player != null;

                    BuildContext buildContext = BuildContext.builder()
                            .player(player)
                            .stack(stack)
                            .build(getWorld());
                    try {
                        Template template = provider.getTemplateForKey(key);
                        if (!nameField.getValue().isEmpty())
                            template = template.withName(nameField.getValue());
                        String json = TemplateIO.writeTemplateJson(template, buildContext);
                        getMinecraft().keyboardHandler.setClipboard(json);
                        player.displayClientMessage(MessageTranslation.CLIPBOARD_COPY_SUCCESS.componentTranslation().setStyle(Styles.DK_GREEN), false);
                    } catch (DataCannotBeWrittenException e) {
                        BuildingGadgets.LOG.error("Failed to write Template.", e);
                        player.displayClientMessage(MessageTranslation.CLIPBOARD_COPY_ERROR_TEMPLATE.componentTranslation().setStyle(Styles.RED), false);
                    } catch (Exception e) {
                        BuildingGadgets.LOG.error("Failed to copy Template to clipboard.", e);
                        player.displayClientMessage(MessageTranslation.CLIPBOARD_COPY_ERROR.componentTranslation().setStyle(Styles.RED), false);
                    }
                });
            });
        });
    }

    private void onPaste() {
        assert getMinecraft().player != null;

        String CBString = getMinecraft().keyboardHandler.getClipboard();
        if (GadgetUtils.mightBeLink(CBString)) {
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_LINK_COPIED.componentTranslation().setStyle(Styles.RED), false);
            return;
        }

        // Attempt to parse into nbt first to check for old 1.12 pastes
        try {
            CompoundTag tagFromJson = TagParser.parseTag(CBString);
            if (!tagFromJson.contains("header")) {
                BuildingGadgets.LOG.error("Attempted to use a 1.12 compound on a newer MC version");
                getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_WRONG_MC_VERSION
                        .componentTranslation("(1.12.x)", Minecraft.getInstance().getVersionType()).setStyle(Styles.RED), false);
                return;

            }
        } catch (CommandSyntaxException ignored) {
        }

        // todo: this needs to be put onto some kind of readTemplateFromJson(input stream).onError(e -> error)
        try {
            Template template = TemplateIO.readTemplateFromJson(CBString);
            Template readTemplate = template.clearMaterials();
            if (!nameField.getValue().isEmpty())
                readTemplate = readTemplate.withName(nameField.getValue());
            boolean replaced = replaceStack();
            ItemStack stack = container.getSlot(1).getItem();
            pasteTemplateToStack(getWorld(), stack, readTemplate, replaced);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_SUCCESS.componentTranslation().setStyle(Styles.DK_GREEN), false);
        } catch (CorruptJsonException e) {
            BuildingGadgets.LOG.error("Failed to parse json syntax.", e);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_CORRUPT_JSON
                    .componentTranslation().setStyle(Styles.RED), false);
        } catch (IllegalMinecraftVersionException e) {
            BuildingGadgets.LOG.error("Attempted to parse Template for Minecraft version {} but expected between {} and {}.",
                    e.getMinecraftVersion(), TemplateHeader.LOWEST_MC_VERSION, TemplateHeader.HIGHEST_MC_VERSION, e);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_WRONG_MC_VERSION
                    .componentTranslation(e.getMinecraftVersion(), TemplateHeader.LOWEST_MC_VERSION, TemplateHeader.HIGHEST_MC_VERSION).setStyle(Styles.RED), false);
        } catch (UnknownTemplateVersionException e) {
            BuildingGadgets.LOG.error("Attempted to parse Template version {} but newest is {}.",
                    e.getTemplateVersion(), TemplateHeader.VERSION, e);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_TOO_RECENT_VERSION
                    .componentTranslation(e.getTemplateVersion(), TemplateHeader.VERSION).setStyle(Styles.RED), false);
        } catch (JsonParseException e) {
            BuildingGadgets.LOG.error("Failed to parse Template json.", e);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_INVALID_JSON
                    .componentTranslation().setStyle(Styles.RED), false);
        } catch (TemplateReadException e) {
            BuildingGadgets.LOG.error("Failed to read Template body.", e);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED_CORRUPT_BODY
                    .componentTranslation().setStyle(Styles.RED), false);
        } catch (Exception e) {
            BuildingGadgets.LOG.error("Failed to paste Template.", e);
            getMinecraft().player.displayClientMessage(MessageTranslation.PASTE_FAILED
                    .componentTranslation().setStyle(Styles.RED), false);
        }
    }
}
