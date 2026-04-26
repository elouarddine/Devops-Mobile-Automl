package com.example.parksmart.fragments.common;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.parksmart.R;
import com.example.parksmart.databinding.FragmentSuccessBinding;
import com.example.parksmart.utils.SessionManager;

public class SuccessFragment extends Fragment {
    private static final String TAG = "user/admin";

    private FragmentSuccessBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        afficherContenuSelonRole();

        handler.postDelayed(() -> {
            String role = sessionManager.getRole();

            if ("admin".equalsIgnoreCase(role)) {
                Log.d(TAG, "Passage à la page de admin");

                Navigation.findNavController(view)
                        .navigate(R.id.action_success_to_admin_home);
                Log.d(TAG, "Passage à la page de admin avec succés");

            } else {
                Log.d(TAG, "Passage à la page de user");

                Navigation.findNavController(view)
                        .navigate(R.id.action_success_to_user_home);

                Log.d(TAG, "Passage à la page de user avec succés");

            }
        }, 500);
    }

    private void afficherContenuSelonRole() {
        String fullName = sessionManager.getUserName();
        String role = sessionManager.getRole();

        if (TextUtils.isEmpty(fullName)) {
            binding.tvSuccessTitle.setText(getString(R.string.success_title_no_name));
        } else {
            binding.tvSuccessTitle.setText(getString(R.string.success_title, fullName));
        }

        if ("admin".equalsIgnoreCase(role)) {
            binding.successRoot.setBackgroundResource(R.drawable.bg_success_admin);
            binding.tvSuccessRoleSubtitle.setText(R.string.success_admin_subtitle);
            binding.tvSuccessMessage.setText(R.string.success_admin_message);
        } else {
            binding.successRoot.setBackgroundResource(R.drawable.bg_success_user);
            binding.tvSuccessRoleSubtitle.setText(R.string.success_user_subtitle);
            binding.tvSuccessMessage.setText(R.string.success_user_message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}