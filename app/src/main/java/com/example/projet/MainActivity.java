package com.example.projet;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.PrefsHelper;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.User;
import com.example.projet.Fragments.HomeFragment;
import com.example.projet.Fragments.LoginFragment;
import com.example.projet.Fragments.RegisterFragment;
import com.example.projet.R;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.HomeListener,
        LoginFragment.LoginListener,
        RegisterFragment.RegisterListener {

    private DrawerLayout drawerLayout;
    private View itemProfile, itemHealth, itemEnvironment, itemLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawerLayout);
        itemProfile = findViewById(R.id.itemProfile);
        itemHealth = findViewById(R.id.itemHealth);
        itemEnvironment = findViewById(R.id.itemEnvironment);
        itemLogout = findViewById(R.id.itemLogout);

        if (savedInstanceState == null) {
            decideStartScreen();
        }

        itemProfile.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        itemHealth.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        itemEnvironment.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        itemLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            v.postDelayed(this::doLogout, 250);
        });
    }


    private void decideStartScreen() {
        int savedId = PrefsHelper.getRememberedUserId(this);

        if (savedId == -1) {
            // No remembered user
            showLogin();
            return;
        }

        // Try to load user from database
        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            User user = db.userDao().getUserById(savedId);
            runOnUiThread(() -> {
                if (user != null) {
                    // Restore session and go to Home
                    UserSession.setUser(user);
                    showHome();
                } else {
                    // Saved id is not valid anymore
                    PrefsHelper.clearRememberedUser(this);
                    showLogin();
                }
            });
        }).start();
    }

    private void showHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void showLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void showRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onOpenRightMenu() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    
    private void doLogout() {
        PrefsHelper.clearRememberedUser(this);
        UserSession.clear();
        showLogin();
    }


    @Override
    public void onLoginSuccess(User user, boolean rememberMe) {

        UserSession.setUser(user);

        if (rememberMe) {
            PrefsHelper.saveRememberedUserId(this, user.getId());
        } else {
            PrefsHelper.clearRememberedUser(this);
        }

        showHome();
    }

    @Override
    public void onRegisterClicked() {
        showRegister();
    }



    @Override
    public void onRegisterSuccess() {

        getSupportFragmentManager().popBackStack();
    }
}