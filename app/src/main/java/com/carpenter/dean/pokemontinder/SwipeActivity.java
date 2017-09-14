package com.carpenter.dean.pokemontinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.carpenter.dean.pokemontinder.pokemon.Move;
import com.carpenter.dean.pokemontinder.pokemon.Pokemon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SwipeActivity extends AppCompatActivity {

    private static final String USER = "user";
    private static final String TAG = "SwipeActivity";

    private UserAdapter userAdapter;
    private User user;
    private ArrayList<User> usersList;
    private HashMap<String, User> matches;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference userLikesRef;
    private DatabaseReference userMatchesRef;

    private ListView drawerList;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String activityTitle;

    private FloatingActionButton heartButton;
    private FloatingActionButton xButton;
    private SwipeFlingAdapterView flingContainer;

    public static Intent newIntent(Context context, User currentUser) {
        Intent intent = new Intent(context, SwipeActivity.class);
        intent.putExtra(USER, currentUser);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        user = getIntent().getParcelableExtra(USER);
        Log.d(TAG, "Current USER: " + user.getName());
        usersList = new ArrayList<>();

        matches = user.getMatches();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("users");
        userLikesRef = usersRef.child(user.getUuid()).child("likes");
        userMatchesRef = usersRef.child(user.getUuid()).child("matches");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_swipe);
        activityTitle = getTitle().toString();
        drawerList = (ListView) findViewById(R.id.navList_swipe_activity);
        addDrawerItems();
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0: {
                        startActivity(MainActivity.newIntent(getApplicationContext(), user));
                        break;
                    }
                    case 1: {
                        startActivity(MatchesActivity.newIntent(getApplicationContext(), user));
                        break;
                    }
                    case 2: {
                        startActivity(MessagesActivity.newIntent(getApplicationContext(), user));
                        break;
                    }
                    case 3: {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        break;
                    }
                }
            }
        });
        setupDrawer();

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.SwipeFlingContainer);
        userAdapter = new UserAdapter(getApplicationContext(), usersList);
        flingContainer.setAdapter(userAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                if(usersList.size() > 0) {
                    usersList.remove(0);
                    userAdapter.notifyDataSetChanged();
                } else
                    Toast.makeText(getApplicationContext(), "No more users in dB :(", Toast.LENGTH_LONG)
                            .show();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

                User user = (User) dataObject;
                Map<String, Object> childUpdates = new HashMap<>();
                User tempUser = new User(user.getUuid(), user.getName());
                childUpdates.put("/users/" + SwipeActivity.this.user.getUuid() + "/dislikes/" + user.getUuid(), tempUser);
                database.getReference().updateChildren(childUpdates);

            }

            @Override
            public void onRightCardExit(Object dataObject) {
                // Updates likes of the user logged in, while also checking for a match
                // tempUser: lightweight user to be stored in dB under 'likes'/'matches' for current user
                User user = (User) dataObject;
                Map<String, Object> childUpdates = new HashMap<>();
                User tempUser = new User(user.getUuid(), user.getName());
                tempUser.setPokemon(user.getPokemon());
                childUpdates.put("/users/" + SwipeActivity.this.user.getUuid() + "/likes/" + user.getUuid(), tempUser);

                Log.d(TAG, "Match? :" + user.getLikes().keySet().contains(SwipeActivity.this.user.getUuid()));

                if(user.getLikes().keySet().contains(SwipeActivity.this.user.getUuid())) {
                    Toast.makeText(getApplicationContext(), "Its a match!",
                            Toast.LENGTH_LONG).show();
                    // updating matches for current user
                    childUpdates.put("/users/" + SwipeActivity.this.user.getUuid() + "/matches/" + user.getUuid(),
                            tempUser);
                    // updating matches for the user that the current user just matched with
                    tempUser = new User(SwipeActivity.this.user.getUuid(), SwipeActivity.this.user.getName());
                    tempUser.setPokemon(SwipeActivity.this.user.getPokemon());
                    childUpdates.put("/users/" + user.getUuid() + "/matches/" + SwipeActivity.this.user.getUuid(),
                            tempUser);
                }
                database.getReference().updateChildren(childUpdates);


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
                //userAdapter.notifyDataSetChanged();
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

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // need to remove likes & matches when adding new users
                HashMap<String, User> mUsersHashMap = new HashMap<>();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    Log.d(TAG, "USER ADDED: " + user.getUuid() + "Pokemon: " + user.getPokemon().getName() +
                            "Weight: " + user.getPokemon().getWeight());
                    mUsersHashMap.put(user.getUuid(), user);
                }
                // update current user's likes/dislikes/matches
                user = mUsersHashMap.get(user.getUuid());
                Log.d(TAG, "SIZE OF mUSERS before removal: " + mUsersHashMap.size());
                // removes previous likes from users pool
                if(user != null) {
                    Set<String> userLikes = user.getLikes().keySet();
                    Set<String> userDislikes = user.getDislikes().keySet();
                    mUsersHashMap.keySet().removeAll(userLikes);
                    mUsersHashMap.keySet().removeAll(userDislikes);
                    mUsersHashMap.keySet().remove(user.getUuid()); // remove yourself from user pool
                }
                usersList.clear();
                usersList.addAll(mUsersHashMap.values());
                Log.d(TAG, "SIZE OF mUSERS after removal: " + mUsersHashMap.size());
                updateUi();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error accessing user database!");
            }
        });

        xButton = (FloatingActionButton) findViewById(R.id.x_button);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usersList.size() > 0) {
                    flingContainer.getTopCardListener().selectLeft();
                }
                else Toast.makeText(getApplicationContext(), "No more users in dB :(", Toast.LENGTH_LONG)
                        .show();
            }
        });

        heartButton = (FloatingActionButton) findViewById(R.id.heart_button);
        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usersList.size() > 0) {
                    flingContainer.getTopCardListener().selectRight();
                }
                else Toast.makeText(getApplicationContext(), "No more users in dB :(", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private class UserAdapter extends ArrayAdapter<User> {
        public UserAdapter(Context context, ArrayList<User> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view

            if(v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.list_pokemon_view, parent, false);
            }
            if(usersList.size() > 0) {
                Pokemon currentPokemon = usersList.get(position).getPokemon();
                TextView pokemonName = (TextView) v.findViewById(R.id.list_pokemon_view_pokemon_name);
                pokemonName.setText(currentPokemon.getName());

                ImageView pokemonPicture = (ImageView) v.findViewById(R.id.list_pokemon_view_pokemon_picture);
                Picasso.with(getContext()).load(currentPokemon.getSprites().getFrontDefault()).into(pokemonPicture);

                TextView pokemonTypes = (TextView) v.findViewById(R.id.list_pokemon_view_pokemon_types);
                if(currentPokemon.getTypes().size() == 1) {
                    pokemonTypes.setText(currentPokemon.getTypes().get(0).getType().getName() +
                            " type");
                } else if(currentPokemon.getTypes().size() == 2) {
                    pokemonTypes.setText(currentPokemon.getTypes().get(0).getType().getName()
                            + " and " +
                            currentPokemon.getTypes().get(1).getType().getName() +
                            " type");
                }
                TextView height = (TextView) v.findViewById(R.id.list_pokemon_view_pokemon_height);
                height.setText("Height:\n " + currentPokemon.getHeight() + " ft");
                TextView weight = (TextView) v.findViewById(R.id.list_pokemon_view_pokemon_weight);
                weight.setText("Weight:\n" + currentPokemon.getWeight() + " lbs");

                TextView moveOne = (TextView) v.findViewById(R.id.list_pokemon_view_move_one);
                moveOne.setText("First move: " + currentPokemon.getMoves().get(0).getMove().getName());
                TextView moveTwo = (TextView) v.findViewById(R.id.list_pokemon_view_move_two);
                moveTwo.setText(("Second move: " + currentPokemon.getMoves().get(1).getMove().getName()));

                TextView usersName = (TextView) v.findViewById(R.id.list_pokemon_view_users_name);
                usersName.setText("User: " + usersList.get(position).getName());
            }

            return v;
        }
    }

    public void updateUi() {

        Log.i(TAG, "Updating UI, usersList count: " + usersList.size());
        userAdapter.notifyDataSetChanged();
    }

    private void addDrawerItems() {
        String[] drawerOptions = {"Main Menu", "Matches", "Messages", "Sign out"};
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
        if(drawerToggle.onOptionsItemSelected(item)) {
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


    //generates random names for db
    public void randomNames() {

        final ArrayList<String> names = new ArrayList<>();

        names.add("krista");
        names.add("amirah");
        names.add("jacob");
        names.add("benny");
        names.add("bella");
        names.add("steven");
        names.add("jessica");
        names.add("mariah");
        names.add("erik");
        names.add("jodi");

        names.add("stannis");
        names.add("brienne");
        names.add("tyrion");
        names.add("benji");
        names.add("rickon");
        names.add("denaerys");
        names.add("sansa");
        names.add("eddard");
        names.add("arya");
        names.add("cersei");
        names.add("joffrey");
        names.add("melisandre");
        names.add("gregor");
        names.add("ramsay");
        names.add("ygritte");

        final ArrayList<Integer> ids = new ArrayList<>();
        for(int i = 0; i < 25; i++) {
            int randomNum = 1 + (int) (Math.random() * ((721 - 1) + 1));
            ids.add(0, randomNum);
        }

        new PokemonDownloader().getPokemon(25, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Gson gson = new Gson();
                Pokemon pokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                // Database contains heights in decimeters , and weight in hectograms
                // Converting to United States customary units
                // This may break if locale is not English or similar!
                DecimalFormat df = new DecimalFormat("#.##");
                pokemon.setHeight(Double.valueOf(df.format(pokemon.getHeight() * 0.328084)));
                pokemon.setWeight(Double.valueOf(df.format(pokemon.getWeight() * 0.220462)));
                pokemon.setMoves(randomlySelectTwoMoves(pokemon.getMoves()));

                User user = new User();
                user.setUuid("" + ids.remove(0));
                user.setName("" + names.remove(0));
                user.setPokemon(pokemon);
                usersRef.child(user.getUuid()).setValue(user);

                Log.d(TAG, "" + user.getName());
            }
        });
    }

    public ArrayList<Move> randomlySelectTwoMoves(ArrayList<Move> moves) {
        Random random = new Random();
        int one = random.nextInt(moves.size());
        int two;
        do {
            two = random.nextInt(moves.size());
        } while(one == two);
        ArrayList<Move> twoMoves = new ArrayList<>(2);
        twoMoves.add(moves.get(one));
        twoMoves.add(moves.get(two));
        return twoMoves;
    }
}



