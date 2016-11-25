package com.carpenter.dean.pokemontinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Collection;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final static String USER = "user";

    User mUser;
    Pokemon mPokemon;
    Button mFindMatchesButton;
    Button mSignoutButton;
    TextView mUserLoggedIn;
    ProgressDialog progressDialog;

    ArrayList<User> mUsers;

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

        hasAccount();
        addUsers();

        mSignoutButton = (Button) findViewById(R.id.sign_out_button);
        mSignoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        mFindMatchesButton = (Button) findViewById(R.id.main_activity_find_matches_button);
        mFindMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUsers.size() > 5) {
                    Intent intent = SwipeActivity.newIntent(getApplicationContext(),
                            mUser, mUsers);
                    startActivity(intent);
                }
                else Toast.makeText(getApplicationContext(),
                        "Not enough users! ", Toast.LENGTH_SHORT).show();
            }
        });


        // SHOW A LOADING SCREEN WHILE ITS WAITING FOR hasAccount to succeed?
    }

    // Checks if user has an account and is logged in, if not, redirect to LoginActivity
    public void hasAccount() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.d(TAG, "FIREBASE USER NOT SIGNED IN");
            progressDialog.dismiss();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            databaseReference.child("users").child(mFirebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUser = dataSnapshot.getValue(User.class);
                            mUserLoggedIn.setText("Welcome " + mUser.getName());
                            Log.d(TAG, "User found: "+mUser.getName());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    //generates random names for db
    public void randomNames() {

        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("bill");
        arrayList.add("jerry");
        arrayList.add("susan");
        arrayList.add("carrie");
        arrayList.add("herbert");
        arrayList.add("frank");
        arrayList.add("dilbert");
        arrayList.add("marysue");
        arrayList.add("pamela");
        arrayList.add("hermie");
        arrayList.add("jeremey");
        arrayList.add("zed");
        arrayList.add("Mr. Potato");
        arrayList.add("eric");
        arrayList.add("gerald");

        final HashMap<String, String> likes = new HashMap<>();
        for(int i = 0; i < 15; i++) {
            int randomNum1 = 0 + (int) (Math.random() * ((15 - 1) + 1));
            int randomNum2 = 0 + (int) (Math.random() * ((500 - 1) + 1));
            likes.put(""+randomNum2, arrayList.get(randomNum1));
        }

        final ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
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
                user.setUuid(""+ids.remove(0));
                user.setName(""+arrayList.remove(0));
                user.setPokemon(pokemon);
                //user.setLikes(likes);
                databaseReference.child("users").child(user.getUuid()).setValue(user);

                Log.d(TAG, ""+user);
/*
                if (mPokemonArrayList.size() > 10) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = SwipeActivity.newIntent(getApplicationContext(),
                                    mPokemonArrayList);
                            startActivity(intent);
                        }
                    });
                }
                */
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
                Log.d(TAG, "SIZE OF mUSERS: "+mUsers.size());
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error accessing user database!");
                progressDialog.dismiss();
            }
        });

    }

}

