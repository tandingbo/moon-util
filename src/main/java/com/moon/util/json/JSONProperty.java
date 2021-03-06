package com.moon.util.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author benshaoye
 * @date 2018/9/14
 */
@Target(ElementType.FIELD)
public @interface JSONProperty {

    /**
     * 重命名
     *
     * @return
     */
    String name() default "";

    /**
     * 给 Date 和 Number 使用
     *
     * @return
     */
    String format() default "";

    boolean stringify() default true;

    boolean parse() default true;
}
