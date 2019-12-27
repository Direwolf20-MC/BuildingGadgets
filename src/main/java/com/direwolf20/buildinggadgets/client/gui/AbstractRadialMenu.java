/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.gui.components.GuiIconActionable;
import com.direwolf20.buildinggadgets.client.gui.components.GuiSliderInt;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.ModSounds;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.building.BuildingModes;
import com.direwolf20.buildinggadgets.common.gadgets.building.ExchangingModes;
import com.direwolf20.buildinggadgets.common.network.*;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AbstractRadialMenu extends GuiScreen {

    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;
    private List<GuiButton> buttons = new ArrayList<>();
    private ResourceLocation[] icons;
    private ItemStack gadget;

    AbstractRadialMenu(ResourceLocation[] icons, ItemStack gadget) {
        this.icons = icons;
        this.segments = icons.length;
        this.gadget = gadget;
    }

    @Override
    public void initGui() {
        super.initGui();

        GuiSliderInt sliderRange = new GuiSliderInt(width / 2 - 82 / 2, height / 2 + 72, 82, 14, "Range ", "", 1, SyncedConfig.maxRange,
            GadgetUtils.getToolRange(this.gadget), false, true, Color.DARK_GRAY, slider -> {
                if (slider.getValueInt() != GadgetUtils.getToolRange(this.gadget))
                    PacketHandler.INSTANCE.sendToServer(new PacketChangeRange(slider.getValueInt()));
            }, (slider, amount) -> {
                int value = slider.getValueInt();
                int valueNew = MathHelper.clamp(value + amount, 1, SyncedConfig.maxRange);
                slider.setValue(valueNew);
                slider.updateSlider();
            });
        sliderRange.precision = 1;
        sliderRange.getComponents().forEach(this::addButton);
    }

    public List<GuiButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<GuiButton> buttons) {
        this.buttons = buttons;
    }

    public ItemStack getCenterGadget() {
        return ItemStack.EMPTY;
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {

        float stime = 5F;
        float fract = Math.min(stime, timeIn + partialTicks) / stime;
        int x = width / 2;
        int y = height / 2;

        int radiusMin = 26;
        int radiusMax = 60;
        double dist = new Vec3d(x, y, 0).distanceTo(new Vec3d(mx, my, 0));
        boolean inRange = false;

        if (segments != 0) {
            inRange = dist > radiusMin && dist < radiusMax;
//            for (GuiButton button : buttonList) {
//                if (button instanceof PositionedIconActionable)
//                    ((PositionedIconActionable) button).setFaded(inRange);
//            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((1 - fract) * x, (1 - fract) * y, 0);
        GlStateManager.scale(fract, fract, fract);
        super.drawScreen(mx, my, partialTicks);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        float angle = mouseAngle(x, y, mx, my);

        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<int[]> stringPositions = new ArrayList();

        slotSelected = -1;

        int modeIndex = 0;

        boolean shouldCenter = (segments + 2) % 4 == 0;
        int indexBottom = segments / 4;
        int indexTop = indexBottom + segments / 2;
        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = isCursorInSlice(angle, totalDeg, degPer, inRange);
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, radiusMax));

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

            float gs = 0.25F;
            if (seg % 2 == 0)
                gs += 0.1F;
            float r = gs;
            float g = gs + (seg == modeIndex ? 1F : 0.0F);
            float b = gs;
            float a = 0.4F;
            if (mouseInSector) {
                slotSelected = seg;
                r = g = b = 1F;
            }

            GlStateManager.color(r, g, b, a);

            for (float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);
                double xp = x + Math.cos(rad) * radius;
                double yp = y + Math.sin(rad) * radius;
                if ((int) i == (int) (degPer / 2))
                    stringPositions.add(new int[]{(int) xp, (int) yp, mouseInSector ? 1 : 0, shouldCenter && (seg == indexBottom || seg == indexTop) ? 1 : 0});

                GL11.glVertex2d(x + Math.cos(rad) * radius / 2.3F, y + Math.sin(rad) * radius / 2.3F);
                GL11.glVertex2d(xp, yp);
            }
            totalDeg += degPer;

            GL11.glEnd();
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();

        for (int i = 0; i < stringPositions.size(); i++) {
            int[] pos = stringPositions.get(i);
            int xp = pos[0];
            int yp = pos[1];

            String name = "TODO: GET NAME";

            int xsp = xp - 4;
            int ysp = yp;
            int width = fontRenderer.getStringWidth(name);

            double mod;
            int xdp, ydp;

            if (xsp < x)
                xsp -= width - 8;
            if (ysp < y)
                ysp -= 9;

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            if (pos[2] > 0)
                fontRenderer.drawStringWithShadow(name, xsp + (pos[3] > 0 ? width / 2f - 4 : 0), ysp, color.getRGB());

            mod = 0.7;
            xdp = (int) ((xp - x) * mod + x);
            ydp = (int) ((yp - y) * mod + y);
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            mc.renderEngine.bindTexture(icons[i]);
            drawModalRectWithCustomSizedTexture(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);

        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        float s = 2.25F * fract;
        GlStateManager.scale(s, s, s);
        GlStateManager.translate(x / s - 8f, y / s - 8, 0);
        mc.getRenderItem().renderItemAndEffectIntoGUI(getCenterGadget(), 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
    }

    private boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    private void changeMode() {
        if (slotSelected >= 0) {
            PacketHandler.INSTANCE.sendToServer(new PacketToggleMode(slotSelected));
            ModSounds.BEEP.playSound();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        changeMode();
    }

    @Override
    public void updateScreen() {
        if (!GameSettings.isKeyDown(KeyBindings.menuSettings)) {
            mc.displayGuiScreen(null);
            changeMode();
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKeyCode(), GameSettings.isKeyDown(k));

        timeIn++;
//        ItemStack tool = getAsStack();
//        boolean builder = tool.getItem() instanceof BuildingGadget;
//        if (!builder && !(tool.getItem() instanceof ExchangingGadget))
//            return;

//        boolean current;
//        boolean changed = false;
//        for (int i = 0; i < conditionalButtons.size(); i++) {
//            GuiButton button = conditionalButtons.get(i);
//            if (builder)
//                current = BuildingGadget.getToolMode(tool) == BuildingModes.SURFACE;
//            else
//                current = i == 0 || ExchangingGadget.getToolMode(tool) == ExchangingModes.SURFACE;
//
//            if (button.visible != current) {
//                button.visible = current;
//                changed = true;
//            }
//        }
//        if (changed)
//            updateButtons(tool);
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

    public enum ScreenPosition {
        RIGHT, LEFT, BOTTOM, TOP
    }

    private static class PositionedIconActionable extends GuiIconActionable {
        public ScreenPosition position;

        PositionedIconActionable(String message, String icon, ScreenPosition position, boolean isSelectable, Predicate<Boolean> action) {
            super(0, 0, icon, message, isSelectable, action);

            this.position = position;
        }

        PositionedIconActionable(String message, String icon, ScreenPosition position, Predicate<Boolean> action) {
            this(message, icon, position, true, action);
        }
    }

    protected static String formatName(String name) {
        return Arrays.stream(name.split("_")).map(e -> (e.toCharArray()[0] + e.toLowerCase().substring(1)) + " ").collect(Collectors.joining());
    }
}
