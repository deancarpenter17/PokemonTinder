package com.carpenter.dean.pokemontinder;

import android.util.Log;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by dean on 11/16/2016.
 */

public class PokemonDownloader {
    public static final String TAG = "PokemonDownloader";
    private OkHttpClient client;

    public PokemonDownloader() {
        this.client = new OkHttpClient.Builder()
                .build();
    }

    public void getPokemon(String pokemonId, Callback callback) {

        Log.d(TAG, pokemonId);

        Request request = new Request.Builder()
                .get()
                .url("http://pokeapi.co/api/v2/pokemon/" + pokemonId)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void getPokemon(int numPokemon, Callback callback) {
        for (int i = 0; i < numPokemon; i++) {

            int randomNum = 1 + (int) (Math.random() * ((721 - 1) + 1));

            Request request = new Request.Builder()
                    .get()
                    .url("http://pokeapi.co/api/v2/pokemon/" + randomNum)
                    .build();
            client.newCall(request).enqueue(callback);
        }
    }
}
