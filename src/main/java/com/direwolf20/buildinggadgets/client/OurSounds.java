package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BuildingGadgetsAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum OurSounds {
    BEEP("beep");
    private SoundEvent sound;

    OurSounds(String name) {
        ResourceLocation loc = new ResourceLocation(BuildingGadgetsAPI.MODID, name);
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

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (OurSounds sound : values()) {
            event.getRegistry().register(sound.getSound());
        }
    }

}
