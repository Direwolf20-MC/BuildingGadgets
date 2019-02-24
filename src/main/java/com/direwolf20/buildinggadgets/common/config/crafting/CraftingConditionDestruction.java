package com.direwolf20.buildinggadgets.common.config.crafting;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionSerializer;

import java.util.function.BooleanSupplier;

public class CraftingConditionDestruction implements IConditionSerializer {
    @Override
    public BooleanSupplier parse(JsonObject json) {
        return Config.GENERAL.enableDestructionGadget::get;
    }
}
