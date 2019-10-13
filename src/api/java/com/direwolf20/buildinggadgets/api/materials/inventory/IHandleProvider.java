package com.direwolf20.buildinggadgets.api.materials.inventory;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IHandleProvider {
    /**
     * @param capProvider    The capProvider to index
     * @param indexMap       The index to add to
     * @param indexedClasses The Set of indexed classes. An {@code IHandleProvider} should add all classes that were indexed by itself to this Set and
     *                       should also query it, whether any indexing should be performed.
     */
    boolean index(ICapabilityProvider capProvider, Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> indexMap, Set<Class<?>> indexedClasses);
}
