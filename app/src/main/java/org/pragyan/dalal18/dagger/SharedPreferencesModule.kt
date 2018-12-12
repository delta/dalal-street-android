package org.pragyan.dalal18.dagger

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import dagger.Module
import dagger.Provides

@Module(includes = arrayOf(ContextModule::class))
class SharedPreferencesModule {

    @Provides
    @DalalStreetApplicationScope
    fun getSharedPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)
}
