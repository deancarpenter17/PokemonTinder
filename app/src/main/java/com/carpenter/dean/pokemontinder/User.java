package com.carpenter.dean.pokemontinder;

import android.os.Parcel;
import android.os.Parcelable;

import com.carpenter.dean.pokemontinder.pokemon.Pokemon;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by deanc on 11/23/2016.
 */

public class User implements Parcelable {

    private String uuid;
    private String name;
    private Pokemon pokemon;
    private HashMap<String, User> likes;
    private HashMap<String, User> matches;

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

    public HashMap<String, User> getLikes() {
        return likes;
    }

    public void setLikes(HashMap<String, User> likes) {
        this.likes = likes;
    }

    public HashMap<String, User> getMatches() {
        return matches;
    }

    public void setMatches(HashMap<String, User> matches) {
        this.matches = matches;
    }

    // Parcelling part
    public User(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        pokemon = in.readParcelable(Pokemon.class.getClassLoader());

        int size = in.readInt();
        likes = new HashMap<>();
        for(int i = 0; i < size; i++) {
            String key = in.readString();
            User value = in.readParcelable(User.class.getClassLoader());
            likes.put(key, value);
        }

        int size2 = in.readInt();
        matches = new HashMap<>();
        for(int i = 0; i < size2; i++) {
            String key = in.readString();
            User value = in.readParcelable(User.class.getClassLoader());
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
        for(Map.Entry<String, User> entry : likes.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), 0);
        }

        dest.writeInt(matches.size());
        for(Map.Entry<String, User> entry : matches.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), 0);
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User user = (User) obj;
        return uuid.equals(((User) obj).getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, pokemon, likes, matches);
    }
}
