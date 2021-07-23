package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GadgetUtils {
    // TODO: migrate to tags
    private static final ImmutableList<Block> DISALLOWED_BLOCKS = ImmutableList.of(
        Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.BEDROCK, Blocks.SPAWNER
    );

    private static final ImmutableList<String> LINK_STARTS = ImmutableList.of("http","www");

    public static boolean mightBeLink(final String s) {
        return LINK_STARTS.stream().anyMatch(s::startsWith);
    }

    public static void addTooltipNameAndAuthor(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        if (world != null) {
            world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
                stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                    Template template = provider.getTemplateForKey(key);
                    TemplateHeader header = template.getHeader();
                    if (header.getName() != null && ! header.getName().isEmpty())
                        tooltip.add(TooltipTranslation.TEMPLATE_NAME.componentTranslation(header.getName()).setStyle(Styles.AQUA));
                    if (header.getAuthor() != null && ! header.getAuthor().isEmpty())
                        tooltip.add(TooltipTranslation.TEMPLATE_AUTHOR.componentTranslation(header.getAuthor()).setStyle(Styles.AQUA));
                });
            });
        }
        EventTooltip.addTemplatePadding(stack, tooltip);
    }

    @Nullable
    public static ByteArrayOutputStream getPasteStream(@Nonnull CompoundTag compound, @Nullable String name) throws IOException {
        CompoundTag withText = name != null && !name.isEmpty() ? compound.copy() : compound;
        if (name != null && !name.isEmpty()) withText.putString(NBTKeys.TEMPLATE_NAME, name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NbtIo.writeCompressed(withText, baos);
        return baos.size() < Short.MAX_VALUE - 200 ? baos : null;
    }

    public static void setAnchor(ItemStack stack) {
        setAnchor(stack, new ArrayList<>());
    }

    public static void setAnchor(ItemStack stack, List<BlockPos> coordinates) {
        //Store 1 set of BlockPos in NBT to anchor the Ghost Blocks in the world when the anchor key is pressed
        CompoundTag tagCompound = stack.getOrCreateTag();
        tagCompound.put(NBTKeys.GADGET_ANCHOR_COORDS, coordinates.stream().map(NbtUtils::writeBlockPos).collect(Collectors.toCollection(ListTag::new)));
        stack.setTag(tagCompound);
    }

    public static Optional<List<BlockPos>> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null)
            return Optional.empty();

        ListTag coordList = (ListTag) tagCompound.get(NBTKeys.GADGET_ANCHOR_COORDS);
        if (coordList == null || coordList.size() == 0)
            return Optional.empty();

        List<BlockPos> coordinates = new ArrayList<>();
        for (int i = 0; i < coordList.size(); i++) {
            coordinates.add(NbtUtils.readBlockPos(coordList.getCompound(i)));
        }

        return Optional.of(coordinates);
    }

    public static void setToolRange(ItemStack stack, int range) {
        //Store the tool's range in NBT as an Integer
        CompoundTag tagCompound = stack.getOrCreateTag();
        tagCompound.putInt("range", range);
    }

    public static int getToolRange(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        return Mth.clamp(tagCompound.getInt("range"), 1, 15);
    }

    public static BlockData rotateOrMirrorBlock(Player player, PacketRotateMirror.Operation operation, BlockData data) {
        if (operation == PacketRotateMirror.Operation.MIRROR)
            return data.mirror(player.getDirection().getAxis() == Axis.X ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK);

        return data.rotate(Rotation.CLOCKWISE_90);
    }

    public static void rotateOrMirrorToolBlock(ItemStack stack, Player player, PacketRotateMirror.Operation operation) {
        setToolBlock(stack, rotateOrMirrorBlock(player, operation, getToolBlock(stack)));
    }

    private static void setToolBlock(ItemStack stack, @Nullable BlockData data) {
        //Store the selected block in the tool's NBT
        CompoundTag tagCompound = stack.getOrCreateTag();
        if (data == null)
            data = BlockData.AIR;

        CompoundTag stateTag = data.serialize(true);
        tagCompound.put(NBTKeys.TE_CONSTRUCTION_STATE, stateTag);
        stack.setTag(tagCompound);
    }


    @Nonnull
    public static BlockData getToolBlock(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        BlockData res = BlockData.tryDeserialize(tagCompound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE), true);
        if (res == null) {
            setToolBlock(stack, BlockData.AIR);
            return BlockData.AIR;
        }
        return res;
    }

    public static void linkToInventory(ItemStack stack, Player player) {
        Level world = player.level;
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, AbstractGadget.shouldRayTraceFluid(stack) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
        if (world.getBlockState(VectorHelper.getLookingAt(player, stack).getBlockPos()) == Blocks.AIR.defaultBlockState())
            return;

        InventoryLinker.Result result = InventoryLinker.linkInventory(player.level, stack, lookingAt);
        player.displayClientMessage(result.getI18n().componentTranslation(), true);
    }

    public static InteractionResultHolder<Block> selectBlock(ItemStack stack, Player player) {
        // Used to find which block the player is looking at, and store it in NBT on the tool.
        Level world = player.level;
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, AbstractGadget.shouldRayTraceFluid(stack) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
        if (world.isEmptyBlock(lookingAt.getBlockPos()))
            return InteractionResultHolder.fail(Blocks.AIR);

        BlockState state = world.getBlockState(lookingAt.getBlockPos());
        if (! ((AbstractGadget) stack.getItem()).isAllowedBlock(state.getBlock()) || state.getBlock() instanceof EffectBlock)
            return InteractionResultHolder.fail(state.getBlock());

        if (DISALLOWED_BLOCKS.contains(state.getBlock())) {
            return InteractionResultHolder.fail(state.getBlock());
        }

        if (state.getDestroySpeed(world, lookingAt.getBlockPos()) < 0) {
            return InteractionResultHolder.fail(state.getBlock());
        }

        Optional<BlockData> data = InventoryHelper.getSafeBlockData(player, lookingAt.getBlockPos(), player.getUsedItemHand());
        data.ifPresent(placeState -> {
            BlockState actualState = placeState.getState(); //.getExtendedState(world, lookingAt.getPos()); 1.14 @todo: fix?

            setToolBlock(stack, new BlockData(actualState, placeState.getTileData()));
        });

        return InteractionResultHolder.success(state.getBlock());
    }

    public static InteractionResult setRemoteInventory(ItemStack stack, Player player, Level world, BlockPos pos, boolean setTool) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null)
            return InteractionResult.PASS;

        if (setTool && te instanceof ConstructionBlockTileEntity) {
            setToolBlock(stack, ((ConstructionBlockTileEntity) te).getConstructionBlockData());
            return InteractionResult.SUCCESS;
        }


        return InteractionResult.FAIL;
    }

    public static boolean anchorBlocks(Player player, ItemStack stack) {
        //Stores the current visual blocks in NBT on the tool, so the player can look around without moving the visual render
        Optional<List<BlockPos>> anchorCoords = getAnchor(stack);

        if( anchorCoords.isPresent() ) {  //If theres already an anchor, remove it.
            setAnchor(stack);
            player.displayClientMessage(MessageTranslation.ANCHOR_REMOVED.componentTranslation().setStyle(Styles.AQUA), true);
            return true;
        }

        //If we don't have an anchor, find the block we're supposed to anchor to
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, stack);
        BlockPos startBlock = lookingAt.getBlockPos();
        Direction sideHit = lookingAt.getDirection();

        //If we aren't looking at anything, exit
        if (player.level.isEmptyBlock(startBlock))
            return false;

        BlockData blockData = getToolBlock(stack);
        AbstractMode.UseContext context = new AbstractMode.UseContext(player.level, blockData.getState(), startBlock, stack, sideHit, stack.getItem() instanceof GadgetBuilding && GadgetBuilding.shouldPlaceAtop(stack), stack.getItem() instanceof GadgetBuilding ? GadgetBuilding.getConnectedArea(stack) : GadgetExchanger.getConnectedArea(stack));

        List<BlockPos> coords = stack.getItem() instanceof GadgetBuilding
                ? GadgetBuilding.getToolMode(stack).getMode().getCollection(context, player)
                : GadgetExchanger.getToolMode(stack).getMode().getCollection(context, player);

        setAnchor(stack, coords); //Set the anchor NBT
        player.displayClientMessage(MessageTranslation.ANCHOR_SET.componentTranslation().setStyle(Styles.AQUA), true);

        return true;
    }

    public static String withSuffix(int count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1));
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName) {
        CompoundTag tagCompound = stack.getOrCreateTag();

        if (pos == null) {
            if (tagCompound.get(tagName) != null) {
                tagCompound.remove(tagName);
                stack.setTag(tagCompound);
            }
            return;
        }
        tagCompound.put(tagName, NbtUtils.writeBlockPos(pos));
        stack.setTag(tagCompound);
    }


    @Nullable
    public static BlockPos getPOSFromNBT(ItemStack stack, String tagName) {
        CompoundTag stackTag = stack.getOrCreateTag();
        if (! stackTag.contains(tagName))
            return null;
        CompoundTag posTag = stack.getOrCreateTag().getCompound(tagName);
        if (posTag.isEmpty())
            return null;
        return NbtUtils.readBlockPos(posTag);
    }


    @Nullable
    public static ResourceLocation getDIMFromNBT(ItemStack stack, String tagName) {
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        CompoundTag posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new CompoundTag())) {
            return null;
        }
        return new ResourceLocation(posTag.getString(NBTKeys.GADGET_DIM));
    }

    /**
     * Drops the IItemHandlerModifiable Inventory of the TileEntity at the specified position.
     */
    public static void dropTileEntityInventory(Level world, BlockPos pos) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null) {
            LazyOptional<IItemHandler> cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            cap.ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    net.minecraft.world.Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                }
            });
        }
    }
}
