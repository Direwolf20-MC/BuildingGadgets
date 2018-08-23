package com.direwolf20.buildinggadgets.commands;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.tools.BlockMapWorldSave;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteBlockMapsCommand extends CommandBase {
    public DeleteBlockMapsCommand() {
        aliases = Lists.newArrayList(BuildingGadgets.MODID, "DeleteBlockMaps", "deleteblockmaps");
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getName() {
        return "DeleteBlockMaps";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "DeleteBlockMaps <player>";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length > 0) {
            if (!(sender.canUseCommand(4, this.getName()))) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only OPS can use this command with an argument."));
                return;
            }
        }
        BlockMapWorldSave worldSave = BlockMapWorldSave.get(sender.getEntityWorld());
        Map<String, NBTTagCompound> tagMap = worldSave.getTagMap();
        Map<String, NBTTagCompound> newMap = new HashMap<String, NBTTagCompound>(tagMap);
        String searchName = (args.length == 0) ? sender.getName() : args[0];
        int counter = 0;
        for (Map.Entry<String, NBTTagCompound> entry : tagMap.entrySet()) {
            NBTTagCompound tagCompound = entry.getValue();
            if (tagCompound.getString("owner").equals(searchName) || searchName.equals("*")) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Deleted stored map for " + tagCompound.getString("owner") + " with UUID:" + tagCompound.getString("UUID")));
                counter++;
                newMap.remove(entry.getKey());
            }
        }
        if (counter > 0) {
            worldSave.setTagMap(newMap);
            worldSave.markForSaving();
            if (searchName == sender.getName()) {
                PacketHandler.INSTANCE.sendTo(new PacketBlockMap(new NBTTagCompound()), (EntityPlayerMP) sender);
                System.out.println("Sending BlockMap Packet");
            }
        }
        sender.sendMessage(new TextComponentString(TextFormatting.WHITE + "Deleted " + counter + " blockmaps in world data."));
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }
}
