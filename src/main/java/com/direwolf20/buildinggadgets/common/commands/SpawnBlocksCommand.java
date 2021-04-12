package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.BuildingGadgets;
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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Developer command to spawn all blocks registered to the Forge Registry
 */
public class SpawnBlocksCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        BuildingGadgets.LOGGER.debug("Spawn Block Command Registered");
        return Commands.literal("spawnblocks")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(SpawnBlocksCommand::spawnBlocks);
    }

    static int spawnBlocks(CommandContext<CommandSource> context) {
        System.out.println("Working");
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrException();
            System.out.println(player.blockPosition());

            BlockPos pos = player.blockPosition();
            int x = pos.getX(), z = pos.getZ();
            for (Map.Entry<RegistryKey<Block>, Block> block : ForgeRegistries.BLOCKS.getEntries()) {
                BlockPos placePos = new BlockPos(x, pos.getY(), z);
                player.level.setBlockAndUpdate(placePos.below(), Blocks.DIRT.defaultBlockState());

                BlockState state = block.getValue().getStateForPlacement(
                        new BlockItemUseContext(new ItemUseContext(player, player.getUsedItemHand(), (BlockRayTraceResult) LookingHelper.getResult(player, false)))
                );

                if (state == null) {
                    player.displayClientMessage(new StringTextComponent("Failed to place " + block.getKey().toString()), false);
                    continue;
                }

                player.level.setBlockAndUpdate(placePos, state);
                player.level.setBlockAndUpdate(placePos.east(), Blocks.BARRIER.defaultBlockState());
                player.level.setBlockAndUpdate(placePos.north(), Blocks.BARRIER.defaultBlockState());
                player.level.setBlockAndUpdate(placePos.south(), Blocks.BARRIER.defaultBlockState());
                player.level.setBlockAndUpdate(placePos.south().east(), Blocks.BARRIER.defaultBlockState());
                player.level.setBlockAndUpdate(placePos.south().west(), Blocks.BARRIER.defaultBlockState());
                player.level.setBlockAndUpdate(placePos.north().west(), Blocks.BARRIER.defaultBlockState());
                player.level.setBlockAndUpdate(placePos.north().east(), Blocks.BARRIER.defaultBlockState());

                x += 2;
                if (x > pos.getX() + 40) {
                    x = pos.getX();
                    z += 2;
                }
            }

            player.displayClientMessage(new StringTextComponent("Blocks Spawned"), false);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
}
