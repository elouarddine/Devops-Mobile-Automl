package com.example.parksmart.controllers;

import androidx.annotation.NonNull;

import com.example.parksmart.R;
import com.example.parksmart.models.auth.RegisterRequest;
import com.example.parksmart.models.auth.RegisterResponse;
import com.example.parksmart.network.ApiClient;
import com.example.parksmart.network.ApiService;
import com.example.parksmart.utils.ValidationUtils;
import com.example.parksmart.view.auth.RegisterView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterController {

    private final RegisterView view;
    private final ApiService apiService;

    public RegisterController(RegisterView view) {
        this(view, ApiClient.getClient().create(ApiService.class));
    }

    public RegisterController(RegisterView view, ApiService apiService) {
        this.view = view;
        this.apiService = apiService;
    }

    public void onRegisterClicked(String rawFullName, String rawEmail, String rawPassword, String rawConfirmPassword, String rawRole) {

        view.clearErrors();

        String fullName = ValidationUtils.sanitizeText(rawFullName);
        String email = ValidationUtils.sanitizeEmail(rawEmail);
        String password = rawPassword == null ? "" : rawPassword.trim();
        String confirmPassword = rawConfirmPassword == null ? "" : rawConfirmPassword.trim();
        String role = rawRole == null ? "" : rawRole.trim().toLowerCase();

        if (!ValidationUtils.isNotEmpty(fullName)) {
            view.showFullNameError(R.string.register_error_fullname_required);
            return;
        }

        if (!ValidationUtils.isValidFullName(fullName)) {
            view.showFullNameError(R.string.register_error_fullname_invalid);
            return;
        }

        if (!ValidationUtils.isNotEmpty(email)) {
            view.showEmailError(R.string.register_error_email_required);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            view.showEmailError(R.string.register_error_email_invalid);
            return;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            view.showPasswordError(R.string.register_error_password_required);
            return;
        }

        if (!ValidationUtils.isNotEmpty(confirmPassword)) {
            view.showConfirmPasswordError(R.string.register_error_confirm_password_required);
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            view.showPasswordError(R.string.register_error_password_invalid);
            return;
        }

        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            view.showConfirmPasswordError(R.string.register_error_password_mismatch);
            return;
        }

        if (!ValidationUtils.isValidRole(role)) {
            view.showRoleError(R.string.register_error_account_type_required);
            return;
        }

        view.setLoading(true);

        RegisterRequest request = new RegisterRequest(fullName, email, password, role);

        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call,
                                   @NonNull Response<RegisterResponse> response) {
                view.setLoading(false);

                if (response.isSuccessful()) {
                    view.showMessage(R.string.register_success_account_created);
                    view.navigateToLoginScreen();
                    return;
                }

                handleErrorResponse(response.code());
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                view.setLoading(false);
                view.showMessage(R.string.register_error_network);
            }
        });
    }

    private void handleErrorResponse(int code) {
        if (code == 409) {
            view.showEmailError(R.string.register_error_email_already_exists);
        } else {
            view.showMessage(R.string.register_error_server);
        }
    }
}