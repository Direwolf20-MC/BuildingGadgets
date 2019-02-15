package com.direwolf20.buildinggadgets.common.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PatternList {
    private final ImmutableList<Pattern> patterns;

    @Nonnull
    public static String getName(IForgeRegistryEntry<?> entry) {
        ResourceLocation name = entry.getRegistryName();
        if (name == null)
            throw new IllegalArgumentException("A registry name for the following IForgeRegistryEntry ("+entry.getClass().getName()+") could not be found: " + entry);

        return name.toString();
    }

    @Nonnull
    static String[] getNames(IForgeRegistryEntry<?>... blocks) {
        return Stream.of(blocks).map(PatternList::getName).toArray(String[]::new);
    }

    public static PatternList ofResourcePattern(String... regex) {
        return of(Stream.of(regex),true);
    }

    public static PatternList ofResourcePattern(Collection<? extends String> regex) {
        return of(regex.stream(),true);
    }

    public static PatternList of(Stream<? extends String> strings, boolean convertToResourceLocations) {
        if (convertToResourceLocations) //this is done, so that users can continue omitting the Minecraft namespace, etc.
            strings = strings.map(ResourceLocation::new).map(ResourceLocation::toString);
        return new PatternList(strings.map(Pattern::compile).collect(ImmutableList.toImmutableList()));
    }

    private PatternList(ImmutableList<Pattern> patterns) {
        this.patterns = patterns;
    }

    public boolean contains(IForgeRegistryEntry<?> object) {
        return contains(getName(object));
    }

    public boolean contains(String s) {
        return patterns.stream().anyMatch(p -> p.matcher(s).matches());
    }

    public List<String> toList() {
        return patterns.stream().map(Pattern::toString).collect(ImmutableList.toImmutableList());
    }
}
