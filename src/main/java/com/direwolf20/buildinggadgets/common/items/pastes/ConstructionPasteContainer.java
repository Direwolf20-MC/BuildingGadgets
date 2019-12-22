package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.config.utils.NBTTool;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

import static com.direwolf20.buildinggadgets.common.gadgets.GadgetGeneric.getOrNewTag;

public class ConstructionPasteContainer extends GenericPasteContainer {

    private IntSupplier maxCapacity;

    public ConstructionPasteContainer(String suffix, IntSupplier maxCapacity) {
        super("constructionpastecontainer" + suffix);
        this.maxCapacity = maxCapacity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelBakery.registerItemVariants(this,
                new ModelResourceLocation(getRegistryName(), "inventory"),
                new ModelResourceLocation(getRegistryName() + "-half", "inventory"),
                new ModelResourceLocation(getRegistryName() + "-full", "inventory"),
                new ModelResourceLocation(getRegistryName() + "-quarter", "inventory"),
                new ModelResourceLocation(getRegistryName() + "-3quarter", "inventory"));
    }

    @Override
    public void setPasteCount(ItemStack stack, int amount) {
        getOrNewTag(stack).setInteger("amount", amount);
    }

    @Override
    public int getPasteCount(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }

        return stack.getTagCompound().getInteger("amount");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        player.setActiveHand(hand);
        InventoryPlayer inv = player.inventory;
        if (!world.isRemote) {
            for (int i = 0; i < 36; ++i) {
                ItemStack itemStack = inv.getStackInSlot(i);
                if (itemStack.getItem() instanceof ConstructionPaste) {
                    InventoryManipulation.addPasteToContainer(player, itemStack);
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        list.add(getAmountDisplayLocalized() + ": " + getPasteAmount(stack));
    }

    @Override
    public int getMaxCapacity() {
        return maxCapacity.getAsInt();
    }

}
