package com.h.aop;

import java.lang.annotation.*;


/**
 * 拦截controller
 * @author yehan
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionNeed {
    String value() default "";
    boolean admin() default true;
}
