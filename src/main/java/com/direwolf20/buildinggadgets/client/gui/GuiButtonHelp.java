package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class GuiButtonHelp extends GuiButtonSelect {

    public GuiButtonHelp(int buttonId, int x, int y) {
        super(buttonId, x, y, 12, 12, "?");
    }

    public String getHoverText() {
        return IHoverHelpText.get("button." + (selected ? "help.exit" : "help.enter"));
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;

        GlStateManager.color(1, 1, 1, 1);
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        float x = this.x + 5.5F;
        int y = this.y + 6;
        double radius = 6;
        int red, green, blue;
        if (selected) {
            red = blue = 0;
            green = 200;
        } else {
            red = green = blue = 120;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0).color(red, green, blue, 255).endVertex();
        double s = 30;
        for(int k = 0; k <= s; k++)  {
            double angle = (Math.PI * 2 * k / s) + Math.toRadians(180);
            buffer.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).color(red, green, blue, 255).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        mouseDragged(mc, mouseX, mouseY);
        int colorText = -1;
        if (packedFGColour != 0)
            colorText = packedFGColour;
        else if (!enabled)
            colorText = 10526880;
        else if (hovered)
            colorText = 16777120;

        mc.fontRenderer.drawString(displayString, this.x + width / 2 - mc.fontRenderer.getStringWidth(displayString) / 2, this.y + (height - 8) / 2, colorText);
    }

}