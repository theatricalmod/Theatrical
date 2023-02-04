package dev.imabad.theatrical.config.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TheatricalConfigItem {
    String[] name() default {};

    Class<?>[] type() default {};

    String[] minValue() default {};

    String[] maxValue() default {};
}
