package com.carpenter.dean.pokemontinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.carpenter.dean.pokemontinder.pokemon.Pokemon;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    Button mLoginButton;
    private ArrayList<Pokemon> mPokemonArrayList;

    Pokemon dbTest;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference();
    Map<String, Pokemon> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPokemonArrayList = new ArrayList<>();
        users = new HashMap<>();

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = SwipeActivity.newIntent(getApplicationContext(), mPokemonArrayList);
                startActivity(i);
            }
        });

        dbTest();

        new PokemonDownloader().getPokemon(5, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final Gson gson = new Gson();

                try {
                    Pokemon pokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                    mPokemonArrayList.add(pokemon);
                    Log.d("POKEMON CREATED: ", pokemon.getName() + ", URL: " + pokemon.getPokemonUrl().getUrl() +
                            ", Pic URL: " + pokemon.getSprites().getFrontDefault());
                    Log.d("IN ALIST", ""+mPokemonArrayList.size());

                    if (mPokemonArrayList.size() > 4) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLoginButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (Exception err) {
                    Log.e("ERR", err.toString());
                }
            }
        });
    }

    public void dbTest() {

        new PokemonDownloader().getPokemon(1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Gson gson = new Gson();

                try {
                    dbTest = gson.fromJson(response.body().charStream(), Pokemon.class);
                    Log.d("DB POKEMON CREATED: ", dbTest.getName() + ", URL: " + dbTest.getPokemonUrl().getUrl() +
                            ", Pic URL: " + dbTest.getSprites().getFrontDefault());
                    DatabaseReference reference = usersRef.child("users").child("krista");
                    reference.setValue(dbTest);

                } catch (Exception err) {
                    Log.e("ERR", err.toString());
                }
            }
        });
    }

}
