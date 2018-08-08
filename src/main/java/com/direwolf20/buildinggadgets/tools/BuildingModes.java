package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.items.BuildingTool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BuildingModes {
	//private int brushRadius = 3;
	//private void changeBrushRadius(int newRadius){
	//	brushRadius = newRadius;
	//}
    private static boolean isReplaceable(World world, BlockPos pos, IBlockState setBlock) {
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos) || !setBlock.getBlock().canPlaceBlockAt(world, pos)) {
            return false;
        }
        return true;
    }

    public static ArrayList<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, ItemStack tool) {
        //BuildingTool.toolModes mode, IBlockState setBlock
        BuildingTool.toolModes mode = BuildingTool.getToolMode(tool);
        IBlockState setBlock = GadgetUtils.getToolBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        BlockPos pos = startBlock;
        int bound = (range - 1) / 2;
        EnumFacing playerFacing = player.getHorizontalFacing();
        int boundX, boundZ;
        if (playerFacing == EnumFacing.SOUTH || playerFacing == EnumFacing.NORTH) {
            boundX = bound;
            boundZ = 0;
        } else {
            boundX = 0;
            boundZ = bound;
        }
        //***************************************************
        //Build to me
        //***************************************************
        if (mode == BuildingTool.toolModes.BuildToMe) {
            if (sideHit == EnumFacing.SOUTH) {
                for (int i = startBlock.getZ() + 1; i <= playerPos.getZ() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.NORTH) {
                for (int i = startBlock.getZ() - 1; i >= playerPos.getZ() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int i = startBlock.getX() + 1; i <= playerPos.getX() - 1; i++) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int i = startBlock.getX() - 1; i >= playerPos.getX() + 1; i--) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.UP) {
                for (int i = startBlock.getY() + 1; i <= playerPos.getY() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int i = startBlock.getY() - 1; i >= playerPos.getY() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //VerticalWall
        //***************************************************
        else if (mode == BuildingTool.toolModes.VerticalWall) {
            if (sideHit == EnumFacing.UP) {
                for (int y = 1; y <= range; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = 1; y <= range; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            } else {
                for (int y = bound; y >= bound * -1; y--) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            }
        }
        //***************************************************
        //VerticalColumn
        //***************************************************
        else if (mode == BuildingTool.toolModes.VerticalColumn) {
            if (sideHit == EnumFacing.UP) {
                for (int y = 1; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() + y, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = 1; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else {
                for (int y = bound * -1; y <= bound; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorizontalColumn
        //***************************************************
        else if (mode == BuildingTool.toolModes.HorizontalColumn) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }
            if (sideHit == EnumFacing.NORTH) {
                for (int z = 1; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() + z);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() - z);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorizontalWall
        //***************************************************
        else if (mode == BuildingTool.toolModes.HorizontalWall) {
            /*if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }*/
            if (sideHit == EnumFacing.NORTH) {
                for (int z = 1; z <= range; z++) {
                    for (int x = bound * -1; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() - z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    for (int x = bound * -1; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else {
                for (int x = bound * -1; x <= bound; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        //***************************************************
        //Stairs
        //***************************************************
        else if (mode == BuildingTool.toolModes.Stairs) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }
            if (sideHit == EnumFacing.NORTH) {
                for (int z = 1; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() - z);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - 1);
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() + z);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + 1);
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() - x + 1, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY() - x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() + x - 1, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //Checkerboard
        //***************************************************
        else if (mode == BuildingTool.toolModes.Checkerboard) {
            range++;
            for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
                for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                    if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + 1, startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        //***************************************************
        //Brush
        //***************************************************
		else if (mode == BuildingTool.toolModes.Brush) {
            double radiusSq = range * range;
			for (int x = 0; x <= range; x++) {
			for (int y = 0; y <= range; y++) {
			for (int z = 0; z <= range; z++) {
			double dSq = x*x+y*y+z*z;
			if (dSq > radiusSq) {
				continue;
			}
			pos = new BlockPos(startBlock.getX()+x,startBlock.getY()+y,startBlock.getZ()+z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()-x,startBlock.getY()+y,startBlock.getZ()+z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()+x,startBlock.getY()-y,startBlock.getZ()+z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()+x,startBlock.getY()+y,startBlock.getZ()-z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()-x,startBlock.getY()-y,startBlock.getZ()+z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()+x,startBlock.getY()-y,startBlock.getZ()-z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()-x,startBlock.getY()+y,startBlock.getZ()-z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
			pos = new BlockPos(startBlock.getX()-x,startBlock.getY()-y,startBlock.getZ()-z);
			if (isReplaceable(world, pos, setBlock)) {
				coordinates.add(pos);
			}
		}//z
		}//y
		}//x
        }
        return coordinates;
    }

    public static ArrayList<BlockPos> sortByDistance(ArrayList<BlockPos> unSortedList, EntityPlayer player) {
        ArrayList<BlockPos> sortedList = new ArrayList<BlockPos>();
        Map<Double, BlockPos> rangeMap = new HashMap<Double, BlockPos>();
        Double distances[] = new Double[unSortedList.size()];
        Double distance;
        double x = player.posX;
        double y = player.posY + player.getEyeHeight();
        double z = player.posZ;
        int i = 0;
        for (BlockPos pos : unSortedList) {
            distance = pos.distanceSqToCenter(x, y, z);
            rangeMap.put(distance, pos);
            distances[i] = distance;
            i++;
        }
        Arrays.sort(distances);
        //ArrayUtils.reverse(distances);

        for (Double dist : distances) {
            //System.out.println(dist);
            sortedList.add(rangeMap.get(dist));
        }
        //System.out.println(unSortedList);
        //System.out.println(sortedList);
        return sortedList;
    }
}
