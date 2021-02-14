package com.direwolf20.buildinggadgets.api.modes;

import net.minecraft.util.ResourceLocation;

/**
 * This interface is the core component to how a Gadget can interface with it's it's modes
 * each {@link IMode} will provide an entry to support the gadgets interfaces.
 */
public interface IModeUiEntry {
    /**
     * Forms a link between a IMode and a IModeEntry
     */
    ResourceLocation modeIdentifier();

    /**
     * Used for any user facing gui / text. Do not use I18n. This will be called from both sides
     */
    String translatedName();

    /** Icon to use for when the mode is active in the radial menu */
    ResourceLocation activatedIcon();

    /** Icon to use for when the mode is not active in the radial menu */
    ResourceLocation deactivatedIcon();
}
