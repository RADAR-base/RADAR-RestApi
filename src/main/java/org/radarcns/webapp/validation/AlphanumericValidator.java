package org.radarcns.webapp.validation;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks that a given String should be alphanumeric plus allowed symbols {@code -_./:#@}.
 */
public class AlphanumericValidator implements ConstraintValidator<Alphanumeric, String> {

    private static final Pattern USER_PATTERN = Pattern.compile("[a-zA-Z0-9-_./:#@]+");

    @Override
    public void initialize(Alphanumeric constraintAnnotation) {
        // no initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && USER_PATTERN.matcher(value).matches();
    }
}
