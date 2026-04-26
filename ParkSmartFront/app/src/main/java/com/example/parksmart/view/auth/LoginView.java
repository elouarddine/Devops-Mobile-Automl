package com.example.parksmart.view.auth;

import androidx.annotation.StringRes;

public interface LoginView extends AuthView {
    void showEmailError(@StringRes int errorResId);
    void showPasswordError(@StringRes int errorResId);
    void navigateToSuccessScreen();
}