package com.carpenter.dean.pokemontinder.pokemon;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dean on 11/18/2016.
 */

public class Sprites implements Parcelable {
    private String backFemale;
    @SerializedName("back_shiny_female")
    private String backShinyFemale;
    @SerializedName("back_default")
    private String backDefault;
    @SerializedName("front_female")
    private String frontFemale;
    @SerializedName("front_shiny_female")
    private String frontShinyFemale;
    @SerializedName("back_shiny")
    private String backShiny;
    @SerializedName("front_default")
    private String frontDefault;
    @SerializedName("front_shiny")
    private String frontShiny;

    public Sprites () {}

    public String getBackFemale() {
        return backFemale;
    }

    public void setBackFemale(String backFemale) {
        this.backFemale = backFemale;
    }

    public String getBackShinyFemale() {
        return backShinyFemale;
    }

    public void setBackShinyFemale(String backShinyFemale) {
        this.backShinyFemale = backShinyFemale;
    }

    public String getBackDefault() {
        return backDefault;
    }

    public void setBackDefault(String backDefault) {
        this.backDefault = backDefault;
    }

    public String getFrontFemale() {
        return frontFemale;
    }

    public void setFrontFemale(String frontFemale) {
        this.frontFemale = frontFemale;
    }

    public String getFrontShinyFemale() {
        return frontShinyFemale;
    }

    public void setFrontShinyFemale(String frontShinyFemale) {
        this.frontShinyFemale = frontShinyFemale;
    }

    public String getBackShiny() {
        return backShiny;
    }

    public void setBackShiny(String backShiny) {
        this.backShiny = backShiny;
    }

    public String getFrontDefault() {
        return frontDefault;
    }

    public void setFrontDefault(String frontDefault) {
        this.frontDefault = frontDefault;
    }

    public String getFrontShiny() {
        return frontShiny;
    }

    public void setFrontShiny(String frontShiny) {
        this.frontShiny = frontShiny;
    }

    // Parcelling part
    public Sprites(Parcel in){
        backFemale = in.readString();
        backShinyFemale = in.readString();
        backDefault = in.readString();
        frontFemale = in.readString();
        frontShinyFemale = in.readString();
        backShiny = in.readString();
        frontDefault = in.readString();
        frontShiny = in.readString();
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(backFemale);
        dest.writeString(backShinyFemale);
        dest.writeString(backDefault);
        dest.writeString(frontFemale);
        dest.writeString(frontShinyFemale);
        dest.writeString(backShiny);
        dest.writeString(frontDefault);
        dest.writeString(frontShiny);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Sprites createFromParcel(Parcel in) {
            return new Sprites(in);
        }

        public Sprites[] newArray(int size) {
            return new Sprites[size];
        }
    };
}
