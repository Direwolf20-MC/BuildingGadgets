package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum BGSound {
    BEEP("beep");
    private SoundEvent sound;

    BGSound(String name) {
        ResourceLocation loc = new ResourceLocation(Reference.MODID, name);
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
        for (BGSound sound : values()) {
            event.getRegistry().register(sound.getSound());
        }
    }

}