package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by deanc on 12/29/2016.
 */

public class MoveDetails implements Parcelable {

    private String name;
    private String url;

    public MoveDetails() {}

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

    protected MoveDetails(Parcel in) {
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
    public static final Parcelable.Creator<MoveDetails> CREATOR = new Parcelable.Creator<MoveDetails>() {
        @Override
        public MoveDetails createFromParcel(Parcel in) {
            return new MoveDetails(in);
        }

        @Override
        public MoveDetails[] newArray(int size) {
            return new MoveDetails[size];
        }
    };
}
