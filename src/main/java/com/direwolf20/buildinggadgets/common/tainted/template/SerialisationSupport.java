package com.direwolf20.buildinggadgets.common.tainted.template;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class SerialisationSupport {
    private SerialisationSupport() {
    }

    private static final ITileDataSerializer SERIALIZER = SerialisationSupport.createDummySerializer();

    public static ITileDataSerializer dummyDataSerializer() {
        return SERIALIZER;
    }

    public static final class DummyTileDataSerializer implements ITileDataSerializer {

        private DummyTileDataSerializer() {
            super();
        }

        @Override
        public CompoundTag serialize(ITileEntityData data, boolean persisted) {
            return new CompoundTag();
        }

        @Override
        public ITileEntityData deserialize(CompoundTag tagCompound, boolean persisted) {
            return TileSupport.dummyTileEntityData();
        }
    }

    public static NBTTileEntityDataSerializer createNbtSerializer() {
        return new NBTTileEntityDataSerializer();
    }

    public static DummyTileDataSerializer createDummySerializer() {
        return new DummyTileDataSerializer();
    }

    public static final class NBTTileEntityDataSerializer implements ITileDataSerializer {
        private NBTTileEntityDataSerializer() {
        }


        @Override
        public CompoundTag serialize(ITileEntityData data, boolean persisted) {
            Preconditions.checkArgument(data instanceof NBTTileEntityData);
            NBTTileEntityData nbtData = (NBTTileEntityData) data;
            CompoundTag res = new CompoundTag();
            res.put(NBTKeys.KEY_DATA, nbtData.getNBT());
            if (nbtData.getRequiredMaterials() != null)
                res.put(NBTKeys.KEY_MATERIALS, nbtData.getRequiredMaterials().serialize(persisted));
            return res;
        }

        @Override
        public ITileEntityData deserialize(CompoundTag tagCompound, boolean persisted) {
            CompoundTag data = tagCompound.getCompound(NBTKeys.KEY_DATA);
            MaterialList materialList = null;
            if (tagCompound.contains(NBTKeys.KEY_MATERIALS, Tag.TAG_COMPOUND))
                materialList = MaterialList.deserialize(tagCompound.getCompound(NBTKeys.KEY_MATERIALS), persisted);
            return new NBTTileEntityData(data, materialList);
        }
    }

    private static final IUniqueObjectSerializer SERIALIZER_UNIQUE = new UniqueItem.Serializer();

    public static IUniqueObjectSerializer uniqueItemSerializer() {
        return SERIALIZER_UNIQUE;
    }
}
