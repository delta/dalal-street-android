package com.hmproductions.theredstreet.dagger;

import com.hmproductions.theredstreet.fragment.OrdersFragment;
import com.hmproductions.theredstreet.fragment.PortfolioFragment;
import com.hmproductions.theredstreet.fragment.TradeFragment;
import com.hmproductions.theredstreet.fragment.MarketDepthFragment;
import com.hmproductions.theredstreet.fragment.HomeFragment;
import com.hmproductions.theredstreet.fragment.LeaderboardFragment;
import com.hmproductions.theredstreet.fragment.MortgageFragment;
import com.hmproductions.theredstreet.fragment.NewsFragment;
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

    void inject(MarketDepthFragment marketDepthFragment);
    void inject(HomeFragment homeFragment);
    void inject(TradeFragment tradeFragment);
    void inject(NewsFragment newsFragment);
    void inject(MortgageFragment mortgageFragment);
    void inject(LeaderboardFragment leaderboardFragment);
    void inject(StockExchangeFragment stockExchangeFragment);
    void inject(TransactionsFragment transactionsFragment);
    void inject(OrdersFragment ordersFragment);
    void inject(PortfolioFragment portfolioFragment);
}
