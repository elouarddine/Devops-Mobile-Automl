package com.example.parksmart.repository.register;

public interface RegisterRepository {

    interface RegisterCallback {
        void onSuccess();
        void onEmailAlreadyExists();
        void onNetworkError();
        void onServerError();
    }

    void register(String fullName,String email,String password,String role,RegisterCallback callback);
}