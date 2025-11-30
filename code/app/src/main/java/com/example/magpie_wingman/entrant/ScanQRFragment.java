package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.magpie_wingman.R;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanQRFragment extends Fragment {

    // 1. The Scanner Launcher
    // This handles the result when the camera closes
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    // Success! The QR code contained this string:
                    String scannedEventId = result.getContents();
                    navigateToEventDetails(scannedEventId);
                }
            });

    public ScanQRFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_q_r, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        Button scanButton = view.findViewById(R.id.button_scan_qr);
        Button testButton = view.findViewById(R.id.button_test_scan);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // 2. Real Scanner Logic
        scanButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan the Event QR Code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class); // Use default capture activity
            barcodeLauncher.launch(options);
        });

        // 3. Cheat Logic (For testing without camera)
        testButton.setOnClickListener(v -> {
            // Uses the ID from your screenshot to simulate a real scan
            String fakeEventId = "qr#9096";
            Toast.makeText(getContext(), "Simulating scan for: " + fakeEventId, Toast.LENGTH_SHORT).show();
            navigateToEventDetails(fakeEventId);
        });
    }

    private void navigateToEventDetails(String eventId) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);

        // This relies on the action existing in nav_graph.xml
        try {
            Navigation.findNavController(getView()).navigate(
                    R.id.action_scanQRFragment_to_detailedEventDescriptionFragment,
                    bundle
            );
        } catch (Exception e) {
            Toast.makeText(getContext(), "Navigation Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}