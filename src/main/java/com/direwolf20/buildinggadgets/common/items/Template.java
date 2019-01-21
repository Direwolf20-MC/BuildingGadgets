package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.api.IMutableTemplate;
import com.direwolf20.buildinggadgets.api.ITemplateOld;
import com.direwolf20.buildinggadgets.api.WorldSave;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderTemplate;
import com.direwolf20.buildinggadgets.common.capability.WrappingCapabilityProvider;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class Template extends Item implements ITemplateOld {
    public Template() {
        setRegistryName("template");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".template");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
    }

    public static void setName(ItemStack stack, String name) {
        IMutableTemplate template = CapabilityProviderTemplate.getTemplate(stack, null);
        template.setName(name);
        CapabilityProviderTemplate.writeTemplate(template, stack, null);
    }

    public static String getName(ItemStack stack) {
        return CapabilityProviderTemplate.getTemplate(stack, null).getName();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        //Add tool information to the tooltip
        super.addInformation(stack, world, list, b);
        list.add(TextFormatting.AQUA + I18n.format("tooltip.template.name") + ": " + getName(stack));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    /**
     * Called from ItemStack.setItem, will hold extra data for the life of this ItemStack.
     * Can be retrieved from stack.getCapabilities()
     * The NBT can be null if this is not called from readNBT or if the item the stack is
     * changing FROM is different then this item, or the previous item had no capabilities.
     * <p>
     * This is called BEFORE the stacks item is set so you can use stack.getItem() to see the OLD item.
     * Remember that getItem CAN return null.
     *
     * @param stack The ItemStack
     * @param nbt   NBT of this item serialized, or null.
     * @return A holder instance associated with this ItemStack where you can hold capabilities for the life of this item.
     */
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new WrappingCapabilityProvider(super.initCapabilities(stack, nbt), new CapabilityProviderTemplate(WorldSave.TEMPLATE_STORAGE));
    }
}
