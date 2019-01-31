//package com.direwolf20.buildinggadgets.common.commands;
//
//import com.direwolf20.buildinggadgets.common.network.PacketHandler;
//import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
//import com.direwolf20.buildinggadgets.common.tools.WorldSave;
//import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import net.minecraft.command.CommandException;
//import net.minecraft.command.CommandSource;
//import net.minecraft.command.Commands;
//import net.minecraft.command.arguments.EntityArgument;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.util.text.TextComponentString;
//import net.minecraft.util.text.TextFormatting;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class DeleteBlockMapsCommand {
//    public static LiteralArgumentBuilder<CommandSource> register() {
//        return Commands.literal("DeleteBlockMaps")
//                .then(Commands.argument("player", EntityArgument.singlePlayer()))
//                .executes(context -> execute(context, EntityArgument.getOnePlayer(context, "player")));
//    }
//
//    public static int execute(CommandContext<CommandSource> ctx, EntityPlayerMP playerMP) throws CommandSyntaxException {
//        EntityPlayerMP sender = ctx.getSource().asPlayer();
////        if (ctx.getArgument("player", EntityArgument.singlePlayer().getClass()).toString() > 0) {
////            if (!(sender.canUseCommand(4, this.getName()))) {
////                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only OPS can use this command with an argument."));
////                return;
////            }
////        }
//
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
//    public DeleteBlockMapsCommand() {
//        super("DeleteBlockMaps", true);
//    }
//
//    @Override
//    protected String getActionFeedback(NBTTagCompound tagCompound) {
//        return TextFormatting.RED + "Deleted stored map for " + tagCompound.getString("owner") + " with UUID:" + tagCompound.getString("UUID");
//    }
//
//    @Override
//    protected String getCompletionFeedback(int counter) {
//        return TextFormatting.WHITE + "Deleted " + counter + " blockmaps in world data.";
//    }
//}
