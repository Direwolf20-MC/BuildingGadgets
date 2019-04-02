// package com.direwolf20.buildinggadgets.client.gui.base;
//
// import net.minecraft.client.Minecraft;
// import net.minecraft.client.gui.Gui;
// import net.minecraft.client.renderer.BufferBuilder;
// import net.minecraft.client.renderer.GlStateManager;
// import net.minecraft.client.renderer.Tessellator;
// import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
// import net.minecraft.util.math.MathHelper;
// import net.minecraftforge.fml.client.config.GuiUtils;
// import org.lwjgl.opengl.GL11;
//
// import java.io.IOException;
//
// /*
//  * This class was adapted from GuiScrollingList from Minecraft Forge 1.12. This class will not be maintained and once
//  * the official version of it was out, it should be deleted.
//  */
// public abstract class GuiScrollingList {
//
//     protected final int listWidth;
//     protected final int listHeight;
//     protected final int screenWidth;
//     protected final int screenHeight;
//     protected final int top;
//     protected final int bottom;
//     protected final int right;
//     protected final int left;
//     protected final int slotHeight;
//     protected int mouseX;
//     protected int mouseY;
//     private float initialMouseClickY = -2.0F;
//     private float scrollFactor;
//     private float scrollDistance;
//     protected int selectedIndex = -1;
//     private long lastClickTime = 0L;
//     private boolean hasHeader;
//     private int headerHeight;
//
//     public GuiScrollingList(int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
//         this.listWidth = width;
//         this.listHeight = height;
//         this.top = top;
//         this.bottom = bottom;
//         this.slotHeight = entryHeight;
//         this.left = left;
//         this.right = width + this.left;
//         this.screenWidth = screenWidth;
//         this.screenHeight = screenHeight;
//     }
//
//     protected void setHeaderInfo(boolean hasHeader, int headerHeight) {
//         this.hasHeader = hasHeader;
//         this.headerHeight = headerHeight;
//         if (!hasHeader) this.headerHeight = 0;
//     }
//
//     protected abstract int getSize();
//
//     protected abstract void elementClicked(int index, boolean doubleClick);
//
//     protected abstract boolean isSelected(int index);
//
//     protected int getContentHeight() {
//         return this.getSize() * this.slotHeight + this.headerHeight;
//     }
//
//     protected abstract void drawBackground();
//
//     /**
//      * Draw anything special on the screen. GL_SCISSOR is enabled for anything that is rendered outside of the view box.
//      * Do not mess with SCISSOR unless you support this.
//      */
//     protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess);
//
//     /**
//      * Draw anything special on the screen. GL_SCISSOR is enabled for anything that is rendered outside of the view box.
//      * Do not mess with SCISSOR unless you support this.
//      */
//     protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
//     }
//
//     protected void clickHeader(int x, int y) {
//     }
//
//     private void applyScrollLimits() {
//         int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
//
//         if (listHeight < 0) {
//             listHeight /= 2;
//         }
//
//         if (this.scrollDistance < 0.0F) {
//             this.scrollDistance = 0.0F;
//         }
//
//         if (this.scrollDistance > (float) listHeight) {
//             this.scrollDistance = (float) listHeight;
//         }
//     }
//
//     public void mouseClicked(int mouseX, int mouseY) {
//         this.mouseX = mouseX;
//         this.mouseY = mouseY;
//
//         boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
//                 mouseY >= this.top && mouseY <= this.bottom;
//         int listLength = this.getSize();
//         int scrollBarWidth = 6;
//         int scrollBarRight = this.left + this.listWidth;
//         int scrollBarLeft = scrollBarRight - scrollBarWidth;
//         int entryRight = scrollBarLeft - 1;
//         int viewHeight = this.bottom - this.top;
//         int border = 4;
//
//         if (this.initialMouseClickY == -1.0F) {
//             if (isHovering) {
//                 int mouseListY = mouseY - this.top - this.headerHeight + (int) this.scrollDistance - border;
//                 int slotIndex = mouseListY / this.slotHeight;
//
//                 if (mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength) {
//                     this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
//                     this.selectedIndex = slotIndex;
//                     this.lastClickTime = System.currentTimeMillis();
//                 } else if (mouseX <= entryRight && mouseListY < 0) {
//                     this.clickHeader(mouseX - this.left, mouseY - this.top + (int) this.scrollDistance - border);
//                 }
//
//                 if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
//                     this.scrollFactor = -1.0F;
//                     int scrollHeight = this.getContentHeight() - viewHeight - border;
//                     if (scrollHeight < 1) scrollHeight = 1;
//
//                     int var13 = (int) ((float) (viewHeight * viewHeight) / (float) this.getContentHeight());
//
//                     if (var13 < 32) var13 = 32;
//                     if (var13 > viewHeight - border * 2)
//                         var13 = viewHeight - border * 2;
//
//                     this.scrollFactor /= (float) (viewHeight - var13) / (float) scrollHeight;
//                 } else {
//                     this.scrollFactor = 1.0F;
//                 }
//
//                 this.initialMouseClickY = mouseY;
//             } else {
//                 this.initialMouseClickY = -2.0F;
//             }
//         } else if (this.initialMouseClickY >= 0.0F) {
//             this.scrollDistance -= ((float) mouseY - this.initialMouseClickY) * this.scrollFactor;
//             this.initialMouseClickY = (float) mouseY;
//         }
//
//         //else
//         // this.initialMouseClickY = -1.0F;
//         // }
//
//         this.applyScrollLimits();
//     }
//
//     public void mouseDragged(double mouseX, double mouseY, int button, double p_mouseDragged_6_, double p_mouseDragged_8_) {
//
//     }
//
//     public void mouseScrolled(double scroll) {
//         boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
//                 mouseY >= this.top && mouseY <= this.bottom;
//         if (!isHovering)
//             return;
//
//         if (Double.compare(scroll, 0) == 0) {
//             this.scrollDistance += (float) ((-1 * scroll / 120.0F) * this.slotHeight / 2);
//         }
//     }
//
//     public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//         this.mouseX = mouseX;
//         this.mouseY = mouseY;
//         this.drawBackground();
//
//         int listLength = this.getSize();
//         int scrollBarWidth = 6;
//         int scrollBarRight = this.left + this.listWidth;
//         int scrollBarLeft = scrollBarRight - scrollBarWidth;
//         int entryRight = scrollBarLeft - 1;
//         int viewHeight = this.bottom - this.top;
//         int border = 4;
//
//         Tessellator tess = Tessellator.getInstance();
//         BufferBuilder worldr = tess.getBuffer();
//
//         ScaledResolution res = new ScaledResolution(Minecraft.getInstance());
//         double scaleW = Minecraft.getInstance().mainWindow.getWidth() / res.getScaledWidth_double();
//         double scaleH = Minecraft.getInstance().mainWindow.getHeight() / res.getScaledHeight_double();
//         GL11.glEnable(GL11.GL_SCISSOR_TEST);
//         GL11.glScissor((int) (left * scaleW), (int) (Minecraft.getInstance().mainWindow.getHeight() - (bottom * scaleH)),
//                 (int) (listWidth * scaleW), (int) (viewHeight * scaleH));
//
//         if (Minecraft.getInstance().world != null) {
//             GuiUtils.drawGradientRect(0, this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
//         } else // Draw dark dirt background
//         {
//             GlStateManager.disableLighting();
//             GlStateManager.disableFog();
//             Minecraft.getInstance().getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
//             GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//             final float scale = 32.0F;
//             worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//             worldr.pos(this.left, this.bottom, 0.0D).tex(this.left / scale, (this.bottom + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//             worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / scale, (this.bottom + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//             worldr.pos(this.right, this.top, 0.0D).tex(this.right / scale, (this.top + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//             worldr.pos(this.left, this.top, 0.0D).tex(this.left / scale, (this.top + (int) this.scrollDistance) / scale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//             tess.draw();
//         }
//
//         int baseY = this.top + border - (int) this.scrollDistance;
//
//         if (this.hasHeader) {
//             this.drawHeader(entryRight, baseY, tess);
//         }
//
//         for (int slotIdx = 0; slotIdx < listLength; ++slotIdx) {
//             int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
//             int slotBuffer = this.slotHeight - border;
//
//             if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top) {
//                 if (this.isSelected(slotIdx)) {
//                     int min = this.left;
//                     int max = entryRight;
//                     GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//                     GlStateManager.disableTexture2D();
//                     worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//                     worldr.pos(min, slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                     worldr.pos(max, slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                     worldr.pos(max, slotTop - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                     worldr.pos(min, slotTop - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                     worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                     worldr.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                     worldr.pos(max - 1, slotTop - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                     worldr.pos(min + 1, slotTop - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                     tess.draw();
//                     GlStateManager.enableTexture2D();
//                 }
//
//                 this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
//             }
//         }
//
//         GlStateManager.disableDepthTest();
//
//         int extraHeight = (this.getContentHeight() + border) - viewHeight;
//         if (extraHeight > 0) {
//             int height = (viewHeight * viewHeight) / this.getContentHeight();
//
//             if (height < 32) height = 32;
//
//             if (height > viewHeight - border * 2)
//                 height = viewHeight - border * 2;
//
//             int barTop = (int) this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
//             if (barTop < this.top) {
//                 barTop = this.top;
//             }
//
//             GlStateManager.disableTexture2D();
//             worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//             worldr.pos(scrollBarLeft, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//             worldr.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//             worldr.pos(scrollBarRight, this.top, 0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//             worldr.pos(scrollBarLeft, this.top, 0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//             tess.draw();
//             worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//             worldr.pos(scrollBarLeft, barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//             worldr.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//             worldr.pos(scrollBarRight, barTop, 0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//             worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//             tess.draw();
//             worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//             worldr.pos(scrollBarLeft, barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//             worldr.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//             worldr.pos(scrollBarRight - 1, barTop, 0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//             worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//             tess.draw();
//         }
//
//         GlStateManager.enableTexture2D();
//         GlStateManager.shadeModel(GL11.GL_FLAT);
//         GlStateManager.enableAlphaTest();
//         GlStateManager.disableBlend();
//         GL11.glDisable(GL11.GL_SCISSOR_TEST);
//     }
//
// }