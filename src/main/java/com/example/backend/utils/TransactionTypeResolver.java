package com.example.backend.utils;

import com.example.backend.entities.TransactionDirection;

import java.util.Locale;

public final class TransactionTypeResolver {

    private TransactionTypeResolver() {
    }

    public static String resolvePaymentType(String paymentType, String legacyTransactionType) {
        if (hasText(paymentType)) {
            return paymentType.trim();
        }
        if (hasText(legacyTransactionType)) {
            return legacyTransactionType.trim();
        }
        return null;
    }

    public static TransactionDirection resolveDirection(TransactionDirection transactionDirection, String paymentType) {
        if (transactionDirection != null) {
            return transactionDirection;
        }
        return deriveDirectionFromPaymentType(paymentType);
    }

    public static TransactionDirection parseDirection(String rawDirection) {
        if (!hasText(rawDirection)) {
            return null;
        }
        String normalized = rawDirection.trim().toUpperCase(Locale.ROOT);
        if ("CREDIT".equals(normalized)) {
            return TransactionDirection.CREDIT;
        }
        if ("DEBIT".equals(normalized)) {
            return TransactionDirection.DEBIT;
        }
        return null;
    }

    public static TransactionDirection deriveDirectionFromPaymentType(String paymentType) {
        if (!hasText(paymentType)) {
            return TransactionDirection.DEBIT;
        }
        String normalized = paymentType.trim().toUpperCase(Locale.ROOT);
        if ("INCOME".equals(normalized) || "CREDIT".equals(normalized)) {
            return TransactionDirection.CREDIT;
        }
        return TransactionDirection.DEBIT;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
