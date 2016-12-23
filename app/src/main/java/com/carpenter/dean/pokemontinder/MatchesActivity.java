package com.carpenter.dean.pokemontinder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MatchesActivity extends AppCompatActivity {

    private static final String TAG = "MatchesActivity";
    private static final String USER = "user";

    RecyclerView mRecyclerView;
    FirebaseRecyclerAdapter<User, UserViewHolder> mAdapter;
    FirebaseUser mUser;
    DatabaseReference mDatabaseRef;
    DatabaseReference matchesRef;

    static User currentUser;

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
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRecyclerView = (RecyclerView) findViewById(R.id.matches_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(mUser != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            matchesRef = mDatabaseRef.child("users").child(mUser.getUid()).child("matches");
            mAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                    User.class,
                    android.R.layout.two_line_list_item,
                    UserViewHolder.class,
                    matchesRef
            ) {
                @Override
                protected void populateViewHolder(UserViewHolder viewHolder, User model, int position) {
                    viewHolder.mText1.setText(model.getName());
                    viewHolder.matchesUser = model;
                    Log.d(TAG, "User in Viewholder: "+model.getName());
                }
            };
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        final Context context;
        TextView mText1;
        TextView mText2;
        User matchesUser;

        public UserViewHolder(View v) {
            super(v);
            context = v.getContext();
            mText1 = (TextView) v.findViewById(android.R.id.text1);
            mText2 = (TextView) v.findViewById(android.R.id.text2);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = MessageActivity.newIntent(context, currentUser, matchesUser);
            context.startActivity(intent);
        }
    }
}
