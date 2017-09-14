package com.carpenter.dean.pokemontinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final static String USER = "user";

    private User user;
    private Button findMatchesButton;
    private TextView userLoggedIn;
    private ProgressDialog progressDialog;

    private ListView drawerList;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String activityTitle;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    public static Intent newIntent(Context context, User user) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(USER, user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userLoggedIn = (TextView) findViewById(R.id.main_activity_user_logged_in_text_view);
        progressDialog = ProgressDialog.show(this, "Turning the wrenches..",
                "Loading", true);

        if (getIntent().getParcelableExtra(USER) != null) {
            user = getIntent().getParcelableExtra(USER);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_activity_main);
        activityTitle = getTitle().toString();
        drawerList = (ListView) findViewById(R.id.navList_main_activity);
        addDrawerItems();
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        startActivity(MatchesActivity.newIntent(getApplicationContext(), user));
                        break;
                    }
                    case 1: {
                        startActivity(MessagesActivity.newIntent(getApplicationContext(), user));
                        break;
                    }
                    case 2: {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        break;
                    }
                }
            }
        });
        setupDrawer();
        hasAccount();

        findMatchesButton = (Button) findViewById(R.id.main_activity_find_matches_button);
        findMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SwipeActivity.newIntent(getApplicationContext(),
                        user);
                startActivity(intent);
            }
        });

    }

    // Checks if user has an account and is logged in, if not, redirect to LoginActivity
    public void hasAccount() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        Log.d(TAG, "Firebase user null: " + firebaseUser);
        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.d(TAG, "FIREBASE USER NOT SIGNED IN");
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            databaseReference.child("users").child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                userLoggedIn.setText("Welcome " + user.getName());
                                Log.d(TAG, "User found: " + user.getName());
                            } else {
                                Log.d(TAG, "FIREBASE USER NOT SIGNED IN");
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "Unable to authenticate user!",
                                    Toast.LENGTH_LONG).show();
                            Log.d(TAG, databaseError.getDetails());
                        }
                    });
        }
    }

    private void addDrawerItems() {
        String[] drawerOptions = {"Matches", "Messages", "Sign out"};
        drawerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerOptions);
        drawerList.setAdapter(drawerAdapter);
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Options");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(activityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}

