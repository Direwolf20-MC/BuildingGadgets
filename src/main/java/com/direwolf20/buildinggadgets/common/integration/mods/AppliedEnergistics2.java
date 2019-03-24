package com.direwolf20.buildinggadgets.common.integration.mods;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.Phase;
import com.direwolf20.buildinggadgets.common.integration.NetworkProvider;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.IStackProvider;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.Operation;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IGrinderRecipe;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

@IntegratedMod("appliedenergistics2")
public class AppliedEnergistics2 extends NetworkProvider implements IPasteRecipeRegistry {

    @Override
    public void registerDeconstructRecipe(RecipieType type, ItemStack input, ItemStack output) {
        AEApi.instance().registries().grinder().addRecipe(new GrinderRecipe(type == RecipieType.BLOCK_TO_CHUNKS ? 4 : 1, input, output));
    }

    @Override
    public void initialize(Phase phase) {
        super.initialize(phase);
        IPasteRecipeRegistry.super.initialize(phase);
    }

    @Override
    @Nullable
    protected IItemHandler getWrappedNetworkInternal(TileEntity te, EntityPlayer player, Operation operation) {
        if (te instanceof IGridHost) {
            IGridNode node = ((IGridHost) te).getGridNode(AEPartLocation.INTERNAL);
            if (node != null) {
                ISecurityGrid security = node.getGrid().getCache(ISecurityGrid.class);
                if (security.hasPermission(player, operation == Operation.EXTRACT ? SecurityPermissions.EXTRACT : SecurityPermissions.INJECT)) {
                    IStorageGrid grid = node.getGrid().getCache(IStorageGrid.class);
                    IMEMonitor<IAEItemStack> network = grid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                    return new NetworkAppliedEnergistics2IO(player, network, operation);
                }
            }
        }
        return null;
    }

    public static class StackProviderAE2 implements IStackProvider {
        @Nonnull
        private IAEItemStack aeStack;

        public StackProviderAE2(@Nonnull IAEItemStack aeStack) {
            this.aeStack = aeStack;
        }

        @Override
        @Nonnull
        public ItemStack getStack() {
            return aeStack.createItemStack();
        }

        @Override
        public void shrinkStack(int amount) {
            aeStack.setStackSize(aeStack.getStackSize() - amount);
        }

        public IAEItemStack withSize(int amount) {
            return aeStack.copy().setStackSize(amount);
        }
    }

    private static class NetworkAppliedEnergistics2IO extends NetworkIO<StackProviderAE2> {
        private IMEMonitor<IAEItemStack> network;
        private IItemStorageChannel storageChannel;

        public NetworkAppliedEnergistics2IO(EntityPlayer player, IMEMonitor<IAEItemStack> network, Operation operation) {
            super(player, operation == Operation.INSERT ? null :
                Streams.stream(network.getStorageList()).map(aeStack -> new StackProviderAE2(aeStack.copy())).collect(Collectors.toList()));
            this.network = network;
            storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        }

        @Override
        @Nullable
        public ItemStack insertItemInternal(ItemStack stack, boolean simulate) {
            IAEItemStack aeStack = storageChannel.createStack(stack);
            if (aeStack == null)
                return ItemStack.EMPTY;

            IAEItemStack remainder = network.injectItems(aeStack, getAction(simulate), new PlayerSource(player));
            return remainder == null ? null : remainder.createItemStack();
        }

        @Override
        @Nonnull
        protected IStackProvider extractItemInternal(StackProviderAE2 stackProvider, int amount, boolean simulate) {
            return new StackProviderAE2(network.extractItems(stackProvider.withSize(amount), getAction(simulate), new PlayerSource(player)));
        }

        private Actionable getAction(boolean simulate) {
            return simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        }
    }

    private static class PlayerSource implements IActionSource {
        private final EntityPlayer player;

        public PlayerSource(EntityPlayer player) {
            Preconditions.checkNotNull(player);
            this.player = player;
        }

        @Override
        public Optional<EntityPlayer> player() {
            return Optional.of(player);
        }

        @Override
        public Optional<IActionHost> machine() {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            return Optional.empty();
        }
    }

    private static class GrinderRecipe implements IGrinderRecipe {
        private final ItemStack input, output;
        private final int turns;

        public GrinderRecipe(int turns, ItemStack input, ItemStack output) {
            this.input = input;
            this.output = output;
            this.turns = turns;
        }

        @Override
        @Nonnull
        public ItemStack getInput() {
            return input;
        }

        @Override
        @Nonnull
        public ItemStack getOutput() {
            return output;
        }

        @Override
        @Nonnull
        public Optional<ItemStack> getOptionalOutput() {
            return Optional.empty();
        }

        @Override
        public Optional<ItemStack> getSecondOptionalOutput() {
            return Optional.empty();
        }

        @Override
        @Nonnull
        public float getOptionalChance() {
            return 0;
        }

        @Override
        public float getSecondOptionalChance() {
            return 0;
        }

        @Override
        public int getRequiredTurns() {
            return turns;
        }
        
    }
}