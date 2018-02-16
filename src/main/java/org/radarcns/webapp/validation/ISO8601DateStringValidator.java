package org.radarcns.webapp.validation;

import java.time.format.DateTimeParseException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.radarcns.util.RadarConverter;

/**
 * Checks that a given String should be ISO8601 compatible Date.
 */
public class ISO8601DateStringValidator implements ConstraintValidator<ISO8601DateString, String> {

    @Override
    public void initialize(ISO8601DateString constraintAnnotation) {
        // no initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            if (value != null) {
                RadarConverter.getISO8601(value);
                return true;
            }
            return false;
        } catch (DateTimeParseException exe) {
            return false;
        }
    }
}
