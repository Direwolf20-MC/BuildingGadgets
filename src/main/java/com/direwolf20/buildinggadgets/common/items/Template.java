package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

//import net.minecraft.util.EnumActionResult;

public class Template extends Item implements ITemplate {

    public Template(Properties builder) {
        super(builder.maxStackSize(1));
    }

    @Override
    public WorldSave getWorldSave(World world) {
        return WorldSave.getTemplateWorldSave(world);
    }

    @Override
    @Nullable
    public String getUUID(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        String uuid = tagCompound.getString(NBTKeys.TEMPLATE_UUID);
        if (uuid.isEmpty()) {
            UUID uid = UUID.randomUUID();
            tagCompound.putString(NBTKeys.TEMPLATE_UUID, uid.toString());
            stack.setTag(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
    }

    public static void setName(ItemStack stack, String name) {
        GadgetUtils.writeStringToNBT(stack, name, NBTKeys.TEMPLATE_NAME);
    }

    public static String getName(ItemStack stack) {
        return GadgetUtils.getStringFromNBT(stack, NBTKeys.TEMPLATE_NAME);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(TooltipTranslation.TEMPLATE_NAME.componentTranslation(getName(stack)).setStyle(Styles.AQUA));
        EventTooltip.addTemplatePadding(stack, tooltip);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (world.isRemote)
            GuiMod.MATERIAL_LIST.openScreen(player);
        ItemStack itemstack = player.getHeldItem(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

}
