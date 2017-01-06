package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by dean on 11/16/2016.
 */

public class Pokemon implements Parcelable {

    private int id;
    private String name;
    private Double height;
    private Double weight;
    private ArrayList<Type> types;
    private ArrayList<Move> moves;
    private Sprites sprites;

    public Pokemon() {
        types = new ArrayList<>();
        moves = new ArrayList<>();
    }

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

    public Sprites getSprites() {
        return sprites;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public ArrayList<Type> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<Type> types) {
        this.types = types;
    }

    public void setSprites(Sprites sprites) {
        this.sprites = sprites;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
    }

    protected Pokemon(Parcel in) {
        id = in.readInt();
        name = in.readString();
        height = in.readByte() == 0x00 ? null : in.readDouble();
        weight = in.readByte() == 0x00 ? null : in.readDouble();
        if (in.readByte() == 0x01) {
            types = new ArrayList<Type>();
            in.readList(types, Type.class.getClassLoader());
        } else {
            types = null;
        }
        if (in.readByte() == 0x01) {
            moves = new ArrayList<Move>();
            in.readList(moves, Move.class.getClassLoader());
        } else {
            moves = null;
        }
        sprites = (Sprites) in.readValue(Sprites.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        if (height == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(height);
        }
        if (weight == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(weight);
        }
        if (types == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(types);
        }
        if (moves == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(moves);
        }
        dest.writeValue(sprites);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Pokemon> CREATOR = new Parcelable.Creator<Pokemon>() {
        @Override
        public Pokemon createFromParcel(Parcel in) {
            return new Pokemon(in);
        }

        @Override
        public Pokemon[] newArray(int size) {
            return new Pokemon[size];
        }
    };
}
