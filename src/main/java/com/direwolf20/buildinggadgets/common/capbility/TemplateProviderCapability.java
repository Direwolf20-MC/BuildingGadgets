package com.direwolf20.buildinggadgets.common.capbility;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.schema.template.provider.ClientTemplateProvider;
import com.direwolf20.buildinggadgets.common.schema.template.provider.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.schema.template.provider.ServerTemplateProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemplateProviderCapability {
    @CapabilityInject(ITemplateProvider.class)
    public static final Capability<ITemplateProvider> TEMPLATE_PROVIDER_CAPABILITY = null;
    private static final ResourceLocation CAP_KEY = new ResourceLocation(BuildingGadgets.MOD_ID, "template_provider");

    public static void register() {
        BuildingGadgets.LOGGER.trace("Registering Template Provider Cap.");
        CapabilityManager.INSTANCE.register(ITemplateProvider.class, new IStorage<ITemplateProvider>() {
            @Override
            public INBT writeNBT(Capability<ITemplateProvider> capability, ITemplateProvider instance, Direction side) {
                return new CompoundNBT();
            }

            @Override
            public void readNBT(Capability<ITemplateProvider> capability, ITemplateProvider instance, Direction side, INBT nbt) {

            }
        }, ClientTemplateProvider::new);
        MinecraftForge.EVENT_BUS.addGenericListener(World.class, TemplateProviderCapability::onAttachWorldCaps);
    }

    private static void onAttachWorldCaps(AttachCapabilitiesEvent<World> attach) {
        attach.addCapability(CAP_KEY, createProvider(attach.getObject()));
    }

    private static ICapabilityProvider createProvider(World world) {
        ITemplateProvider provider = world instanceof ServerWorld ?
                new ServerTemplateProvider((ServerWorld) world) :
                new ClientTemplateProvider();
        return new ICapabilityProvider() {
            private final LazyOptional<ITemplateProvider> opt = LazyOptional.of(() -> provider);

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap == TEMPLATE_PROVIDER_CAPABILITY)
                    return opt.cast();
                return LazyOptional.empty();
            }
        };
    }
}
