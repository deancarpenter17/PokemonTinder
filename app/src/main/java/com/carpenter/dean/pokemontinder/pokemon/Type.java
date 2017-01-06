package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deanc on 12/29/2016.
 */

public class Type implements Parcelable {
    transient Integer slot;
    private TypeDetails type;

    public Type() {}

    public TypeDetails getType() {
        return type;
    }

    public void setType(TypeDetails type) {
        this.type = type;
    }

    protected Type(Parcel in) {
        slot = in.readByte() == 0x00 ? null : in.readInt();
        type = (TypeDetails) in.readValue(TypeDetails.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (slot == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(slot);
        }
        dest.writeValue(type);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Type> CREATOR = new Parcelable.Creator<Type>() {
        @Override
        public Type createFromParcel(Parcel in) {
            return new Type(in);
        }

        @Override
        public Type[] newArray(int size) {
            return new Type[size];
        }
    };
}

