package com.direwolf20.buildinggadgets.common.template;

import com.direwolf20.buildinggadgets.common.building.tilesupport.IAdditionalBlockData;
import com.direwolf20.buildinggadgets.common.building.tilesupport.IAdditionalBlockDataSerializer;
import com.direwolf20.buildinggadgets.common.building.tilesupport.NBTAdditionalBlockData;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;

public final class SerialisationSupport {
    private SerialisationSupport() {}

    @ObjectHolder(Reference.TileDataSerializerReference.DUMMY_SERIALIZER)
    private static IAdditionalBlockDataSerializer DUMMY_TILE_DATA_SERIALIZER = new DummyAdditionalBlockDataSerializer()
            .setRegistryName(Reference.TileDataSerializerReference.DUMMY_SERIALIZER_RL);

    public static IAdditionalBlockDataSerializer dummyDataSerializer() {
        return DUMMY_TILE_DATA_SERIALIZER;
    }

    private static final class DummyAdditionalBlockDataSerializer extends ForgeRegistryEntry<IAdditionalBlockDataSerializer> implements IAdditionalBlockDataSerializer {

        private DummyAdditionalBlockDataSerializer() {
            super();
        }

        @Override
        public CompoundNBT serialize(IAdditionalBlockData data, boolean persisted) {
            return new CompoundNBT();
        }

        @Override
        public IAdditionalBlockData deserialize(CompoundNBT tagCompound, boolean persisted) {
            return TileSupport.dummyTileEntityData();
        }
    }

    @ObjectHolder(Reference.TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER)
    private static IAdditionalBlockDataSerializer NBT_TILE_DATA_SERIALIZER = new NBTAdditionalBlockEntityDataSerializer()
            .setRegistryName(Reference.TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL);

    public static IAdditionalBlockDataSerializer nbtTileDataSerializer() {
        return NBT_TILE_DATA_SERIALIZER;
    }

    private static final class NBTAdditionalBlockEntityDataSerializer extends ForgeRegistryEntry<IAdditionalBlockDataSerializer> implements IAdditionalBlockDataSerializer {
        private NBTAdditionalBlockEntityDataSerializer() {}

        @Override
        public CompoundNBT serialize(IAdditionalBlockData data, boolean persisted) {
            Preconditions.checkArgument(data instanceof NBTAdditionalBlockData);
            NBTAdditionalBlockData nbtData = (NBTAdditionalBlockData) data;
            CompoundNBT res = new CompoundNBT();
            res.put(NBTKeys.KEY_DATA, nbtData.getNBT());
            if (nbtData.getRequiredMaterials() != null)
                res.put(NBTKeys.KEY_MATERIALS, nbtData.getRequiredMaterials().serialize(persisted));
            return res;
        }

        @Override
        public IAdditionalBlockData deserialize(CompoundNBT tagCompound, boolean persisted) {
            CompoundNBT data = tagCompound.getCompound(NBTKeys.KEY_DATA);
            MaterialList materialList = null;
            if (tagCompound.contains(NBTKeys.KEY_MATERIALS, NBT.TAG_COMPOUND))
                materialList = MaterialList.deserialize(tagCompound.getCompound(NBTKeys.KEY_MATERIALS), persisted);
            return new NBTAdditionalBlockData(data, materialList);
        }
    }

    @ObjectHolder(Reference.UniqueObjectSerializerReference.SIMPLE_UNIQUE_ITEM_ID)
    private static IUniqueObjectSerializer UNIQUE_ITEM_SERIALIZER = new UniqueItem.Serializer()
            .setRegistryName(Reference.UniqueObjectSerializerReference.SIMPLE_UNIQUE_ITEM_ID_RL);

    public static IUniqueObjectSerializer uniqueItemSerializer() {
        return UNIQUE_ITEM_SERIALIZER;
    }

}
