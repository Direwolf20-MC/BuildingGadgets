package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
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
}
