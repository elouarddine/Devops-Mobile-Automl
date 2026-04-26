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
import com.example.parksmart.databinding.FragmentStartscreenBinding;

public class StartingFragment extends Fragment {

    private static final String TAG = "StartingFragment";
    private FragmentStartscreenBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentStartscreenBinding.inflate(inflater, container, false);
        Log.d(TAG, "Layout fragment_startscreen chargé");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        if (binding.btnStart == null) {
            Log.e(TAG, "ERREUR : btn_start introuvable dans fragment_startscreen.xml");
            return;
        }

        Log.d(TAG, "btn_start trouvé avec succès");
        binding.btnStart.setOnClickListener(v -> {
            Log.d(TAG, "Clic sur btn_start -> navigation vers LoginFragment");
            Navigation.findNavController(v).navigate(R.id.action_starting_to_login);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView() appelé");
    }
}