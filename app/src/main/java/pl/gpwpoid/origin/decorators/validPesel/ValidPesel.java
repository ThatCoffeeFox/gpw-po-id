package pl.gpwpoid.origin.decorators.validPesel;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PeselValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPesel {
    String message() default "Nieprawidłowy numer PESEL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
