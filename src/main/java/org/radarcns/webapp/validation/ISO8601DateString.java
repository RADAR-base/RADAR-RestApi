package org.radarcns.webapp.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;

/**
 * Checks that a given String should be ISO8601 compatible Date.
 */
@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ISO8601DateStringValidator.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public @interface ISO8601DateString {

}
