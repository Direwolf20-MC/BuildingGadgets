package com.direwolf20.buildinggadgets.common.building.placement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * When the annotation is applied to a method or a field, the given value, should not be copied as a reference anywhere.
 * This means the usage of the value should be kept pure. It is not guaranteed that errors and unrecoverable errors will occur.
 * In other words, it must not keep a direct reference the value, but making a copy is fine.
 * </p>
 *
 * <p>
 * When the annotation is applied to a parameter, it means the method will kept the usage of that parameter pure.
 * In other words, it is guaranteed that it will not copy a direct reference to the parameter, but making a copy of the parameter is fine.
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
