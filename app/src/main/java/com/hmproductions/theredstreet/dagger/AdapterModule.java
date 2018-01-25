package com.hmproductions.theredstreet.dagger;

import android.content.Context;

import com.hmproductions.theredstreet.adapter.CompanyTickerRecyclerAdapter;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.adapter.PortfolioRecyclerAdapter;

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
    public CompanyTickerRecyclerAdapter getCompanyRecyclerAdapter(Context context) {
        return new CompanyTickerRecyclerAdapter(context ,null);
    }

    @Provides
    @DalalStreetApplicationScope
    public PortfolioRecyclerAdapter getPortfolioRecyclerAdapter(Context context) {
        return new PortfolioRecyclerAdapter(context, null);
    }
}
