package com.carpenter.dean.pokemontinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.carpenter.dean.pokemontinder.pokemon.Move;
import com.carpenter.dean.pokemontinder.pokemon.Pokemon;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callbacks} interface
 * to handle interaction events.
 */
public class PokemonChooserFragment extends Fragment {

    private final static String TAG = "PokemonChooserFragment";
    private final static int RC_SIGN_IN = 0;
    private final static String USER = "user";

    private Pokemon mPokemon;
    private boolean picLoaded = false;
    private User mUser;

    private TextView mPokemonNameTextView;
    private TextView mPokemonTypesTextView;
    private TextView mPokemonHeightTextView;
    private TextView mPokemonWeightTextView;
    private TextView mPokemonMoveOneTextView;
    private TextView mPokemonMoveTwoTextView;
    private TextView mUsersNameTextView;
    private EditText mPokemonEditText;
    private ImageView mPokemonImage;
    private Button mFindPokemon;
    private Button mCreateAccount;
    private ProgressDialog progressDialog;

    private DatabaseReference mDatabase;
    private FirebaseUser mFirebaseUser;

    private Callbacks mListener;

    public static PokemonChooserFragment newInstance(User user) {
        PokemonChooserFragment fragment = new PokemonChooserFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pokemon_chooser, container, false);

        mUser = getArguments().getParcelable(USER);
        if(mUser != null) {
            Log.d(TAG, "USER: " + mUser.getName());
        }
        else {
            Log.e(TAG, "UNABLE TO RETRIEVE CURRENT USER!");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mPokemonEditText = (EditText) v.findViewById(R.id.choose_pokemon_edit_text);
        mPokemonEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mPokemonEditText.setHint(R.string.pokemon_choice_hint);
                }
                else {
                    hideKeyboardFrom(getContext(), mPokemonEditText);
                }
            }
        });
        mPokemonEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_SEND) {
                    hideKeyboardFrom(getContext(), mPokemonEditText);
                    mFindPokemon.performClick();
                    return true;
                }
                return false;
            }
        });
        mPokemonNameTextView = (TextView) v.findViewById(R.id.pokemon_chooser_pokemon_name);
        mPokemonTypesTextView = (TextView) v.findViewById(R.id.pokemon_chooser_pokemon_types);
        mPokemonHeightTextView = (TextView) v.findViewById(R.id.pokemon_chooser_pokemon_height);
        mPokemonWeightTextView = (TextView) v.findViewById(R.id.pokemon_chooser_pokemon_weight);
        mPokemonMoveOneTextView = (TextView) v.findViewById(R.id.pokemon_chooser_move_one);
        mPokemonMoveTwoTextView = (TextView) v.findViewById(R.id.pokemon_chooser_move_two);
        mUsersNameTextView = (TextView) v.findViewById(R.id.pokemon_chooser_users_name);
        mPokemonImage = (ImageView) v.findViewById(R.id.pokemon_chooser_pokemon_picture);
        mFindPokemon = (Button) v.findViewById(R.id.find_pokemon);
        mFindPokemon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboardFrom(getContext(), mPokemonEditText);
                progressDialog = ProgressDialog.show(getContext(), "Turning the wrenches..",
                        "Loading", true);

                new PokemonDownloader().getPokemon(mPokemonEditText.getText().toString().toLowerCase()
                        , new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(TAG, "Error downloading Pokemon!");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Network Error! Could not " +
                                                "connect to Pokeapi.co!", Toast.LENGTH_LONG).show();
                                    }
                                });
                                //Updating UI THREAD on BACKGROUND THREAD, POTENTIAL BUG
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final Gson gson = new Gson();

                                try {

                                    if(response.isSuccessful()) {

                                        mPokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                                        Log.d("POKEMON CREATED: ", mPokemon.getName() +
                                                ", Pic URL: " + mPokemon.getSprites().getFrontDefault() +
                                                ", Height: " + mPokemon.getHeight() +
                                                ", Weight: " + mPokemon.getWeight() +
                                                ", Number of types: " + mPokemon.getTypes().size() +
                                                ", Number of moves: " + mPokemon.getMoves().size()
                                        );
                                        // randomly select 2 moves from all possible moves the dB retrieved for that pokemon
                                        // see method below for more details
                                        mPokemon.setMoves(randomlySelectTwoMoves(mPokemon.getMoves()));

                                        final String picUrl = mPokemon.getSprites().getFrontDefault();
                                        if(picUrl != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Picasso.with(getContext()).load(picUrl).into(mPokemonImage);
                                                    picLoaded = true;
                                                    mPokemonNameTextView.setText(mPokemon.getName());
                                                    if(mPokemon.getTypes().size() == 1) {
                                                        mPokemonTypesTextView.setText(mPokemon.getTypes().get(0).getType().getName() +
                                                                " type");
                                                    } else if(mPokemon.getTypes().size() == 2) {
                                                        mPokemonTypesTextView.setText(mPokemon.getTypes().get(0).getType().getName()
                                                                + " and " +
                                                                mPokemon.getTypes().get(1).getType().getName() +
                                                                " type");
                                                    }
                                                    // Database contains heights in decimeters , and weight in hectograms
                                                    // Converting to United States customary units
                                                    // This may break if locale is not English or similar!
                                                    DecimalFormat df = new DecimalFormat("#.##");
                                                    mPokemon.setHeight(Double.valueOf(df.format(mPokemon.getHeight()*0.328084)));
                                                    mPokemon.setWeight(Double.valueOf(df.format(mPokemon.getWeight()*0.220462)));
                                                    mPokemonHeightTextView.setText("Height:\n" + mPokemon.getHeight() + " ft");
                                                    mPokemonWeightTextView.setText("Weight:\n" + mPokemon.getWeight() + " lbs");

                                                    mPokemonMoveOneTextView.setText("First move: " + mPokemon.getMoves().get(0).getMove().getName());
                                                    mPokemonMoveTwoTextView.setText(("Second move: " + mPokemon.getMoves().get(1).getMove().getName()));

                                                    mUsersNameTextView.setText("User: " + mUser.getName());

                                                }
                                            });
                                        }
                                        mUser.setPokemon(mPokemon);
                                    }

                                    if(!response.isSuccessful()) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(android.text.TextUtils.isDigitsOnly(mPokemonEditText.getText().toString())) {
                                                    Toast.makeText(getContext(),
                                                            "Couldn't find Pokemon. Pokedex range is [1-721]",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Toast.makeText(getContext(),
                                                            "Couldn't find Pokemon. Check spelling & try again",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    progressDialog.dismiss();

                                } catch(Exception errSwipe) {
                                    Log.e("Network Error: CAA", errSwipe.toString());
                                    progressDialog.dismiss();
                                }
                            }
                        });
            }
        });

        mCreateAccount = (Button) v.findViewById(R.id.account_activity_create_account_button);
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser == null) {
                    Toast.makeText(getContext(), "Must sign in with Google",
                            Toast.LENGTH_LONG).show();
                } else if(mUser.getPokemon() == null) {
                    Toast.makeText(getContext(), "Must choose a Pokemon!",
                            Toast.LENGTH_LONG).show();
                } else if(mUser.getName() != null && mUser.getUuid() != null) {
                    mDatabase.child("users").child(mUser.getUuid()).setValue(mUser);
                    Intent intent = MainActivity.newIntent(getContext(), mUser);
                    getActivity().startActivity(intent);
                }
            }
        });

        return v;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /*
    The database retrieves a large list of moves that each pokemon may learn.
    This method extracts two random moves that the User will be assigned.
     */
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Callbacks) {
            mListener = (Callbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface Callbacks {
        // TODO: Update argument type and name
    }
}
