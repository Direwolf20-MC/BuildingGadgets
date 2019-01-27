package com.direwolf20.buildinggadgets.common.tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;

public class BlockStateWrapper implements Comparable<BlockStateWrapper> {
    private final IBlockState state;
    private final ImmutableSortedSet<IProperty<?>> propertiesByName;

    public BlockStateWrapper(IBlockState state) {
        Preconditions.checkArgument(state.getBlock().getRegistryName() != null, "Attempted to construct wrapper for unregistered Block!");
        this.state = state;
        this.propertiesByName = ImmutableSortedSet.copyOf(Comparator.comparing(IProperty::getName), state.getProperties());
    }

    public IBlockState getState() {
        return state;
    }

    /**
     * @return The Properties of this Wrappers state ordered by name
     */
    public ImmutableSortedSet<IProperty<?>> getProperties() {
        return propertiesByName;
    }

    /**
     * Compares two BlockStateWrappers by their BlocksRegistryNames and then all their Properties values
     */
    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(@Nonnull BlockStateWrapper o) {
        assert getState().getBlock().getRegistryName() != null;
        assert o.getState().getBlock().getRegistryName() != null;
        int compare = getState().getBlock().getRegistryName().compareTo(o.getState().getBlock().getRegistryName());
        ImmutableList<IProperty<?>> ownProps = getProperties().asList();
        ImmutableList<IProperty<?>> otherProps = o.getProperties().asList();
        for (int i = 0; compare == 0 && i < ownProps.size() && i < otherProps.size(); i++) {
            try {
                compare = ((Comparable<Object>) getState().get(ownProps.get(i))).compareTo(o.getState().get(otherProps.get(i)));
            } catch (Exception e) { //ClassCast exceptions or other unwanted things should not, but might happen
                compare = ownProps.get(i).getName().compareTo(otherProps.get(i).getName());
            }
        }
        if (compare == 0) compare = ownProps.size() - otherProps.size();//if we have more props, then we are the king
        return compare;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     * an execution of a Java application, the {@code hashCode} method
     * must consistently return the same integer, provided no information
     * used in {@code equals} comparisons on the object is modified.
     * This integer need not remain consistent from one execution of an
     * application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     * method, then calling the {@code hashCode} method on each of
     * the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     * according to the {@link Object#equals(Object)}
     * method, then calling the {@code hashCode} method on each of the
     * two objects must produce distinct integer results.  However, the
     * programmer should be aware that producing distinct integer results
     * for unequal objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined
     * by class {@code Object} does return distinct integers for
     * distinct objects. (The hashCode may or may not be implemented
     * as some function of an object's memory address at some point
     * in time.)
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see System#identityHashCode
     */
    @Override
    public int hashCode() {
        assert getState().getBlock().getRegistryName() != null;
        int hash = getState().getBlock().getRegistryName().hashCode();
        for (IProperty<?> property : getProperties()) {
            hash = hash * 31 + getState().get(property).hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockStateWrapper)) return false;

        BlockStateWrapper that = (BlockStateWrapper) o;

        ImmutableList<IProperty<?>> ownProps = getProperties().asList();
        ImmutableList<IProperty<?>> otherProps = that.getProperties().asList();
        for (int i = 0; i < ownProps.size() && i < otherProps.size(); i++) {
            try {
                if (getState().get(ownProps.get(i)).equals(that.getState().get(otherProps.get(i)))) {
                    return true;
                }
            } catch (Exception e) { //ClassCast exceptions or other unwanted things should not, but might happen
                ;//this has to be ignored, as we need to check all
            }
        }
        return false;
    }
}
