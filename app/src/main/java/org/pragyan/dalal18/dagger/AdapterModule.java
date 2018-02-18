package org.pragyan.dalal18.dagger;

import android.content.Context;

import org.pragyan.dalal18.adapter.CompanyTickerRecyclerAdapter;
import org.pragyan.dalal18.adapter.NewsRecyclerAdapter;
import org.pragyan.dalal18.adapter.NotificationRecyclerAdapter;
import org.pragyan.dalal18.adapter.PortfolioRecyclerAdapter;

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

    @Provides
    @DalalStreetApplicationScope
    NotificationRecyclerAdapter getNotificationRecyclerAdapter(Context context) {
        return new NotificationRecyclerAdapter(context, null);
    }
}
