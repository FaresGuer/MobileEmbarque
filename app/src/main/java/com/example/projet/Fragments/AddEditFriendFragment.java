package com.example.projet.Fragments;

import android.os.Bundle;
import android.util.Patterns;
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
import com.example.projet.Entities.Enums.FriendStatus;
import com.example.projet.Entities.Friend;
import com.example.projet.Entities.User;
import com.example.projet.R;

public class AddEditFriendFragment extends Fragment {

    private static final String ARG_FRIEND_ROW_ID = "friend_row_id";

    public static AddEditFriendFragment newCreate() {
        return new AddEditFriendFragment();
    }

    public static AddEditFriendFragment newEdit(int friendRowId) {
        AddEditFriendFragment f = new AddEditFriendFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_FRIEND_ROW_ID, friendRowId);
        f.setArguments(b);
        return f;
    }

    private int friendRowId = -1;

    private EditText etFriendEmail;
    private CheckBox cbFavorite;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_friend, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        etFriendEmail = view.findViewById(R.id.etFriendEmail);
        cbFavorite = view.findViewById(R.id.cbFavorite);
        Button btnSave = view.findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        if (getArguments() != null) {
            friendRowId = getArguments().getInt(ARG_FRIEND_ROW_ID, -1);
        }

        if (friendRowId != -1) {
            loadExisting(friendRowId);
            etFriendEmail.setEnabled(false);
        }

        btnSave.setOnClickListener(v -> save());

        return view;
    }

    private void loadExisting(int rowId) {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            Friend f = db.friendDao().getById(rowId);
            if (f == null) return;

            User friendUser = db.userDao().getById(f.friendUserId);

            requireActivity().runOnUiThread(() -> {
                if (friendUser != null) {
                    etFriendEmail.setText(friendUser.getEmail());
                } else {
                    etFriendEmail.setText("");
                }
                cbFavorite.setChecked(f.isFavorite);
            });
        }).start();
    }

    private void save() {
        User owner = UserSession.getUser();
        if (owner == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean fav = cbFavorite.isChecked();
        String email = etFriendEmail.getText().toString().trim();

        if (friendRowId == -1) {
            if (email.isEmpty()) {
                etFriendEmail.setError("Required");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etFriendEmail.setError("Please enter a valid email");
                return;
            }
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {

            if (friendRowId == -1) {
                User other = db.userDao().getByEmail(email);

                if (other == null) {
                    requireActivity().runOnUiThread(() ->
                            etFriendEmail.setError("User not found on this phone")
                    );
                    return;
                }

                if (other.getId() == owner.getId()) {
                    requireActivity().runOnUiThread(() ->
                            etFriendEmail.setError("You cannot add yourself")
                    );
                    return;
                }

                Friend existing = db.friendDao().getByOwnerAndFriend(owner.getId(), other.getId());
                if (existing != null && !existing.status.name().equals("REJECTED")) {
                    requireActivity().runOnUiThread(() ->
                            etFriendEmail.setError("Already exists: " + existing.status.name())
                    );
                    return;
                }

                // If there is an incoming pending request from other -> owner, do not create a second request
                Friend incoming = db.friendDao().getByOwnerAndFriend(other.getId(), owner.getId());
                if (incoming != null && incoming.status == FriendStatus.PENDING) {
                    requireActivity().runOnUiThread(() ->
                            etFriendEmail.setError("This user already requested you. Go to Friend Requests.")
                    );
                    return;
                }

                Friend req = new Friend();
                req.ownerUserId = owner.getId();
                req.friendUserId = other.getId();
                req.status = FriendStatus.PENDING;
                req.isFavorite = fav;

                db.friendDao().insert(req);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Request sent", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });

            } else {
                // Edit existing row (only favorite allowed)
                Friend f = db.friendDao().getById(friendRowId);
                if (f != null) {
                    f.isFavorite = fav;
                    db.friendDao().update(f);
                }

                requireActivity().runOnUiThread(() ->
                        requireActivity().getSupportFragmentManager().popBackStack()
                );
            }

        }).start();
    }
}