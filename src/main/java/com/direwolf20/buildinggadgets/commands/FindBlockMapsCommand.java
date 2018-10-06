package com.direwolf20.buildinggadgets.commands;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class FindBlockMapsCommand extends CommandAlterBlockMaps {
    public FindBlockMapsCommand() {
        super("FindBlockMaps", false);
    }

    @Override
    protected String getActionFeedback(NBTTagCompound tagCompound)
    {
        return TextFormatting.WHITE + tagCompound.getString("owner") + ":" + tagCompound.getString("UUID");
    }

    @Override
    protected String getCompletionFeedback(int counter)
    {
        return TextFormatting.WHITE + "Found " + counter + " blockmaps in world data.";
    }
}
