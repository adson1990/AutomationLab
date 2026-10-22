package br.com.automationlab.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CpfValido, String> {
    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || cpf.isBlank()) {
            return true;
        }

        if (!cpf.matches("[0-9.\\-/\\s]+")) {
            return false;
        }
        String digits = cpf.replaceAll("[.\\-/\\s]", "");
        if (!digits.matches("\\d{11}") || digits.chars().distinct().count() == 1) {
            return false;
        }

        return hasValidCheckDigit(digits, 9) && hasValidCheckDigit(digits, 10);
    }

    private boolean hasValidCheckDigit(String cpf, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (length + 1 - i);
        }
        int remainder = (sum * 10) % 11;
        int expected = remainder == 10 ? 0 : remainder;
        return expected == Character.getNumericValue(cpf.charAt(length));
    }
}
