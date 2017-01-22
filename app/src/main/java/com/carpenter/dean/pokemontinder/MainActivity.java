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

import com.carpenter.dean.pokemontinder.pokemon.Pokemon;
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

    private User mUser;
    private Pokemon mPokemon;
    private Button mFindMatchesButton;
    private TextView mUserLoggedIn;
    private ProgressDialog progressDialog;

    private ListView mDrawerList;
    private ArrayAdapter<String> mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
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
        mUserLoggedIn = (TextView) findViewById(R.id.main_activity_user_logged_in_text_view);
        progressDialog = ProgressDialog.show(this, "Turning the wrenches..",
                "Loading", true);

        if (getIntent().getParcelableExtra(USER) != null) {
            mUser = getIntent().getParcelableExtra(USER);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_activity_main);
        mActivityTitle = getTitle().toString();
        mDrawerList = (ListView) findViewById(R.id.navList_main_activity);
        addDrawerItems();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        startActivity(MatchesActivity.newIntent(getApplicationContext(), mUser));
                        break;
                    }
                    case 1: {
                        startActivity(MessagesActivity.newIntent(getApplicationContext(), mUser));
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

        mFindMatchesButton = (Button) findViewById(R.id.main_activity_find_matches_button);
        mFindMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SwipeActivity.newIntent(getApplicationContext(),
                        mUser);
                startActivity(intent);
            }
        });

    }

    // Checks if user has an account and is logged in, if not, redirect to LoginActivity
    public void hasAccount() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        Log.d(TAG, "Firebase user null: " + mFirebaseUser);
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.d(TAG, "FIREBASE USER NOT SIGNED IN");
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            databaseReference.child("users").child(mFirebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUser = dataSnapshot.getValue(User.class);
                            if (mUser != null) {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                mUserLoggedIn.setText("Welcome " + mUser.getName());
                                Log.d(TAG, "User found: " + mUser.getName());
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
        mDrawerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerOptions);
        mDrawerList.setAdapter(mDrawerAdapter);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
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
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}

