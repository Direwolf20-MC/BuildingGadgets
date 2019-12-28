/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */

package com.direwolf20.buildinggadgets.client.screens;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.screens.components.GuiIconActionable;
import com.direwolf20.buildinggadgets.client.screens.components.GuiSliderInt;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.client.ModSounds;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.*;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AbstractRadialMenu extends GuiScreen {

    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;
    int modeIndex = 0;

    private ModeIcon[] icons;
    private ItemStack gadget;

    // This is the simplest way to cleanly assign locations to buttons.
    List<ZeroButton> topSlots = new ArrayList<>();
    List<ZeroButton> leftSlots = new ArrayList<>();
    List<ZeroButton> rightSlots = new ArrayList<>();

    AbstractRadialMenu(ModeIcon[] icons, ItemStack gadget) {
        this.icons = icons;
        this.segments = icons.length;
        this.gadget = gadget;
    }

    @Override
    public void initGui() {
        super.initGui();

        topSlots.clear();
        leftSlots.clear();
        rightSlots.clear();

        this.rightSlots.add(new ZeroButton("Place on fluids", "raytrace_fluid", send -> {
            if (send) PacketHandler.INSTANCE.sendToServer(new PacketToggleRayTraceFluid());
            return AbstractGadget.shouldRayTraceFluid(gadget);
        }));

        this.leftSlots.add(new ZeroButton("Anchor", "anchor", send -> {
            if (send) PacketHandler.INSTANCE.sendToServer(new PacketAnchor());
            if (gadget.getItem() instanceof CopyGadget)
                return CopyGadget.getAnchor(gadget) != null;

            return !GadgetUtils.getAnchor(gadget).isEmpty();
        }));

        if( !(gadget.getItem() instanceof ExchangingGadget) ) {
            this.leftSlots.add(new ZeroButton("Undo", "undo", false, send -> {
                if (send) PacketHandler.INSTANCE.sendToServer(new PacketUndo());
                return false;
            }));
        }

        if( !(gadget.getItem() instanceof DestructionGadget) ) {
            this.leftSlots.add(new ZeroButton("Rotate", "rotate", false, send -> {
                if (send) PacketHandler.INSTANCE.sendToServer(new PacketRotateMirror(PacketRotateMirror.Operation.ROTATE));
                return false;
            }));

            this.leftSlots.add(new ZeroButton("Mirror", "mirror", false, send -> {
                if (send) PacketHandler.INSTANCE.sendToServer(new PacketRotateMirror(PacketRotateMirror.Operation.MIRROR));
                return false;
            }));

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

        // Move all to the top slot for destruction until we get modes
        if( gadget.getItem() instanceof DestructionGadget ) {
            this.topSlots.addAll(this.rightSlots);
            this.topSlots.addAll(this.leftSlots);
            this.rightSlots.clear();
            this.leftSlots.clear();
        }
    }

    public void sortButtons() {
        List<Pair<ScreenPosition, List<ZeroButton>>> btns = new ArrayList<Pair<ScreenPosition, List<ZeroButton>>>(){{
            add(new ImmutablePair<>(ScreenPosition.LEFT, leftSlots));
            add(new ImmutablePair<>(ScreenPosition.RIGHT, rightSlots));
            add(new ImmutablePair<>(ScreenPosition.TOP, topSlots));
        }};

        int baseX = width / 2;
        int baseY = height / 2;
        btns.forEach(e -> {
            for (int i = 0; i < e.getValue().size(); i++) {
                ScreenPosition pos = e.getKey();
                ZeroButton btn = e.getValue().get(i);

                int difference = pos == ScreenPosition.LEFT || pos == ScreenPosition.TOP ? 100 : -100 + btn.height;
                if( ScreenPosition.isX(pos) ) {
                    btn.x = baseX - difference;
                    btn.y = (baseY - ((e.getValue().size() * (btn.height + 3)) / 2) + 3) + (i * (btn.height + 3));
                } else {
                    btn.x = (baseX - ((e.getValue().size() * (btn.height + 3)) / 2) + 3) + (i * (btn.height + 3));
                    btn.y = baseY - difference - (gadget.getItem() instanceof DestructionGadget ? -40 : 0);
                }
            }
        });

        btns.forEach(e -> e.getValue().forEach(this::addButton));
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        float fract = Math.min(5F, timeIn + partialTicks) / 5F;
        int x = width / 2;
        int y = height / 2;

        int radiusMin = 26;
        int radiusMax = 60;
        double dist = new Vec3d(x, y, 0).distanceTo(new Vec3d(mx, my, 0));
        boolean inRange = false;

        if (segments != 0) {
            inRange = dist > radiusMin && dist < radiusMax;
            for (GuiButton button : buttonList) {
                if (button instanceof ZeroButton)
                    ((ZeroButton) button).setFaded(inRange);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((1 - fract) * x, (1 - fract) * y, 0);
        GlStateManager.scale(fract, fract, fract);
        super.drawScreen(mx, my, partialTicks);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<int[]> stringPositions = new ArrayList<>();

        slotSelected = -1;
        boolean shouldCenter = (segments + 2) % 4 == 0;
        int indexBottom = segments / 4;
        int indexTop = indexBottom + segments / 2;
        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = isCursorInSlice(mouseAngle(x, y, mx, my), totalDeg, degPer, inRange);
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, radiusMax));

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            float gs = 0.25F;
            if (seg % 2 == 0)
                gs += 0.1F;

            float r = gs;
            float g = gs + (seg == modeIndex ? 1F : 0.0F);
            float b = gs;

            if (mouseInSector) {
                slotSelected = seg;
                r = g = b = 1F;
            }

            GlStateManager.color(r, g, b, 0.4F);
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
            String name = icons[i].text.getUnformattedComponentText();

            int xsp = pos[0] - 4;
            int ysp = pos[1];
            int width = fontRenderer.getStringWidth(name);

            if (xsp < x) xsp -= width - 8;
            if (ysp < y) ysp -= 9;

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            if (pos[2] > 0)
                fontRenderer.drawStringWithShadow(name, xsp + (pos[3] > 0 ? width / 2f - 4 : 0), ysp, color.getRGB());

            int xdp = (int) ((pos[0] - x) * 0.7 + x);
            int ydp = (int) ((pos[1] - y) * 0.7 + y);
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            mc.renderEngine.bindTexture(icons[i].icon);
            drawModalRectWithCustomSizedTexture(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        float s = 2.25F * fract;
        GlStateManager.scale(s, s, s);
        GlStateManager.translate(x / s - 8f, y / s - 8, 0);
        mc.getRenderItem().renderItemAndEffectIntoGUI(this.gadget, 0, 0);
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
            modeIndex = slotSelected;
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
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public ItemStack getGadget() {
        return gadget;
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(Vector2f.dot(baseVec, mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }

    public enum ScreenPosition {
        RIGHT, LEFT, TOP;

        public static boolean isX(ScreenPosition pos) {
            return pos == LEFT || pos == RIGHT;
        }
    }

    static class ZeroButton extends GuiIconActionable {
        ZeroButton(String message, String icon, boolean isSelectable, Predicate<Boolean> action) {
            super(0, 0, icon, message, isSelectable, action);
        }

        ZeroButton(String message, String icon, Predicate<Boolean> action) {
            this(message, icon, true, action);
        }
    }

    static class ModeIcon {
        private ResourceLocation icon;
        private TextComponentTranslation text;

        ModeIcon(String icon, String text) {
            this.icon = new ResourceLocation(BuildingGadgets.MODID, icon);
            this.text = new TextComponentTranslation("buildinggadgets.modes." + text);
        }
    }
}
