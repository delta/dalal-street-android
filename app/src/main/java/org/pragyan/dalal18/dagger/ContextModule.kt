package org.pragyan.dalal18.dagger

import android.content.Context

import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val context: Context) {

    @Provides
    @DalalStreetApplicationScope
    fun context() = context
}
