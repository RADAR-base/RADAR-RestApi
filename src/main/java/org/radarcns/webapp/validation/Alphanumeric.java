package org.radarcns.webapp.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;

/**
 * Indicates that a given String should be alphanumeric plus allowed symbols {@code -_./:#@}.
 */
@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = AlphanumericValidator.class)
public @interface Alphanumeric {

}
