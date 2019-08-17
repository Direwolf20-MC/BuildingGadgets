package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

public class BlockMapCommand {

    public static LiteralArgumentBuilder<CommandSource> registerDelete() {
        return Commands.literal("DeleteBlockMaps")
                .requires(commandSource -> commandSource.hasPermissionLevel(4))
                .then(Commands.argument("targets", EntityArgument.player())
                        .executes(context -> execute(context, EntityArgument.getEntity(context, "targets"), true))
                );
    }


    public static LiteralArgumentBuilder<CommandSource> registerList() {
        return Commands.literal("FindBlockMaps")
                .requires(commandSource -> commandSource.hasPermissionLevel(4))
                .then(Commands.argument("targets", EntityArgument.player())
                        .executes(context -> execute(context, EntityArgument.getPlayer(context, "targets"), false))
                );
    }

    // I don't get the logic going on here but this is basically what it was originally :P
    private static int execute(CommandContext<CommandSource> ctx, Entity entity, boolean removeData) throws CommandSyntaxException {
        ServerPlayerEntity sender = ctx.getSource().asPlayer();

        if( !(entity instanceof PlayerEntity)) {
            sender.sendMessage(new StringTextComponent("Entity not valid"));
            return 0;
        }

        WorldSave worldSave = WorldSave.getWorldSave(sender.getEntityWorld());
        Map<String, CompoundNBT> tagMap = worldSave.getTagMap();
        Map<String, CompoundNBT> newMap = new HashMap<>(tagMap);

        int counter = 0;
        for (Map.Entry<String, CompoundNBT> entry : tagMap.entrySet()) {
            CompoundNBT tagCompound = entry.getValue();
            if (tagCompound.getString(NBTKeys.TEMPLATE_OWNER).equals(entity.getName().getString())) {
                //TODO Missing localisation
                sender.sendMessage(new StringTextComponent(TextFormatting.RED + "Deleted stored map for " + tagCompound.getString(NBTKeys.TEMPLATE_OWNER) + " with UUID:" + tagCompound.getString(NBTKeys.GADGET_UUID)));
                counter++;
                if (removeData) newMap.remove(entry.getKey());
            }
        }

        if (removeData && counter > 0) {
            worldSave.setTagMap(newMap);
            worldSave.markForSaving();
            if (entity.getName().equals(sender.getName())) {
                PacketHandler.sendTo(new PacketBlockMap(new CompoundNBT()), sender);
            }
        }

        //TODO Missing localisation
        sender.sendMessage(new StringTextComponent(TextFormatting.WHITE + "Deleted " + counter + " blockmaps in world data."));

        return 1;
    }
}
