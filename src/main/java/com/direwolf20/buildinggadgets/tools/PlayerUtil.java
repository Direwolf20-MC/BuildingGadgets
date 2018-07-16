package com.direwolf20.buildinggadgets.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

public class PlayerUtil
{
//    This is done like this instead of using TextComponentTranslation because it does color format the replacements
    public static void sendPlayerActionBarMessage(EntityPlayer player, String translationKey, Object... replacements) {
        String translationMessage = I18n.format(translationKey, replacements);
        TextComponentString textComponentString = new TextComponentString(translationMessage);
        player.sendStatusMessage(textComponentString, true);
    }

}
