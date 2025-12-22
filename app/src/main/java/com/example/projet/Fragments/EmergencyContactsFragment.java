package com.example.projet.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.User;
import com.example.projet.Fragments.Adapters.EmergencyContactsAdapter;
import com.example.projet.R;
import android.content.Context;
import java.util.List;

public class EmergencyContactsFragment extends Fragment {
    public interface MenuListener {
        void onOpenMenu();
    }
    private EmergencyContactsAdapter adapter;
    private MenuListener menuListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false);

        RecyclerView rv = view.findViewById(R.id.rvContacts);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (menuListener != null) menuListener.onOpenMenu();
        });
        adapter = new EmergencyContactsAdapter(new EmergencyContactsAdapter.Listener() {
            @Override
            public void onClick(EmergencyContact c) {
                openEdit(c.id);
            }

            @Override
            public void onLongClick(EmergencyContact c) {
                deleteContact(c);
            }
        });

        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> openCreate());

        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        User user = UserSession.getUser();
        if (user == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            List<EmergencyContact> data = db.emergencyContactDao().getForUser(user.getId());
            requireActivity().runOnUiThread(() -> adapter.submit(data));
        }).start();
    }

    private void openCreate() {
        AddEditEmergencyContactFragment f = AddEditEmergencyContactFragment.newCreate();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    private void openEdit(int contactId) {
        AddEditEmergencyContactFragment f = AddEditEmergencyContactFragment.newEdit(contactId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    private void deleteContact(EmergencyContact c) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            db.emergencyContactDao().delete(c);
            requireActivity().runOnUiThread(this::loadData);
        }).start();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuListener) {
            menuListener = (MenuListener) context;
        } else {
            throw new IllegalStateException("MainActivity must implement EmergencyContactsFragment.MenuListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        menuListener = null;
    }
}