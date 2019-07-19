package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Represents an operation which can be performed by an {@link ITemplateTransaction} in order to modify a given {@link ITemplate}.
 * The following 3 types of operations are supported:
 * <ul>
 * <li>Transforming or removing Block-Positions
 * <li>Transforming or removing Block-Data
 * <li>Attaching new Blocks and the Data to this Template.
 * </ul>
 */
public interface ITransactionOperator {
    /**
     * Represents characteristics (operations) an {@code ITransactionOperator} might have.
     * <p>
     * <b>It is not guaranteed that the order of this {@link Enum}'s values will remain consistend nor is
     * it guaranteed that no values will be added.</b> Users switch casing over this enum should therefore always provide
     * a default case. Users may furthermore not depend on this Enum's ordinal for serialisation. <br>
     * However it is guaranteed, that the id field will remain the same across all non-major releases.
     */
    enum Characteristic {
        /**
         * This {@code Characteristic} represents the ability of an {@code ITransactionOperator} to transform the
         * {@link TemplateHeader} of a given {@link ITemplate}.
         */
        TRANSFORM_HEADER(0),
        /**
         * This {@code Characteristic} represents the ability of an {@code ITransactionOperator} to transform positions via
         * {@link #transformPos(ITransactionExecutionContext, BlockPos, BlockData)}.
         */
        TRANSFORM_POSITION(1),
        /**
         * This {@code Characteristic} represents the ability of an {@code ITransactionOperator} to transform data via
         * {@link #transformData(ITransactionExecutionContext, BlockData)}.
         */
        TRANSFORM_DATA(2),
        /**
         * This {@code Characteristic} represents the ability of an {@code ITransactionOperator} to create new data via
         * {@link #createPos(ITransactionExecutionContext)} and {@link #createDataForPos(ITransactionExecutionContext, BlockPos)}.
         */
        CREATE_DATA(3);

        private final byte id;

        Characteristic(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return id;
        }
    }

    /**
     * Allows this {@code ITransactionOperator} to add arbitrary {@link BlockPos} to a given {@link ITemplate}. Notice that returning a
     * {@link BlockPos} which is already contained in a given {@link ITemplate} will effectively replace the previous data with whatever
     * {@link BlockData} {@link #createDataForPos(ITransactionExecutionContext, BlockPos)} returns.
     * @return A new {@link BlockPos} to add to a given {@link ITemplate} or null if no more positions should be added
     */
    @Nullable
    default BlockPos createPos(ITransactionExecutionContext context) {
        return null;
    }

    /**
     * This method creates arbitrary {@link BlockData} for a given {@link BlockPos} returned by {@link #createPos(ITransactionExecutionContext)},
     * overwriting previous data in the process.
     * <p>
     * Notice that even though you may return null from this Method, doing so for an {@link BlockPos} which is not already present in
     * the backing {@link ITemplate} is considered an Exceptional-Condition and will result in Runtime-Failure of the executing
     * {@link ITemplateTransaction}.
     * @param context The {@link ITransactionExecutionContext} used during this transaction.
     * @param pos The pos for which to create {@link BlockData} for.
     * @return A new {@link BlockData} for a given {@link BlockPos} or null if none can or should be created.
     * @implNote The default implementation throws {@link UnsupportedOperationException} as this cannot be supported. Bear in mind that returning a
     *         non-null value from {@link #createPos(ITransactionExecutionContext)} will require you to overwrite this Method, as it will fail otherwise.
     */
    @Nullable
    default BlockData createDataForPos(ITransactionExecutionContext context, BlockPos pos) {
        throw new UnsupportedOperationException("Default implementation does not support creating BlockData!");
    }

    /**
     * @param context The {@link ITransactionExecutionContext} used during this transaction.
     * @param header The {@link TemplateHeader} to transform. Will never be null.
     * @return The transformed {@link TemplateHeader}. This may not be null, as an {@link ITemplate} may never be without a header.
     */
    default TemplateHeader transformHeader(ITransactionExecutionContext context, TemplateHeader header) {
        return header;
    }

    /**
     * Performs arbitrary operations on the given {@link BlockData}. <br>
     * Will be called after {@link #createPos(ITransactionExecutionContext)} and {@link #createDataForPos(ITransactionExecutionContext, BlockPos)} are finished executing.
     * @param context The {@link ITransactionExecutionContext} used during this transaction.
     * @param data The {@link BlockData} to be transformed by this {@code ITransactionOperator}'s
     * @return The transformed {@link BlockData} or null to remove it <b>and all referencing positions</b> from the Template.
     * @implNote The default implementation is the identity function.
     */
    @Nullable
    default BlockData transformData(ITransactionExecutionContext context, BlockData data) {
        return data;
    }

    /**
     * Performs arbitrary operations on the given {@link BlockPos}. The {@link BlockData} associated with this position is passed into this Method
     * in order to provide more context. <br>
     * Will be called after {@link #transformData(ITransactionExecutionContext, BlockData)} is finished executing for the {@link BlockData} associated with this {@link BlockPos}.
     * @param context The {@link ITransactionExecutionContext} used during this transaction.
     * @param pos The position to transform
     * @param data The {@link BlockData} associated with this position.
     * @return The new transformed {@link BlockPos} or null if it should be removed from the given {@link ITemplate}
     * @implNote The default implementation just returns the given position
     */
    @Nullable
    default BlockPos transformPos(ITransactionExecutionContext context, BlockPos pos, BlockData data) {
        return pos;
    }

    /**
     * Returns an {@link Set} of {@link Characteristic} to indicate which operations are performed by this {@code ITransactionOperator}.
     * All {@link Characteristic} returned by this Method are guaranteed to be executed.
     * @return A {@link Set} representing the {@link Characteristic} of this {@code ITransactionOperator}
     */
    default Set<Characteristic> characteristics() {
        return ImmutableSet.of();
    }
}
