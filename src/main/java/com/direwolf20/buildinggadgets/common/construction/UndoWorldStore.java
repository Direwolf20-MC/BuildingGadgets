package com.direwolf20.buildinggadgets.common.construction;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Stores the undo data in a hash map stored as a list with the key as part of the compound and then put
 * back to the list using said key. It's not ideal but I want / need a way to remove and fetch from the stack
 * dynamically without having to loop through it :D Quick access basically.
 *
 * Most of the real leg work is handled by each gadgets individual implementations of undo to make them all
 * unique in their actions.
 */
public class UndoWorldStore extends WorldSavedData {
    private static final String NAME = BuildingGadgets.MOD_ID + "_undo_world_store";
    private final HashMap<UUID, List<UndoBit>> undoStack = new HashMap<>();

    public UndoWorldStore() {
        super(NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        undoStack.clear();

        ListNBT list = nbt.getList("undo-data", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.getCompound(i));
            List<UndoBit> undoBits = new ArrayList<>();
            ListNBT bits = list.getCompound(i).getList("bits", Constants.NBT.TAG_COMPOUND);

            for (int x = 0; x < bits.size(); x++) {
                undoBits.add(UndoBit.deserialize(bits.getCompound(x)));
            }

            undoStack.put(
                    list.getCompound(i).getUniqueId("key"),
                    undoBits
            );
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT nbt = new ListNBT();

        for (Map.Entry<UUID, List<UndoBit>> undo : undoStack.entrySet()) {
            CompoundNBT comp = new CompoundNBT();
            comp.putUniqueId("key", undo.getKey());

            ListNBT list = new ListNBT();
            for (UndoBit undoBit : undo.getValue()) {
                list.add(undoBit.serialize());
            }

            comp.put("bits", list);
            nbt.add(comp);
        }

        compound.put("undo-data", nbt);
        return compound;
    }

    public static UndoWorldStore get(World world) {
        UndoWorldStore storage = ((ServerWorld) world).getSavedData().get(UndoWorldStore::new, NAME);

        if (storage == null) {
            storage = new UndoWorldStore();
            storage.markDirty();
            ((ServerWorld) world).getSavedData().set(storage);
        }

        return storage;
    }

    public HashMap<UUID, List<UndoBit>> getUndoStack() {
        return undoStack;
    }
}
