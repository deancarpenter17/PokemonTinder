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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

    private final static String TAG = "ConversationActivity";
    private final static String CURRENT_USER_EXTRA = "currentUser";
    private final static String OTHER_USER_EXTRA = "otherUser";

    /**
     * messageId ensures the same id for messages between 2 people in the database.
     * created by comparing firebaseIds: smallerId_biggerId
     */
    private String messageId;

    private User currentUser;
    private User otherUser;
    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;
    private DatabaseReference messageRef;
    private DatabaseReference userRef;

    private RecyclerView messageRecyclerView;
    private FirebaseRecyclerAdapter<Message, MessageHolder> adapter;
    private LinearLayoutManager linearLayoutManager;

    private EditText messageEditText;
    private Button sendButton;

    private ListView drawerList;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String activityTitle;

    public static Intent newIntent(Context context, User currentUser, User otherUser) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        intent.putExtra(OTHER_USER_EXTRA, otherUser);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userRef = databaseReference.child("users").child(firebaseUser.getUid());
        Log.d(TAG, ""+firebaseUser.getUid());
        currentUser = getIntent().getParcelableExtra(CURRENT_USER_EXTRA);
        otherUser = getIntent().getParcelableExtra(OTHER_USER_EXTRA);

        Log.d(TAG, "Current User"+currentUser.getName());
        Log.d(TAG, "Other User: "+otherUser.getName());

        // see messageId declaration at beginning of class ^^^
        messageId = currentUser.getUuid().compareTo(otherUser.getUuid()) < 0 ?
                currentUser.getUuid() + "_" + otherUser.getUuid() :
                otherUser.getUuid() + "_" + currentUser.getUuid();
        messageRef = databaseReference.child("messages").child(messageId);

        messageRecyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);
        messageRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        adapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(
                Message.class,
                R.layout.item_message,
                MessageHolder.class,
                messageRef
        ) {
            @Override
            protected void populateViewHolder(MessageHolder viewHolder, Message model, int position) {
                viewHolder.messageTextView.setText(model.getMessage());
                viewHolder.messengerTextView.setText(model.getName());
                Picasso.with(getApplicationContext()).load(model.getPhotoUrl()).into(
                        viewHolder.messengerImageView);
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    messageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        messageRecyclerView.setAdapter(adapter);

        messageEditText = (EditText) findViewById(R.id.messageEditText);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message(
                        currentUser.getName(),
                        messageEditText.getText().toString(),
                        currentUser.getPokemon().getSprites().getFrontDefault()
                );
                messageRef.push().setValue(message);
                messageEditText.setText("");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_activity_conversation);
        activityTitle = getTitle().toString();
        drawerList = (ListView) findViewById(R.id.navList_activity_conversation);
        addDrawerItems();
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        startActivity(MainActivity.newIntent(getApplicationContext(), currentUser));
                        break;
                    }
                    case 1: {
                        startActivity(MatchesActivity.newIntent(getApplicationContext(), currentUser));
                        break;
                    }
                    case 2: {
                        startActivity(MessagesActivity.newIntent(getApplicationContext(), currentUser));
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

    }

    public static class MessageHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
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
