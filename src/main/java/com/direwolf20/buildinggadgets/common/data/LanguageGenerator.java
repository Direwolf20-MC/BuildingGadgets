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
        add(key("no-block-selected"), "No valid block found to select");

        // Key Bindings
        add("category", modName);
        add("key.range", "Range");
        add("key.settings_menu", "Gadget Settings");

        // Items
        addItem(ModItems.BUILDING_GADGET, "Building Gadget");
        addItem(ModItems.EXCHANGING_GADGET, "Exchanging Gadget");
        addItem(ModItems.DESTRUCTION_GADGET, "Destruction Gadget");
        addItem(ModItems.COPIER_GADGET, "Copier Gadget");
    }
}
