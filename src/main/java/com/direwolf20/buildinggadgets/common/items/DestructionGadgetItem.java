package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.capability.gadget.GadgetMeta;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetAbilities;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DestructionGadgetItem extends AbstractGadget {

    public DestructionGadgetItem() {
        super(Config.GADGETS.GADGET_DESTRUCTION);
    }

    @Override
    List<ITextComponent> addMetaInformation(GadgetMeta meta, ItemStack stack, @Nullable World world) {
        List<ITextComponent> tips = super.addMetaInformation(meta, stack, world);
        tips.add(TooltipTranslation.GADGET_DESTROYWARNING.componentTranslation().mergeStyle(TextFormatting.RED));
        tips.add(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY.componentTranslation(String.valueOf(meta.isShowOverlay())).mergeStyle(TextFormatting.DARK_AQUA));
        return tips;
    }

    @Override
    public Set<IMode> getModes() {
        return new HashSet<>();
    }

    @Override
    public GadgetAbilities getAbilities() {
        return GadgetAbilities.NONE;
    }
}
