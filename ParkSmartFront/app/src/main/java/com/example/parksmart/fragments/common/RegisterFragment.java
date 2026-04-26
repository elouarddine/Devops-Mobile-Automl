package com.example.parksmart.fragments.common;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.parksmart.R;
import com.example.parksmart.controllers.RegisterController;
import com.example.parksmart.databinding.FragmentRegisterBinding;
import com.example.parksmart.utils.FeedbackManager;
import com.example.parksmart.view.auth.RegisterView;

public class RegisterFragment extends Fragment implements RegisterView {

    private static final String TAG = "RegisterFragment";

    private FragmentRegisterBinding binding;
    private RegisterController registerController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerController = new RegisterController(this);

        binding.btnRegister.setOnClickListener(v -> {
            String fullName = getTextSafely(binding.etFullName);
            String email = getTextSafely(binding.etEmail);
            String password = getTextSafely(binding.etPassword);
            String confirmPassword = getTextSafely(binding.etConfirmPassword);
            String role = getSelectedRole();

            Log.d(TAG, "Clic sur btn_register");
            registerController.onRegisterClicked(fullName, email, password, confirmPassword, role);
        });

        binding.tvGoLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_register_to_login)
        );

        binding.tilPassword.setErrorIconDrawable(null);
        binding.tilConfirmPassword.setErrorIconDrawable(null);
    }

    private String getTextSafely(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    private String getSelectedRole() {
        int checkedId = binding.rgAccountType.getCheckedRadioButtonId();

        if (checkedId == R.id.rb_user) {
            return "user";
        }

        if (checkedId == R.id.rb_admin) {
            return "admin";
        }

        return "";
    }

    @Override
    public void clearErrors() {
        FeedbackManager.clearAllFieldErrors(
                binding.tilFullName,
                binding.tilEmail,
                binding.tilPassword,
                binding.tilConfirmPassword
        );

        binding.tvAccountType.setTextColor(ContextCompat.getColor(requireContext(), R.color.ps_text));
    }

    @Override
    public void showFullNameError(int errorResId) {
        FeedbackManager.showFieldError(binding.tilFullName, requireContext(), errorResId);
    }

    @Override
    public void showEmailError(int errorResId) {
        FeedbackManager.showFieldError(binding.tilEmail, requireContext(), errorResId);
    }

    @Override
    public void showPasswordError(int errorResId) {
        FeedbackManager.showFieldError(binding.tilPassword, requireContext(), errorResId);
    }

    @Override
    public void showConfirmPasswordError(int errorResId) {
        FeedbackManager.showFieldError(binding.tilConfirmPassword, requireContext(), errorResId);
    }

    @Override
    public void showRoleError(int errorResId) {
        binding.tvAccountType.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.ps_error)
        );
        FeedbackManager.showToast(requireContext(), errorResId);
    }

    @Override
    public void showMessage(int messageResId) {
        FeedbackManager.showToast(requireContext(), messageResId);
    }

    @Override
    public void setLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        binding.etFullName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
        binding.rbUser.setEnabled(!isLoading);
        binding.rbAdmin.setEnabled(!isLoading);

        binding.btnRegister.setAlpha(isLoading ? 0.5f : 1f);
    }

    @Override
    public void navigateToLoginScreen() {
        Log.d(TAG, "Inscription réussie -> navigation vers LoginFragment");
        Navigation.findNavController(requireView())
                .navigate(R.id.action_register_to_login);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}