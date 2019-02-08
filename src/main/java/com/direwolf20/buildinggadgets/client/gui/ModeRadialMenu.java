/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketToggleMode;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModeRadialMenu extends GuiScreen {

    private static final ResourceLocation[] signs = new ResourceLocation[]{
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/buildtome.png"),
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/verticalcolumn.png"),
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/horizontalcolumn.png"),
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/verticalwall.png"),
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/horizontalwall.png"),
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/stairs.png"),
            new ResourceLocation(BuildingGadgets.MODID,"textures/ui/checker.png")
    };

    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;

    public ModeRadialMenu(ItemStack stack) {
        mc = Minecraft.getMinecraft();
        if (stack.getItem() instanceof GadgetGeneric)
            setSocketable(stack);
    }

    public void setSocketable(ItemStack stack) {
        if (stack.getItem() instanceof GadgetBuilding)
            segments = GadgetBuilding.ToolMode.values().length;
        else if (stack.getItem() instanceof GadgetExchanger)
            segments = GadgetExchanger.ToolMode.values().length;
        else if (stack.getItem() instanceof GadgetCopyPaste)
            segments = GadgetCopyPaste.ToolMode.values().length;
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        if (segments == 0) return;
        super.drawScreen(mx, my, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        int x = width / 2;
        int y = height / 2;
        int maxRadius = 80;

        boolean mouseIn = true;
        float angle = mouseAngle(x, y, mx, my);

        int highlight = 5;

        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<int[]> stringPositions = new ArrayList();

        ItemStack tool = GadgetGeneric.getGadget(Minecraft.getMinecraft().player);
        if (tool.isEmpty())
            return;

        slotSelected = -1;

        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = mouseIn && angle > totalDeg && angle < totalDeg + degPer;
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, maxRadius));

            GL11.glBegin(GL11.GL_TRIANGLE_FAN);

            float gs = 0.25F;
            if (seg % 2 == 0)
                gs += 0.1F;
            float r = gs;
            float g = gs;
            float b = gs;
            float a = 0.4F;
            if (mouseInSector) {
                slotSelected = seg;
                Color color = new Color(255, 255, 255);
                r = color.getRed() / 255F;
                g = color.getGreen() / 255F;
                b = color.getBlue() / 255F;
            }

            GlStateManager.color(r, g, b, a);
            GL11.glVertex2i(x, y);

            for (float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);
                double xp = x + Math.cos(rad) * radius;
                double yp = y + Math.sin(rad) * radius;
                if ((int) i == (int) (degPer / 2))
                    stringPositions.add(new int[]{(int) xp, (int) yp, mouseInSector ? 'n' : 'r'});

                GL11.glVertex2d(xp, yp);
            }
            totalDeg += degPer;

            GL11.glVertex2i(x, y);
            GL11.glEnd();

            if (mouseInSector)
                radius -= highlight;
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();

        for (int i = 0; i < stringPositions.size(); i++) {
            int[] pos = stringPositions.get(i);
            int xp = pos[0];
            int yp = pos[1];
            char c = (char) pos[2];

            String name = "";
            if (tool.getItem() instanceof GadgetBuilding)
                name = "\u00a7" + c + GadgetBuilding.ToolMode.values()[i].name();
            else if (tool.getItem() instanceof GadgetExchanger)
                name = "\u00a7" + c + GadgetExchanger.ToolMode.values()[i].name();
            else if (tool.getItem() instanceof GadgetCopyPaste)
                name = "\u00a7" + c + GadgetCopyPaste.ToolMode.values()[i].name();

            int xsp = xp - 4;
            int ysp = yp;
            int width = fontRenderer.getStringWidth(name);

            double mod = 0.6;
            int xdp = (int) ((xp - x) * mod + x);
            int ydp = (int) ((yp - y) * mod + y);

            if (xsp < x)
                xsp -= width - 8;
            if (ysp < y)
                ysp -= 9;

            fontRenderer.drawStringWithShadow(name, xsp, ysp, 0xFFFFFF);

            mod = 0.8;
            xdp = (int) ((xp - x) * mod + x);
            ydp = (int) ((yp - y) * mod + y);

            mc.renderEngine.bindTexture(signs[i]);
            drawModalRectWithCustomSizedTexture(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);

        }

        float stime = 5F;
        float fract = Math.min(stime, timeIn + partialTicks) / stime;
        float s = 3F * fract;
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        if (!tool.isEmpty()) {
            GlStateManager.scale(s, s, s);
            GlStateManager.translate(x / s - 8, y / s - 8, 0);
            mc.getRenderItem().renderItemAndEffectIntoGUI(tool, 0, 0);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (!GameSettings.isKeyDown(KeyBindings.modeSwitch)) {
            mc.displayGuiScreen(null);
            if (slotSelected != -1) {
                PacketHandler.INSTANCE.sendToServer(new PacketToggleMode(slotSelected));
            }
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKeyCode(), GameSettings.isKeyDown(k));

        timeIn++;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(Vector2f.dot(baseVec, mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }
}
