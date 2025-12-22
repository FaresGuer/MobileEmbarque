package com.example.projet.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.User;
import com.example.projet.R;

public class AddEditEmergencyContactFragment extends Fragment {

    private static final String ARG_CONTACT_ID = "contact_id";

    public static AddEditEmergencyContactFragment newCreate() {
        return new AddEditEmergencyContactFragment();
    }

    public static AddEditEmergencyContactFragment newEdit(int contactId) {
        AddEditEmergencyContactFragment f = new AddEditEmergencyContactFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_CONTACT_ID, contactId);
        f.setArguments(b);
        return f;
    }

    private int contactId = -1;

    private EditText etName, etPhone, etRelationship;
    private CheckBox cbPrimary;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_emergency_contact, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        cbPrimary = view.findViewById(R.id.cbPrimary);
        Button btnSave = view.findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (getArguments() != null) {
            contactId = getArguments().getInt(ARG_CONTACT_ID, -1);
        }

        if (contactId != -1) {
            loadExisting(contactId);
        }

        btnSave.setOnClickListener(v -> save());

        return view;
    }

    private void loadExisting(int id) {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            EmergencyContact c = db.emergencyContactDao().getById(id);
            requireActivity().runOnUiThread(() -> {
                if (c == null) return;
                etName.setText(c.displayName);
                etPhone.setText(c.phoneNumber);
                cbPrimary.setChecked(c.isPrimary);
            });
        }).start();
    }

    private void save() {
        User user = UserSession.getUser();
        if (user == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        boolean primary = cbPrimary.isChecked();

        if (name.isEmpty()) {
            etName.setError("Required");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Required");
            return;
        }
        if (!phone.matches("\\d{8}")) {
            etPhone.setError("Phone must be exactly 8 digits");
            return;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            if (primary) {
                db.emergencyContactDao().clearPrimaryForUser(user.getId());
            }

            if (contactId == -1) {
                EmergencyContact c = new EmergencyContact(user.getId(), name, phone, primary);
                db.emergencyContactDao().insert(c);
            } else {
                EmergencyContact c = db.emergencyContactDao().getById(contactId);
                if (c != null) {
                    c.displayName = name;
                    c.phoneNumber = phone;
                    c.isPrimary = primary;
                    db.emergencyContactDao().update(c);
                }
            }

            requireActivity().runOnUiThread(() ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }).start();
    }
}