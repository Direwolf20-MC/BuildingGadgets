/**
 * Provides serialisation capabilities both for {@link com.direwolf20.buildinggadgets.api.template.ITemplate} and {@link com.direwolf20.buildinggadgets.api.building.tilesupport.ITileEntityData}
 * via their corresponding serializers. Both have their own {@link net.minecraftforge.registries.IForgeRegistry} of whom you can receive {@link net.minecraftforge.event.RegistryEvent}'s.
 * <p>
 * This package also provides an implementation for the {@link com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader} which is used to provide further
 * information for users about the {@link com.direwolf20.buildinggadgets.api.template.ITemplate}'s they are using.
 * @see com.direwolf20.buildinggadgets.api.Registries
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
package com.direwolf20.buildinggadgets.api.serialisation;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;