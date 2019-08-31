package com.direwolf20.buildinggadgets.common.config.crafting;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class CraftingConditionDestruction implements IConditionSerializer {

    @Override
    public void write(JsonObject jsonObject, ICondition iCondition) {

    }

    @Override
    public ICondition read(JsonObject jsonObject) {
        return new ICondition() {
            @Override
            public ResourceLocation getID() {
                return null;
            }

            @Override
            public boolean test() {
                return false;
            }
        };
    }

    @Override
    public ResourceLocation getID() {
        return null;
    }
}
