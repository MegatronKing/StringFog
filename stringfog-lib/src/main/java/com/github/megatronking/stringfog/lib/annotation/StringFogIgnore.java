package com.github.megatronking.stringfog.lib.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * This annotation could keep no fog for string.
 *
 * @author Megatron King
 * @since 2017/3/8 8:41
 */
@Retention(RetentionPolicy.CLASS)
@Target(value={TYPE})
public @interface StringFogIgnore {

}
