package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.config.InGameConfig;
import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class CraftingConditionDestruction implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
        final boolean result;
        if (InGameConfig.enableDestructionGadget) {
            result = true;
        } else {
            result = false;
        }
        return () -> result;
    }

}
