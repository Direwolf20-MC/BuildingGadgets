package com.direwolf20.buildinggadgets.client.renderer;

import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;


public class ChargingStationTESR extends TileEntityRenderer<ChargingStationTileEntity> {

    public ChargingStationTESR() {
        System.out.println("Newed");
    }

    @Override
    public void render(ChargingStationTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();

        // Translate to the location of our tile entity
        GlStateManager.translatef((float) x, (float) y, (float) z);
        GlStateManager.disableRescaleNormal();

        // Render our item
        renderItem(te);

        GlStateManager.popMatrix();
    }

    private void renderItem(ChargingStationTileEntity te) {
        ItemStack stack = te.getRenderStack();
        if (!stack.isEmpty()) {
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableLighting();
            GlStateManager.pushMatrix();
            // Translate to the center of the block and .9 points higher
            GlStateManager.translatef((float) .5, (float) 1.25, (float) .5);
            GlStateManager.scalef(.4f, .4f, .4f);

            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.NONE);

            GlStateManager.popMatrix();
        }
    }
}
