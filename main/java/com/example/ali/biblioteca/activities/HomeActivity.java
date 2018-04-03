package com.example.ali.biblioteca.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.fragments.AccountFragment;
import com.example.ali.biblioteca.fragments.AddBookFragment;
import com.example.ali.biblioteca.fragments.AddStockFragment;
import com.example.ali.biblioteca.fragments.BookFragment;
import com.example.ali.biblioteca.fragments.BooksLoanedFragment;
import com.example.ali.biblioteca.fragments.EditFragment;
import com.example.ali.biblioteca.fragments.HistoryFragment;
import com.example.ali.biblioteca.fragments.LoanToFragment;
import com.example.ali.biblioteca.fragments.ManageFragment;
import com.example.ali.biblioteca.fragments.RulesFragment;
import com.example.ali.biblioteca.fragments.SearchBookFragment;
import com.example.ali.biblioteca.model.Rules;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.example.ali.biblioteca.utility.NotificationReceiver;
import com.example.ali.biblioteca.utility.PopupActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public final static int NOTIFICATION_REQUEST_CODE = 100;

    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseUser fbUser;
    private TextView tvName, tvFunction;
    private DatabaseReference mDatabase;
    private ViewPager viewPager;
    private int currentFragment;
    private FragmentStateAdapter pageAdapter;
    private Stock transferStock;

    private Rules rules;

    private String orderCriteria = SearchBookFragment.CRITERIA_TITLE;
    private String searchGenre = "";
    private String currentSearchText = "";

    private User currentUser = null;
    private boolean receivedNotitificantion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        viewPager = (ViewPager) findViewById(R.id.fragmentContainer);
        setupViewPager(viewPager);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        fbUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadElements();

        receivedNotitificantion = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            User userNotification = (User) bundle.getSerializable("Notification");
            if(userNotification != null) {
                receivedNotitificantion = true;
                //currentUser = userNotification;
                //setViewPager(FragmentStateAdapter.HOME_HISTORY);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setViewPager(FragmentStateAdapter.HOME_HISTORY);
                    }
                }, 1000);
            }
        }

        updateRules();

        if(fbUser != null) {
            tvName.setText(fbUser.getDisplayName());
            setUserRole();
        }


    }

    private void startNotification() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 5);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        //Log.e("Notification", "Set");
    }

    public Rules getRules() {
        return this.rules;
    }

    private void updateRules() {
        mDatabase.child("rules").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rules = dataSnapshot.getValue(Rules.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Stock getTransferStock() {
        return transferStock;
    }

    public void setTransferStock(Stock transferStock) {
        this.transferStock = transferStock;
    }

    public int getCurrentFragment() {
        return this.currentFragment;
    }

    private void setupViewPager(ViewPager viewPager) {
        pageAdapter = new FragmentStateAdapter(getSupportFragmentManager());
        pageAdapter.addFragment(new SearchBookFragment(), "Search book");
        pageAdapter.addFragment(new AddStockFragment(), "Add stock");
        pageAdapter.addFragment(new AddBookFragment(), "Add book");
        pageAdapter.addFragment(new BookFragment(), "Book");
        pageAdapter.addFragment(new AccountFragment(), "Account Info");
        pageAdapter.addFragment(new RulesFragment(), "Rules");
        pageAdapter.addFragment(new ManageFragment(), "Manage");
        pageAdapter.addFragment(new LoanToFragment(), "Loan to");
        pageAdapter.addFragment(new HistoryFragment(), "History");
        pageAdapter.addFragment(new EditFragment(), "Edit stock");
        pageAdapter.addFragment(new BooksLoanedFragment(), "Books loaned");
        viewPager.setAdapter(pageAdapter);

        currentFragment = FragmentStateAdapter.HOME_SEARCH_BOOK;
    }

    public void setViewPager(int fragmentId) {
        currentFragment = fragmentId;
        viewPager.setCurrentItem(fragmentId, false);
        invalidateOptionsMenu();
        switch (fragmentId) {
            case FragmentStateAdapter.HOME_SEARCH_BOOK:
                ((SearchBookFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.HOME_ADD_BOOK:
                ((AddStockFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.ADD_BOOK_COPY:
                ((AddBookFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.SEARCH_SHOW_BOOK:
                ((BookFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.HOME_ACCOUNT_INFO:
                ((AccountFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.HOME_CHANGE_RULES:
                ((RulesFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.HOME_MANAGE_ACCOUNTS:
                ((ManageFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.BOOK_LOAN_TO:
                ((LoanToFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.HOME_HISTORY:
                ((HistoryFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.EDIT_BOOK:
                ((EditFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
            case FragmentStateAdapter.HOME_BOOKS_LOANED:
                ((BooksLoanedFragment)pageAdapter.getItem(fragmentId)).refreshFragment();
                break;
        }
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    private void setUserRole() {
        String userId = fbUser.getUid();
        mDatabase.child("user").orderByChild("email").equalTo(fbUser.getEmail()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.e("User", dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if(user != null) {
                        user.setKey(ds.getKey());
                        currentUser = user;
                        optionsForRole(user.getRole());
                        if(user.isBlocked()) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getApplicationContext(), "Your account is blocked. You're being logged out.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        if(!receivedNotitificantion)
                            startNotification();

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void optionsForRole(int role) {
        Menu menu = navigationView.getMenu();

        if(role == User.ROLE_USER) {
            menu.setGroupVisible(R.id.menuLibrarian, false);
            menu.setGroupVisible(R.id.menuAdmin, false);
            tvFunction.setText("User");
        }
        else if(role == User.ROLE_LIBRARIAN) {
            menu.setGroupVisible(R.id.menuAdmin, false);
            menu.setGroupVisible(R.id.menuLibrarian, true);
            tvFunction.setText("Librarian");
        }
        else if(role == User.ROLE_ADMIN) {
            tvFunction.setText("Admin");
            menu.setGroupVisible(R.id.menuLibrarian, false);
            menu.setGroupVisible(R.id.menuAdmin, true);
        }
    }

    private void loadElements() {
        View hView = navigationView.getHeaderView(0);
        tvName = (TextView) hView.findViewById(R.id.tvName);
        tvFunction = (TextView) hView.findViewById(R.id.tvFunction);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            switch (currentFragment) {
                case FragmentStateAdapter.HOME_SEARCH_BOOK:
                    moveTaskToBack(true);
                    break;
                case FragmentStateAdapter.HOME_ACCOUNT_INFO:
                case FragmentStateAdapter.SEARCH_SHOW_BOOK:
                case FragmentStateAdapter.HOME_ADD_BOOK:
                case FragmentStateAdapter.HOME_CHANGE_RULES:
                case FragmentStateAdapter.HOME_HISTORY:
                case FragmentStateAdapter.HOME_MANAGE_ACCOUNTS:
                case FragmentStateAdapter.HOME_BOOKS_LOANED:
                    setViewPager(FragmentStateAdapter.HOME_SEARCH_BOOK);
                    break;
                case FragmentStateAdapter.ADD_BOOK_COPY:
                case FragmentStateAdapter.BOOK_LOAN_TO:
                case FragmentStateAdapter.EDIT_BOOK:
                    setViewPager(FragmentStateAdapter.SEARCH_SHOW_BOOK);
                    break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem itemSearch = menu.findItem(R.id.menuSearch);
        MenuItem itemFilter = menu.findItem(R.id.menuFilter);
        if(currentFragment == FragmentStateAdapter.HOME_SEARCH_BOOK) {
            itemSearch.setVisible(true);
            itemFilter.setVisible(true);

            SearchView searchView = (SearchView) itemSearch.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ((SearchBookFragment)pageAdapter.getItem(viewPager.getCurrentItem())).selectSpecificStock(orderCriteria, searchGenre, newText);
                    currentSearchText = newText;
                    return false;
                }
            });

            itemFilter.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent popupBox = new Intent(HomeActivity.this, PopupActivity.class);
                    popupBox.putExtra("orderCriteria", orderCriteria);
                    popupBox.putExtra("searchGenre", searchGenre);
                    startActivityForResult(popupBox, 99);

                    return false;
                }
            });
        }
        else if(currentFragment == FragmentStateAdapter.HOME_MANAGE_ACCOUNTS){
            itemSearch.setVisible(true);
            itemFilter.setVisible(false);

            SearchView searchView = (SearchView) itemSearch.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ((ManageFragment)pageAdapter.getItem(viewPager.getCurrentItem())).selectSpecificUser(ManageFragment.ORDER_NAME, newText);

                    return false;
                }
            });
        }
        else if(currentFragment == FragmentStateAdapter.BOOK_LOAN_TO) {
            itemSearch.setVisible(true);
            itemFilter.setVisible(false);

            SearchView searchView = (SearchView) itemSearch.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ((LoanToFragment)pageAdapter.getItem(viewPager.getCurrentItem())).selectSpecificUser(ManageFragment.ORDER_NAME, newText);

                    return false;
                }
            });
        }
        else {
            itemSearch.setVisible(false);
            itemFilter.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_logout) {
            if(fbUser != null) {
                mAuth.signOut();
                Intent goToLogin = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(goToLogin);
            }
        }
        else if(id == R.id.nav_add_book) {
            setViewPager(FragmentStateAdapter.HOME_ADD_BOOK);
        }
        else if(id == R.id.nav_search_book) {
            setViewPager(FragmentStateAdapter.HOME_SEARCH_BOOK);
        }
        else if(id == R.id.nav_account_info) {
            setViewPager(FragmentStateAdapter.HOME_ACCOUNT_INFO);
        }
        else if(id == R.id.nav_rules) {
            setViewPager(FragmentStateAdapter.HOME_CHANGE_RULES);
        }
        else if(id == R.id.nav_manage_accounts) {
            setViewPager(FragmentStateAdapter.HOME_MANAGE_ACCOUNTS);
        }
        else if(id == R.id.nav_history) {
            setViewPager(FragmentStateAdapter.HOME_HISTORY);
        }
        else if(id == R.id.nav_books_loaned) {
            setViewPager(FragmentStateAdapter.HOME_BOOKS_LOANED);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 99) {
            orderCriteria = ((String) data.getStringExtra("orderCriteria")).toLowerCase();
            searchGenre = ((String) data.getStringExtra("genre")).toLowerCase();

            //Toast.makeText(getApplicationContext(), "Criteria: " + orderCriteria + " | " + searchGenre, Toast.LENGTH_SHORT).show();
            switch (currentFragment) {
                case FragmentStateAdapter.HOME_SEARCH_BOOK:
                    ((SearchBookFragment)pageAdapter.getItem(viewPager.getCurrentItem())).selectSpecificStock(orderCriteria, searchGenre, currentSearchText);
            }

        }
    }
}
