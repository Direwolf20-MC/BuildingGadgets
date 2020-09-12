package com.direwolf20.buildinggadgets.common.tainted;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be attached to any method, class or var that is inferred to be tainted. This will be used to
 * automatically get a good understanding of how much code needs to be rewritten and what systems are
 * viable to be moved over to the new system
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Tainted {
    String reason();
    String resolve() default "Replace with a new system";
}
