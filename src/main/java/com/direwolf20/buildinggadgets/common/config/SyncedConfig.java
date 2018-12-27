package com.direwolf20.buildinggadgets.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SyncedConfig {
    /**
     * This name must be the same on both client and server.
     * Defaults to the field name
     * @return The Name Id used to sync this field
     */
    String value() default "";

    /**
     * Can be used for specifying a FieldMapper, in order to have some kind of complex Type stored in the Config.
     * Necessary for sync.
     * Make sure that you are referring to a Mapper of the desired Type, as using incompatible mappers can have unexpected consequences!
     * @return The id of the registered FieldMapper to be used for transforming this field
     */
    String mapperId() default  "";

}
