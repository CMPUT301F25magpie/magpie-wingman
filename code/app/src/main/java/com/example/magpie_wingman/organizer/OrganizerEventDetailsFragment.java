package com.example.magpie_wingman.organizer;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrganizerEventDetailsFragment extends Fragment {

    private String eventId;

    private TextView titleView;
    private TextView locationView;
    private TextView dateView;
    private TextView descriptionView;
    private ImageView qrCodeButton;
    private ImageView posterView;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public OrganizerEventDetailsFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleView       = view.findViewById(R.id.text_event_title);
        locationView    = view.findViewById(R.id.text_event_location);
        dateView        = view.findViewById(R.id.text_event_date_time);
        descriptionView = view.findViewById(R.id.text_event_description);
        qrCodeButton    = view.findViewById(R.id.image_qr);
        posterView      = view.findViewById(R.id.image_event_poster);

        ImageButton btnBack         = view.findViewById(R.id.button_back);
        Button btnWaitlist          = view.findViewById(R.id.btn_waiting_list);
        Button btnSelected          = view.findViewById(R.id.btn_selected_list);
        Button btnAccepted          = view.findViewById(R.id.btn_accepted_list);
        Button btnCancelled         = view.findViewById(R.id.btn_cancelled_list);
        Button btnLottery           = view.findViewById(R.id.btn_lottery);
        Button btnNotify            = view.findViewById(R.id.btn_notify);
        Button btnCancelNonReg      = view.findViewById(R.id.btn_cancel_non_registered);
        Button btnFinalize          = view.findViewById(R.id.btn_finalize);

        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails();
        } else {
            titleView.setText("Error: No Event ID Passed");
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        setupNavButton(view, btnWaitlist, R.id.action_organizerEventDetailsFragment_to_waitingListFragment);
        setupNavButton(view, btnSelected, R.id.action_organizerEventDetailsFragment_to_selectedEntrantsListFragment);
        setupNavButton(view, btnAccepted, R.id.action_organizerEventDetailsFragment_to_acceptedListFragment);
        setupNavButton(view, btnCancelled, R.id.action_organizerEventDetailsFragment_to_cancelledListFragment);
        setupNavButton(view, btnLottery, R.id.action_organizerEventDetailsFragment_to_organizerLotteryFragment);
        setupNavButton(view, btnNotify, R.id.action_organizerEventDetailsFragment_to_organizerNotifyFragment);
        setupNavButton(view, btnFinalize, R.id.action_organizerEventDetailsFragment_to_organizerFinalizedListFragment);

        btnCancelNonReg.setOnClickListener(v -> confirmCancelNonRegistered());
    }

    private void loadEventDetails() {
        DbManager.getInstance().getDb()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) return;

                    titleView.setText(event.getEventName());
                    locationView.setText(
                            event.getEventLocation() != null
                                    ? event.getEventLocation()
                                    : "TBD"
                    );
                    descriptionView.setText(event.getDescription());

                    if (event.getEventStartTime() != null) {
                        dateView.setText(dateFormat.format(event.getEventStartTime()));
                    }

                    // --- QR code handling ---
                    String qrContent = event.getQrCodeHash();
                    if (qrContent != null && !qrContent.isEmpty()) {
                        qrCodeButton.setOnClickListener(v -> showQRDialog(qrContent));
                    } else {
                        qrCodeButton.setOnClickListener(v ->
                                Toast.makeText(getContext(),
                                        "No QR Code generated",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }

                    // --- Poster handling (no Glide/Picasso) ---
                    String posterUrl = event.getEventPosterURL();
                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        posterView.setImageResource(R.drawable.ic_image_placeholder);
                        loadPosterImageAsync(posterUrl);
                    } else {
                        posterView.setImageResource(R.drawable.ic_broken_image);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("OrgEventDetails", "Failed to load event", e)
                );
    }

    /**
     * Downloads the image from the given URL on a background thread and sets it
     * into image_event_poster on the main thread.
     */
    private void loadPosterImageAsync(@NonNull String urlString) {
        new Thread(() -> {
            Bitmap bmp = null;
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                try (InputStream is = conn.getInputStream()) {
                    bmp = BitmapFactory.decodeStream(is);
                }
            } catch (Exception e) {
                Log.e("OrgEventDetails", "Poster load failed", e);
            }

            final Bitmap finalBmp = bmp;
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (finalBmp != null) {
                    posterView.setImageBitmap(finalBmp);
                } else {
                    posterView.setImageResource(R.drawable.ic_broken_image);
                }
            });
        }).start();
    }

    private void showQRDialog(@NonNull String content) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 600, 600);

            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(bitmap);
            imageView.setPadding(30, 30, 30, 30);

            new AlertDialog.Builder(getContext())
                    .setTitle("Event QR Code")
                    .setView(imageView)
                    .setPositiveButton("Close", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error generating QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavButton(View root, Button button, int actionId) {
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (eventId == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            Navigation.findNavController(root).navigate(actionId, bundle);
        });
    }

    private void confirmCancelNonRegistered() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel non-registered entrants?")
                .setMessage("This will move all entrants who did not sign up to the Cancelled list.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    DbManager.getInstance()
                            .cancelNonRegisteredEntrants(eventId)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(requireContext(),
                                            "Non-registered entrants cancelled.",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
