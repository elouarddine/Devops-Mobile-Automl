package com.example.parksmart.fragments.common;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.parksmart.R;
import com.example.parksmart.controllers.LoginController;
import com.example.parksmart.databinding.FragmentLoginBinding;
import com.example.parksmart.utils.FeedbackManager;
import com.example.parksmart.utils.SessionManager;
import com.example.parksmart.view.auth.LoginView;

public class LoginFragment extends Fragment implements LoginView {

    private static final String TAG = "LoginFragment";

    private FragmentLoginBinding binding;
    private LoginController loginController;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        loginController = new LoginController(this, sessionManager);

        binding.btnLogin.setOnClickListener(v -> {
            String email = getTextSafely(binding.etIdentifier);
            String password = getTextSafely(binding.etPassword);
            boolean rememberMe = binding.cbRememberMe.isChecked();

            Log.d(TAG, "Clic sur btn_login");
            loginController.onLoginClicked(email, password, rememberMe);
        });

        binding.tvGoRegister.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_register);
        });
    }

    private String getTextSafely(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    @Override
    public void clearErrors() {
        FeedbackManager.clearAllFieldErrors(binding.tilIdentifier, binding.tilPassword);
    }

    @Override
    public void showEmailError(int errorResId) {
        FeedbackManager.showFieldError(binding.tilIdentifier, requireContext(), errorResId);
    }

    @Override
    public void showPasswordError(int errorResId) {
        FeedbackManager.showFieldError(binding.tilPassword, requireContext(), errorResId);
    }

    @Override
    public void showMessage(int messageResId) {
        FeedbackManager.showToast(requireContext(), messageResId);
    }

    @Override
    public void setLoading(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.etIdentifier.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.cbRememberMe.setEnabled(!isLoading);
        binding.btnLogin.setAlpha(isLoading ? 0.5f : 1f);
    }

    @Override
    public void navigateToSuccessScreen() {
        Log.d(TAG, "Connexion réussie -> navigation vers SuccessFragment");
        Navigation.findNavController(requireView())
                .navigate(R.id.action_login_to_success);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}