package com.example.wallet.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AssetSymbolValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAssetSymbol {

    String message() default "Symbol must have 3-4 uppercase characters (e.g.: BTC, ETH)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
