package com.direwolf20.buildinggadgets.commands;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.tools.BlockMapWorldSave;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FindBlockMapsCommand extends CommandBase {
    public FindBlockMapsCommand() {
        aliases = Lists.newArrayList(BuildingGadgets.MODID, "FindBlockMaps", "findblockmaps");
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getName() {
        return "FindBlockMaps";
    }

    @Override
    @Nonnull
    public String getUsage(ICommandSender sender) {
        return "FindBlockMaps <player>";
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            if (!(sender.canUseCommand(4, this.getName()))) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only OPS can use this command with an argument."));
                return;
            }
        }
        BlockMapWorldSave worldSave = BlockMapWorldSave.get(sender.getEntityWorld());
        Map<String, NBTTagCompound> tagMap = worldSave.getTagMap();
        String searchName = (args.length == 0) ? sender.getName() : args[0];
        int counter = 0;
        for (Map.Entry<String, NBTTagCompound> entry : tagMap.entrySet()) {
            NBTTagCompound tagCompound = entry.getValue();
            if (tagCompound.getString("owner").equals(searchName) || searchName.equals("*")) {
                sender.sendMessage(new TextComponentString(TextFormatting.WHITE + tagCompound.getString("owner") + ":" + tagCompound.getString("UUID")));
                counter++;
            }
        }
        sender.sendMessage(new TextComponentString(TextFormatting.WHITE + "Found " + counter + " blockmaps in world data."));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }
}
