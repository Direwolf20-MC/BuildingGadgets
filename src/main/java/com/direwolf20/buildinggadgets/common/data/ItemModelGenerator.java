package com.direwolf20.buildinggadgets.common.data;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.fml.RegistryObject;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BuildingGadgets.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generateGadget(ModItems.BUILDING_GADGET);
    }

    /**
     * Creates the placement in the hand for the Gadget models
     *
     * @param item Gadget
     */
    private void generateGadget(RegistryObject<Item> item) {
        ResourceLocation name = item.get().getRegistryName();
        assert name != null;

        singleTexture(name.getPath(), mcLoc("item/handheld"), "layer0", modLoc("items/" + name.getPath()))
                .transforms()
                    .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT)
                        .rotation(0, 0, 0)
                        .translation(0, 0, 0)
                        .scale(.5f)
                    .end()
                    .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT)
                        .rotation(-45, 0, 0)
                        .translation(6, 0, -7)
                        .scale(.5f)
                    .end()
                .end();
    }
}
