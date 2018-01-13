package com.hmproductions.theredstreet.dagger;

import com.hmproductions.theredstreet.fragment.BuySellFragment;
import com.hmproductions.theredstreet.fragment.CompanyProfileFragment;
import com.hmproductions.theredstreet.fragment.HomeFragment;
import com.hmproductions.theredstreet.fragment.LeaderboardFragment;
import com.hmproductions.theredstreet.fragment.MortgageFragment;
import com.hmproductions.theredstreet.fragment.NewsFragment;
import com.hmproductions.theredstreet.fragment.PortfolioFragment;
import com.hmproductions.theredstreet.fragment.StockExchangeFragment;
import com.hmproductions.theredstreet.fragment.TransactionsFragment;
import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.ui.LoginActivity;
import com.hmproductions.theredstreet.ui.SplashActivity;

import dagger.Component;

@DalalStreetApplicationScope
@Component(modules = { ChannelModule.class, ContextModule.class, StubModule.class, AdapterModule.class, SharedPreferencesModule.class } )

public interface DalalStreetApplicationComponent {

    void inject(LoginActivity loginActivity);
    void inject(MainActivity mainActivity);
    void inject(SplashActivity splashActivity);

    void inject(CompanyProfileFragment companyProfileFragment);
    void inject(HomeFragment homeFragment);
    void inject(BuySellFragment buySellFragment);
    void inject(NewsFragment newsFragment);
    void inject(MortgageFragment mortgageFragment);
    void inject(LeaderboardFragment leaderboardFragment);
    void inject(StockExchangeFragment stockExchangeFragment);
    void inject(TransactionsFragment transactionsFragment);

}
