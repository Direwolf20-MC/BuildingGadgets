/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui.blocks;

import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.buffers.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.util.buffers.ToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.List;

public class TemplateManagerGUI extends ContainerScreen<TemplateManagerContainer> {
    public static final int HELP_TEXT_BACKGROUNG_COLOR = 1694460416;

    private boolean panelClicked;
    private int clickButton;
    //    private long lastDragTime;
    private int clickX, clickY;
    private float initRotX, initRotY, initZoom, initPanX, initPanY;
    private float prevRotX, prevRotY;// prevPanX, prevPanY;
    private float momentumX, momentumY;
    private float momentumDampening = 0.98f;
    private float rotX = 0, rotY = 0, zoom = 1;
    private float panX = 0, panY = 0;
    private Rectangle2d panel = new Rectangle2d(8, 18, 62, 62);

//    private int scrollAcc;

    private TextFieldWidget nameField;
    private Button buttonSave, buttonLoad, buttonCopy, buttonPaste;

//    private GuiButtonHelp buttonHelp; replace with my lovely replacement
//    private List<IHoverHelpText> helpTextProviders = new ArrayList<>();

    private TemplateManagerTileEntity te;
    private TemplateManagerContainer container;

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/template_manager.png");

    public TemplateManagerGUI(TemplateManagerContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        this.te = container.getTe();
    }

    public TemplateManagerGUI(TemplateManagerTileEntity tileEntity, TemplateManagerContainer container, PlayerInventory inv) {
        super(container, inv, new StringTextComponent("Template Manager Gui")); //TODO find out the usage of this TextComponent
        this.te = tileEntity;
        this.container = container;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        //TODO re-enable
        /*drawGuiContainerBackgroundLayer(partialTicks,mouseX,mouseY);
        super.render(mouseX, mouseY, partialTicks);
        if (buttonHelp.isSelected()) {
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.disableLighting();
            for (IHoverHelpText helpTextProvider : helpTextProviders)
                helpTextProvider.drawRect(this, HELP_TEXT_BACKGROUNG_COLOR);

            GlStateManager.enableLighting();
            for (IHoverHelpText helpTextProvider : helpTextProviders) {
                if (helpTextProvider.isHovered(mouseX, mouseY))
                    renderComponentHoverEffect(new StringTextComponent(helpTextProvider.getHoverHelpText()), mouseX, mouseY);
            }
        } else {
            this.renderHoveredToolTip(mouseX, mouseY);
        }
        if (buttonHelp.isMouseOver(mouseX, mouseY))
            renderComponentHoverEffect(new StringTextComponent(buttonHelp.getHoverText()), mouseX, mouseY);
         */
        drawString(font, "Currently disabled", getGuiLeft() + getXSize() / 2, getGuiTop() + getYSize() / 2, 0);
    }

    @Override
    public void init() {
        this.nameField = new TextFieldWidget(this.font, this.guiLeft + 8, this.guiTop + 6, 149, this.font.FONT_HEIGHT, "name?");
        /*
        super.init();
        helpTextProviders.clear();
        buttonHelp = addButton(new GuiButtonHelp(this.guiLeft + this.xSize - 16, this.guiTop + 4, (button) -> buttonHelp.toggleSelected()));
        buttonSave = addButton(createAndAddButton(79, 17, 30, 20, "Save", (button) -> PacketHandler.sendToServer(new PacketTemplateManagerSave(te.getPos(), nameField.getText()))));
        buttonLoad = addButton(createAndAddButton(137, 17, 30, 20, "Load", (button) -> PacketHandler.sendToServer(new PacketTemplateManagerLoad(te.getPos()))));
        buttonCopy = addButton(createAndAddButton(79, 61, 30, 20, "Copy", (button) -> TemplateManagerCommands.copyTemplate(container)));
        buttonPaste = addButton(createAndAddButton(135, 61, 34, 20, "Paste", (button) -> {
            String CBString = getMinecraft().keyboardListener.getClipboardString();
            if (GadgetUtils.mightBeLink(CBString)) {
                Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.pastefailed.linkcopied").getUnformattedComponentText()),false);
                return;
            }
            try {
                //Anything larger than below is likely to overflow the max packet size, crashing your client.
                ByteArrayOutputStream pasteStream = GadgetUtils.getPasteStream(JsonToNBT.getTagFromJson(CBString), nameField.getText());
                if (pasteStream != null) {
                    PacketHandler.sendToServer(new PacketTemplateManagerPaste(pasteStream, te.getPos(), nameField.getText()));
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.pastesuccess").getUnformattedComponentText()), false);
                } else {
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.pastetoobig").getUnformattedComponentText()), false);
                }
            } catch (Throwable t) {
                BuildingGadgets.LOG.error(t);
                Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.pastefailed").getUnformattedComponentText()), false);
            }
        }));

        this.nameField.setMaxStringLength(50);
        this.nameField.setVisible(true);
        children.add(nameField);

        helpTextProviders.add(new AreaHelpText(nameField, "field.template_name"));
        helpTextProviders.add(new AreaHelpText(this.getContainer().getSlot(0), guiLeft, guiTop, "slot.gadget"));
        helpTextProviders.add(new AreaHelpText(this.getContainer().getSlot(1), guiLeft, guiTop, "slot.template"));
        helpTextProviders.add(new AreaHelpText(guiLeft + 112, guiTop + 41, 22, 15, "arrow.data_flow"));
        helpTextProviders.add(new AreaHelpText(panel, guiLeft, guiTop + 10, "preview"));
        */
    }

    private Button createAndAddButton(int x, int y, int witdth, int height, String text, IPressable action) {
        Button button = new Button(guiLeft + x, guiTop + y, witdth, height, text, action);
//        helpTextProviders.add(button);
        return button;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(background);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (! buttonCopy.isHovered() && ! buttonPaste.isHovered())
            drawTexturedModalRectReverseX(guiLeft + 112, guiTop + 41, 176, 0, 22, 15, buttonLoad.isHovered());

        this.nameField.render(mouseX, mouseY, partialTicks);
        drawStructure();
    }

    public void drawTexturedModalRectReverseX(int x, int y, int textureX, int textureY, int width, int height, boolean reverse) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        if (reverse) {
            bufferbuilder.pos(x, y + height, 0).tex((textureX + width) * 0.00390625F, textureY * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y + height, 0).tex(textureX * 0.00390625F, textureY * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y, 0).tex(textureX * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
            bufferbuilder.pos(x, y, 0).tex((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
        } else {
            bufferbuilder.pos(x, y + height, 0).tex(textureX * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y + height, 0).tex((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y, 0).tex((textureX + width) * 0.00390625F, textureY * 0.00390625F).endVertex();
            bufferbuilder.pos(x, y, 0).tex(textureX * 0.00390625F, textureY * 0.00390625F).endVertex();
        }
        tessellator.draw();
    }

    private void drawStructure() {
        double scale = getMinecraft().mainWindow.getGuiScaleFactor();
        fill(guiLeft + panel.getX() - 1, guiTop + panel.getY() - 1, guiLeft + panel.getX() + panel.getWidth() + 1, guiTop + panel.getY() + panel.getHeight() + 1, 0xFF8A8A8A);
        ItemStack itemstack = this.container.getSlot(0).getStack();
//        BlockRendererDispatcher dispatcher = this.mc.getBlockRendererDispatcher();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        //float rotX = 165, rotY = 0, zoom = 1;
        if (!itemstack.isEmpty()) {
            String UUID = OurItems.gadgetCopyPaste.getUUID(itemstack);
            ToolBufferBuilder bufferBuilder = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (bufferBuilder != null) {
                BlockPos startPos = OurItems.gadgetCopyPaste.getStartPos(itemstack);
                BlockPos endPos = OurItems.gadgetCopyPaste.getEndPos(itemstack);
                if (startPos == null || endPos == null) return;
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

                GlStateManager.pushMatrix();
                //GlStateManager.translate(panel.getX() + (panel.getWidth() / 2), panel.getY() + (panel.getHeight() / 2), 100);

                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                //int scale = new ScaledResolution(mc).getScaleFactor();
                GlStateManager.multMatrix(Matrix4f.perspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000));
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                //GlStateManager.translate(-panel.getX() - panel.getWidth() / 2, -panel.getY() - panel.getHeight() / 2, 0);
                GlStateManager.viewport((int) Math.round((guiLeft + panel.getX()) * scale),
                        (int) Math.round(getMinecraft().mainWindow.getFramebufferHeight() - (guiTop + panel.getY() + panel.getHeight()) * scale),
                        (int) Math.round(panel.getWidth() * scale),
                        (int) Math.round(panel.getHeight() * scale));
                GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT, true);

                //double sc = 300 + 8 * 0.0125 * (Math.sqrt(zoom + 99) - 9);
                sc = (293 * sc) + zoom / zoomScale;
                GlStateManager.scaled(sc, sc, sc);
                int moveX = startPos.getX() - endPos.getX();

                //GlStateManager.rotate(30, 0, 1, 0);
                if (startPos.getX() >= endPos.getX()) {
                    moveX--;
                }

                GlStateManager.translated((moveX) / 1.75, -Math.abs(startPos.getY() - endPos.getY()) / 1.75, 0);
                GlStateManager.translated(panX, -panY, 0);
                GlStateManager.translated(((startPos.getX() - endPos.getX()) / 2) * -1, ((startPos.getY() - endPos.getY()) / 2) * -1, ((startPos.getZ() - endPos.getZ()) / 2) * -1);
                GlStateManager.rotatef(-rotX, 1, 0, 0);
                GlStateManager.rotatef(rotY, 0, 1, 0);
                GlStateManager.translated(((startPos.getX() - endPos.getX()) / 2), ((startPos.getY() - endPos.getY()) / 2), ((startPos.getZ() - endPos.getZ()) / 2));

                getMinecraft().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                if (bufferBuilder.getVertexCount() > 0) {
                    VertexFormat vertexformat = bufferBuilder.getVertexFormat();
                    int i = vertexformat.getSize();
                    ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
                    List<VertexFormatElement> list = vertexformat.getElements();

                    for (int j = 0; j < list.size(); ++j) {
                        VertexFormatElement vertexformatelement = list.get(j);
//                        VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
//                        int k = vertexformatelement.getType().getGlConstant();
//                        int l = vertexformatelement.getIndex();
                        bytebuffer.position(vertexformat.getOffset(j));

                        // moved to VertexFormatElement.preDraw
                        vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
                    }

                    GlStateManager.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
                    int i1 = 0;

                    for (int j1 = list.size(); i1 < j1; ++i1) {
                        VertexFormatElement vertexformatelement1 = list.get(i1);
//                        VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
//                        int k1 = vertexformatelement1.getIndex();

                        // moved to VertexFormatElement.postDraw
                        vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
                    }
                }

                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.viewport(0, 0, getMinecraft().mainWindow.getFramebufferWidth(), getMinecraft().mainWindow.getFramebufferHeight());
            }
        } else {
            rotX = 0;
            rotY = 0;
            zoom = 1;
            momentumX = 0;
            momentumY = 0;
            panX = 0;
            panY = 0;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.nameField.mouseClicked(mouseX, mouseY, mouseButton)) {
            nameField.setFocused2(true);
        } else {
            nameField.setFocused2(false);
            if (panel.contains((int) mouseX - guiLeft, (int) mouseY - guiTop)) {
                clickButton = mouseButton;
                panelClicked = true;
                clickX = (int) getMinecraft().mouseHelper.getMouseX();
                clickY = (int) getMinecraft().mouseHelper.getMouseY();
            }
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
    protected void drawGuiContainerForegroundLayer(int j, int i) {
        boolean doMomentum = false;
        if (panelClicked) {
            if (clickButton == 0) {
                prevRotX = rotX;
                prevRotY = rotY;
                rotX = initRotX - ((int) getMinecraft().mouseHelper.getMouseY() - clickY);
                rotY = initRotY + ((int) getMinecraft().mouseHelper.getMouseX() - clickX);
                momentumX = rotX - prevRotX;
                momentumY = rotY - prevRotY;
                doMomentum = false;
            } else if (clickButton == 1) {
//                prevPanX = panX;
//                prevPanY = panY;
                panX = initPanX + ((int) getMinecraft().mouseHelper.getMouseX() - clickX) / 8;
                panY = initPanY + ((int) getMinecraft().mouseHelper.getMouseY() - clickY) / 8;
            }
        }

        if (doMomentum) {
            rotX += momentumX;
            rotY += momentumY;
            momentumX *= momentumDampening;
            momentumY *= momentumDampening;
        }

        if (!nameField.isFocused() && nameField.getText().isEmpty())
            getMinecraft().fontRenderer.drawString("template name", nameField.x - guiLeft + 4, nameField.y - guiTop, - 10197916);

        if (buttonSave.isHovered() || buttonLoad.isHovered() || buttonPaste.isHovered())
            drawSlotOverlay(buttonLoad.isHovered() ? container.getSlot(0) : container.getSlot(1));
    }

    private void drawSlotOverlay(Slot slot) {
        GlStateManager.translated(0, 0, 1000);
        fill(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, - 1660903937);
        GlStateManager.translated(0, 0, -1000);
    }


    /*
    public boolean mouseScrolled(double p_mouseScrolled_1_) {
        zoom = initZoom + (float) p_mouseScrolled_1_ / 2;
        if (zoom < -200) zoom = -200;
        if (zoom > 1000) zoom = 1000;

        return super.mouseScrolled(p_mouseScrolled_1_);
    }*/

    @Override
    public void tick() {
        super.tick();
        nameField.tick();
        if (!panelClicked) {
            initRotX = rotX;
            initRotY = rotY;
            initZoom = zoom;
            initPanX = panX;
            initPanY = panY;
        }
    }
}