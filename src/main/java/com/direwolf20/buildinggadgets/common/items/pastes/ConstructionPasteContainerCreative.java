package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.utils.Reference;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionPasteContainerCreative extends GenericPasteContainer {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.MODID,"construction_paste_container_creative");
    public ConstructionPasteContainerCreative(Properties builder) {
        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    public void initModel() {
//        // We don't use custom item mesh definition since creative container will be always full, and there is no need to register mesh
//        // definition for the item in CommonProxy
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        list.add(new TextComponentTranslation("tooltip.pasteContainer.creative.amountMsg").setStyle(new Style().setColor(TextFormatting.WHITE)));
    }

    @Override
    public void setPasteCount(ItemStack stack, int amount) {}

    @Override
    public int getPasteCount(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxCapacity() {
        return Integer.MAX_VALUE;
    }

}
