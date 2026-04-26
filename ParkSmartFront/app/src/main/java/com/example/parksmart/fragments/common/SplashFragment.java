package com.example.parksmart.fragments.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.parksmart.R;
import com.example.parksmart.databinding.FragmentSplashBinding;

@SuppressLint("CustomSplashScreen")
public class SplashFragment extends Fragment {

    private static final String TAG = "SplashFragment";
    private static final long SPLASH_DELAY = 800;

    private FragmentSplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable navigateRunnable = () -> {
        if (binding != null) {
            Log.d(TAG, "Splash terminé -> navigation vers StartingFragment");
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_splash_to_starting);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "Layout fragment_splash chargé");

        binding.ivSplashLogo.setAlpha(0f);
        binding.ivSplashLogo.animate()
                .alpha(1f)
                .setDuration(1200)
                .start();

        handler.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(navigateRunnable);
        binding = null;
    }
}