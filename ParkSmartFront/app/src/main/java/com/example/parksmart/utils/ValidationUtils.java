package com.example.parksmart.utils;

import android.util.Patterns;

public final class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MAX_NAME_LENGTH = 50;

    private ValidationUtils() {
        // Empêche l'instanciation
    }

    // ── Nettoyage ──────────────────────────────────────
    public static String sanitizeText(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String sanitizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    // ── Champs obligatoires ────────────────────────────
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // ── Full name ──────────────────────────────────────
    public static boolean isValidFullName(String fullName) {
        if (!isNotEmpty(fullName)) return false;

        String cleanName = sanitizeText(fullName);

        if (cleanName.length() < 2 || cleanName.length() > MAX_NAME_LENGTH) {
            return false;
        }

        return cleanName.matches("^[a-zA-ZÀ-ÿ' -]+$");
    }

    // ── Email ──────────────────────────────────────────
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;

        String cleanEmail = sanitizeEmail(email);

        if (cleanEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        if (cleanEmail.contains(" ")) {
            return false;
        }

        return Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches();
    }

    // ── Password ───────────────────────────────────────
    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) return false;

        String cleanPassword = password.trim();

        if (cleanPassword.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasUppercase = cleanPassword.matches(".*[A-Z].*");
        boolean hasLowercase = cleanPassword.matches(".*[a-z].*");
        boolean hasDigit = cleanPassword.matches(".*\\d.*");

        return hasUppercase && hasLowercase && hasDigit;
    }

    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    // ── Role ───────────────────────────────────────────
    public static boolean isValidRole(String role) {
        if (!isNotEmpty(role)) return false;

        String cleanRole = role.trim().toLowerCase();
        return cleanRole.equals("admin") || cleanRole.equals("user");
    }
}