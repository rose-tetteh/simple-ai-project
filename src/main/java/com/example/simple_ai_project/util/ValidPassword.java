package com.example.simple_ai_project.util;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 12 characters with at least one uppercase, one lowercase, one digit, and one special character.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}