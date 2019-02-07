package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionPasteContainerCreative extends GenericPasteContainer {

    public ConstructionPasteContainerCreative() {
        setRegistryName("constructionpastecontainercreative");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".constructionpastecontainercreative");     // Used for localization (en_US.lang)
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        list.add(TextFormatting.WHITE + I18n.format("tooltip.pasteContainer.creative.amountMsg"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        // Remove right click function
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void setPastes(ItemStack stack, int amount) {
    }

    @Override
    public int getPastes(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        // We don't use custom item mesh definition since creative container will be always full, and there is no need to register mesh
        // definition for the item in CommonProxy
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public int getMaxAmount() {
        return Integer.MAX_VALUE;
    }

}
