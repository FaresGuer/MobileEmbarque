package com.example.projet;

import android.os.Bundle;
import android.view.View;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.PrefsHelper;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.User;
import com.example.projet.Fragments.EditProfileFragment;
import com.example.projet.Fragments.EmergencyContactsFragment;
import com.example.projet.Fragments.FriendsFragment;
import com.example.projet.Fragments.HomeFragment;
import com.example.projet.Fragments.LoginFragment;
import com.example.projet.Fragments.RegisterFragment;


public class MainActivity extends AppCompatActivity
        implements HomeFragment.HomeListener,
        LoginFragment.LoginListener,
        EmergencyContactsFragment.MenuListener,
        FriendsFragment.MenuListener,
        RegisterFragment.RegisterListener {

    private DrawerLayout drawerLayout;
    private View itemProfile, itemHealth, itemEnvironment, itemLogout,itemEmergencyContacts,itemFriends;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserSession.loadUser(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        itemProfile = findViewById(R.id.itemProfile);
        itemHealth = findViewById(R.id.itemHealth);
        itemEnvironment = findViewById(R.id.itemEnvironment);
        itemEmergencyContacts = findViewById(R.id.itemEmergencyContacts);
        itemFriends = findViewById(R.id.itemFriends);
        itemLogout = findViewById(R.id.itemLogout);

        if (savedInstanceState == null) {
            decideStartScreen();
        }

        itemProfile.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);
            v.postDelayed(() -> {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit();
            }, 200);
        });

        itemHealth.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);
        });

        itemEnvironment.setOnClickListener(v -> {

            drawerLayout.closeDrawer(GravityCompat.START);
        });
        itemEmergencyContacts.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            v.postDelayed(() -> {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EmergencyContactsFragment())
                        .addToBackStack(null)
                        .commit();
            }, 200);
        });
        itemFriends.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            v.postDelayed(() -> {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FriendsFragment())
                        .addToBackStack(null)
                        .commit();
            }, 200);
        });

        itemLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            v.postDelayed(this::doLogout, 250);
        });
    }


    private void decideStartScreen() {
        int savedId = PrefsHelper.getRememberedUserId(this);

        if (savedId == -1) {
            showLogin();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            User user = db.userDao().getUserById(savedId);
            runOnUiThread(() -> {
                if (user != null) {

                    UserSession.saveUser(this, user);
                    showHome();
                } else {

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
    @Override
    public void onOpenMenu() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void doLogout() {
        PrefsHelper.clearRememberedUser(this);
        UserSession.clear(this);
        getSupportFragmentManager().popBackStack(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
        showLogin();
    }


    @Override
    public void onLoginSuccess(User user, boolean rememberMe) {

        UserSession.saveUser(this, user);

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