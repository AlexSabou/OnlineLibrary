package com.example.ali.biblioteca.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 * Created by Ali on 08.01.2018.
 */

public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    private TextView tvName, tvRole, tvEmail, tvCNP;
    private EditText etPassword, etEmail;
    private Button btnPassword, btnEmail;
    private User user;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.account_layout, container, false);
        user = ((HomeActivity)getActivity()).getCurrentUser();
        Log.e(TAG, "Started");

        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
    */
    public void refreshFragment() {
        user = ((HomeActivity)getActivity()).getCurrentUser();
        tvName.setText(user.getName());
        tvEmail.setText("Email: " + user.getEmail());
        tvCNP.setText("CNP: " + user.getCnp());
        switch (user.getRole()) {
            case User.ROLE_USER: {
                tvRole.setText("Role: User");
                break;
            }
            case User.ROLE_LIBRARIAN: {
                tvRole.setText("Role: Librarian");
                break;
            }
            case User.ROLE_ADMIN: {
                tvRole.setText("Role: Admin");
                break;
            }
        }
        etEmail.setText("");
        etPassword.setText("");

    }

    /**
     * Method that loads the UI elements needed for the page.
     * @param view
     */
    private void loadElements(View view) {
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvRole = (TextView) view.findViewById(R.id.tvRole);
        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvCNP = (TextView) view.findViewById(R.id.tvCNP);

        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etEmail = (EditText) view.findViewById(R.id.etEmail);

        btnPassword = (Button) view.findViewById(R.id.btnPassword);
        btnEmail = (Button) view.findViewById(R.id.btnEmail);


        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareChangePassword();
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareChangeEmail();
            }
        });
    }

    /**
     * Checks if all the inserted datas are correct.
     */
    private void prepareChangePassword() {
        String password = etPassword.getText().toString().trim();
        if(password.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert your new password.", Toast.LENGTH_SHORT).show();
            return;
        }
        changePassword(password);
    }

    /**
     * Changes the password for the current user into @param password
     * @param password
     */
    private void changePassword(String password) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fbUser != null) {
            fbUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Password changed.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Checks if all the inserted datas are correct.
     */
    private void prepareChangeEmail() {
        String email = etEmail.getText().toString().trim();
        if(email.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert your new password.", Toast.LENGTH_SHORT).show();
            return;
        }
        changeEmail(email);
    }

    /**
     * Changes the password for the current user into @param password
     * @param email
     */
    private void changeEmail(final String email) {
        final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fbUser != null) {
            fbUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        user.setEmail(email);
                        FirebaseDatabase.getInstance().getReference("user").child(fbUser.getUid()).child("email").setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Email changed.", Toast.LENGTH_SHORT).show();
                                    tvEmail.setText("Email: " + email);

                                }
                                else {
                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
