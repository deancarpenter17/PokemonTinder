package com.carpenter.dean.pokemontinder.pokemon;

/**
 * Created by deanc on 12/29/2016.
 */

public class Type {
    transient Integer slot;
    private TypeDetails type;

    public TypeDetails getType() {
        return type;
    }

    public void setType(TypeDetails type) {
        this.type = type;
    }
}
