package com.carpenter.dean.pokemontinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final static String USER = "user";

    private User mUser;
    private Pokemon mPokemon;
    private Button mFindMatchesButton;
    private Button mSignoutButton;
    private TextView mUserLoggedIn;
    private ProgressDialog progressDialog;

    private ListView mDrawerList;
    private ArrayAdapter<String> mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;


    private ArrayList<User> mUsers;

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
        mUsers = new ArrayList<>();

        if(getIntent().getParcelableExtra(USER) != null) {
            mUser = getIntent().getParcelableExtra(USER);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        mDrawerList = (ListView) findViewById(R.id.navList);
        addDrawerItems();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0: {
                        Toast.makeText(getApplicationContext(), "Messages", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case 1: {
                        Toast.makeText(getApplicationContext(), "Matches", Toast.LENGTH_LONG).show();
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

        //  randomNames();
        hasAccount();
        addUsers();

        mFindMatchesButton = (Button) findViewById(R.id.main_activity_find_matches_button);
        mFindMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUsers.size() > 0) {
                    Intent intent = SwipeActivity.newIntent(getApplicationContext(),
                            mUser, mUsers);
                    startActivity(intent);
                } else Toast.makeText(getApplicationContext(),
                        "No more users in dB! ", Toast.LENGTH_SHORT).show();
            }
        });


        // SHOW A LOADING SCREEN WHILE ITS WAITING FOR hasAccount to succeed?
    }

    // Checks if user has an account and is logged in, if not, redirect to LoginActivity
    public void hasAccount() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        Log.d(TAG, "Firebase user null: " + mFirebaseUser);
        if(mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.d(TAG, "FIREBASE USER NOT SIGNED IN");
            progressDialog.dismiss();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            databaseReference.child("users").child(mFirebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUser = dataSnapshot.getValue(User.class);
                            if(mUser != null) {
                                mUserLoggedIn.setText("Welcome " + mUser.getName());
                                Log.d(TAG, "User found: " + mUser.getName());
                            } else {
                                Log.d(TAG, "FIREBASE USER NOT SIGNED IN");
                                progressDialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    //generates random names for db
    public void randomNames() {

        final ArrayList<String> names = new ArrayList<>();
        names.add("test16");
        names.add("test17");
        names.add("test18");
        names.add("test19");
        names.add("test20");
        names.add("test21");
        names.add("test22");
        names.add("test23");
        names.add("test24");
        names.add("test25");
        names.add("test26");
        names.add("test27");
        names.add("test28");
        names.add("test29");
        names.add("test30");

        final ArrayList<Integer> ids = new ArrayList<>();
        for(int i = 0; i < 15; i++) {
            int randomNum = 1 + (int) (Math.random() * ((721 - 1) + 1));
            ids.add(0, randomNum);
        }

        new PokemonDownloader().getPokemon(15, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Gson gson = new Gson();
                Pokemon pokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                User user = new User();
                user.setUuid("" + ids.remove(0));
                user.setName("" + names.remove(0));
                user.setPokemon(pokemon);
                databaseReference.child("users").child(user.getUuid()).setValue(user);

                Log.d(TAG, "" + user.getName());
            }
        });
    }

    public void addUsers() {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // need to remove likes & matches when adding new users
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    Log.d(TAG, "USER ADDED: " + user.getName());
                    mUsers.add(user);
                }
                Log.d(TAG, "SIZE OF mUSERS: " + mUsers.size());
                // removes previous likes from users pool
                if(mUser != null)
                    mUsers.removeAll(mUser.getLikes().values());
                // removes current user from users pool
                mUsers.remove(mUser);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error accessing user database!");
                progressDialog.dismiss();
            }
        });
    }

    private void addDrawerItems() {
        String[] drawerOptions = {"Messages", "Matches", "Sign out"};
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
        if(mDrawerToggle.onOptionsItemSelected(item)) {
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

