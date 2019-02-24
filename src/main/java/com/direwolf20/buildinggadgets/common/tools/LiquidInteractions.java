package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.utils.helpers.VectorHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.Arrays;

public class LiquidInteractions {
    public enum GadgetGeneric {
        IGNORE(0) {
            public RayTraceResult getLookingAt(EntityPlayer player) {
                return VectorHelper.getLookingAt(player);
            }
        },
        REPLACE_TOP(1) {
            public RayTraceResult getLookingAt(EntityPlayer player) {
                return VectorHelper.getLookingAt(player, RayTraceFluidMode.ALWAYS);
            }
        };
        public abstract RayTraceResult getLookingAt(EntityPlayer player);
        public byte id;
        GadgetGeneric(int id) {
            this.id = (byte) id;
        }
        public static GadgetGeneric fromNBT(byte ID) {
            return Arrays.stream(values()).filter(val -> val.id == ID).findFirst().get();
        }
    }
    public enum GadgetBuilding {

        IGNORE(0) {
            public RayTraceResult getLookingAt(EntityPlayer player) {
                return VectorHelper.getLookingAt(player);
            }
            public BlockPos getLookingPos(EntityPlayer player) {
                return getLookingAt(player).getBlockPos();
            }
        },

        REPLACE_TOP(1) {
            public RayTraceResult getLookingAt(EntityPlayer player) {
                return VectorHelper.getLookingAt(player, RayTraceFluidMode.ALWAYS);
            }
            public BlockPos getLookingPos(EntityPlayer player) {
                BlockPos pos = getLookingAt(player).getBlockPos();
                IBlockState state = player.world.getBlockState(pos);
                Block b = state.getBlock();
                if (b instanceof IFluidBlock) {
                    pos.add(0, -1, 0);
                }
                return pos;
            }
        },

        PLACE_ABOVE(2) {
            public RayTraceResult getLookingAt(EntityPlayer player) {
                return VectorHelper.getLookingAt(player, RayTraceFluidMode.ALWAYS);
            }
            public BlockPos getLookingPos(EntityPlayer player) {
                return getLookingAt(player).getBlockPos();
            }
        };

        public byte id;

        GadgetBuilding(int id) {
            this.id = (byte)id;
        }

        public abstract BlockPos getLookingPos(EntityPlayer player);
        public abstract RayTraceResult getLookingAt(EntityPlayer player);
        public static GadgetBuilding fromNBT(byte ID) {
            return Arrays.stream(values()).filter(val -> val.id == ID).findFirst().get();
        }
    }
}
