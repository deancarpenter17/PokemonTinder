package com.carpenter.dean.pokemontinder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MessagesActivity extends AppCompatActivity {

    private static final String TAG = "MessagesActivity";
    private static final String USER = "user";

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<User, MessagesViewHolder> mAdapter;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference matchesRef;

    private static User currentUser;

    public static Intent newIntent(Context context, User currentUser) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(USER, currentUser);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        currentUser = getIntent().getParcelableExtra(USER);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRecyclerView = (RecyclerView) findViewById(R.id.messages_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(mUser != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            matchesRef = mDatabaseRef.child("users").child(mUser.getUid()).child("matches");
            mAdapter = new FirebaseRecyclerAdapter<User, MessagesViewHolder>(
                    User.class,
                    R.layout.messages_item,
                    MessagesViewHolder.class,
                    matchesRef
            ) {
                @Override
                protected void populateViewHolder(MessagesViewHolder viewHolder, User model, int position) {
                    viewHolder.mUserName.setText(model.getName());
                    Picasso.with(getApplicationContext())
                            .load(model.getPokemon().getSprites().getFrontDefault())
                            .into(viewHolder.usersPokemonImage);
                    viewHolder.matchesUser = model;
                    Log.d(TAG, "User in Viewholder: "+model.getName());
                }
            };
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        final Context context;
        TextView mUserName;
        ImageView usersPokemonImage;
        User matchesUser;

        public MessagesViewHolder(View v) {
            super(v);
            context = v.getContext();
            mUserName = (TextView) v.findViewById(R.id.messages_user_name);
            usersPokemonImage = (ImageView) v.findViewById(R.id.messages_user_picture);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ConversationActivity.newIntent(context, currentUser, matchesUser);
            context.startActivity(intent);
        }
    }
}
