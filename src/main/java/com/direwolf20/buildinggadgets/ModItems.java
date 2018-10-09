package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.items.*;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    @GameRegistry.ObjectHolder("buildinggadgets:buildingtool")
    public static BuildingTool buildingTool;

    @GameRegistry.ObjectHolder("buildinggadgets:exchangertool")
    public static ExchangerTool exchangerTool;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpaste")
    public static ConstructionPaste constructionPaste;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpastecontainer")
    public static ConstructionPasteContainer constructionPasteContainer;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpastecontainert2")
    public static ConstructionPasteContainerT2 constructionPasteContainert2;

    @GameRegistry.ObjectHolder("buildinggadgets:constructionpastecontainert3")
    public static ConstructionPasteContainerT3 constructionPasteContainert3;

    @GameRegistry.ObjectHolder("buildinggadgets:copypastetool")
    public static CopyPasteTool copyPasteTool;

    @GameRegistry.ObjectHolder("buildinggadgets:template")
    public static Template template;

    @GameRegistry.ObjectHolder("buildinggadgets:destructiontool")
    public static DestructionTool destructionTool;
}
