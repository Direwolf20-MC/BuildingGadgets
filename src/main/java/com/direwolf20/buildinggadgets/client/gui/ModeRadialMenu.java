/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketChangeRange;
import com.direwolf20.buildinggadgets.common.network.packets.PacketToggleConnectedArea;
import com.direwolf20.buildinggadgets.common.network.packets.PacketToggleFuzzy;
import com.direwolf20.buildinggadgets.common.network.packets.PacketToggleMode;
import com.direwolf20.buildinggadgets.common.registry.objects.BGSound;
import com.direwolf20.buildinggadgets.common.utils.Reference;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModeRadialMenu extends GuiScreen {

    private static final ResourceLocation[] signsBuilding = new ResourceLocation[]{
        new ResourceLocation(Reference.MODID, "textures/ui/build_to_me.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/vertical_column.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/horizontal_column.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/vertical_wall.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/horizontal_wall.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/stairs.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/grid.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/surface.png")
    };
    private static final ResourceLocation[] signsExchanger = new ResourceLocation[]{
        new ResourceLocation(Reference.MODID, "textures/ui/surface.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/vertical_column.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/horizontal_column.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/grid.png")
    };
    private static final ResourceLocation[] signsCopyPaste = new ResourceLocation[]{
        new ResourceLocation(Reference.MODID, "textures/ui/copy.png"),
        new ResourceLocation(Reference.MODID, "textures/ui/paste.png")
    };
    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;

    public ModeRadialMenu(ItemStack stack) {
        mc = Minecraft.getInstance();
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
    public void initGui() {
        ItemStack tool = getGadget();
        boolean destruction = false;
        if (tool.getItem() instanceof GadgetDestruction) {
            destruction = true;
            addButton(new GuiButtonActionCallback(I18n.format("tooltip.gadget.destroy.overlay"), send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketChangeRange());

                return GadgetDestruction.getOverlay(getGadget());
            }));
        }
        if (!(tool.getItem() instanceof GadgetCopyPaste)) {
            if (!destruction || Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get()) {
                addButton(new GuiButtonActionCallback(I18n.format("tooltip.gadget.fuzzy"), send -> {
                    if (send)
                        PacketHandler.sendToServer(new PacketToggleFuzzy());

                    return GadgetGeneric.getFuzzy(getGadget());
                }));
            }
            addButton(new GuiButtonActionCallback(I18n.format("message.gadget.connected" + (destruction ? "area" : "surface")), send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketToggleConnectedArea());

                return GadgetGeneric.getConnectedArea(getGadget());
            }));
        }
        updateButtons(tool);
    }

    private void updateButtons(ItemStack tool) {
        int x = 0;
        for (int i = 0; i < buttons.size(); i++) {
            GuiButtonSound button = (GuiButtonSound) buttons.get(i);
            SoundEvent sound = BGSound.BEEP.getSound();
            button.setSounds(sound, sound, 1F, 0.6F);
            if (!button.visible) continue;
            int len = mc.fontRenderer.getStringWidth(button.displayString) + 6;
            x += len + 10;
            button.width = len;
            button.height = mc.fontRenderer.FONT_HEIGHT + 3;
            button.y = height / 2 - (tool.getItem() instanceof GadgetDestruction ? button.height + 4 : 110);
        }
        x = width / 2 - (x - 10) / 2;
        for (GuiButton button : buttons) {
            if (!button.visible) continue;
            button.x = x;
            x += button.width + 10;
        }
    }

    private ItemStack getGadget() {
        return GadgetGeneric.getGadget(Minecraft.getInstance().player);
    }

    @Override
    public void render(int mx, int my, float partialTicks) {
        float stime = 5F;
        float fract = Math.min(stime, timeIn + partialTicks) / stime;
        int x = width / 2;
        int y = height / 2;
        GlStateManager.pushMatrix();
        GlStateManager.translatef((1 - fract) * x, (1 - fract) * y, 0);
        GlStateManager.scalef(fract, fract, fract);
        super.render(mx, my, partialTicks);
        GlStateManager.popMatrix();
        if (segments == 0)
            return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        
        int maxRadius = 80;

        float angle = mouseAngle(x, y, mx, my);

        int highlight = 5;

        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<int[]> stringPositions = new ArrayList<>();

        ItemStack tool = getGadget();
        if (tool.isEmpty())
            return;

        slotSelected = -1;
        float offset = 8.5F;
        double dist = new Vec3d(x, y, 0).distanceTo(new Vec3d(mx, my, 0));
        boolean inRange = dist > 35 && dist < 81;
        ResourceLocation[] signs;
        int modeIndex;
        if (tool.getItem() instanceof GadgetBuilding) {
            modeIndex = GadgetBuilding.getToolMode(tool).ordinal();
            signs = signsBuilding;
        } else if (tool.getItem() instanceof GadgetExchanger) {
            modeIndex = GadgetExchanger.getToolMode(tool).ordinal();
            signs = signsExchanger;
        } else {
            modeIndex = GadgetCopyPaste.getToolMode(tool).ordinal();
            signs = signsCopyPaste;
        }

        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = inRange && angle > totalDeg && angle < totalDeg + degPer;
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, maxRadius));

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

            GlStateManager.color4f(r, g, b, a);

            for (float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);
                double xp = x + Math.cos(rad) * radius;
                double yp = y + Math.sin(rad) * radius;
                if ((int) i == (int) (degPer / 2))
                    stringPositions.add(new int[]{(int) xp, (int) yp, mouseInSector ? 'n' : 'r'});

                GL11.glVertex2d(x + Math.cos(rad) * radius / 2.3F, y + Math.sin(rad) * radius / 2.3F);
                GL11.glVertex2d(xp, yp);
            }
            totalDeg += degPer;

            GL11.glEnd();
            GlStateManager.color4f(1, 1, 1, 1);

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
                name = GadgetBuilding.ToolMode.values()[i].toString();
            else if (tool.getItem() instanceof GadgetExchanger)
                name = GadgetExchanger.ToolMode.values()[i].toString();
            else
                name = GadgetCopyPaste.ToolMode.values()[i].toString();

            name = "\u00a7" + c + name;

            int xsp = xp - 4;
            int ysp = yp;
            int width = fontRenderer.getStringWidth(name);

            if (xsp < x)
                xsp -= width - 8;
            if (ysp < y)
                ysp -= 9;

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            fontRenderer.drawStringWithShadow(name, xsp, ysp, color.getRGB());

            double mod = 0.7;
            int xdp = (int) ((xp - x) * mod + x);
            int ydp = (int) ((yp - y) * mod + y);

            GlStateManager.color4f(color.getRed() / 255, color.getGreen() / 255, color.getBlue() / 255F, 1);
            mc.getTextureManager().bindTexture(signs[i]);
            drawModalRectWithCustomSizedTexture(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        float s = 3F * fract;
        GlStateManager.scalef(s, s, s);
        GlStateManager.translatef(x / s - offset, y / s - 8, 0);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(tool, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
    }

    private void changeMode() {
        if (slotSelected >= 0) {
            PacketHandler.sendToServer(new PacketToggleMode(slotSelected));
            BGSound.BEEP.playSound();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        changeMode();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
//    TODO 1.13 only works for bound keys; not bound mouse buttons
        if (!InputMappings.isKeyDown(KeyBindings.modeSwitch.getKey().getKeyCode())) {
            close();
            changeMode();
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKey(), k.isKeyDown());

        timeIn++;
        ItemStack tool = getGadget();
        boolean builder = tool.getItem() instanceof GadgetBuilding;
        if (!builder && !(tool.getItem() instanceof GadgetExchanger))
            return;

        boolean curent;
        boolean changed = false;
        for (int i = 0; i < 2; i++) {
            GuiButton button = buttons.get(i);
            if (builder)
                curent = GadgetBuilding.getToolMode(tool) == GadgetBuilding.ToolMode.Surface;
            else
                curent = i == 0 || GadgetExchanger.getToolMode(tool) == GadgetExchanger.ToolMode.Surface;

            if (button.visible != curent) {
                button.visible = curent;
                changed = true;
            }
        }
        if (changed)
            updateButtons(tool);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(baseVec.dot(mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }
}
