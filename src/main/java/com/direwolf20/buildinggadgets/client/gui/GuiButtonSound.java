package com.direwolf20.buildinggadgets.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundEvent;

public class GuiButtonSound extends GuiButtonSelect {
    private SoundEvent soundSelect, soundDeselect;
    private float pitchSelect, pitchDeselect;
    private boolean silent;

    public GuiButtonSound(int x, int y, int width, int height, String text, String helpTextKey, @Nullable Runnable action) {
        super(x, y, width, height, text, helpTextKey, action);
        pitchSelect = pitchDeselect = 1;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setSounds(SoundEvent soundSelect, SoundEvent soundDeselect) {
        setSounds(soundSelect, soundDeselect, 1, 1);
    }

    public void setSounds(SoundEvent soundSelect, SoundEvent soundDeselect, float pitchSelect, float pitchDeselect) {
        this.soundSelect = soundSelect;
        this.soundDeselect = soundDeselect;
        this.pitchSelect = pitchSelect;
        this.pitchDeselect = pitchDeselect;
        silent = false;
    }

    @Override
    public void playPressSound(SoundHandler soundHandler) {
        if (silent)
            return;

        SoundEvent sound = soundSelect == null ? SoundEvents.UI_BUTTON_CLICK : (selected ? soundDeselect : soundSelect);
        soundHandler.play(SimpleSound.getMasterRecord(sound, selected ? pitchDeselect : pitchSelect));
    }
}