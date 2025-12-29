package com.example.projet.Fragments.User;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.Entities.PasswordUtils;
import com.example.projet.Entities.User;
import com.example.projet.R;

public class LoginFragment extends Fragment {

    public interface LoginListener {
        void onLoginSuccess(User user, boolean rememberMe);
        void onRegisterClicked();
    }

    private LoginListener listener;
    private EditText etUsername, etPassword;
    private CheckBox cbRemember;
    private AppDatabase db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LoginListener) {
            listener = (LoginListener) context;
        } else {
            throw new RuntimeException("Activity must implement LoginListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        cbRemember = view.findViewById(R.id.cbRemember);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGoRegister = view.findViewById(R.id.btnGoRegister);

        db = AppDatabase.getInstance(requireContext());

        btnLogin.setOnClickListener(v -> doLogin());
        btnGoRegister.setOnClickListener(v -> listener.onRegisterClicked());

        return view;
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean hasError = false;

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            hasError = true;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        String passwordHash = PasswordUtils.hashPassword(password);

        new Thread(() -> {
            User user = db.userDao().login(username, passwordHash);
            requireActivity().runOnUiThread(() -> {
                if (user != null) {
                    listener.onLoginSuccess(user, cbRemember.isChecked());
                } else {
                    Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}