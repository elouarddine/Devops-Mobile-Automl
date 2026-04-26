package com.example.parksmart.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.textfield.TextInputLayout;

public final class FeedbackManager {

    private FeedbackManager() {
        // Pour Empêcher l'instanciation
    }

    public static void showToast(@NonNull Context context, @StringRes int messageResId) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show();
    }

    public static void showToast(@NonNull Context context, @NonNull String message) {
        if (TextUtils.isEmpty(message.trim())) {
            return;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showFieldError(@NonNull TextInputLayout inputLayout,
                                      @NonNull Context context,
                                      @StringRes int errorResId) {
        inputLayout.setError(context.getString(errorResId));
        inputLayout.setErrorEnabled(true);

        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().requestFocus();
        }
    }

    public static void showFieldError(@NonNull TextInputLayout inputLayout,
                                      @NonNull String errorMessage) {
        if (TextUtils.isEmpty(errorMessage.trim())) {
            return;
        }

        inputLayout.setError(errorMessage);
        inputLayout.setErrorEnabled(true);

        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().requestFocus();
        }
    }

    public static void clearFieldError(@NonNull TextInputLayout inputLayout) {
        inputLayout.setError(null);
        inputLayout.setErrorEnabled(false);
    }

    public static void clearAllFieldErrors(TextInputLayout... inputLayouts) {
        if (inputLayouts == null) {
            return;
        }

        for (TextInputLayout inputLayout : inputLayouts) {
            if (inputLayout != null) {
                clearFieldError(inputLayout);
            }
        }
    }
}