package com.carpenter.dean.pokemontinder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * TO DO:
 * 1.) Set up messages section in dB
 * 2.) Retrive extra from intent on who the other user is
 * 3.) Create the dB reference for the user1_user2 id under messages section in dB. (user1_user2
 *                      is ordered by natural order for uniformity)
 * 4.) Retrieve those messages with RecyclerView
 */

public class MessageActivity extends AppCompatActivity {

    private final static String TAG = "MessageActivity";
    private final static String CURRENT_USER_EXTRA = "currentUser";
    private final static String OTHER_USER_EXTRA = "otherUser";

    /**
     * messageId ensures the same id for messages between 2 people in the database.
     * created by comparing firebaseIds: smallerId_biggerId
     */
    private String messageId;

    User currentUser;
    User otherUser;
    FirebaseUser firebaseUser;

    DatabaseReference mDatabaseRef;
    DatabaseReference mMessageRef;
    DatabaseReference mUserRef;

    RecyclerView mMessageRecyclerView;
    FirebaseRecyclerAdapter<Message, MessageHolder> mAdapter;
    LinearLayoutManager mLinearLayoutManager;

    private EditText mMessageEditText;
    private Button mSendButton;

    public static Intent newIntent(Context context, User currentUser, User otherUser) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        intent.putExtra(OTHER_USER_EXTRA, otherUser);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mDatabaseRef.child("users").child(firebaseUser.getUid());
        Log.d(TAG, ""+firebaseUser.getUid());
        currentUser = getIntent().getParcelableExtra(CURRENT_USER_EXTRA);
        otherUser = getIntent().getParcelableExtra(OTHER_USER_EXTRA);

        Log.d(TAG, "Current User"+currentUser.getName());
        Log.d(TAG, "Other User: "+otherUser.getName());

        // see messageId declaration at beginning of class ^^^
        messageId = currentUser.getUuid().compareTo(otherUser.getUuid()) < 0 ?
                currentUser.getUuid() + "_" + otherUser.getUuid() :
                otherUser.getUuid() + "_" + currentUser.getUuid();
        mMessageRef = mDatabaseRef.child("messages").child(messageId);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);
        mMessageRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(
                Message.class,
                R.layout.item_message,
                MessageHolder.class,
                mMessageRef
        ) {
            @Override
            protected void populateViewHolder(MessageHolder viewHolder, Message model, int position) {
                viewHolder.messageTextView.setText(model.getMessage());
                viewHolder.messengerTextView.setText(model.getName());
                Picasso.with(getApplicationContext()).load(model.getPhotoUrl()).into(
                        viewHolder.messengerImageView);
            }
        };

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message(
                        currentUser.getName(),
                        mMessageEditText.getText().toString(),
                        currentUser.getPokemon().getSprites().getFrontDefault()
                );
                mMessageRef.push().setValue(message);
                mMessageEditText.setText("");
            }
        });

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


}
