package com.direwolf20.buildinggadgets.common;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public enum ModSounds {
    BEEP("beep");

    private SoundEvent sound;

    private ModSounds(String name) {
        ResourceLocation loc = new ResourceLocation(BuildingGadgets.MODID, name);
        sound = new SoundEvent(loc).setRegistryName(name);
    }

    public SoundEvent getSound() {
        return sound;
    }

    public void playSound(SoundHandler soundHandler) {
        playSound(soundHandler, 1.0F);
    }

    public void playSound(SoundHandler soundHandler, float pitch) {
        soundHandler.playSound(PositionedSoundRecord.getMasterRecord(sound, pitch));
    }
}