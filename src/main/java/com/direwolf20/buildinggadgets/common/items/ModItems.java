package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    @GameRegistry.ObjectHolder("buildinggadgets:buildingtool")
    public static GadgetBuilding gadgetBuilding;

    @GameRegistry.ObjectHolder("buildinggadgets:exchangertool")
    public static GadgetExchanger gadgetExchanger;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpaste")
    public static ConstructionPaste constructionPaste;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpastecontainer")
    public static ConstructionPasteContainer constructionPasteContainer;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpastecontainert2")
    public static ConstructionPasteContainerT2 constructionPasteContainert2;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpastecontainert3")
    public static ConstructionPasteContainerT3 constructionPasteContainert3;

    @GameRegistry.ObjectHolder("buildinggadgets:copypastetool")
    public static GadgetCopyPaste gadgetCopyPaste;

    @GameRegistry.ObjectHolder("buildinggadgets:template")
    public static Template template;

    @GameRegistry.ObjectHolder("buildinggadgets:destructiontool")
    public static GadgetDestruction gadgetDestruction;
}
