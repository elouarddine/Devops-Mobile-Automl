package com.example.parksmart.view.auth;

import androidx.annotation.StringRes;

public interface RegisterView extends AuthView {
    void showFullNameError(@StringRes int errorResId);
    void showEmailError(@StringRes int errorResId);
    void showPasswordError(@StringRes int errorResId);
    void showConfirmPasswordError(@StringRes int errorResId);
    void showRoleError(@StringRes int errorResId);
    void navigateToLoginScreen();
}