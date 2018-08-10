package com.direwolf20.buildinggadgets.tools;

import java.util.function.BooleanSupplier;

import com.direwolf20.buildinggadgets.Config;
import com.google.gson.JsonObject;


import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class CraftingCondition implements IConditionFactory {
	@Override
	public BooleanSupplier parse( JsonContext jsonContext, JsonObject jsonObject )
	{
		final boolean result;

        if (Config.enablePaste) {
            result = true;
        } else {
            result = false;
        }

		return () -> result;

	}
}