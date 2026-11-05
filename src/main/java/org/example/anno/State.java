package org.example.anno;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.validation.StateValidation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {StateValidation.class })
@Target({ FIELD})
@Retention(RUNTIME)
public @interface State {
    String message() default "{文章的state必须为已发布/草稿}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
