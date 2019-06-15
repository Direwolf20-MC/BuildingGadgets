package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class GuiScreenTextFields extends Screen {
    private List<GuiTextFieldBase> fields = new ArrayList<>();

    public GuiScreenTextFields() {
        super(new StringTextComponent("page title... duh! lmao")); //TODO figure that one out
    }

    protected GuiTextFieldBase addField(GuiTextFieldBase field) {
        fields.add(field);
        children.add(field);
        return field;
    }

    protected void forEachField(Consumer<GuiTextFieldBase> action) {
        fields.forEach(field -> action.accept(field));
    }

    protected Iterator<GuiTextFieldBase> getFieldIterator() {
        return fields.iterator();
    }

    @Override
    public void tick() {
        super.tick();
        fields.forEach(field -> field.tick());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        fields.forEach(field -> field.render(mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}  