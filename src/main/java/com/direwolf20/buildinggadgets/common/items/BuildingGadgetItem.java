package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.building.Modes;
import com.direwolf20.buildinggadgets.common.capability.OurCapabilities;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.Set;

public class BuildingGadgetItem extends AbstractGadget {
    private static final GadgetAbilities ABILITIES = new GadgetAbilities(true, true, true, false, false, false);

    public BuildingGadgetItem() {
        super(Config.GADGETS.GADGET_BUILDING);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack gadget = player.getHeldItem(hand);

        RayTraceResult pick = player.pick(50, 1f, false);
        if (player.isCrouching() && pick.getType() == RayTraceResult.Type.BLOCK) {
            gadget.getCapability(OurCapabilities.GADGET_META).ifPresent(meta -> meta.setBlockState(world.getBlockState(((BlockRayTraceResult) pick).getPos())));
        }

        if (!player.isCrouching()) {
            gadget.getCapability(OurCapabilities.GADGET_META).ifPresent(meta -> System.out.println(meta.getBlockState()));
            System.out.println(gadget.getOrCreateTag().toString());
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public Set<IMode> getModes() {
        return Modes.getBuildingModes();
    }

    @Override
    public GadgetAbilities getAbilities() {
        return ABILITIES;
    }
}
