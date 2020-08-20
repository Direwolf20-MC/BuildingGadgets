package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.TemplateProviderCapability;
import com.direwolf20.buildinggadgets.common.helpers.LookingHelper;
import com.direwolf20.buildinggadgets.common.schema.BoundingBox;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import com.direwolf20.buildinggadgets.common.schema.template.Template.Builder;
import com.direwolf20.buildinggadgets.common.schema.template.TemplateData;
import com.direwolf20.buildinggadgets.common.schema.template.provider.ITemplateProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;

public final class CutPasteShowcase extends Item {
    private enum Mode {
        CUT,
        PASTE
    }

    private static final String KEY_AREA = "area";
    private static final String KEY_MODE = "mode";
    private static final String KEY_ID = "id";
    private static final boolean READD_TEST = true;

    public CutPasteShowcase() {
        super(new Properties().maxStackSize(1));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        BlockRayTraceResult rayTrace = LookingHelper.getBlockResult(playerIn, false);
        if (rayTrace == null)
            return super.onItemRightClick(worldIn, playerIn, handIn);
        ItemStack gadget = playerIn.getHeldItem(handIn);
        Mode mode = getMode(gadget);
        if (! worldIn.isRemote && mode == Mode.CUT)
            onCut(worldIn, (ServerPlayerEntity) playerIn, gadget, rayTrace.getPos());
        else if (worldIn.isRemote && mode == Mode.PASTE)
            onCutPaste(worldIn, gadget, rayTrace.getPos());
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public void onCut(World world, ServerPlayerEntity player, ItemStack stack, BlockPos click) {
        assert ! world.isRemote;
        BoundingBox bounds = getArea(stack);
        final BoundingBox fBounds = player.isSneaking() ?
                new BoundingBox(bounds.createMinPos(), click) :
                new BoundingBox(click, bounds.createMaxPos());
        setArea(stack, fBounds);
        BlockPos min = fBounds.createMinPos();
        if (min.equals(BlockPos.ZERO) || fBounds.createMaxPos().equals(BlockPos.ZERO))
            return;

        world.getCapability(TemplateProviderCapability.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(p -> {
            String author = player.getDisplayName().getUnformattedComponentText();
            BuildingGadgets.LOGGER.info("{} is cutting area {}.", author, bounds);
            long startTime = System.nanoTime();
            Builder builder = Template.builder(min).author(author);
            for (Mutable m : fBounds.yxzIterable()) {
                BlockState state = world.getBlockState(m);
                if (state.isAir(world, m))
                    continue;
                TileEntity tileEntity = world.getTileEntity(m);
                CompoundNBT nbt = null;
                if (tileEntity != null)
                    nbt = tileEntity.write(new CompoundNBT());
                builder.recordBlock(m, state, nbt);
                world.setBlockState(m, Blocks.AIR.getDefaultState());
            }
            long buildTime = System.nanoTime();
            Template res = builder.build();
            long end = System.nanoTime();
            BuildingGadgets.LOGGER.info("Cutting area {} completed. Result is {}.", bounds, res);
            BuildingGadgets.LOGGER.info("Cutting took {}ms, assembling took {}ms => {}ms in total.",
                    timeString(buildTime - startTime), timeString(end - buildTime), timeString(end - startTime));
            p.setAndUpdateRemote(getID(stack), res, PacketDistributor.PLAYER.with(() -> player));
        });

    }

    public void onCutPaste(World world, ItemStack stack, BlockPos click) {
        assert world.isRemote;
        world.getCapability(TemplateProviderCapability.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(p -> {
            UUID id = getID(stack);
            p.getIfAvailable(id).map(t -> {
                BuildingGadgets.LOGGER.info("Placing cut paste {} with id {} at {}.", t, id, click);
                Mutable thePos = new Mutable();
                long time = System.nanoTime();
                //pass 1 place the blocks
                for (TemplateData aggregateData : t) {
                    thePos.setPos(aggregateData.getPos()).move(click.getX(), click.getY(), click.getZ());
                    aggregateData.getData().placeInWorld(world, thePos);
                }
                long pass1Time = System.nanoTime();
                //pass 2 perform post-placement updates
                //Mojang actually does 3 passes, with the second pass updating post-placement for neighbouring blocks (BitSetVoxelShapePart!!!)
                //and the third pass using Block#getValidBlockForPosition for post-placement updates. The third pass
                //then actually notifies neighbours. I'm unsure if this is truly needed, so I'm doing 2 passes for now,
                //but this needs future evaluation for production code
                for (TemplateData aggregateData : t) {
                    thePos.setPos(aggregateData.getPos()).move(click.getX(), click.getY(), click.getZ());
                    BlockState setState = aggregateData.getData().getState();
                    BlockState state = Block.getValidBlockForPosition(setState, world, thePos);
                    if (state != setState)
                        world.setBlockState(thePos, state, BlockFlags.NO_NEIGHBOR_DROPS);
                    world.notifyNeighbors(thePos, state.getBlock());
                }
                long end = System.nanoTime();
                BuildingGadgets.LOGGER.info("Cut paste placing completed.");
                BuildingGadgets.LOGGER.info("Pass 1 took {}ms, Pass 2 took {}ms => total={}ms",
                        timeString(pass1Time - time), timeString(end - pass1Time), timeString(end - time));
                return null;
            }).orElseGet(() -> {
                BuildingGadgets.LOGGER.error("Cannot paste Template with id {} as it does not exist.", id);
                return null;
            });
        });
    }

    public void onRotate(World world, ItemStack stack) {
        assert world.isRemote;
        world.getCapability(TemplateProviderCapability.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(p -> {
            UUID id = getID(stack);
            Axis randAxis = Axis.values()[world.rand.nextInt(Axis.values().length)];
            Rotation rot = Rotation.values()[world.rand.nextInt(Rotation.values().length)];

            p.getIfAvailable(id).map(t -> {
                rotate(p, id, t, randAxis, rot);
                return null;
            }).orElseGet(() -> {
                //normally this would happen on the server and therefore the following code would only contain a log
                //and not register a callback - that's just for testing purposes
                p.registerUpdateCallback(id, new BiPredicate<Optional<Template>, Boolean>() {
                    private boolean applied = false; //this is only necessary for when READD_TEST = true

                    @Override
                    public boolean test(Optional<Template> opt, Boolean retrieval) {
                        if (! applied && opt.isPresent()) {
                            applied = true;
                            rotate(p, id, opt.get(), randAxis, rot);
                        } else if (! applied)
                            BuildingGadgets.LOGGER.error("Could not mirror Template with id {} as it does not exist.", id);
                        return READD_TEST;
                    }
                });
                return null;
            });
        });
    }

    private void rotate(ITemplateProvider p, UUID id, Template template, Axis axis, Rotation rot) {
        BuildingGadgets.LOGGER.info("Rotating {} with id {} {} around  axis {}.", template, id, rot, axis);
        long time = System.nanoTime();
        Template res = template.rotate(axis, rot);
        time = System.nanoTime() - time;
        BuildingGadgets.LOGGER.info("Rotating took {}ms. Result is {}.", timeString(time), res);
        p.setAndUpdateRemote(id, res, PacketDistributor.SERVER.with(() -> null));
    }

    public void onMirror(World world, ItemStack stack) {
        assert world.isRemote;
        world.getCapability(TemplateProviderCapability.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(p -> {
            UUID id = getID(stack);
            Axis randAxis = Axis.values()[world.rand.nextInt(Axis.values().length)];
            p.getIfAvailable(id).map(t -> {
                mirror(p, id, t, randAxis);
                return null;
            }).orElseGet(() -> {
                //normally this would happen on the server and therefore the following code would only contain a log
                //and not register a callback - that's just for testing purposes
                p.registerUpdateCallback(id, new BiPredicate<Optional<Template>, Boolean>() {
                    private boolean applied = false; //this is only necessary for when READD_TEST = true

                    @Override
                    public boolean test(Optional<Template> opt, Boolean retrieval) {
                        if (! applied && opt.isPresent()) {
                            applied = true;
                            mirror(p, id, opt.get(), randAxis);
                        } else
                            BuildingGadgets.LOGGER.error("Could not mirror Template with id {} as it does not exist.", id);
                        return READD_TEST;
                    }
                });
                return null;
            });
        });
    }

    private void mirror(ITemplateProvider p, UUID id, Template template, Axis axis) {
        BuildingGadgets.LOGGER.info("Mirroring {} with id {} around axis {}.", template, id, axis);
        long time = System.nanoTime();
        Template res = template.mirror(axis);
        time = System.nanoTime() - time;
        BuildingGadgets.LOGGER.info("Mirroring took {}ms. Result is {}.", timeString(time), res);
        p.setAndUpdateRemote(id, res, PacketDistributor.SERVER.with(() -> null));
    }

    private String timeString(long nanoTime) {
        return String.format("%.3f", nanoTime / 1_000_000f);
    }

    private BoundingBox getArea(ItemStack stack) {
        if (stack.getOrCreateTag().contains(KEY_AREA, NBT.TAG_INT_ARRAY))
            return BoundingBox.of(stack.getOrCreateTag().getIntArray(KEY_AREA));
        return BoundingBox.ZEROS;
    }

    private void setArea(ItemStack stack, BoundingBox boundingBox) {
        stack.getOrCreateTag().putIntArray(KEY_AREA, boundingBox.toArray());
    }

    public UUID getID(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (nbt.hasUniqueId(KEY_ID))
            return nbt.getUniqueId(KEY_ID);
        UUID id = UUID.randomUUID();
        nbt.putUniqueId(KEY_ID, id);
        return id;
    }

    public void onModeSwitch(ItemStack stack) {
        Mode mode = getMode(stack);
        stack.getOrCreateTag().putString(KEY_MODE, Mode.values()[(mode.ordinal() + 1) % Mode.values().length].name());
    }

    private Mode getMode(ItemStack stack) {
        return stack.getOrCreateTag().contains(KEY_MODE, NBT.TAG_STRING) ?
                Mode.valueOf(stack.getOrCreateTag().getString(KEY_MODE)) : Mode.CUT;
    }
}
