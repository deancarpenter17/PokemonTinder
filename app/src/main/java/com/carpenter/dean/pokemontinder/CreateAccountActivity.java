package com.carpenter.dean.pokemontinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.carpenter.dean.pokemontinder.pokemon.Pokemon;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateAccountActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private final static int RC_SIGN_IN = 0;
    private final static String USER = "user";

    private Pokemon mPokemon;
    private boolean picLoaded = false;
    private User mUser;

    private EditText mPokemonEditText;
    private ImageView mPokemonImage;
    private Button mFindPokemon;
    private Button mCreateAccount;
    private SignInButton mSignInButton;
    private ProgressDialog progressDialog;

    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mUser = new User();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mCreateAccount = (Button) findViewById(R.id.account_activity_create_account_button);
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser == null) {
                    Toast.makeText(getApplicationContext(), "Must sign in with Google",
                            Toast.LENGTH_LONG).show();
                } else if (mUser.getPokemon() == null) {
                    Toast.makeText(getApplicationContext(), "Must choose a Pokemon!",
                            Toast.LENGTH_LONG).show();
                } else if (mUser.getName() != null && mUser.getUuid() != null) {
                    mDatabase.child("users").child(mUser.getUuid()).setValue(mUser);
                    Intent intent = MainActivity.newIntent(getApplicationContext(), mUser);
                    startActivity(intent);
                }
            }
        });

        mPokemonEditText = (EditText) findViewById(R.id.choose_pokemon_edit_text);
        mPokemonEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPokemonEditText.setHint(R.string.pokemon_choice_hint);
                } else {
                    mPokemonEditText.setHint("");
                }
            }
        });
        mPokemonImage = (ImageView) findViewById(R.id.account_creation_pokemon_pic);
        mFindPokemon = (Button) findViewById(R.id.find_pokemon);
        mFindPokemon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboardFrom(getApplicationContext(), mPokemonEditText);
                progressDialog = ProgressDialog.show(CreateAccountActivity.this, "Turning the wrenches..",
                        "Loading", true);

                new PokemonDownloader().getPokemon(mPokemonEditText.getText().toString()
                        , new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("CreateAccountActivity", "Error downloading Pokemon!");
                                //Updating UI THREAD on BACKGROUND THREAD, POTENTIAL BUG
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final Gson gson = new Gson();

                                try {
                                    mPokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                                    Log.d("POKEMON CREATED: ", mPokemon.getName() + ", URL: "
                                            + mPokemon.getPokemonUrl().getUrl() +
                                            ", Pic URL: " + mPokemon.getSprites().getFrontDefault());
                                    final String picUrl = mPokemon.getSprites().getFrontDefault();
                                    if (picUrl != null) {
                                        CreateAccountActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Picasso.with(getApplicationContext()).load(picUrl).into(mPokemonImage);
                                                picLoaded = true;
                                            }
                                        });
                                    }
                                    mUser.setPokemon(mPokemon);
                                    progressDialog.dismiss();

                                } catch (Exception errSwipe) {
                                    Log.e("Network Error: CAA", errSwipe.toString());
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Network Error downloading Pokemon",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    mUser.setUuid(mFirebaseUser.getUid());
                    mUser.setName(mFirebaseUser.getDisplayName());
                    Log.d("onAuthStateChanged", "signed_in:" + mFirebaseUser.getUid());
                } else {
                    // User is signed out
                    Log.d("", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mSignInButton = (SignInButton) findViewById(R.id.create_account_sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.create_account_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient.clearDefaultAccountAndReconnect();
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(this, acct.getDisplayName(), Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(acct);
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("AUTH", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("AUTH", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the mFirebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in mFirebaseUser can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("AUTH", "signInWithCredential", task.getException());
                            Toast.makeText(CreateAccountActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
