package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dean on 11/18/2016.
 */

/** This class is needed since there is a nested 'pokemon' JSON object in the .. /pokemon-form api call
 * This nested pokemon object contains 'name', and 'url', we only need to URL. Allows us to now use
 * Gson with the main Pokemon class.
 */
public class PokemonUrl implements Parcelable {
        @SerializedName("url")
        private String url;

    public PokemonUrl() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Parcelling part
    public PokemonUrl(Parcel in){
        url = in.readString();
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PokemonUrl createFromParcel(Parcel in) {
            return new PokemonUrl(in);
        }

        public PokemonUrl[] newArray(int size) {
            return new PokemonUrl[size];
        }
    };

}
