package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deanc on 12/29/2016.
 */

public class Move implements Parcelable {

    private MoveDetails move;

    public Move() {}

    public MoveDetails getMove() {
        return move;
    }

    public void setMove(MoveDetails move) {
        this.move = move;
    }

    protected Move(Parcel in) {
        move = (MoveDetails) in.readValue(MoveDetails.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(move);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Move> CREATOR = new Parcelable.Creator<Move>() {
        @Override
        public Move createFromParcel(Parcel in) {
            return new Move(in);
        }

        @Override
        public Move[] newArray(int size) {
            return new Move[size];
        }
    };
}
