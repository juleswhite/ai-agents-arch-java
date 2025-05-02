package com.juleswhite.module1;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterTool {
    /**
     * The name of the tool. If not specified, the method name will be used.
     */
    String name() default "";

    /**
     * The description of the tool. If not specified, the method's Javadoc will be used.
     */
    String description() default "";

    /**
     * Whether this tool terminates the agent loop when called.
     */
    boolean terminal() default false;

    /**
     * Tags for categorizing this tool.
     */
    String[] tags() default {};

    /**
     * Optional parameters schema the tool, auto-discovered from the method params if not present.
     */
    String parameters() default "";
}