package com.example.ali.biblioteca.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.example.ali.biblioteca.utility.PicassoClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 09.01.2018.
 */

public class ManageFragment extends Fragment {
    private static final String TAG = "ManageFragment";
    public final static String ORDER_NAME = "name";

    private ListView lwAccounts;
    private List<User> userList;
    private UserAdapter userAdapter;
    private View view;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manage_accounts, container, false);
        Log.e(TAG, "Started");
        loadElements(view);

        return view;
    }

    public void refreshFragment() {
        currentUser = ((HomeActivity) getActivity()).getCurrentUser();
        selectAll(ORDER_NAME);
    }

    public void selectSpecificUser(String orderCriteria, final String text) {
        FirebaseDatabase.getInstance().getReference("user").orderByChild(orderCriteria).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSpecificUpdates(dataSnapshot, text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSpecificUpdates(DataSnapshot dataSnapshot, String text) {
        String formattedText = text.trim().toLowerCase();

        if(userList == null)
            userList = new ArrayList<>();
        else
            userList.clear();

        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            User user = ds.getValue(User.class);
            if(user != null) {
                user.setKey(ds.getKey());
                if(!user.getEmail().equals(currentUser.getEmail()))
                    if(user.getName().toLowerCase().contains(formattedText) || user.getEmail().toLowerCase().contains(formattedText))
                        userList.add(user);
            }
        }

        if(lwAccounts == null)
            lwAccounts = (ListView) view.findViewById(R.id.lwAccounts);

        userAdapter = new UserAdapter(getActivity(), userList);
        lwAccounts.setAdapter(userAdapter);

        if(userList.size() == 0) {
            Toast.makeText(getContext(), "No account found matching this criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectAll(String orderCriteria) {
        FirebaseDatabase.getInstance().getReference("user").orderByChild(orderCriteria).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getUpdates(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUpdates(DataSnapshot dataSnapshot) {
        if(userList == null)
            userList = new ArrayList<>();
        else
            userList.clear();

        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            User user = ds.getValue(User.class);
            if(user != null) {
                user.setKey(ds.getKey());
                if(!user.getEmail().equals(currentUser.getEmail()))
                    userList.add(user);
            }
        }

        if(lwAccounts == null)
            lwAccounts = (ListView) view.findViewById(R.id.lwAccounts);

        userAdapter = new UserAdapter(getActivity(), userList);
        lwAccounts.setAdapter(userAdapter);

        if(userList.size() == 0) {
            Toast.makeText(getContext(), "No stock found matching this criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadElements(View view) {
        lwAccounts = (ListView) view.findViewById(R.id.lwAccounts);
    }



    class UserAdapter extends BaseAdapter {

        private Context context;
        private List<User> userList;
        private LayoutInflater inflater;

        public UserAdapter(Context context, List<User> userList) {
            this.context = context;
            this.userList = userList;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.account_list_row, parent, false);

            ManageFragment.UserHolder holder = new ManageFragment.UserHolder(convertView);
            final User user = userList.get(position);

            if(user != null) {
                holder.tvName.setText(user.getName());
                holder.tvEmail.setText(user.getEmail());


                switch (user.getRole()) {
                    case User.ROLE_USER: {
                        holder.rbUser.setChecked(true);
                        holder.tvRole.setText("User");
                        break;
                    }
                    case User.ROLE_LIBRARIAN: {
                        holder.rbLibrarian.setChecked(true);
                        holder.tvRole.setText("Librarian");
                        break;
                    }
                    case User.ROLE_ADMIN: {
                        holder.rbAdmin.setChecked(true);
                        holder.tvRole.setText("Admin");
                        break;
                    }
                }
                if(user.isBlocked())
                    holder.btnBlock.setText("Unblock");
                else
                    holder.btnBlock.setText("Block");
            }

            final View copyView = convertView;
            final UserHolder copyHolder = holder;

            holder.rbAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user != null)
                        if(user.getRole() != User.ROLE_ADMIN)
                            updateUserRole(user, User.ROLE_ADMIN);
                }
            });

            holder.rbLibrarian.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user != null)
                        if(user.getRole() != User.ROLE_LIBRARIAN)
                            updateUserRole(user, User.ROLE_LIBRARIAN);
                }
            });

            holder.rbUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user != null)
                        if(user.getRole() != User.ROLE_USER)
                            updateUserRole(user, User.ROLE_USER);
                }
            });

            /*holder.rgRole.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RadioButton rbSelected = copyView.findViewById(copyHolder.rgRole.getCheckedRadioButtonId());
                    switch(rbSelected.getText().toString()) {
                        case "User": {
                            if(user.getRole() != User.ROLE_USER)
                                updateUserRole(user, User.ROLE_USER);
                            break;
                        }
                        case "Librarian": {
                            if(user.getRole() != User.ROLE_LIBRARIAN)
                                updateUserRole(user, User.ROLE_LIBRARIAN);
                            break;
                        }
                        case "Admin": {
                            if(user.getRole() != User.ROLE_ADMIN)
                                updateUserRole(user, User.ROLE_ADMIN);
                            break;
                        }
                    }
                }
            });*/

            /*holder.rgRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton rbSelected = copyView.findViewById(checkedId);
                    switch(rbSelected.getText().toString()) {
                        case "User": {
                            if(user.getRole() != User.ROLE_USER)
                                updateUserRole(user, User.ROLE_USER);
                            break;
                        }
                        case "Librarian": {
                            if(user.getRole() != User.ROLE_LIBRARIAN)
                                updateUserRole(user, User.ROLE_LIBRARIAN);
                            break;
                        }
                        case "Admin": {
                            if(user.getRole() != User.ROLE_ADMIN)
                                updateUserRole(user, User.ROLE_ADMIN);
                            break;
                        }
                    }
                }
            });*/

            holder.btnBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.isBlocked())
                        unblockUser(user, copyHolder);
                    else
                        blockUser(user, copyHolder);
                }
            });

            return convertView;
        }

        private void updateUserRole(final User user, final int role) {
            FirebaseDatabase.getInstance().getReference("user").child(user.getKey()).child("role").setValue(role).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(getActivity(), "User " + user.getName() + "'s role has been changed.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void unblockUser(final User user, final UserHolder holder) {
            FirebaseDatabase.getInstance().getReference("user").child(user.getKey()).child("blocked").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        holder.btnBlock.setText("Block");
                        Toast.makeText(getActivity(), "User " + user.getName() + " has been unblocked.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void blockUser(final User user, final UserHolder holder) {
            FirebaseDatabase.getInstance().getReference("user").child(user.getKey()).child("blocked").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        holder.btnBlock.setText("Unblock");
                        Toast.makeText(getActivity(), "User " + user.getName() + " has been blocked.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    class UserHolder {

        RadioGroup rgRole;
        RadioButton rbSelected, rbUser, rbLibrarian, rbAdmin;
        TextView tvName, tvEmail, tvRole;
        Button btnBlock;

        public UserHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvEmail = (TextView) view.findViewById(R.id.tvEmail);
            tvRole = (TextView) view.findViewById(R.id.tvRole);

            rgRole = (RadioGroup) view.findViewById(R.id.rgRole);
            rbUser = (RadioButton) view.findViewById(R.id.rbUser);
            rbLibrarian = (RadioButton) view.findViewById(R.id.rbLibrarian);
            rbAdmin = (RadioButton) view.findViewById(R.id.rbAdmin);

            btnBlock = (Button) view.findViewById(R.id.btnBlock);
        }

    }
}
