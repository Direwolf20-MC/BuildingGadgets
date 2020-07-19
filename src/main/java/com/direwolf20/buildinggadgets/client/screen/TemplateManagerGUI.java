/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateManagerTemplateCreated;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.template.*;
import com.direwolf20.buildinggadgets.common.template.ITemplateProvider.IUpdateListener;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
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
import com.direwolf20.buildinggadgets.common.world.MockDelegationWorld;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateManagerGUI extends ContainerScreen<TemplateManagerContainer> {
    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/template_manager.png");

    private Rectangle2d panel = new Rectangle2d((8 - 20), 12, 136, 80);
    private boolean panelClicked;
    private int clickButton, clickX, clickY;
    private float initRotX, initRotY, initZoom, initPanX, initPanY;
    private float momentumX, momentumY;
    private float rotX = 0, rotY = 0, zoom = 1;
    private float panX = 0, panY = 0;

    private TextFieldWidget nameField;
    private Button buttonSave, buttonLoad, buttonCopy, buttonPaste;

    private TemplateManagerTileEntity te;
    private TemplateManagerContainer container;
    private LazyOptional<ITemplateProvider> templateProvider = getWorld().getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY);

    // It is so stupid I can't get the key from the template.
    private Template template;

    public TemplateManagerGUI(TemplateManagerContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, new StringTextComponent(""));

        this.container = container;
        this.te = container.getTe();
    }

    @Override
    public void init() {
        super.init();
        this.nameField = new TextFieldWidget(this.font, (this.guiLeft - 20) + 8, guiTop - 5, xSize - 16, this.font.FONT_HEIGHT + 3, GuiTranslation.TEMPLATE_NAME_TIP.format());

        int x = (guiLeft - 20) + 180;
        buttonSave = addButton(new Button(x, guiTop + 17, 60, 20, GuiTranslation.BUTTON_SAVE.format(), b -> onSave()));
        buttonLoad = addButton(new Button(x,guiTop + 39, 60, 20, GuiTranslation.BUTTON_LOAD.format(), b -> onLoad()));
        buttonCopy = addButton(new Button(x, guiTop + 66, 60, 20, GuiTranslation.BUTTON_COPY.format(), b -> onCopy()));
        buttonPaste = addButton(new Button(x, guiTop + 89, 60, 20, GuiTranslation.BUTTON_PASTE.format(), b -> onPaste()));

        this.nameField.setMaxStringLength(50);
        this.nameField.setVisible(true);
        children.add(nameField);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        validateCache(partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.renderBackground();

        getMinecraft().getTextureManager().bindTexture(background);
        blit(guiLeft - 20, guiTop - 12, 0, 0, xSize, ySize + 25);
        blit((guiLeft - 20) + xSize, guiTop + 8, xSize + 3, 30, 71, ySize);

        if (! buttonCopy.isHovered() && ! buttonPaste.isHovered()) {
            if( buttonLoad.isHovered() )
                blit((guiLeft + xSize) - 44, guiTop + 38, xSize, 0, 17, 24);
            else
                blit((guiLeft + xSize) - 44, guiTop + 38, xSize + 17, 0, 16, 24);
        }

        this.nameField.render(mouseX, mouseY, partialTicks);
        fill(guiLeft + panel.getX() - 1, guiTop + panel.getY() - 1, guiLeft + panel.getX() + panel.getWidth() + 1, guiTop + panel.getY() + panel.getHeight() + 1, 0xFF8A8A8A);

        if( this.template != null ) {
            renderPanel();
            renderRequirement();
        }
    }

    private void validateCache(float partialTicks) {
        // Invalidate the render
        if( container.getSlot(0).getStack().isEmpty() && template != null ) {
            template = null;
            resetViewport();
            return;
        }

        container.getSlot(0).getStack().getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
            // Make sure we're not re-creating the same cache.
            Template template = provider.getTemplateForKey(key);
            if( this.template == template )
                return;

            this.template = template;

//            IBuildView view = template.createViewInContext(
//                    SimpleBuildContext.builder()
//                            .buildingPlayer(getMinecraft().player)
//                            .usedStack(container.getSlot(0).getStack())
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
        BlockRendererDispatcher dispatcher = getMinecraft().getBlockRendererDispatcher();

        BufferBuilder bufferBuilder = new BufferBuilder(2097152);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (PlacementTarget target : view) {
            target.placeIn(view.getContext());
            BlockPos targetPos = target.getPos();
            BlockState renderBlockState = view.getContext().getWorld().getBlockState(targetPos);
            TileEntity te = view.getContext().getWorld().getTileEntity(targetPos);

            if (renderBlockState.getRenderType() == BlockRenderType.MODEL) {
                IBakedModel model = dispatcher.getModelForState(renderBlockState);
//                dispatcher.getBlockModelRenderer().renderModelFlat()
//                        .renderModelFlat(getWorld(), model, renderBlockState, target.getPos(), bufferBuilder, false,
//                        rand, 0L, te != null ? te.getModelData() : EmptyModelData.INSTANCE);
            }

            if (te != null) {
                try {
                    TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
                    if (renderer != null) {
//                        if (te.hasFastRenderer())
//                            renderer.renderTileEntityFast(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, - 1, bufferBuilder);
//                        else
//                            renderer.render(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, - 1);
                    }
                    //remember vanilla Tiles rebinding the TextureAtlas
                    getMinecraft().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
                } catch (Exception e) {
                    BuildingGadgets.LOG.error("Error rendering TileEntity", e);
                }
            }
        }

        bufferBuilder.finishDrawing();

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

    private void renderRequirement() {
        MaterialList requirements = this.template.getHeaderAndForceMaterials(SimpleBuildContext.builder().build(getWorld())).getRequiredItems();
        if( requirements == null )
            return;

        RenderHelper.enableStandardItemLighting();

        RenderSystem.pushMatrix();
        RenderSystem.translated(guiLeft - 32, guiTop - 5, 0);
        RenderSystem.scalef(.8f, .8f, .8f);

        drawRightAlignedString(getMinecraft().fontRenderer, "Requirements", 0, 0, Color.WHITE.getRGB());

        // The things you have to do to get anything from this system is just stupid.
        MatchResult list = InventoryHelper.CREATIVE_INDEX.tryMatch(requirements);
        ImmutableMultiset<IUniqueObject<?>> foundItems = list.getFoundItems();

        // Reverse sorted list of items required.
        List<Multiset.Entry<IUniqueObject<?>>> sortedEntries = ImmutableList.sortedCopyOf(Comparator
                .<Multiset.Entry<IUniqueObject<?>>, Integer>comparing(Multiset.Entry::getCount)
                .reversed(), list.getChosenOption().entrySet());

        int index = 0, column = 0;
        for(Multiset.Entry<IUniqueObject<?>> e: sortedEntries) {
                itemRenderer.renderItemAndEffectIntoGUI(this.minecraft.player, e.getElement().createStack(2), -20 - (column * 25), 25 + (index * 25));
                itemRenderer.renderItemOverlayIntoGUI(Minecraft.getInstance().fontRenderer, e.getElement().createStack(2), -20 - (column * 25), 25 + (index * 25), GadgetUtils.withSuffix(foundItems.count(e.getElement())));
                index ++;
                if( index % 8 == 0 ) {
                    column ++;
                    index = 0;
                }
        }

        RenderHelper.disableStandardItemLighting();
        RenderSystem.popMatrix();

    }

    private void pasteTemplateToStack(World world, ItemStack stack, Template newTemplate, boolean replaced) {
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider ->
                pasteTemplateToStack(provider, stack, newTemplate, replaced && world.isRemote()));
    }

    private void pasteTemplateToStack(ITemplateProvider provider, ItemStack stack, Template newTemplate, boolean replaced) {
        stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
            provider.setTemplate(key, newTemplate);
            if (replaced)
                PacketHandler.sendToServer(new PacketTemplateManagerTemplateCreated(provider.getId(key), te.getPos()));
            else
                provider.requestRemoteUpdate(key);
        });
    }

    private boolean replaceStack() {
        ItemStack stack = container.getSlot(1).getStack();
        if (stack.isEmpty())
            return false;

        if (stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return false;

        else if (TemplateManagerTileEntity.TEMPLATE_CONVERTIBLES.contains(stack.getItem())) {
            container.putStackInSlot(1, new ItemStack(OurItems.template));
            return true;
        }

        return false;
    }

    private void rename(ItemStack stack) {
        if (nameField.getText().isEmpty())
            return;

        stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
            Template template = provider.getTemplateForKey(key);
            template = template.withName(nameField.getText());
            provider.setTemplate(key, template);
            provider.requestRemoteUpdate(key);
        }));
    }

    private void renderPanel() {
        double scale = getMinecraft().getMainWindow().getGuiScaleFactor();

        BlockPos startPos = template.getHeader().getBoundingBox().getMin();
        BlockPos endPos = template.getHeader().getBoundingBox().getMax();

        double lengthX = Math.abs(startPos.getX() - endPos.getX());
        double lengthY = Math.abs(startPos.getY() - endPos.getY());
        double lengthZ = Math.abs(startPos.getZ() - endPos.getZ());

        final double maxW = 6 * 16;
        final double maxH = 11 * 16;

        double overW = Math.max(lengthX * 16 - maxW, lengthZ * 16 - maxW);
        double overH = lengthY * 16 - maxH;

        double sc = 1;
        double zoomScale = 1;

        if (overW > 0 && overW >= overH) {
            sc = maxW / (overW + maxW);
            zoomScale = overW / 40;
        } else if (overH > 0 && overH >= overW) {
            sc = maxH / (overH + maxH);
            zoomScale = overH / 40;
        }

        RenderSystem.pushMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();

        RenderSystem.multMatrix(Matrix4f.perspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000));
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.viewport((int) Math.round((guiLeft + panel.getX()) * scale),
                (int) Math.round(getMinecraft().getMainWindow().getFramebufferHeight() - (guiTop + panel.getY() + panel.getHeight()) * scale),
                (int) Math.round(panel.getWidth() * scale),
                (int) Math.round(panel.getHeight() * scale));

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, true);

        sc = (293 * sc) + zoom / zoomScale;
        RenderSystem.scaled(sc, sc, sc);
        int moveX = startPos.getX() - endPos.getX();

        RenderSystem.rotatef(30, 0, 1, 0);
        if (startPos.getX() >= endPos.getX())
            moveX--;

        RenderSystem.translated((moveX) / 1.75, -Math.abs(startPos.getY() - endPos.getY()) / 1.75, 0);
        RenderSystem.translated(panX, -panY, 0);
        RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f) * -1, ((startPos.getY() - endPos.getY()) / 2f) * -1, ((startPos.getZ() - endPos.getZ()) / 2f) * -1);
        RenderSystem.rotatef(-rotX, 1, 0, 0);
        RenderSystem.rotatef(rotY, 0, 1, 0);
        RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f), ((startPos.getY() - endPos.getY()) / 2f), ((startPos.getZ() - endPos.getZ()) / 2f));

        getMinecraft().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

//        RenderSystem.callList(displayList);

        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.viewport(0, 0, getMinecraft().getMainWindow().getFramebufferWidth(), getMinecraft().getMainWindow().getFramebufferHeight());
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
        if (panel.contains((int) mouseX - guiLeft, (int) mouseY - guiTop)) {
            clickButton = mouseButton;
            panelClicked = true;
            clickX = (int) getMinecraft().mouseHelper.getMouseX();
            clickY = (int) getMinecraft().mouseHelper.getMouseY();
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
    protected void drawGuiContainerForegroundLayer(int j, int i) {
        if (panelClicked) {
            if (clickButton == 0) {
                float prevRotX = rotX;
                float prevRotY = rotY;
                rotX = initRotX - ((int) getMinecraft().mouseHelper.getMouseY() - clickY);
                rotY = initRotY + ((int) getMinecraft().mouseHelper.getMouseX() - clickX);
                momentumX = rotX - prevRotX;
                momentumY = rotY - prevRotY;
            } else if (clickButton == 1) {
                panX = initPanX + ((int) getMinecraft().mouseHelper.getMouseX() - clickX) / 8f;
                panY = initPanY + ((int) getMinecraft().mouseHelper.getMouseY() - clickY) / 8f;
            }
        }

        rotX += momentumX;
        rotY += momentumY;
        float momentumDampening = 0.98f;
        momentumX *= momentumDampening;
        momentumY *= momentumDampening;

        if (! nameField.isFocused() && nameField.getText().isEmpty())
            getMinecraft().fontRenderer.drawString(GuiTranslation.TEMPLATE_PLACEHOLDER.format(), nameField.x - guiLeft + 4, (nameField.y + 2) - guiTop, - 10197916);

        if (buttonSave.isHovered() || buttonLoad.isHovered() || buttonPaste.isHovered())
            drawSlotOverlay(buttonLoad.isHovered() ? container.getSlot(0) : container.getSlot(1));
    }

    private void drawSlotOverlay(Slot slot) {
        RenderSystem.translated(0, 0, 1000);
        fill(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, - 1660903937);
        RenderSystem.translated(0, 0, - 1000);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        zoom = initZoom + ((float) scrollDelta * 20);
        if (zoom < -200) zoom = -200;
        if (zoom > 5000) zoom = 5000;

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public void tick() {
        super.tick();
        nameField.tick();
        if (! panelClicked) {
            initRotX = rotX;
            initRotY = rotY;
            initZoom = zoom;
            initPanX = panX;
            initPanY = panY;
        }
    }

    private World getWorld() {
        return getMinecraft().world;
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    // Events
    // we need to ensure that the Template we want to look at is recent, before we take any further action
    private void runAfterUpdate(int slot, Runnable runnable) {
        container.getSlot(slot).getStack().getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
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
        ItemStack left = container.getSlot(0).getStack();
        ItemStack right = container.getSlot(1).getStack();
        if (left.isEmpty()) {
            rename(right);
            return;
        }

        runAfterUpdate(0, () -> { //we are copying form 0 to 1 => slot 0 needs to be the recent one
            templateProvider.ifPresent(provider -> {
                left.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                    Template templateToSave = provider.getTemplateForKey(key);
                    pasteTemplateToStack(provider, right, templateToSave, replaced);
                });
            });
        });
    }

    private void onLoad() {
        boolean replaced = replaceStack();
        ItemStack left = container.getSlot(0).getStack();
        ItemStack right = container.getSlot(1).getStack();
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

            ItemStack stack = container.getSlot(0).getStack();
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                templateProvider.ifPresent(provider -> {
                    PlayerEntity player = getMinecraft().player;
                    assert player != null;

                    IBuildContext buildContext = SimpleBuildContext.builder()
                            .buildingPlayer(player)
                            .usedStack(stack)
                            .build(getWorld());
                    try {
                        Template template = provider.getTemplateForKey(key);
                        if (! nameField.getText().isEmpty())
                            template = template.withName(nameField.getText());
                        String json = TemplateIO.writeTemplateJson(template, buildContext);
                        getMinecraft().keyboardListener.setClipboardString(json);
                        player.sendStatusMessage(MessageTranslation.CLIPBOARD_COPY_SUCCESS.componentTranslation().setStyle(Styles.DK_GREEN), false);
                    } catch (DataCannotBeWrittenException e) {
                        BuildingGadgets.LOG.error("Failed to write Template.", e);
                        player.sendStatusMessage(MessageTranslation.CLIPBOARD_COPY_ERROR_TEMPLATE.componentTranslation().setStyle(Styles.RED), false);
                    } catch (Exception e) {
                        BuildingGadgets.LOG.error("Failed to copy Template to clipboard.", e);
                        player.sendStatusMessage(MessageTranslation.CLIPBOARD_COPY_ERROR.componentTranslation().setStyle(Styles.RED), false);
                    }
                });
            });
        });
    }

    private void onPaste() {
        assert getMinecraft().player != null;

        String CBString = getMinecraft().keyboardListener.getClipboardString();
        if (GadgetUtils.mightBeLink(CBString)) {
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_SUCCESS.componentTranslation().setStyle(Styles.RED), false);
            return;
        }

        // todo: this needs to be put onto some kind of readTemplateFromJson(input stream).onError(e -> error)
        try {
            Template readTemplate = TemplateIO.readTemplateFromJson(CBString).clearMaterials();
            if (! nameField.getText().isEmpty())
                readTemplate = readTemplate.withName(nameField.getText());
            boolean replaced = replaceStack();
            ItemStack stack = container.getSlot(1).getStack();
            pasteTemplateToStack(getWorld(), stack, readTemplate, replaced);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_SUCCESS.componentTranslation().setStyle(Styles.DK_GREEN), false);
        } catch (CorruptJsonException e) {
            BuildingGadgets.LOG.error("Failed to parse json syntax.", e);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_FAILED_CORRUPT_JSON
                    .componentTranslation().setStyle(Styles.RED), false);
        } catch (IllegalMinecraftVersionException e) {
            BuildingGadgets.LOG.error("Attempted to parse Template for Minecraft version {} but expected {}.",
                    e.getMinecraftVersion(), e.getExpectedVersion(), e);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_FAILED_WRONG_MC_VERSION
                    .componentTranslation(e.getMinecraftVersion(), e.getExpectedVersion()).setStyle(Styles.RED), false);
        } catch (UnknownTemplateVersionException e) {
            BuildingGadgets.LOG.error("Attempted to parse Template version {} but newest is {}.",
                    e.getTemplateVersion(), TemplateHeader.VERSION, e);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_FAILED_TOO_RECENT_VERSION
                    .componentTranslation(e.getTemplateVersion(), TemplateHeader.VERSION).setStyle(Styles.RED), false);
        } catch (JsonParseException e) {
            BuildingGadgets.LOG.error("Failed to parse Template json.", e);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_FAILED_INVALID_JSON
                    .componentTranslation().setStyle(Styles.RED), false);
        } catch (TemplateReadException e) {
            BuildingGadgets.LOG.error("Failed to read Template body.", e);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_FAILED_CORRUPT_BODY
                    .componentTranslation().setStyle(Styles.RED), false);
        } catch (Exception e) {
            BuildingGadgets.LOG.error("Failed to paste Template.", e);
            getMinecraft().player.sendStatusMessage(MessageTranslation.PASTE_FAILED
                    .componentTranslation().setStyle(Styles.RED), false);
        }
    }
}