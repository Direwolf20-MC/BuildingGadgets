package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.helpers.LookingHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Developer command to spawn all blocks registered to the Forge Registry
 */
public class ClearUndoStoreCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("clearUndoStore")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(ClearUndoStoreCommand::clearStore);
    }

    static int clearStore(CommandContext<CommandSource> context) {
        UndoWorldStore store = UndoWorldStore.get(context.getSource().getWorld());
        store.getUndoStack().clear();
        store.markDirty();
        return 1;
    }
}
