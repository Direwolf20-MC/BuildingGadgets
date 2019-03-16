package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public class GuiButtonSound extends GuiButtonSelect {
    private SoundEvent soundSelect, soundDeselect;
    private float pitchSelect, pitchDeselect;
    private boolean silent;

    public GuiButtonSound(int buttonId, int x, int y, int width, int height, String text, String helpTextKey) {
        super(buttonId, x, y, width, height, text, helpTextKey);
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
        soundHandler.playSound(PositionedSoundRecord.getMasterRecord(sound, selected ? pitchDeselect : pitchSelect));
    }
}