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
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(SpawnBlocksCommand::spawnBlocks);
    }

    static int spawnBlocks(CommandContext<CommandSource> context) {
        System.out.println("Working");
        try {
            ServerPlayerEntity player = context.getSource().asPlayer();
            System.out.println(player.getPosition());

            BlockPos pos = player.getPosition();
            int x = pos.getX(), z = pos.getZ();
            for (Map.Entry<RegistryKey<Block>, Block> block : ForgeRegistries.BLOCKS.getEntries()) {
                BlockPos placePos = new BlockPos(x, pos.getY(), z);
                player.world.setBlockState(placePos.down(), Blocks.DIRT.getDefaultState());

                BlockState state = block.getValue().getStateForPlacement(
                        new BlockItemUseContext(new ItemUseContext(player, player.getActiveHand(), (BlockRayTraceResult) LookingHelper.getResult(player, false)))
                );

                if (state == null) {
                    player.sendStatusMessage(new StringTextComponent("Failed to place " + block.getKey().toString()), false);
                    continue;
                }

                player.world.setBlockState(placePos, state);
                player.world.setBlockState(placePos.east(), Blocks.BARRIER.getDefaultState());
                player.world.setBlockState(placePos.north(), Blocks.BARRIER.getDefaultState());
                player.world.setBlockState(placePos.south(), Blocks.BARRIER.getDefaultState());
                player.world.setBlockState(placePos.south().east(), Blocks.BARRIER.getDefaultState());
                player.world.setBlockState(placePos.south().west(), Blocks.BARRIER.getDefaultState());
                player.world.setBlockState(placePos.north().west(), Blocks.BARRIER.getDefaultState());
                player.world.setBlockState(placePos.north().east(), Blocks.BARRIER.getDefaultState());

                x += 2;
                if (x > pos.getX() + 40) {
                    x = pos.getX();
                    z += 2;
                }
            }

            player.sendStatusMessage(new StringTextComponent("Blocks Spawned"), false);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
}
