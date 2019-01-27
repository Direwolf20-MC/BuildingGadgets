package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;

public class ConstructionPaste extends Item {
    public ConstructionPaste(Builder builder) {
        super(builder);
        setRegistryName("constructionpaste");        // The unique name (within your mod) that identifies this item
//        setUnlocalizedName(BuildingGadgets.MODID + ".constructionpaste");     // Used for localization (en_US.lang)
    }

    @OnlyIn(Dist.CLIENT)
    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        itemstack = InventoryManipulation.addPasteToContainer(player, itemstack);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

}
