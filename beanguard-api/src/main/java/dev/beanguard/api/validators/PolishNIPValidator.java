package dev.beanguard.api.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PolishNIPValidator implements ConstraintValidator<PolishNIP, String> {

    private static final Pattern NIP_PATTERN = Pattern.compile("\\d{10}");
    private static final int[] WEIGHTS = {6, 5, 7, 2, 3, 4, 5, 6, 7};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || !NIP_PATTERN.matcher(value).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (value.charAt(i) - '0') * WEIGHTS[i];
        }

        int checksum = sum % 11;
        int controlDigit = value.charAt(9) - '0';

        return checksum == controlDigit;
    }
}
