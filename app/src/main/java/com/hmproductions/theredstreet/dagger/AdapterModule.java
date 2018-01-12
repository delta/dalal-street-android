package com.hmproductions.theredstreet.dagger;

import android.content.Context;

import com.hmproductions.theredstreet.adapter.CompanyRecyclerAdapter;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

@Module
public class AdapterModule {

    @Provides
    @DalalStreetApplicationScope
    public NewsRecyclerAdapter getNewsRecyclerAdapter(Context context) {
        return new NewsRecyclerAdapter(context, null);
    }

    @Provides
    @DalalStreetApplicationScope
    public CompanyRecyclerAdapter getCompanyRecyclerAdapter(Context context) {
        return new CompanyRecyclerAdapter(context ,null);
    }
}
