package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class RegistryHandler {

    /**
     * Register our creative tab. Notice that we're also modifying the NBT data of the
     * building gadget to remove the damage / energy indicator from the creative
     * tabs icon.
     */
    public static ItemGroup creativeTab = new ItemGroup(Reference.MODID){
        @Override
        public ItemStack createIcon() {
            ItemStack stack = new ItemStack(OurItems.gadgetBuilding);
            stack.getOrCreateTag().putByte(NBTKeys.CREATIVE_MARKER, (byte) 0);
            return stack;
        }
    };

    public static void setup() {
        OurItems.setup();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            OurEntities.registerModels();
            OurBlocks.OurTileEntities.registerRenderers();
        });
    }

    public static void clientSetup() {
        OurContainers.registerContainerScreens();
        OurBlocks.constructionBlock.initColorHandler(Minecraft.getInstance().getBlockColors());
    }
}
