//package com.direwolf20.buildinggadgets.common.commands;
//
//import com.direwolf20.buildinggadgets.common.BuildingGadgets;
//import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
//import com.direwolf20.buildinggadgets.common.network.PacketHandler;
//import com.direwolf20.buildinggadgets.common.tools.WorldSave;
//import com.google.common.collect.Lists;
//import net.minecraft.command.CommandBase;
//import net.minecraft.command.CommandException;
//import net.minecraft.command.ICommandSender;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.text.TextComponentString;
//import net.minecraft.util.text.TextFormatting;
////
//import javax.annotation.Nullable;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public abstract class CommandAlterBlockMaps extends CommandBase {
//    private final String name;
//    private final List<String> aliases;
//    private final boolean removeData;
//
//    public CommandAlterBlockMaps(String name, boolean removeData) {
//        this.name = name;
//        this.removeData = removeData;
//        aliases = Lists.newArrayList(BuildingGadgets.MODID, name, name.toLowerCase());
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public String getUsage(ICommandSender sender) {
//        return name + " <player>";
//    }
//
//    @Override
//    public List<String> getAliases() {
//        return aliases;
//    }
//
//    abstract protected String getActionFeedback(NBTTagCompound tagCompound);
//
//    abstract protected String getCompletionFeedback(int counter);
//
//    @Override
//    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//        if (args.length > 0) {
//            if (!(sender.canUseCommand(4, this.getName()))) {
//                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only OPS can use this command with an argument."));
//                return;
//            }
//        }
//        WorldSave worldSave = WorldSave.getWorldSave(sender.getEntityWorld());
//        Map<String, NBTTagCompound> tagMap = worldSave.getTagMap();
//        Map<String, NBTTagCompound> newMap = new HashMap<String, NBTTagCompound>(tagMap);
//        String searchName = (args.length == 0) ? sender.getName() : args[0];
//        int counter = 0;
//        for (Map.Entry<String, NBTTagCompound> entry : tagMap.entrySet()) {
//            NBTTagCompound tagCompound = entry.getValue();
//            if (tagCompound.getString("owner").equals(searchName) || searchName.equals("*")) {
//                sender.sendMessage(new TextComponentString(getActionFeedback(tagCompound)));
//                counter++;
//                if (removeData) newMap.remove(entry.getKey());
//            }
//        }
//        if (removeData && counter > 0) {
//            worldSave.setTagMap(newMap);
//            worldSave.markForSaving();
//            if (searchName.equals(sender.getName())) {
//                PacketHandler.INSTANCE.sendTo(new PacketBlockMap(new NBTTagCompound()), (EntityPlayerMP) sender);
//                //System.out.println("Sending BlockMap Packet");
//            }
//        }
//        sender.sendMessage(new TextComponentString(getCompletionFeedback(counter)));
//    }
//
//    @Override
//    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
//        return true;
//    }
//
//    @Override
//    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
//        return Collections.emptyList();
//    }
//}
