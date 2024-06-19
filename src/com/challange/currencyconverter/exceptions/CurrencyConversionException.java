package com.challange.currencyconverter.exceptions;

public class CurrencyConversionException extends RuntimeException {
    private String message;

    public CurrencyConversionException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}