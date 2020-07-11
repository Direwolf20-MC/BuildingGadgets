package com.direwolf20.buildinggadgets.common.data;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import static com.direwolf20.buildinggadgets.common.helpers.MessageHelper.*;

public class LanguageGenerator extends LanguageProvider {
    public LanguageGenerator(DataGenerator gen) {
        super(gen, BuildingGadgets.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        final String modName = "Building Gadgets";

        // Mod name / Generic
        add("name", modName);
        add("itemGroup.buildinggadgets", modName);

        addKeyed("message", "no-block-selected", "No valid block found to select");
        addKeyed("message", "block-selected", "%s Selected");
        addKeyed("message", "range-updated", "Range: %s");
        addKeyed("message", "mode-updated", "Mode: %s");
        addKeyed("message", "undo-save-failure", "Undo data lost! Failure to store undo...");
        addKeyed("message", "undo-fetch-failure", "Undo data not found! The world save may have been cleared...");
        addKeyed("message", "undo-store-empty", "No undo's left!");
        addKeyed("message", "build-successful", "Blocks Built!");
        addKeyed("message", "block-selection-banned", "%s is a banned block, you can't select this one.");
        addKeyed("message", "blocks-undo", "%s blocks undone");
        addKeyed("message", "no-blocks-placed", "No blocks were able to be placed :(");

        addKeyed("tooltip", "energy", "Energy: %s FE");
        addKeyed("tooltip", "selected-block", "Block: %s");
        addKeyed("tooltip", "mode", "Mode: %s");

        // Key Bindings
        addKeyed("key", "category", modName);
        addKeyed("key", "range", "Range Cycle");
        addKeyed("key", "mode", "Mode Cycle");
        addKeyed("key", "settings_menu", "Gadget Settings");

        // Items
        addItem(ModItems.BUILDING_GADGET, "Building Gadget");
        addItem(ModItems.EXCHANGING_GADGET, "Exchanging Gadget");
        addItem(ModItems.DESTRUCTION_GADGET, "Destruction Gadget");
        addItem(ModItems.COPIER_GADGET, "Copier Gadget");

        addMode("build_to_me", "Build to me");
        addMode("grid", "Grid");
        addMode("horizontal_column", "Horizontal Column");
        addMode("horizontal_wall", "Horizontal Wall");
        addMode("vertical_column", "Vertical Column");
        addMode("vertical_wall", "Vertical Wall");
        addMode("stairs", "Stairs");
        addMode("surface", "Surface");
        addMode("custom_area", "Custom Area");
        addMode("cut", "Cut");
        addMode("copy", "Copy");
        addMode("paste", "Paste");

    }

    private void addMode(String mode, String text) {
        add(translationKey("mode", mode), text);
    }

    private void addKeyed(String group, String key, String text) {
        add(translationKey(group, key), text);
    }
}
