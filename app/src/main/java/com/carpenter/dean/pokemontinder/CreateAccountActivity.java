package com.carpenter.dean.pokemontinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity
        implements GoogleLoginFragment.Callbacks,
        PokemonChooserFragment.Callbacks {

    FragmentManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation_fragment_container);

        mManager = getSupportFragmentManager();
        Fragment fragment = mManager.findFragmentById(R.id.activity_create_account_fragment_container);
        if(fragment == null) {
            fragment = GoogleLoginFragment.newInstance();
            mManager.beginTransaction()
                    .add(R.id.activity_create_account_fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void switchToPokemonChooserFrag(User user) {
        PokemonChooserFragment chooserFragment = PokemonChooserFragment.newInstance(user);
        mManager.beginTransaction()
                .replace(R.id.activity_create_account_fragment_container, chooserFragment)
                .commit();
    }
}
