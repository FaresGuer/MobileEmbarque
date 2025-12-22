package com.example.projet.Fragments;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.User;
import com.example.projet.R;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class EditProfileFragment extends Fragment {

    private ImageButton btnBack;
    private ImageView ivAvatar;
    private EditText etUsername, etEmail, etPhone, etDob,etPassword;
    private TextView tpassword;
    private Button btnSave;

    private Uri selectedAvatarUri = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    ivAvatar.setImageURI(uri);
                }
            });

    private static final Pattern DOB_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern PHONE_8_DIGITS = Pattern.compile("^\\d{8}$");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etDob = view.findViewById(R.id.etDob);
        etPassword=view.findViewById(R.id.etPassword);
        tpassword=view.findViewById(R.id.tPassword);
        btnSave = view.findViewById(R.id.btnCreate);

        btnSave.setText("Save changes");

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        etDob.setFocusable(false);
        etDob.setOnClickListener(v -> openDatePicker());
        etPassword.setVisibility(View.GONE);
        tpassword.setVisibility(View.GONE);
        fillFormFromSession();

        btnSave.setOnClickListener(v -> saveChanges());

        return view;
    }

    private void fillFormFromSession() {
        User me = UserSession.getUser();
        if (me == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        etUsername.setText(me.getUsername());
        etEmail.setText(me.getEmail());
        etPhone.setText(me.getPhoneNumber());
        etDob.setText(me.getDateOfBirth());

         if (me.getAvatarPath() != null) ivAvatar.setImageURI(Uri.parse(me.getAvatarPath()));
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {
                    String dob = String.format(Locale.US, "%04d-%02d-%02d", year, (month + 1), day);
                    etDob.setText(dob);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void saveChanges() {
        User me = UserSession.getUser();
        if (me == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Required");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

        if (!PHONE_8_DIGITS.matcher(phone).matches()) {
            etPhone.setError("Phone must be 8 digits");
            return;
        }

        if (!DOB_PATTERN.matcher(dob).matches()) {
            etDob.setError("Use YYYY-MM-DD");
            return;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            User dbUser = db.userDao().getById(me.getId());
            if (dbUser == null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "User not found.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            dbUser.setUsername(username);
            dbUser.setEmail(email);
            dbUser.setPhoneNumber(phone);
            dbUser.setDateOfBirth(dob);
            if (selectedAvatarUri != null) {
                dbUser.setAvatarPath(selectedAvatarUri.toString());
            }

            db.userDao().update(dbUser);


            UserSession.saveUser(requireContext(), dbUser);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }).start();
    }
}