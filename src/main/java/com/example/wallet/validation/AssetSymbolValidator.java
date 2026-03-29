package com.example.wallet.validation;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class AssetSymbolValidator implements ConstraintValidator<ValidAssetSymbol, String> {

    @Value("${application.validation.asset-symbol-pattern}")
    private String assetSymbolPattern;

    private Pattern pattern;

    @Override
    public void initialize(ValidAssetSymbol annotation) {
        pattern = Pattern.compile(assetSymbolPattern);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return pattern.matcher(value).matches();
    }
}
