package com.example.parksmart.fragments.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.parksmart.R;
import com.example.parksmart.databinding.FragmentAdminHomeBinding;
import com.example.parksmart.models.admin.AdminDashboardData;
import com.example.parksmart.models.admin.AdminJobItem;
import com.example.parksmart.models.admin.AdminResultsData;
import com.example.parksmart.models.home.UserProfile;
import com.example.parksmart.repository.admin.AdminRepository;
import com.example.parksmart.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    private AdminRepository repository;
    private SessionManager sessionManager;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.###");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        repository = new AdminRepository(requireContext());

        setupClicks();
        preloadLocalProfile();
        refreshAll();
    }

    private void setupClicks() {
        binding.btnAdminRefresh.setOnClickListener(v -> refreshAll());
        binding.btnImportDataset.setOnClickListener(v -> onImportDataset());
        binding.btnLaunchTrain.setOnClickListener(v -> launchTrain());
        binding.btnLaunchEvaluate.setOnClickListener(v -> launchEvaluate());
        binding.btnLoadEvalResults.setOnClickListener(v -> loadResults("eval"));
        binding.btnLoadTrainResults.setOnClickListener(v -> loadResults("train"));
        binding.btnAdminLogout.setOnClickListener(v -> logout());

        binding.btnNavDashboard.setOnClickListener(v -> scrollToSection(binding.cardDashboardSection));
        binding.btnNavData.setOnClickListener(v -> scrollToSection(binding.cardDataSection));
        binding.btnNavResults.setOnClickListener(v -> scrollToSection(binding.cardResultsSection));
        binding.btnNavJobs.setOnClickListener(v -> scrollToSection(binding.cardJobsSection));
        binding.btnNavProfile.setOnClickListener(v -> scrollToSection(binding.cardProfileSection));
    }

    private void scrollToSection(View target) {
        if (target == null) return;
        binding.adminScrollView.post(() -> binding.adminScrollView.smoothScrollTo(0, target.getTop()));
    }

    private void preloadLocalProfile() {
        String name = sessionManager.getUserName();
        String role = sessionManager.getRole();
        binding.tvAdminGreeting.setText(TextUtils.isEmpty(name) ? getString(R.string.admin_title) : name);
        binding.tvAdminProfileName.setText(TextUtils.isEmpty(name) ? getString(R.string.admin_title) : name);
        binding.tvAdminEmail.setText(TextUtils.isEmpty(name) ? getString(R.string.admin_subtitle) : name);
        binding.tvAdminRole.setText(TextUtils.isEmpty(role) ? getString(R.string.admin_role_label) : capitalize(role));
        binding.tvAdminProfileEmail.setText(getString(R.string.profile_email_placeholder));
    }

    private void refreshAll() {
        fetchProfile();
        fetchDashboard();
        loadResults("eval");
        fetchJobs();
    }

    private void fetchProfile() {
        repository.fetchProfile(new AdminRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                String name = safe(profile.getFullName(), sessionManager.getUserName(), getString(R.string.admin_title));
                String email = safe(profile.getEmail(), "", getString(R.string.profile_email_placeholder));
                String role = safe(profile.getRole(), sessionManager.getRole(), "admin");

                binding.tvAdminGreeting.setText(name);
                binding.tvAdminEmail.setText(email);
                binding.tvAdminRole.setText(capitalize(role));
                binding.tvAdminProfileName.setText(name);
                binding.tvAdminProfileEmail.setText(email);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void fetchDashboard() {
        repository.fetchDashboard(new AdminRepository.DashboardCallback() {
            @Override
            public void onSuccess(AdminDashboardData data) {
                binding.tvApiStatus.setText(safe(data.getApiStatus(), "OK", "OK"));
                binding.tvModelName.setText(data.getModel() == null || TextUtils.isEmpty(data.getModel().getName())
                        ? getString(R.string.admin_model_default)
                        : data.getModel().getName());
                binding.tvModelType.setText(data.getModel() == null || TextUtils.isEmpty(data.getModel().getTaskType())
                        ? "task_type indisponible"
                        : data.getModel().getTaskType());
                binding.tvDatasetName.setText(data.getDataset() == null || TextUtils.isEmpty(data.getDataset().getName())
                        ? getString(R.string.admin_dataset_name_default)
                        : data.getDataset().getName());
                binding.tvDatasetUpdate.setText(data.getDataset() == null || TextUtils.isEmpty(data.getDataset().getLastUpdate())
                        ? getString(R.string.admin_dataset_update_default)
                        : data.getDataset().getLastUpdate());
                binding.tvLastTrainingSummary.setText(TextUtils.isEmpty(data.getLastTrainingSummary())
                        ? getString(R.string.admin_result_summary_default)
                        : data.getLastTrainingSummary());
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void loadResults(String mode) {
        repository.fetchResults(mode, new AdminRepository.ResultsCallback() {
            @Override
            public void onSuccess(AdminResultsData data) {
                binding.tvResultsMode.setText("Mode : " + safe(data.getMode(), mode, mode));
                binding.tvResultsSummary.setText(TextUtils.isEmpty(data.getSummary())
                        ? getString(R.string.admin_result_summary_default)
                        : data.getSummary());
                renderMetrics(data.getMetrics());
            }

            @Override
            public void onError(String message) {
                binding.tvResultsMode.setText("Mode : " + mode);
                binding.tvResultsSummary.setText(message);
                renderMetrics(null);
            }
        });
    }

    private void fetchJobs() {
        repository.fetchJobs(new AdminRepository.JobsCallback() {
            @Override
            public void onSuccess(List<AdminJobItem> jobs) {
                binding.jobsContainer.removeAllViews();
                boolean empty = jobs == null || jobs.isEmpty();
                binding.tvJobsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                if (empty) {
                    return;
                }
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                for (AdminJobItem job : jobs) {
                    View itemView = inflater.inflate(R.layout.item_admin_job, binding.jobsContainer, false);
                    TextView tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
                    TextView tvJobSubtitle = itemView.findViewById(R.id.tvJobSubtitle);
                    TextView tvJobMeta = itemView.findViewById(R.id.tvJobMeta);
                    tvJobTitle.setText(getString(R.string.admin_job_title,
                            safe(job.getType(), "job", "job"),
                            safe(job.getStatus(), "pending", "pending")));
                    tvJobSubtitle.setText(getString(R.string.admin_job_subtitle, simplifyIso(job.getStartedAt())));
                    tvJobMeta.setText(getString(R.string.admin_job_meta, formatDuration(job.getDurationSec())));
                    binding.jobsContainer.addView(itemView);
                }
            }

            @Override
            public void onError(String message) {
                binding.jobsContainer.removeAllViews();
                binding.tvJobsEmpty.setVisibility(View.VISIBLE);
                binding.tvJobsEmpty.setText(message);
            }
        });
    }

    private void onImportDataset() {
        String datasetName = binding.etDatasetName.getText() == null ? "" : binding.etDatasetName.getText().toString().trim();
        String datasetUrl = binding.etDatasetUrl.getText() == null ? "" : binding.etDatasetUrl.getText().toString().trim();
        if (datasetName.isEmpty() || datasetUrl.isEmpty()) {
            showToast(getString(R.string.admin_input_required));
            return;
        }
        setButtonsEnabled(false);
        repository.importDataset(datasetName, datasetUrl, new AdminRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                setButtonsEnabled(true);
                showToast(getString(R.string.admin_dataset_imported));
                fetchDashboard();
            }

            @Override
            public void onError(String message) {
                setButtonsEnabled(true);
                showToast(message);
            }
        });
    }

    private void launchTrain() {
        setButtonsEnabled(false);
        repository.launchTrain(new AdminRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                setButtonsEnabled(true);
                showToast(getString(R.string.admin_action_success));
                fetchDashboard();
                fetchJobs();
                loadResults("train");
            }

            @Override
            public void onError(String message) {
                setButtonsEnabled(true);
                showToast(message);
                fetchJobs();
            }
        });
    }

    private void launchEvaluate() {
        setButtonsEnabled(false);
        repository.launchEvaluate(new AdminRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                setButtonsEnabled(true);
                showToast(getString(R.string.admin_action_success));
                fetchJobs();
                loadResults("eval");
            }

            @Override
            public void onError(String message) {
                setButtonsEnabled(true);
                showToast(message);
                fetchJobs();
            }
        });
    }

    private void renderMetrics(Map<String, Double> metrics) {
        LinearLayout container = binding.layoutMetricsContainer;
        container.removeAllViews();
        if (metrics == null || metrics.isEmpty()) {
            TextView placeholder = new TextView(requireContext());
            placeholder.setText(R.string.admin_metrics_empty);
            placeholder.setTextColor(requireContext().getColor(R.color.ps_text_muted));
            placeholder.setTextSize(13f);
            container.addView(placeholder);
            return;
        }
        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            TextView metricView = new TextView(requireContext());
            metricView.setText(getString(R.string.admin_metric_value, entry.getKey(), decimalFormat.format(entry.getValue())));
            metricView.setTextColor(requireContext().getColor(R.color.ps_text));
            metricView.setTextSize(14f);
            metricView.setPadding(0, 0, 0, 12);
            container.addView(metricView);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        binding.btnImportDataset.setEnabled(enabled);
        binding.btnLaunchTrain.setEnabled(enabled);
        binding.btnLaunchEvaluate.setEnabled(enabled);
        binding.btnLoadEvalResults.setEnabled(enabled);
        binding.btnLoadTrainResults.setEnabled(enabled);
        binding.btnAdminRefresh.setEnabled(enabled);
    }

    private void logout() {
        sessionManager.logout();
        Navigation.findNavController(requireView()).navigate(
                R.id.loginFragment,
                null,
                new NavOptions.Builder().setPopUpTo(R.id.adminHomeFragment, true).build()
        );
    }

    private String safe(String value, String fallback, String defaultValue) {
        if (!TextUtils.isEmpty(value)) return value.trim();
        if (!TextUtils.isEmpty(fallback)) return fallback.trim();
        return defaultValue;
    }

    private String capitalize(String text) {
        if (TextUtils.isEmpty(text)) return "";
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1).toLowerCase(Locale.getDefault());
    }

    private String simplifyIso(String value) {
        if (TextUtils.isEmpty(value)) {
            return "—";
        }
        return value.replace('T', ' ');
    }

    private String formatDuration(Integer durationSec) {
        if (durationSec == null) {
            return getString(R.string.admin_duration_unknown);
        }
        if (durationSec < 60) {
            return durationSec + "s";
        }
        int minutes = durationSec / 60;
        int seconds = durationSec % 60;
        return minutes + "min " + seconds + "s";
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
