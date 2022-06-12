package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface OurSounds {
    DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MODID);

    RegistryObject<SoundEvent> BEEP = REGISTRY.register("beep", () -> new SoundEvent(new ResourceLocation(Reference.MODID, "beep")));

    static void playSound(SoundEvent sound, float pitch) {
        ClientProxy.playSound(sound, pitch);
    }

    static void playSound(SoundEvent sound) {
        ClientProxy.playSound(sound, 1.0F);
    }
}