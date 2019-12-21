package com.direwolf20.buildinggadgets.common.config.utils;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoSync {
    /**
     * This name must be the same on both client and server.
     * @implSpec Defaults to the field name
     * @return The Name Id used to sync this field
     */
    String value() default "";

    /**
     * Can be used for specifying a FieldMapper, in order to have some kind of complex Type stored in the {@link SyncedConfig}.
     * In order to make use of this, first register a {@link com.direwolf20.buildinggadgets.common.config.utils.FieldMapper} with the desired id.
     * You can then switch the Type of the Field to the Field Type mapped by the Mapper
     * @implNote <b>Make sure that you are referring to a Mapper of the desired Type, as using incompatible mappers can have unexpected consequences!</b>
     * @return The id of the registered FieldMapper to be used for transforming this field
     */
    String mapperId() default  "";

}
