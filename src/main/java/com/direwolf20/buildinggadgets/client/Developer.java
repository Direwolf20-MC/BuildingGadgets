package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.ForgeHooks;

public class Developer {
    private boolean isDev = FMLForgePlugin.RUNTIME_DEOBF;

    public Developer() {
        if( !isDev )
            return;

        BuildingGadgets.logger.info("Developer Mode Started");
    }

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
