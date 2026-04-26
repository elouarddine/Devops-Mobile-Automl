package com.example.parksmart.repository.auth;

public interface AuthRepository {

    interface LoginCallback {
        void onSuccess(String fullName, String role);
        void onAccountNotFound();
        void onWrongPassword();
        void onWrongCredentials();
        void onNetworkError();
        void onServerError();
    }

    void login(String email, String password, boolean rememberMe, LoginCallback callback);
}