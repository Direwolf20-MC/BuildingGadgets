package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ITemplateSerializer extends IForgeRegistryEntry<ITemplateSerializer> {
    TemplateHeader createHeaderFor(ITemplate template);

    NBTTagCompound serialize(ITemplate template);

    ITemplate deserialize(NBTTagCompound tagCompound);
}
