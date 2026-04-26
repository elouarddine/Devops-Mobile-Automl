package com.example.parksmart.repository.register;

import android.os.Handler;
import android.os.Looper;

public class FakeRegisterRepository implements RegisterRepository {

    @Override
    public void register(String fullName, String email, String password, String role, RegisterCallback callback) {

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if ("user@parksmart.com".equalsIgnoreCase(email)
                    || "admin@parksmart.com".equalsIgnoreCase(email)
                    || "jean.dupont@gmail.com".equalsIgnoreCase(email)) {
                callback.onEmailAlreadyExists();
                return;
            }

            callback.onSuccess();
        }, 1200);
    }
}