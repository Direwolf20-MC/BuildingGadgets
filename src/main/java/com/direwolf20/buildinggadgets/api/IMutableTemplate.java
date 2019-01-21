package com.direwolf20.buildinggadgets.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IMutableTemplate extends ITemplate {

    public ITemplateDataStorage getDataStorage();

    public void setName(String name);

    public void setId(UUID id);

    /**
     * This Method is called to read Data that doesn't need to be stored in the world save, as well as trigger a world data load <b>if necessary</b>.
     *
     * @param nonWorldData The TagCompound storing data outside the WorldSave
     * @param world        The world for which to load the WorldSave for. May be null if world data doesn't need to be read
     */
    public void readNBT(@Nonnull NBTTagCompound nonWorldData, @Nullable World world);

    /**
     * This Method is called to write Data that doesn't need to be stored in the world save, as well as trigger a world data save <b>if necessary</b>.
     *
     * @param nonWorldData The TagCompound storing data outside the WorldSave
     * @param world        The world for which to save the WorldSave for. May be null if world data doesn't need to be saved
     */
    public void writeNBT(@Nonnull NBTTagCompound nonWorldData, @Nullable World world);

    /**
     * This Method copies the given Mappings into this {@link IMutableTemplate}.
     * Be aware that after this has been called a call to {@link #writeNBT(NBTTagCompound, World)} might be necessary.
     *
     * @param newBlockMap The new List of {@link BlockMap}'s to be used. May be an {@link com.google.common.collect.ImmutableList}.
     * @param start       The origin {@link BlockPos} for this copy
     * @param end         The target {@link BlockPos} for this copy
     * @param thePlayer   The Player who performed the copy and who is to be used for constructing {@link UniqueItem}'s. May be null if unknown.
     */
    public void onCopy(@Nonnull List<BlockMap> newBlockMap, @Nonnull BlockPos start, @Nonnull BlockPos end, @Nonnull EntityPlayer thePlayer);

    /**
     * Copies all the Data described by the given Template. Will discard any Data currently stored in this instance.
     *
     * @param template The template who is to be cloned
     */
    public void copyFrom(ITemplate template);
}
