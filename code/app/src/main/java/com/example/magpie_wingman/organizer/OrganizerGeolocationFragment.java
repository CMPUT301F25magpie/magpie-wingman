package com.example.magpie_wingman.organizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerGeolocationFragment extends Fragment {
    private GoogleMap map;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_geolocation, container, false);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                if (eventId != null) {
                    checkGeolocationEnabled();
                }
            });
        }
    }

    private void checkGeolocationEnabled() {
        FirebaseFirestore db = DbManager.getInstance().getDb();

        db.collection("events").document(eventId).get().addOnSuccessListener(eventSnap -> {
                    Boolean geoRequired = eventSnap.getBoolean("geolocationRequired");

                    if (geoRequired == null || !geoRequired) {
                        Toast.makeText(requireContext(),
                                "Geolocation was not enabled for this event.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    loadEntrantLocations();
                });
    }

    /**
     * Load all entrant coordinates from Firestore:
     * events/{eventId}/waitlist/{userId}
     *  ->latitude
     *  ->longitude
     */
    private void loadEntrantLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).collection("waitlist").get().addOnSuccessListener(snapshot -> {
                    boolean hasMarkers = false;

                    for (DocumentSnapshot doc : snapshot) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String userId = doc.getId();

                        if (lat != null && lng != null) {
                            LatLng point = new LatLng(lat, lng);
                            map.addMarker(new MarkerOptions()
                                    .position(point)
                                    .title("Entrant: " + userId));

                            if (!hasMarkers) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 12f));
                                hasMarkers = true;
                            }
                        }
                    }

                    if (!hasMarkers) {
                        Toast.makeText(requireContext(),
                                "No entrant location data available.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load entrant locations.",
                                Toast.LENGTH_SHORT).show()
                );
    }
}