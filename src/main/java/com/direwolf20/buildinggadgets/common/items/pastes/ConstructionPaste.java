package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConstructionPaste extends Item {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(BuildingGadgets.MODID, "construction_paste");
    public ConstructionPaste(Properties builder) {
        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        itemstack = InventoryManipulation.addPasteToContainer(player, itemstack);

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

}
