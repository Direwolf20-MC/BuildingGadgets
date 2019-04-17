package com.direwolf20.buildinggadgets.common.utils.lang;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum TooltipTranslation implements ITranslationProvider {
    CONSTRUCTIONBLOCKPOWDER_HELPTEXT("constructionblockpowder.helptext", 0),
    GADGET_BLOCK("gadget.block", 1),
    GADGET_DESTROYSHOWOVERLAY("gadget.destroyshowoverlay", 1),
    GADGET_DESTROYWARNING("gadget.destroywarning", 0),
    GADGET_ENERGY("gadget.energy", 2),
    GADGET_MODE("gadget.mode", 1),
    GADGET_RANGE("gadget.range", 1),
    GADGET_FUZZY("gadget.fuzzy", 1),
    GADGET_RAYTRACE_FLUID("gadget.raytrace_fluid", 1),
    GADGET_BUILDING_PLACE_ATOP("gadget.building.place_atop", 1),
    GADGET_CONNECTED("gadget.connected", 1),
    GADGET_CONNECTED_AREA("gadget.connected_area", 0),
    GAGDGET_CONNECTED_SURFACE("gadget.connected_surface", 0),
    GADGET_MIRROR("gadget.mirror", 0),
    GADGET_ANCHOR("gadget.anchor", 0),
    GADGET_UNDO("gadget.undo", 0),
    PASTECONTAINER_AMOUNT("pasteContainer.amount", 1),
    PASTECONTAINER_CREATIVE_AMOUNT("pasteContainer.creative.amountMsg", 0),
    TEMPLATE_NAME("template.name", 1);
    private static final String PREFIX = "tooltip.";
    private final String key;
    private final int argCount;

    TooltipTranslation(@Nonnull String key, @Nonnegative int argCount) {
        this.key = PREFIX + key;
        this.argCount = argCount;
    }

    @Override
    public boolean areValidArguments(Object... args) {
        return args.length == argCount;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
