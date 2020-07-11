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
        add(translationKey("message", "no-block-selected"), "No valid block found to select");
        add(translationKey("message", "block-selected"), "%s Selected");
        add(translationKey("message", "range-updated"), "Range: %s");
        add(translationKey("message", "mode-updated"), "Mode: %s");
        add(translationKey("message", "undo-save-failure"), "Undo data lost! Failure to store undo...");
        add(translationKey("message", "undo-fetch-failure"), "Undo data not found! The world save may have been cleared...");
        add(translationKey("message", "undo-store-empty"), "No undo's left!");
        add(translationKey("message", "build-successful"), "Blocks Built!");
        add(translationKey("message", "block-selection-banned"), "%s is a banned block, you can't select this one.");

        add(translationKey("tooltip", "energy"), "Energy: %s FE");
        add(translationKey("tooltip", "selected-block"), "Block: %s");
        add(translationKey("tooltip", "mode"), "Mode: %s");

        // Key Bindings
        add(translationKey("key", "category"), modName);
        add(translationKey("key", "range"), "Range Cycle");
        add(translationKey("key", "mode"), "Mode Cycle");
        add(translationKey("key", "settings_menu"), "Gadget Settings");

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
}
