/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import com.direwolf20.buildinggadgets.client.gui.AreaHelpText;
import com.direwolf20.buildinggadgets.client.gui.GuiButtonHelp;
import com.direwolf20.buildinggadgets.client.gui.GuiButtonHelpText;
import com.direwolf20.buildinggadgets.client.gui.IHoverHelpText;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateManagerLoad;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateManagerPaste;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateManagerSave;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.buffers.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.utils.buffers.ToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TemplateManagerGUI extends GuiContainer {
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

    private GuiTextField nameField;
    private GuiButton buttonSave, buttonLoad, buttonCopy, buttonPaste;

    private GuiButtonHelp buttonHelp;
    private List<IHoverHelpText> helpTextProviders = new ArrayList<>();

    private TemplateManagerTileEntity te;
    private TemplateManagerContainer container;

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/testcontainer.png");

    public TemplateManagerGUI(TemplateManagerTileEntity tileEntity, TemplateManagerContainer container) {
        super(container);
        this.te = tileEntity;
        this.container = container;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        if (buttonHelp.isSelected()) {
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.disableLighting();
            for (IHoverHelpText helpTextProvider : helpTextProviders)
                helpTextProvider.drawRect(this, HELP_TEXT_BACKGROUNG_COLOR);

            GlStateManager.enableLighting();
            for (IHoverHelpText helpTextProvider : helpTextProviders) {
                if (helpTextProvider.isHovered(mouseX, mouseY))
                    drawHoveringText(helpTextProvider.getHoverHelpText(), mouseX, mouseY);
            }
        } else {
            this.renderHoveredToolTip(mouseX, mouseY);
        }
        if (buttonHelp.isMouseOver())
            drawHoveringText(buttonHelp.getHoverText(), mouseX, mouseY);
    }

    @Override
    public void initGui() {
        super.initGui();
        helpTextProviders.clear();
        buttonHelp = addButton(new GuiButtonHelp(this.guiLeft + this.xSize - 16, this.guiTop + 4, () -> buttonHelp.toggleSelected()));
        buttonSave = addButton(createAndAddButton(79, 17, 30, 20, "Save", () -> PacketHandler.sendToServer(new PacketTemplateManagerSave(te.getPos(), nameField.getText()))));
        buttonLoad = addButton(createAndAddButton(137, 17, 30, 20, "Load", () -> PacketHandler.sendToServer(new PacketTemplateManagerLoad(te.getPos()))));
        buttonCopy = addButton(createAndAddButton(79, 61, 30, 20, "Copy", () -> TemplateManagerCommands.copyTemplate(container)));
        buttonPaste = addButton(createAndAddButton(135, 61, 34, 20, "Paste", () -> {
            String CBString = mc.keyboardListener.getClipboardString();
            if (GadgetUtils.mightBeLink(CBString)) {
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.pastefailed.linkcopied").getUnformattedComponentText()),false);
                return;
            }
            try {
                //Anything larger than below is likely to overflow the max packet size, crashing your client.
                ByteArrayOutputStream pasteStream = GadgetUtils.getPasteStream(JsonToNBT.getTagFromJson(CBString), nameField.getText());
                if (pasteStream != null) {
                    PacketHandler.sendToServer(new PacketTemplateManagerPaste(pasteStream, te.getPos(), nameField.getText()));
                    Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.pastesuccess").getUnformattedComponentText()), false);
                } else {
                    Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.pastetoobig").getUnformattedComponentText()), false);
                }
            } catch (Throwable t) {
                BuildingGadgets.LOG.error(t);
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.pastefailed").getUnformattedComponentText()), false);
            }
        }));

        this.nameField = new GuiTextField(0, this.fontRenderer, this.guiLeft + 8, this.guiTop + 6, 149, this.fontRenderer.FONT_HEIGHT);
        this.nameField.setMaxStringLength(50);
        this.nameField.setVisible(true);
        children.add(nameField);

        helpTextProviders.add(new AreaHelpText(nameField, "field.template_name"));
//      TODO 1.13
//        helpTextProviders.add(new AreaHelpText(inventorySlots.getSlot(0), guiLeft, guiTop, "slot.gadget"));
//        helpTextProviders.add(new AreaHelpText(inventorySlots.getSlot(1), guiLeft, guiTop, "slot.template"));
        helpTextProviders.add(new AreaHelpText(guiLeft + 112, guiTop + 41, 22, 15, "arrow.data_flow"));
        helpTextProviders.add(new AreaHelpText(panel, guiLeft, guiTop + 10, "preview"));
    }

    private GuiButton createAndAddButton(int x, int y, int witdth, int height, String text, @Nullable Runnable action) {
        GuiButtonHelpText button = new GuiButtonHelpText(guiLeft + x, guiTop + y, witdth, height, text, text.toLowerCase(), action);
        helpTextProviders.add(button);
        return button;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (!buttonCopy.isMouseOver() && !buttonPaste.isMouseOver())
            drawTexturedModalRectReverseX(guiLeft + 112, guiTop + 41, 176, 0, 22, 15, buttonLoad.isMouseOver());

        this.nameField.drawTextField(mouseX, mouseY, partialTicks);
//      TODO 1.13
//        drawStructure();
    }

    public void drawTexturedModalRectReverseX(int x, int y, int textureX, int textureY, int width, int height, boolean reverse) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        if (reverse) {
            bufferbuilder.pos(x + 0, y + height, zLevel).tex((textureX + width) * 0.00390625F, textureY * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y + height, zLevel).tex(textureX * 0.00390625F, textureY * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y + 0, zLevel).tex(textureX * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
            bufferbuilder.pos(x + 0, y + 0, zLevel).tex((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
        } else {
            bufferbuilder.pos(x + 0, y + height, zLevel).tex(textureX * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y + height, zLevel).tex((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
            bufferbuilder.pos(x + width, y + 0, zLevel).tex((textureX + width) * 0.00390625F, textureY * 0.00390625F).endVertex();
            bufferbuilder.pos(x + 0, y + 0, zLevel).tex(textureX * 0.00390625F, textureY * 0.00390625F).endVertex();
        }
        tessellator.draw();
    }

    private void drawStructure() {
        int scale = mc.mainWindow.getScaleFactor(this.mc.gameSettings.guiScale);

        drawRect(guiLeft + panel.getX() - 1, guiTop + panel.getY() - 1, guiLeft + panel.getX() + panel.getWidth() + 1, guiTop + panel.getY() + panel.getHeight() + 1, 0xFF8A8A8A);
        ItemStack itemstack = this.container.getSlot(0).getStack();
//        BlockRendererDispatcher dispatcher = this.mc.getBlockRendererDispatcher();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        //float rotX = 165, rotY = 0, zoom = 1;
        if (!itemstack.isEmpty()) {
            String UUID = ((GadgetCopyPaste) BGItems.gadgetCopyPaste).getUUID(itemstack);
            ToolBufferBuilder bufferBuilder = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (bufferBuilder != null) {
                BlockPos startPos = ((GadgetCopyPaste) BGItems.gadgetCopyPaste).getStartPos(itemstack);
                BlockPos endPos = ((GadgetCopyPaste) BGItems.gadgetCopyPaste).getEndPos(itemstack);
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


                //System.out.println(distance);
                GlStateManager.pushMatrix();
                //GlStateManager.translate(panel.getX() + (panel.getWidth() / 2), panel.getY() + (panel.getHeight() / 2), 100);

                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                //int scale = new ScaledResolution(mc).getScaleFactor();
                Matrix4f.perspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                //GlStateManager.translate(-panel.getX() - panel.getWidth() / 2, -panel.getY() - panel.getHeight() / 2, 0);
                GlStateManager.viewport((guiLeft + panel.getX()) * scale, this.height - (guiTop + panel.getY() + panel.getHeight()) * scale, panel.getWidth() * scale, panel.getHeight() * scale);
                GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

                //double sc = 300 + 8 * 0.0125 * (Math.sqrt(zoom + 99) - 9);
                sc = (293 * sc) + zoom / zoomScale;
                GlStateManager.scaled(sc, sc, sc);
                int moveX = startPos.getX() - endPos.getX();

                //GlStateManager.rotate(30, 0, 1, 0);
                if (startPos.getX() >= endPos.getX()) {
                    moveX--;
                    //GlStateManager.rotate(90, 0, -1, 0);
                }

                GlStateManager.translated((moveX) / 1.75, -Math.abs(startPos.getY() - endPos.getY()) / 1.75, 0);
                GlStateManager.translated(panX, panY, 0);
//System.out.println(((startPos.getX() - endPos.getX()) / 2) * -1 + ":" + ((startPos.getY() - endPos.getY()) / 2) * -1 + ":" + ((startPos.getZ() - endPos.getZ()) / 2) * -1);
                GlStateManager.translated(((startPos.getX() - endPos.getX()) / 2) * -1, ((startPos.getY() - endPos.getY()) / 2) * -1, ((startPos.getZ() - endPos.getZ()) / 2) * -1);
                GlStateManager.rotatef(rotX, 1, 0, 0);
                GlStateManager.rotatef(rotY, 0, 1, 0);
                GlStateManager.translated(((startPos.getX() - endPos.getX()) / 2), ((startPos.getY() - endPos.getY()) / 2), ((startPos.getZ() - endPos.getZ()) / 2));

                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if ((startPos.getX() - endPos.getX()) == 0) {
                    //GlStateManager.rotate(270, 0, 1, 0);
                }
                //Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                //dispatcher.renderBlockBrightness(Blocks.GLASS.getDefaultState(), 1f);
                //Tessellator.getInstance().draw();

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
                GlStateManager.viewport(0, 0, this.width, this.height);

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
            nameField.setFocused(true);
        } else {
            nameField.setFocused(false);
            if (panel.contains((int) mouseX - guiLeft, (int) mouseY - guiTop)) {
                clickButton = mouseButton;
                panelClicked = true;
                clickX = (int) mc.mouseHelper.getMouseX();
                clickY = (int) mc.mouseHelper.getMouseY();
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
                rotX = initRotX - ((int) mc.mouseHelper.getMouseY() - clickY);
                rotY = initRotY + ((int) mc.mouseHelper.getMouseX() - clickX);
                momentumX = rotX - prevRotX;
                momentumY = rotY - prevRotY;
                doMomentum = false;
            } else if (clickButton == 1) {
//                prevPanX = panX;
//                prevPanY = panY;
                panX = initPanX + ((int) mc.mouseHelper.getMouseX() - clickX) / 8;
                panY = initPanY + ((int) mc.mouseHelper.getMouseY() - clickY) / 8;
            }
        }

        if (doMomentum) {
            rotX += momentumX;
            rotY += momentumY;
            momentumX *= momentumDampening;
            momentumY *= momentumDampening;
        }

        if (!nameField.isFocused() && nameField.getText().isEmpty())
            fontRenderer.drawString("template name", nameField.x - guiLeft + 4, nameField.y - guiTop, -10197916);

//        TODO 1.13
//        if (buttonSave.isMouseOver() || buttonLoad.isMouseOver() || buttonPaste.isMouseOver())
//            drawSlotOverlay(buttonLoad.isMouseOver() ? container.getSlot(0) : container.getSlot(1));
    }

    private void drawSlotOverlay(Slot slot) {
        GlStateManager.translated(0, 0, 1000);
        drawRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, -1660903937);
        GlStateManager.translated(0, 0, -1000);
    }


    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_) {
        zoom = initZoom + (float) p_mouseScrolled_1_ / 2;
        if (zoom < -200) zoom = -200;
        if (zoom > 1000) zoom = 1000;

        return super.mouseScrolled(p_mouseScrolled_1_);
    }

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