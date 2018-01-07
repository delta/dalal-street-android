package com.hmproductions.theredstreet.dagger;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    private Context context;

    public ContextModule(Context context) {
        this.context = context;
    }

    @Provides
    @DalalStreetApplicationScope
    public Context context(){
        return this.context;
    }
}
