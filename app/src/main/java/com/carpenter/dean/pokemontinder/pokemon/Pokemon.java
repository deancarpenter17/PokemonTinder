package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dean on 11/16/2016.
 */

public class Pokemon implements Parcelable {

    private int id;
    private String name;
    @SerializedName("pokemon")
    private PokemonUrl pokemonUrl; // Created because there is a nested 'pokemon' jsonobject within the api call ../pokemon-form
    private Sprites sprites;

    public Pokemon() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PokemonUrl getPokemonUrl() {
        return pokemonUrl;
    }

    public void setPokemonUrl(PokemonUrl pokemonUrl) {
        this.pokemonUrl = pokemonUrl;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public void setSprites(Sprites sprites) {
        this.sprites = sprites;
    }

    // Parcelling part
    public Pokemon(Parcel in){
        id = in.readInt();
        name = in.readString();
        pokemonUrl = in.readParcelable(PokemonUrl.class.getClassLoader());
        sprites = in.readParcelable(Sprites.class.getClassLoader());
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeParcelable(pokemonUrl, 0);
        dest.writeParcelable(sprites, 0);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Pokemon createFromParcel(Parcel in) {
            return new Pokemon(in);
        }

        public Pokemon[] newArray(int size) {
            return new Pokemon[size];
        }
    };
}
