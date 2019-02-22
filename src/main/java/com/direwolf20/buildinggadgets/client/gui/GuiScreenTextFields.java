package com.direwolf20.buildinggadgets.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiScreen;

public class GuiScreenTextFields extends GuiScreen {
    private List<GuiTextFieldBase> fields = new ArrayList<>();

    protected GuiTextFieldBase addField(GuiTextFieldBase field) {
        fields.add(field);
        children.add(field);
        return field;
    }

    protected void forEachField(Consumer<GuiTextFieldBase> action) {
        fields.forEach(field -> action.accept(field));
    }

    @Override
    public void tick() {
        super.tick();
        fields.forEach(field -> field.tick());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        fields.forEach(field -> field.drawTextField(mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}  