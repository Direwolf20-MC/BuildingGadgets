package com.direwolf20.buildinggadgets.blocks.Models;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ConstructionProperty implements IUnlistedProperty<ConstructionID> {
    private final String name;

    public ConstructionProperty(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(ConstructionID value) {
        return true;
    }

    @Override
    public Class<ConstructionID> getType() {
        return ConstructionID.class;
    }

    @Override
    public String valueToString(ConstructionID value) {
        return value.toString();
    }
}
