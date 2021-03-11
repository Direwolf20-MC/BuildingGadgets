package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

/**
 * Dear viewer, you have found our Easter Eggs, enjoy but please don't tell people who haven't seen this yet (directly, hinting that there might be something is allowed ;) ). :)
 */
public final class Additions {
    private static final Random EASTER_RAND = new Random();

    private enum NineByNineTranslation implements ITranslationProvider {
        FOR_THE_COLLECTION(".for_the_collection"),
        JUST_LIKE_DIRE(".just_like_dire"),
        DIRE_WOULD_BE_PROUD(".dire_would_be_proud"),
        NO_DIRE_WIRE_PLEASE(".no_dire_wire");
        private static final List<ITranslationProvider> VALUES = ImmutableList.copyOf(values());
        private static final String PREFIX = Reference.MODID + ".easter_eggs.9x9";
        private final String key;
        private final int argCount;

        NineByNineTranslation(@Nonnull String key, @Nonnegative int argCount) {
            this.key = PREFIX + key;
            this.argCount = argCount;
        }

        NineByNineTranslation(@Nonnull String key) {
            this(key, 0);
        }

        @Override
        public boolean areValidArguments(Object... args) {
            return args.length == argCount;
        }

        @Override
        public String getTranslationKey() {
            return key;
        }
    }

    private enum DireNineByNineTranslation implements ITranslationProvider {
        ANOTHER_ONE_REALLY(".another_one"),
        DONT_TELL_YOUR_VIEWERS(".dont_tell"),
        YOU_SURE_THATS_THE_CORRECT_SIZE(".correct_size"),
        THIS_TIME_NO_DIRE_WIRE(".no_dire_wire");
        private static final List<ITranslationProvider> VALUES = ImmutableList.copyOf(values());
        private static final String PREFIX = Reference.MODID + ".easter_eggs.dire_9x9";
        private final String key;
        private final int argCount;

        DireNineByNineTranslation(@Nonnull String key, @Nonnegative int argCount) {
            this.key = PREFIX + key;
            this.argCount = argCount;
        }

        DireNineByNineTranslation(@Nonnull String key) {
            this(key, 0);
        }

        @Override
        public boolean areValidArguments(Object... args) {
            return args.length == argCount;
        }

        @Override
        public String getTranslationKey() {
            return key;
        }
    }

    // even though it's called sizeInvalid to hide it's purpose, it invokes the 9X9 easter egg
    public static boolean sizeInvalid(PlayerEntity player, Region region) {
        BlockPos size = region.getMax().subtract(region.getMin());
        if (size.getX() == 8 && size.getZ() == 8) { //size is 8 if it's a 9X9
            List<ITranslationProvider> list = player.getName().getContents().equals("Direwolf20") ? DireNineByNineTranslation.VALUES : NineByNineTranslation.VALUES;
            int pos = EASTER_RAND.nextInt(list.size());
            ITranslationProvider provider = list.get(pos);
            player.displayClientMessage(provider.componentTranslation().setStyle(Styles.DK_GREEN), true);
            return true;
        }
        return false;
    }
}
