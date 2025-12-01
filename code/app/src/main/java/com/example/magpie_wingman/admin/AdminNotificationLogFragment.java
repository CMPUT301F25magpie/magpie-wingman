package com.example.magpie_wingman.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;

/**
 * Admin screen: shows log of all notifications sent to entrants.
 *
 * US 03.08.01 – As an administrator, I want to review logs of all notifications sent to entrants by organizers.
 */
public class AdminNotificationLogFragment extends Fragment {

    private AdminNotificationLogAdapter adapter;
    private DbManager dbManager;

    public AdminNotificationLogFragment() {
        // Required empty public constructor
    }

    public static AdminNotificationLogFragment newInstance(String p1, String p2) {
        AdminNotificationLogFragment f = new AdminNotificationLogFragment();
        Bundle b = new Bundle();
        b.putString("param1", p1);
        b.putString("param2", p2);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = DbManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notification_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        RecyclerView recycler = view.findViewById(R.id.recycler_view_admin_notifications);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminNotificationLogAdapter();
        recycler.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        dbManager.getAllNotifications()
                .addOnSuccessListener(list -> {
                    adapter.setNotifications(list);

                    if (getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "Loaded " + list.size() + " notifications",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    e.printStackTrace();
                });
    }
}