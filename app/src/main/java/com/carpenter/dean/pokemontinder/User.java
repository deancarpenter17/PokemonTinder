package com.carpenter.dean.pokemontinder;

import android.os.Parcel;
import android.os.Parcelable;

import com.carpenter.dean.pokemontinder.pokemon.Pokemon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by deanc on 11/23/2016.
 */

public class User implements Parcelable {

    private String uuid;
    private String name;
    private Pokemon pokemon;
    private HashMap<String, String> likes;
    private HashMap<String, String> matches;

    public User() {
        likes = new HashMap<>();
        matches = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public HashMap<String, String> getLikes() {
        return likes;
    }

    public void setLikes(HashMap<String, String> likes) {
        this.likes = likes;
    }

    public HashMap<String, String> getMatches() {
        return matches;
    }

    public void setMatches(HashMap<String, String> matches) {
        this.matches = matches;
    }

    // Parcelling part
    public User(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        pokemon = in.readParcelable(Pokemon.class.getClassLoader());

        int size = in.readInt();
        likes = new HashMap<>();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            String value = in.readString();
            likes.put(key,value);
        }

        int size2 = in.readInt();
        matches = new HashMap<>();
        for(int i = 0; i < size2; i++) {
            String key = in.readString();
            String value = in.readString();
            matches.put(key, value);
        }

    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeParcelable(pokemon, 0);

        dest.writeInt(likes.size());
        for(Map.Entry<String,String> entry : likes.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }

        dest.writeInt(matches.size());
        for(Map.Entry<String, String> entry : matches.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
