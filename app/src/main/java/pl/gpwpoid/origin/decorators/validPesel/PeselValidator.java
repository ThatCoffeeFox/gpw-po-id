package pl.gpwpoid.origin.decorators.validPesel;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PeselValidator implements ConstraintValidator<ValidPesel, String> {

    private static final int[] WEIGHTS = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
    private static final int PESEL_LENGTH = 11;

    @Override
    public void initialize(ValidPesel constraintAnnotation) {
    }

    @Override
    public boolean isValid(String pesel, ConstraintValidatorContext context) {
        if (pesel == null) {
            return true;
        }

        if (!pesel.matches("^\\d{" + PESEL_LENGTH + "}$")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < PESEL_LENGTH - 1; i++) {
            sum += Character.getNumericValue(pesel.charAt(i)) * WEIGHTS[i];
        }

        int controlSum = sum % 10;
        int controlDigitFromPesel = Character.getNumericValue(pesel.charAt(10));

        int expectedControlDigit = (10 - controlSum) % 10;

        return expectedControlDigit == controlDigitFromPesel;
    }
}
