package com.carpenter.dean.pokemontinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SwipeActivity extends AppCompatActivity {

    private static final String USER = "user";
    private static final String USERS = "users";
    private static final String TAG = "SwipeActivity";

    private UserAdapter mUserAdapter;
    private User mUser;
    private ArrayList<User> mUsers;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserLikesRef;
    private DatabaseReference mUserMatchesRef;

    private FloatingActionButton heartButton;
    private FloatingActionButton xButton;
    private SwipeFlingAdapterView flingContainer;
    private ProgressDialog dialog;

    public static Intent newIntent(Context context, User currentUser, ArrayList<User> users) {
        Intent intent = new Intent(context, SwipeActivity.class);
        intent.putExtra(USER, currentUser);
        intent.putParcelableArrayListExtra(USERS, users);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        mUser = getIntent().getParcelableExtra(USER);
        Log.d(TAG, "USER: " + mUser.getName());
        mUsers = getIntent().getParcelableArrayListExtra(USERS);

        mDatabase = FirebaseDatabase.getInstance();
        mUsersRef = mDatabase.getReference().child("users");
        mUserLikesRef = mUsersRef.child(mUser.getUuid()).child("likes");
        mUserMatchesRef = mUsersRef.child(mUser.getUuid()).child("matches");

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.SwipeFlingContainer);

        xButton = (FloatingActionButton) findViewById(R.id.x_button);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectLeft();
            }
        });

        heartButton = (FloatingActionButton) findViewById(R.id.heart_button);
        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectRight();
            }
        });

        mUserAdapter = new UserAdapter(getApplicationContext(), mUsers);
        flingContainer.setAdapter(mUserAdapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                if(mUsers.size() > 0) {
                    mUsers.remove(0);
                    mUserAdapter.notifyDataSetChanged();
                }
                else Toast.makeText(getApplicationContext(), "No more users in dB :(", Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

            }

            @Override
            public void onRightCardExit(Object dataObject) {
                // Updates likes of the user logged in, while also checking for a match
                User user = (User) dataObject;
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/users/"+mUser.getUuid()+"/likes/"+user.getUuid(), user);

                if(user.getLikes().values().contains(mUser)) {
                    Toast.makeText(getApplicationContext(), "Its a match!",
                            Toast.LENGTH_LONG).show();
                    // updating matches for current user
                    childUpdates.put("/users/"+mUser.getUuid()+"/matches/"+user.getUuid(),
                            user);
                    // updating matches for the user that the current user just matched with
                    childUpdates.put("/users/"+user.getUuid()+"/matches/"+mUser.getUuid(),
                            mUser);
                }
                mDatabase.getReference().updateChildren(childUpdates);


            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                /*al.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
                */
                //addUsers(4);
                //mUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float v) {

            }
        });

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

            }
        });
    }

    private class UserAdapter extends ArrayAdapter<User> {
        public UserAdapter(Context context, ArrayList<User> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view

            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_pokemon_view, parent, false);
            }
            TextView pokemonName = (TextView) convertView.findViewById(R.id.pokemon_name);
            pokemonName.setText(mUsers.get(position).getPokemon().getName());
            ImageView pokemonPicture = (ImageView) convertView.findViewById(R.id.pokemon_picture);
            Picasso.with(getContext()).load(mUsers.get(position).getPokemon().getSprites().getFrontDefault()).into(pokemonPicture);


            return convertView;
        }
    }
}
