package com.direwolf20.buildinggadgets.api.abstraction;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IItemStack extends ICapabilityProvider {
    public void setCount(int count);

    public int getCount();
}
