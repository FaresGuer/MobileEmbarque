package com.example.projet.Fragments.User;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.Entities.PasswordUtils;
import com.example.projet.Entities.User;
import com.example.projet.R;
import android.util.Patterns;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
public class RegisterFragment extends Fragment {

    public interface RegisterListener {
        void onRegisterSuccess();
    }

    private RegisterListener listener;
    private EditText etUsername, etEmail, etPhone, etDob, etPassword;
    private ImageView ivAvatar;
    private AppDatabase db;


    private String selectedAvatarUri;


    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    this::onImagePicked
            );

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RegisterListener) {
            listener = (RegisterListener) context;
        } else {
            throw new RuntimeException("Activity must implement RegisterListener");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        ivAvatar = view.findViewById(R.id.ivAvatar);

        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etDob = view.findViewById(R.id.etDob);
        etPassword = view.findViewById(R.id.etPassword);
        Button btnCreate = view.findViewById(R.id.btnCreate);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        etDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dlg = new DatePickerDialog(
                    requireContext(),
                    R.style.AppDatePickerTheme,
                    (datePicker, y, m, d) -> {
                        String formatted = String.format(
                                Locale.getDefault(),
                                "%04d-%02d-%02d",
                                y, (m + 1), d
                        );
                        etDob.setText(formatted);
                    },
                    year, month, day
            );

            dlg.getDatePicker().setMaxDate(System.currentTimeMillis());

            dlg.show();
        });

        db = AppDatabase.getInstance(requireContext());

        ivAvatar.setOnClickListener(v -> {
            Animation bounce = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce);
            ivAvatar.startAnimation(bounce);

            openImagePicker();
        });
        btnCreate.setOnClickListener(v -> createAccount());
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        imagePickerLauncher.launch(intent);
    }

    private void onImagePicked(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();
            if (uri != null) {
                selectedAvatarUri = uri.toString();
                ivAvatar.setImageURI(uri);
            }
        }
    }

    private void createAccount() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            return;
        }
        if(email.isEmpty())
        {
            etEmail.setError("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return;
        }
        if(phone.isEmpty())
        {
            etPhone.setError("Phone is required");
            return;
        }
        if (!phone.matches("\\d{8}")) {
            etPhone.setError("Phone must be exactly 8 digits");
            return;
        }
        if(dob.isEmpty())
        {
            etDob.setError("Date of Birth is required");
            return;
        }
        if (!isValidDate(dob)) {
            etDob.setError("Date of birth must be YYYY-MM-DD and a real date");
            return;
        }
        if(password.isEmpty())
        {
            etPassword.setError("Password is required");
            return;
        }

        String avatarPath = selectedAvatarUri;

        String passwordHash = PasswordUtils.hashPassword(password);
        User user = new User(username, email, phone, dob, avatarPath, passwordHash);

        new Thread(() -> {
            long id = db.userDao().insertUser(user);
            requireActivity().runOnUiThread(() -> {
                if (id > 0) {
                    Toast.makeText(requireContext(), "Account created, please login", Toast.LENGTH_SHORT).show();
                    listener.onRegisterSuccess();
                } else {
                    Toast.makeText(requireContext(), "Error creating account", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    private boolean isValidDate(String dateStr) {
        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}