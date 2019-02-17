package com.direwolf20.buildinggadgets.common.tools.gadget.placement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * When the annotation is applied to a method or a field, the given value, should not be copied as a reference anywhere.
 * This means the usage of the value should be kept pure. It is not guaranteed that errors and unrecoverable errors will occur.
 * </p>
 *
 * <p>
 * When the annotation is applied to a parameter, it means the method will kept the usage of that parameter pure.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.PARAMETER
})
public @interface NoBorrow {

}
