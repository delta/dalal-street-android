package org.pragyan.dalal18.dagger;

import org.pragyan.dalal18.fragment.CompanyFragment;
import org.pragyan.dalal18.fragment.HomeFragment;
import org.pragyan.dalal18.fragment.LeaderboardFragment;
import org.pragyan.dalal18.fragment.mortgage.MortgageFragment;
import org.pragyan.dalal18.fragment.NewsFragment;
import org.pragyan.dalal18.fragment.OrdersFragment;
import org.pragyan.dalal18.fragment.PortfolioFragment;
import org.pragyan.dalal18.fragment.StockExchangeFragment;
import org.pragyan.dalal18.fragment.TradeFragment;
import org.pragyan.dalal18.fragment.TransactionsFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthGraphFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthTableFragment;
import org.pragyan.dalal18.fragment.mortgage.RetrieveFragment;
import org.pragyan.dalal18.notifications.NotificationFragment;
import org.pragyan.dalal18.notifications.NotificationService;
import org.pragyan.dalal18.ui.LoginActivity;
import org.pragyan.dalal18.ui.MainActivity;
import org.pragyan.dalal18.ui.RegistrationActivity;
import org.pragyan.dalal18.ui.ResetPasswordActivity;
import org.pragyan.dalal18.ui.SplashActivity;

import dagger.Component;

@DalalStreetApplicationScope
@Component(modules = { ChannelModule.class, ContextModule.class, StubModule.class, AdapterModule.class, SharedPreferencesModule.class, ConnectivityModule.class } )

public interface DalalStreetApplicationComponent {

    void inject(LoginActivity loginActivity);
    void inject(MainActivity mainActivity);
    void inject(SplashActivity splashActivity);
    void inject(RegistrationActivity registrationActivity);
    void inject(ResetPasswordActivity resetPasswordActivity);

    void inject(DepthTableFragment depthTableFragment);
    void inject(DepthGraphFragment depthGraphFragment);
    void inject(HomeFragment homeFragment);
    void inject(TradeFragment tradeFragment);
    void inject(NewsFragment newsFragment);
    void inject(MortgageFragment mortgageFragment);
    void inject(RetrieveFragment retrieveFragment);
    void inject(LeaderboardFragment leaderboardFragment);
    void inject(StockExchangeFragment stockExchangeFragment);
    void inject(TransactionsFragment transactionsFragment);
    void inject(OrdersFragment ordersFragment);
    void inject(PortfolioFragment portfolioFragment);
    void inject(NotificationFragment notificationFragment);
    void inject(CompanyFragment companyFragment);

    void inject(NotificationService notificationService);
}
