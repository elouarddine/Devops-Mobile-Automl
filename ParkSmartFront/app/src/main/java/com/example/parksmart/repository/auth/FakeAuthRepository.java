package com.example.parksmart.repository.auth;

import android.os.Handler;
import android.os.Looper;

public class FakeAuthRepository implements AuthRepository {

    @Override
    public void login(String email, String password, boolean rememberMe, LoginCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if ("admin@parksmart.com".equalsIgnoreCase(email) && "Admin123".equals(password)) {
                callback.onSuccess("Admin ParkSmart", "admin");
                return;
            }

            if ("user@parksmart.com".equalsIgnoreCase(email) && "User1234".equals(password)) {
                callback.onSuccess("Jean Dupont", "user");
                return;
            }

            callback.onWrongCredentials();
        }, 1200);
    }
}