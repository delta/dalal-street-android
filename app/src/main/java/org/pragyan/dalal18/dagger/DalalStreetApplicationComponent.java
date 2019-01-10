package org.pragyan.dalal18.dagger;

import org.pragyan.dalal18.fragment.HomeFragment;
import org.pragyan.dalal18.fragment.LeaderboardFragment;
import org.pragyan.dalal18.fragment.MortgageFragment;
import org.pragyan.dalal18.fragment.NewsFragment;
import org.pragyan.dalal18.fragment.OrdersFragment;
import org.pragyan.dalal18.fragment.PortfolioFragment;
import org.pragyan.dalal18.fragment.StockExchangeFragment;
import org.pragyan.dalal18.fragment.TradeFragment;
import org.pragyan.dalal18.fragment.TransactionsFragment;
import org.pragyan.dalal18.fragment.WorthFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthGraphFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthTableFragment;
import org.pragyan.dalal18.notifications.NotificationFragment;
import org.pragyan.dalal18.notifications.NotificationService;
import org.pragyan.dalal18.ui.LoginActivity;
import org.pragyan.dalal18.ui.MainActivity;
import org.pragyan.dalal18.ui.NewsDetailsActivity;
import org.pragyan.dalal18.ui.RegistrationActivity;
import org.pragyan.dalal18.ui.SplashActivity;

import dagger.Component;

@DalalStreetApplicationScope
@Component(modules = { ChannelModule.class, ContextModule.class, StubModule.class, AdapterModule.class, SharedPreferencesModule.class } )

public interface DalalStreetApplicationComponent {

    void inject(LoginActivity loginActivity);
    void inject(MainActivity mainActivity);
    void inject(SplashActivity splashActivity);
    void inject(RegistrationActivity registrationActivity);
    void inject(NewsDetailsActivity newsDetailsActivity);

    void inject(DepthTableFragment depthTableFragment);
    void inject(DepthGraphFragment depthGraphFragment);
    void inject(HomeFragment homeFragment);
    void inject(TradeFragment tradeFragment);
    void inject(NewsFragment newsFragment);
    void inject(MortgageFragment mortgageFragment);
    void inject(LeaderboardFragment leaderboardFragment);
    void inject(StockExchangeFragment stockExchangeFragment);
    void inject(TransactionsFragment transactionsFragment);
    void inject(OrdersFragment ordersFragment);
    void inject(PortfolioFragment portfolioFragment);
    void inject(NotificationFragment notificationFragment);
    void inject(WorthFragment worthFragment);

    void inject(NotificationService notificationService);
}
