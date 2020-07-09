package com.direwolf20.buildinggadgets.common.data;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import static com.direwolf20.buildinggadgets.common.helpers.LangHelper.*;

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
        add(key("message", "no-block-selected"), "No valid block found to select");
        add(key("message", "block-selected"), "%s Selected");
        add(key("message", "range-updated"), "Range: %s");
        add(key("message", "mode-updated"), "Mode: %s");

        // Key Bindings
        add(key("key", "category"), modName);
        add(key("key", "range"), "Range Cycle");
        add(key("key", "mode"), "Mode Cycle");
        add(key("key", "settings_menu"), "Gadget Settings");

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

    }

    private void addMode(String mode, String text) {
        add(key("mode", mode), text);
    }
}
