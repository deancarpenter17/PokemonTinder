package com.carpenter.dean.pokemontinder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MatchesActivity extends AppCompatActivity {

    private static final String TAG = "MatchesActivity";
    private static final String USER = "user";

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<User, UserViewHolder> adapter;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private DatabaseReference matchesRef;

    private static User currentUser;

    private ListView drawerList;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String activityTitle;

    public static Intent newIntent(Context context, User currentUser) {
        Intent intent = new Intent(context, MatchesActivity.class);
        intent.putExtra(USER, currentUser);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUser = getIntent().getParcelableExtra(USER);
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = (RecyclerView) findViewById(R.id.matches_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            matchesRef = databaseReference.child("users").child(user.getUid()).child("matches");
            adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                    User.class,
                    R.layout.matches_item,
                    UserViewHolder.class,
                    matchesRef
            ) {
                @Override
                protected void populateViewHolder(UserViewHolder viewHolder, User model, int position) {
                    viewHolder.mUserName.setText(model.getName());
                    Picasso.with(getApplicationContext())
                            .load(model.getPokemon().getSprites().getFrontDefault())
                            .into(viewHolder.usersPokemonImage);
                    viewHolder.matchesUser = model;
                    Log.d(TAG, "User in Viewholder: "+model.getName());
                }
            };
            recyclerView.setAdapter(adapter);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_activity_matches);
        activityTitle = getTitle().toString();
        drawerList = (ListView) findViewById(R.id.navList_matches_activity);
        addDrawerItems();
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0: {
                        startActivity(MainActivity.newIntent(getApplicationContext(), currentUser));
                        break;
                    }
                    case 1: {
                        startActivity(MessagesActivity.newIntent(getApplicationContext(), currentUser));
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
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        final Context context;
        TextView mUserName;
        ImageView usersPokemonImage;
        User matchesUser;

        public UserViewHolder(View v) {
            super(v);
            context = v.getContext();
            mUserName = (TextView) v.findViewById(R.id.matches_user_name);
            usersPokemonImage = (ImageView) v.findViewById(R.id.matches_user_picture);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ConversationActivity.newIntent(context, currentUser, matchesUser);
            context.startActivity(intent);
        }
    }

    private void addDrawerItems() {
        String[] drawerOptions = {"Main Menu", "Messages", "Sign out"};
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
}
