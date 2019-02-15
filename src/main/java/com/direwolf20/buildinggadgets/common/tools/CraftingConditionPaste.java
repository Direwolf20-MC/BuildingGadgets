package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionSerializer;

import java.util.function.BooleanSupplier;

public class CraftingConditionPaste implements IConditionSerializer {
    @Override
    public BooleanSupplier parse(JsonObject json) {
        return Config.GENERAL.enablePaste::get;
    }
}
