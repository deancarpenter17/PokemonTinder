package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deanc on 12/29/2016.
 */

public class TypeDetails implements Parcelable {

    private String name;
    private String url;

    public TypeDetails() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    protected TypeDetails(Parcel in) {
        name = in.readString();
        url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TypeDetails> CREATOR = new Parcelable.Creator<TypeDetails>() {
        @Override
        public TypeDetails createFromParcel(Parcel in) {
            return new TypeDetails(in);
        }

        @Override
        public TypeDetails[] newArray(int size) {
            return new TypeDetails[size];
        }
    };
}
