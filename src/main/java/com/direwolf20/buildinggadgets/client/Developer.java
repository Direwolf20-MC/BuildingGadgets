package com.direwolf20.buildinggadgets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;

public class Developer {
    private boolean isDev = true;

    /**
     * Note this class is only client sided
     */
    public void onKeyPress() {
        EntityPlayer player = Minecraft.getMinecraft().player;

        RayTraceResult result = player.rayTrace(30D, 1f);
        if( result == null || result.typeOfHit == RayTraceResult.Type.MISS )
            return;

//        BuildToMeMode mode = new BuildToMeMode();
//        System.out.println(mode.collect(result.getBlockPos(), player.getPosition(), result.sideHit));
        System.out.println(result.sideHit);
    }

    public boolean isIsDev() {
        return isDev;
    }
}
