package com.example.parksmart.controllers;

import androidx.annotation.NonNull;

import com.example.parksmart.R;
import com.example.parksmart.models.auth.LoginRequest;
import com.example.parksmart.models.auth.LoginResponse;
import com.example.parksmart.network.ApiClient;
import com.example.parksmart.network.ApiService;
import com.example.parksmart.utils.SessionManager;
import com.example.parksmart.utils.ValidationUtils;
import com.example.parksmart.view.auth.LoginView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginController {

    private final LoginView view;
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public LoginController(LoginView view, SessionManager sessionManager) {
        this(view, sessionManager, ApiClient.getClient().create(ApiService.class));
    }

    public LoginController(LoginView view, SessionManager sessionManager, ApiService apiService) {
        this.view = view;
        this.sessionManager = sessionManager;
        this.apiService = apiService;
    }

    public void onLoginClicked(String rawEmail, String rawPassword, boolean rememberMe) {
        view.clearErrors();

        String email = ValidationUtils.sanitizeEmail(rawEmail);
        String password = rawPassword == null ? "" : rawPassword.trim();

        if (!ValidationUtils.isNotEmpty(email)) {
            view.showEmailError(R.string.login_error_email_required);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            view.showEmailError(R.string.login_error_identifier_invalid);
            return;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            view.showPasswordError(R.string.login_error_password_required);
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            view.showPasswordError(R.string.login_error_password_invalid);
            return;
        }

        view.setLoading(true);

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                                   @NonNull Response<LoginResponse> response) {
                view.setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    String token = loginResponse.getAccessToken();
                    String role = loginResponse.getRole();
                    String userId = loginResponse.getUserId();
                    String fullName = loginResponse.getFullName();

                    if (!ValidationUtils.isNotEmpty(token) || !ValidationUtils.isNotEmpty(role)) {
                        view.showMessage(R.string.login_error_server);
                        return;
                    }

                    sessionManager.saveSession(token, role, userId);
                    sessionManager.saveUserName(fullName == null ? "" : fullName);
                    sessionManager.setRememberMe(rememberMe);

                    view.showMessage(R.string.Login_success_account_connexion);
                    view.navigateToSuccessScreen();
                    return;
                }

                handleErrorResponse(response.code());
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                view.setLoading(false);
                view.showMessage(R.string.login_error_network);
            }
        });
    }

    private void handleErrorResponse(int code) {
        if (code == 400 || code == 401) {
            view.showMessage(R.string.login_error_wrong_credentials);
        } else if (code == 404) {
            view.showEmailError(R.string.login_error_account_not_found);
        } else {
            view.showMessage(R.string.login_error_server);
        }
    }
}