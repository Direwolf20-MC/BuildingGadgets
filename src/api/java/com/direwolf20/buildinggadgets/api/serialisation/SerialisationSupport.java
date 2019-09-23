package com.direwolf20.buildinggadgets.api.serialisation;

import com.direwolf20.buildinggadgets.api.APIReference.TemplateSerializerReference;
import com.direwolf20.buildinggadgets.api.APIReference.TileDataSerializerReference;
import com.direwolf20.buildinggadgets.api.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;

public final class SerialisationSupport {
    private SerialisationSupport() {}

    @ObjectHolder(TileDataSerializerReference.DUMMY_SERIALIZER)
    private static ITileDataSerializer DUMMY_TILE_DATA_SERIALIZER = new DummyTileDataSerializer();

    public static ITileDataSerializer dummyDataSerializer() {
        return DUMMY_TILE_DATA_SERIALIZER;
    }

    private static final class DummyTileDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {

        private DummyTileDataSerializer() {
            super();
        }

        @Override
        public CompoundNBT serialize(ITileEntityData data, boolean persisted) {
            return new CompoundNBT();
        }

        @Override
        public ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted) {
            return TileSupport.dummyTileEntityData();
        }
    }

    @ObjectHolder(TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER)
    private static ITileDataSerializer NBT_TILE_DATA_SERIALIZER = new NBTTileEntityDataSerializer();

    public static ITileDataSerializer nbtTileDataSerializer() {
        return NBT_TILE_DATA_SERIALIZER;
    }

    private static final class NBTTileEntityDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {
        private NBTTileEntityDataSerializer() {}

        @Override
        public CompoundNBT serialize(ITileEntityData data, boolean persisted) {
            Preconditions.checkArgument(data instanceof NBTTileEntityData);
            NBTTileEntityData nbtData = (NBTTileEntityData) data;
            CompoundNBT res = new CompoundNBT();
            res.put(NBTKeys.KEY_DATA, nbtData.getNBT());
            if (nbtData.getRequiredMaterials() != null)
                res.put(NBTKeys.KEY_MATERIALS, nbtData.getRequiredMaterials().serialize(persisted));
            return res;
        }

        @Override
        public ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted) {
            CompoundNBT data = tagCompound.getCompound(NBTKeys.KEY_DATA);
            MaterialList materialList = null;
            if (tagCompound.contains(NBTKeys.KEY_MATERIALS, NBT.TAG_COMPOUND))
                materialList = MaterialList.deserialize(tagCompound.getCompound(NBTKeys.KEY_MATERIALS), persisted);
            return new NBTTileEntityData(data, materialList);
        }
    }

    @ObjectHolder(TemplateSerializerReference.IMMUTABLE_TEMPLATE_SERIALIZER)
    private static ITemplateSerializer IMMUTABLE_TEMPLATE_SERIALIZER;

    public static ITemplateSerializer immutableTemplateSerializer() {
        return IMMUTABLE_TEMPLATE_SERIALIZER;
    }

    @ObjectHolder(TemplateSerializerReference.DELEGATING_TEMPLATE_SERIALIZER)
    private static ITemplateSerializer DELEGATING_TEMPLATE_SERIALIZER;

    public static ITemplateSerializer delegatingTemplateSerializer() {
        return DELEGATING_TEMPLATE_SERIALIZER;
    }

}
