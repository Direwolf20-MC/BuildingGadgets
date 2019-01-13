package com.direwolf20.buildinggadgets.common.schematics;

import net.minecraft.nbt.NBTTagCompound;

public enum SchematicLoader {
    MC_STRUCTURE {
        @Override
        public ISchematic loadFromNBT(NBTTagCompound compound) {
            return null;
        }

        @Override
        public NBTTagCompound writeToNBT(ISchematic schematic) {
            return null;
        }
    },
    MC_ALPHA {
        @Override
        public ISchematic loadFromNBT(NBTTagCompound compound) {
            return null;
        }

        @Override
        public NBTTagCompound writeToNBT(ISchematic schematic) {
            return null;
        }
    },
    //really old format - not truly needed, but for the sake of completeness might as well put this here
    MC_CLASSIC {
        @Override
        public ISchematic loadFromNBT(NBTTagCompound compound) {
            return null;
        }

        @Override
        public NBTTagCompound writeToNBT(ISchematic schematic) {
            return null;
        }
    };

    public abstract ISchematic loadFromNBT(NBTTagCompound compound);

    public abstract NBTTagCompound writeToNBT(ISchematic schematic);
}
