package com.direwolf20.buildinggadgets.common.construction;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wrapper for interacting with the gadgets internal nbt data store of the UndoBits
 * UUID's and for cleaning up some logic
 */
public class UndoStack {
    private ItemStack stack;

    public UndoStack(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * Extract the UndoBit UUID's from the gadget as a list of pairs
     */
    public List<Pair<UUID, DimensionType>> getBitKeys() {
        List<Pair<UUID, DimensionType>> bits = new ArrayList<>();
        ListNBT list = this.getUndoList();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT item = list.getCompound(i);

            if (!item.hasUniqueId("key") || !item.contains("dimension")) {
                continue;
            }

            bits.add(Pair.of(
                    item.getUniqueId("key"),
                    DimensionType.byName(new ResourceLocation(item.getString("dimension")))
            ));
        }

        return bits;
    }

    /**
     * @return the list of undo uuids & dimensions
     */
    private ListNBT getUndoList() {
        return this.stack.getOrCreateTag().getList("undo-list", Constants.NBT.TAG_COMPOUND);
    }

    /**
     * Only return UUID's based on a dimension
     */
    public List<UUID> getBitsByDimension(DimensionType type) {
        List<Pair<UUID, DimensionType>> bits = this.getBitKeys();

        return bits.stream()
                .filter(e -> e.getValue().equals(type))
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }

    public Optional<UUID> pollBit(DimensionType type) {
        CompoundNBT compound = stack.getOrCreateTag();
        List<UUID> bits = this.getBitsByDimension(type);

        if (bits.size() == 0) {
            return Optional.empty();
        }

        // Get the last uuid of this dimension
        UUID uuid = bits.get(bits.size() - 1);

        ListNBT undoList = this.getUndoList();
        int index = this.getIndexOfUUID(undoList, uuid);

        if (index == -1) {
            return Optional.empty();
        }

        undoList.remove(index);
        compound.put("undo-list", undoList);

        return Optional.of(uuid);
    }

    /**
     * Undo's are stored on the world with a UUID to identify them. This pushes one of those UUID's
     * to the gadget so we know what data we can undo.
     *
     * The UndoStack handles the removal :D
     */
    public boolean pushUndo(ItemStack stack, UUID uuid, DimensionType type) {
        ResourceLocation dimensionName = type.getRegistryName();
        if (dimensionName == null) {
            BuildingGadgets.LOGGER.fatal("Current dimension does not have registry name!");
            return false;
        }

        List<UUID> bitsByDimension = this.getBitsByDimension(type);

        CompoundNBT compound = stack.getOrCreateTag();
        ListNBT list = compound.getList("undo-list", Constants.NBT.TAG_COMPOUND);

        CompoundNBT data = new CompoundNBT();
        data.putUniqueId("key", uuid);
        data.putString("dimension", dimensionName.toString());

        // Pop off the last item from the dimension store if over the max undos
        if (bitsByDimension.size() > Config.CommonConfig.gadgetsMaxUndos.get()) {
            int idOfLastUuid = this.getIndexOfUUID(list, uuid);

            if (idOfLastUuid != -1) {
                list.remove(idOfLastUuid);
            }
        }

        list.add(data);
        compound.put("undo-list", list);
        return true;
    }

    public int getIndexOfUUID(ListNBT undoList, UUID uuid) {
        int index = -1;

        for (int i = 0; i < undoList.size(); i++) {
            CompoundNBT item = undoList.getCompound(i);

            if (!item.getUniqueId("key").equals(uuid)) {
                continue;
            }

            index = i;
            break;
        }

        return index;
    }
}
