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
}
