package com.direwolf20.buildinggadgets.blocks.templatemanager;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketTemplateManagerLoad;
import com.direwolf20.buildinggadgets.network.PacketTemplateManagerPaste;
import com.direwolf20.buildinggadgets.network.PacketTemplateManagerSave;
import com.direwolf20.buildinggadgets.tools.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.tools.ToolDireBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class TemplateManagerGUI extends GuiContainer {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextField nameField;

    private TemplateManagerTileEntity te;
    private TemplateManagerContainer container;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    public TemplateManagerGUI(TemplateManagerTileEntity tileEntity, TemplateManagerContainer container) {
        super(container);
        this.te = tileEntity;
        this.container = container;
        //xSize = WIDTH;
        //ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        //The parameters of GuiButton are(id, x, y, width, height, text);
        this.buttonList.add(new GuiButton(1, this.guiLeft + 87, this.guiTop + 11, 30, 20, "Save"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 136, this.guiTop + 11, 30, 20, "Load"));
        this.buttonList.add(new GuiButton(3, this.guiLeft + 87, this.guiTop + 55, 30, 20, "Copy"));
        this.buttonList.add(new GuiButton(4, this.guiLeft + 134, this.guiTop + 55, 35, 20, "Paste"));
        this.nameField = new GuiTextField(0, this.fontRenderer, this.guiLeft + 5, this.guiTop + 6, 80, this.fontRenderer.FONT_HEIGHT);
        this.nameField.setMaxStringLength(50);
        this.nameField.setVisible(true);
        //NOTE: the id always has to be different or else it might get called twice or never!
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.nameField.drawTextBox();
        //drawStructure();
    }

    private void drawStructure() {
        Rectangle panel = new Rectangle(this.guiLeft + 8, this.guiTop + 14, 50, 50);
        ItemStack itemstack = this.container.getSlot(0).getStack();
        BlockRendererDispatcher dispatcher = this.mc.getBlockRendererDispatcher();
        float rotX = 165, rotY = 0, zoom = 1;
        if (!itemstack.isEmpty()) {
            String UUID = CopyPasteTool.getUUID(itemstack);
            ToolDireBuffer bufferBuilder = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (bufferBuilder != null) {

                GlStateManager.pushMatrix();
                GlStateManager.translate(guiLeft, guiTop, 0);
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                //GlStateManager.loadIdentity();
                int scale = new ScaledResolution(mc).getScaleFactor();
                //Project.gluPerspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                //GlStateManager.translate(-panel.getX() - panel.getWidth() / 2, -panel.getY() - panel.getHeight() / 2, 0);
                //GlStateManager.viewport((guiLeft + panel.getX()) * scale, mc.displayHeight - (guiTop + panel.getY() + panel.getHeight()) * scale, panel.getWidth() * scale, panel.getHeight() * scale);
                GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
                GlStateManager.scale(50, 10, 0);//Slightly Larger block to avoid z-fighting.

                if (bufferBuilder.getVertexCount() > 0) {
                    VertexFormat vertexformat = bufferBuilder.getVertexFormat();
                    int i = vertexformat.getNextOffset();
                    ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
                    List<VertexFormatElement> list = vertexformat.getElements();

                    for (int j = 0; j < list.size(); ++j) {
                        VertexFormatElement vertexformatelement = list.get(j);
                        VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                        int k = vertexformatelement.getType().getGlConstant();
                        int l = vertexformatelement.getIndex();
                        bytebuffer.position(vertexformat.getOffset(j));

                        // moved to VertexFormatElement.preDraw
                        vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
                    }

                    GlStateManager.glDrawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
                    int i1 = 0;

                    for (int j1 = list.size(); i1 < j1; ++i1) {
                        VertexFormatElement vertexformatelement1 = list.get(i1);
                        VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
                        int k1 = vertexformatelement1.getIndex();

                        // moved to VertexFormatElement.postDraw
                        vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
                    }
                }

                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);

            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 1) {
            PacketHandler.INSTANCE.sendToServer(new PacketTemplateManagerSave(te.getPos(), nameField.getText()));
        } else if (b.id == 2) {
            PacketHandler.INSTANCE.sendToServer(new PacketTemplateManagerLoad(te.getPos()));
        } else if (b.id == 3) {
            TemplateManagerCommands.CopyTemplate(container);
        } else if (b.id == 4) {
            String CBString = getClipboardString();
            //System.out.println("CBString Length: " + CBString.length());
            //System.out.println(CBString);
            try {
                NBTTagCompound tagCompound = JsonToNBT.getTagFromJson(CBString);
                //BlockMapIntState MapIntState = CopyPasteTool.getBlockMapIntState(tagCompound);
                int[] stateArray = tagCompound.getIntArray("stateIntArray");
                //int[] posArray = tagCompound.getIntArray("posIntArray");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                CompressedStreamTools.writeCompressed(tagCompound, baos);
                //ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                //NBTTagCompound newTag = CompressedStreamTools.readCompressed(bais);
                //System.out.println("BAOS Size: " + baos.size());

                //Anything larger than below is likely to overflow the max packet size, crashing your client.
                if (stateArray.length <= 12000 && baos.size() < 31000) {
                    PacketHandler.INSTANCE.sendToServer(new PacketTemplateManagerPaste(tagCompound, te.getPos(), nameField.getText()));
                } else {
                    Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.pastetoobig").getUnformattedComponentText()), false);
                }

            } catch (Throwable t) {
                System.out.println(t);
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.pastefailed").getUnformattedComponentText()), false);
            }

        }
    }

    /*public static void sendSplitArrays(int[] stateArray, int[] posArray, Map<Short, IBlockState> stateMap) {

        System.out.println("PosArray Length: " + posArray.length);
    }*/

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.nameField.mouseClicked(mouseX, mouseY, mouseButton)) {
            nameField.setFocused(true);
        } else {
            nameField.setFocused(false);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
