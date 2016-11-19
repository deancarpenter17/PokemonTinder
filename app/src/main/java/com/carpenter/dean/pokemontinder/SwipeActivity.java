package com.carpenter.dean.pokemontinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.carpenter.dean.pokemontinder.pokemon.Pokemon;
import com.google.gson.Gson;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;

public class SwipeActivity extends AppCompatActivity {

    private static final String POKEMONLIST = "pokemonlist";

    private ArrayList<Pokemon> mPokemon;
    private PokemonAdapter mPokemonAdapter;

    private FloatingActionButton heartButton;
    private FloatingActionButton xButton;
    private SwipeFlingAdapterView flingContainer;

    public static Intent newIntent(Context context, ArrayList<Pokemon> pokemon) {
        Intent intent = new Intent(context, SwipeActivity.class);
        intent.putParcelableArrayListExtra(POKEMONLIST, pokemon);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.SwipeFlingContainer);
        mPokemon = getIntent().getParcelableArrayListExtra(POKEMONLIST);

        xButton = (FloatingActionButton) findViewById(R.id.x_button);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectLeft();
            }
        });

        heartButton = (FloatingActionButton) findViewById(R.id.heart_button);
        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectRight();
            }
        });

        mPokemonAdapter = new PokemonAdapter(getApplicationContext(), mPokemon);
        flingContainer.setAdapter(mPokemonAdapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                mPokemon.remove(0);
                mPokemonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

            }

            @Override
            public void onRightCardExit(Object dataObject) {
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                /*al.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
                */
                addPokemon();
                mPokemonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float v) {

            }
        });

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

            }
        });
    }

    private class PokemonAdapter extends ArrayAdapter<Pokemon> {
        public PokemonAdapter(Context context, ArrayList<Pokemon> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            // Check if an existing view is being reused, otherwise inflate the view

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_pokemon_view, parent, false);
            }
            TextView pokemonName = (TextView) convertView.findViewById(R.id.pokemon_name);
            pokemonName.setText(mPokemon.get(position).getName());
            ImageView pokemonPicture = (ImageView) convertView.findViewById(R.id.pokemon_picture);
            Picasso.with(getContext()).load(mPokemon.get(position).getSprites().getFrontDefault()).into(pokemonPicture);


            return convertView;
        }
    }

    public void addPokemon() {
        new PokemonDownloader().getPokemon(3, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final Gson gson = new Gson();

                try {
                    Pokemon pokemon = gson.fromJson(response.body().charStream(), Pokemon.class);
                    mPokemon.add(pokemon);
                    Log.d("POKEMON CREATED: ", pokemon.getName() + ", URL: " + pokemon.getPokemonUrl().getUrl() +
                            ", Pic URL: " + pokemon.getSprites().getFrontDefault());
                } catch (Exception errSwipe) {
                    Log.e("Network Error Swipe", errSwipe.toString());
                }
            }
        });
    }
}
