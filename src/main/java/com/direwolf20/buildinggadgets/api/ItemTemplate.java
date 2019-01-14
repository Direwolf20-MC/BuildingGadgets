package com.direwolf20.buildinggadgets.api;

import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class ItemTemplate extends Template {
    private static final String KEY_COPY_COUNT = "copycounter";
    private static final String KEY_ID = "UUID";
    private int copyCounter;

    public ItemTemplate() {
        super();
        copyCounter = 0;
    }

    public void readItemShareNBT(NBTTagCompound stackCompound) {
        copyCounter = stackCompound.getInteger(KEY_COPY_COUNT);
        UUID id = stackCompound.getUniqueId(KEY_ID);
        if (id == null) {
            String rep = stackCompound.getString(KEY_ID);
            if (!rep.isEmpty()) {
                id = UUID.fromString(rep); //older versions stored it as a string
            } else {
                id = UUID.randomUUID();
            }
        }
        setId(id); //this has to be the correct id
    }

    public void writeItemShareNBT(NBTTagCompound stackCompound) {
        stackCompound.setInteger(KEY_COPY_COUNT, copyCounter);
        if (getID() != null) {
            stackCompound.setUniqueId(KEY_ID, getID());
        }
    }
}
