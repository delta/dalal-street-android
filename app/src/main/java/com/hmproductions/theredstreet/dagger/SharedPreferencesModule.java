package com.hmproductions.theredstreet.dagger;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;

@Module (includes = ContextModule.class)
public class SharedPreferencesModule {

    @Provides
    @DalalStreetApplicationScope
    SharedPreferences getSharedpreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
