package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.BuildingGadgets.ClientProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public enum BGSounds {
    BEEP("beep");

    private SoundEvent sound;

    BGSounds(String name) {
        ResourceLocation loc = new ResourceLocation(BuildingGadgets.MODID, name);
        sound = new SoundEvent(loc).setRegistryName(name);
    }

    public SoundEvent getSound() {
        return sound;
    }

    public void playSound() {
        playSound(1.0F);
    }

    public void playSound(float pitch) {
        ClientProxy.playSound(sound, pitch);
    }
}