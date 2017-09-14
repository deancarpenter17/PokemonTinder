package com.carpenter.dean.pokemontinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class GoogleLoginFragment extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener {

    private final static int RC_SIGN_IN = 0;
    private final static String USER = "user";
    private final static String TAG = "GoogleLoginFragment";

    private Pokemon pokemon;

    private User user;

    private SignInButton signInButton;
    private Button nextButton;
    private ProgressDialog progressDialog;

    private DatabaseReference dataBase;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener authStateListener;

    private Callbacks callbacks;

    public static Fragment newInstance() {
        return new GoogleLoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_google_login, container, false);

        user = new User();

        dataBase = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null) {
                    user.setUuid(firebaseUser.getUid());
                    // only want to store first name in database
                    int i = firebaseUser.getDisplayName().indexOf(' ');
                    String name = firebaseUser.getDisplayName().substring(0, i);
                    user.setName(name);
                    Log.d("onAuthStateChanged", "signed_in:" + firebaseUser.getUid());
                    Toast.makeText(getContext(), firebaseUser.getDisplayName() + " signed in!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) v.findViewById(R.id.create_account_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        v.findViewById(R.id.create_account_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleApiClient.clearDefaultAccountAndReconnect();
                signIn();
            }
        });

        nextButton = (Button) v.findViewById(R.id.login_fragment_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseUser == null) {
                    Toast.makeText(getContext(), "MUST SIGN IN WITH GOOGLE!", Toast.LENGTH_LONG).show();
                }
                else if(user != null) {
                    callbacks.switchToPokemonChooserFrag(user);
                }
                else {
                    Toast.makeText(getContext(), "Error logging in!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return v;
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", "handleSignInResult:" + result.isSuccess());
        if(result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        } else {
            Log.e(TAG, "Could not sign in!");
            Toast.makeText(getContext(), "Error logging in!", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("AUTH", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("AUTH", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the firebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in firebaseUser can be handled in the listener.
                        if(!task.isSuccessful()) {
                            Log.w("AUTH", "signInWithCredential", task.getException());
                            Toast.makeText(getContext(), "Unable to log in!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Callbacks) {
            callbacks = (Callbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    public interface Callbacks {
        // TODO: Update argument type and name
        public void switchToPokemonChooserFrag(User user);
    }
}


