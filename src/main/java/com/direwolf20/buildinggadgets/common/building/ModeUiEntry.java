package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.modes.IModeUiEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * This is a shortcut class to simplify some of the logic behind each mod.
 */
public class ModeUiEntry implements IModeUiEntry {
    ResourceLocation modeId;
    String translatedName;
    ResourceLocation activated;
    ResourceLocation deactivated;

    public ModeUiEntry(String name, ResourceLocation modeId) {
        this.modeId = modeId;
        this.translatedName = new TranslationTextComponent("buildinggadgets.modes." + name).getString();
        this.activated = new ResourceLocation(BuildingGadgetsAPI.MODID, "textures/gui/mode/" + name + ".png");
        this.deactivated = new ResourceLocation(BuildingGadgetsAPI.MODID, "textures/gui/mode/" + name + ".png");
    }

    @Override
    public ResourceLocation modeIdentifier() {
        return this.modeId;
    }

    @Override
    public String translatedName() {
        return this.translatedName;
    }

    @Override
    public ResourceLocation activatedIcon() {
        return this.activated;
    }

    @Override
    public ResourceLocation deactivatedIcon() {
        return this.deactivated;
    }
}
