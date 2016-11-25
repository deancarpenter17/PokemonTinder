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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Set;

public class SwipeActivity extends AppCompatActivity {

    private static final String USER = "user";
    private static final String USERS = "users";
    private static final String TAG = "SwipeActivity";

    private UserAdapter mUserAdapter;
    private User mUser;
    private ArrayList<User> mUsers;

    DatabaseReference dbRef;
    DatabaseReference usersRef;

    private FloatingActionButton heartButton;
    private FloatingActionButton xButton;
    private SwipeFlingAdapterView flingContainer;
    ProgressDialog dialog;

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

        dbRef = FirebaseDatabase.getInstance().getReference();
        usersRef = dbRef.child("users");

        mUser = getIntent().getParcelableExtra(USER);
        Log.d(TAG, "USER: "+mUser.getName());
        mUsers = getIntent().getParcelableArrayListExtra(USERS);

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
                mUsers.remove(0);
                mUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

            }

            @Override
            public void onRightCardExit(Object dataObject) {
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                /*al.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
                */
                addUsers(4);
                mUserAdapter.notifyDataSetChanged();
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

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_pokemon_view, parent, false);
            }
            TextView pokemonName = (TextView) convertView.findViewById(R.id.pokemon_name);
            pokemonName.setText(mUsers.get(position).getPokemon().getName());
            ImageView pokemonPicture = (ImageView) convertView.findViewById(R.id.pokemon_picture);
            Picasso.with(getContext()).load(mUsers.get(position).getPokemon().getSprites().getFrontDefault()).into(pokemonPicture);


            return convertView;
        }
    }
/*
    public void addPokemon(int numOfPokemon) {
        new PokemonDownloader().getPokemon(numOfPokemon, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final Gson gson = new Gson();

                try {
                    Pokemon pokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                    mPokemonArrayList.add(pokemon);
                    Log.d("POKEMON CREATED: ", pokemon.getName() + ", URL: " + pokemon.getPokemonUrl().getUrl() +
                            ", Pic URL: " + pokemon.getSprites().getFrontDefault());
                } catch (Exception errSwipe) {
                    Log.e("Network Error Swipe", errSwipe.toString());
                }
            }
        });
    }
    */

    public void addUsers(int numOfUsers) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // need to remove likes & matches when adding new users
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    mUsers.add(child.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
