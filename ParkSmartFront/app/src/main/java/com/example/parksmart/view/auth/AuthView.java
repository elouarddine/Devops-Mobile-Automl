package com.example.parksmart.view.auth;

import androidx.annotation.StringRes;

public interface AuthView {
    void clearErrors();
    void showMessage(@StringRes int messageResId);
    void setLoading(boolean isLoading);
}