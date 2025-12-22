package com.example.projet.Fragments;

import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.User;
import com.example.projet.R;

public class HomeFragment extends Fragment {


    public interface HomeListener {
        void onOpenRightMenu();
    }

    private HomeListener listener;
    private TextView tvWelcome;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeListener) {
            listener = (HomeListener) context;
        } else {
            throw new RuntimeException("Activity must implement HomeListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        tvWelcome = view.findViewById(R.id.tvWelcome);



        User user = UserSession.getUser();
        if (user != null) {
            tvWelcome.setText("Welcome, " + user.getUsername());
        }

        btnOpenMenu.setOnClickListener(v -> {
            if (listener != null) listener.onOpenRightMenu();
        });

        return view;
    }
}