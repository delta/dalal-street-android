package org.pragyan.dalal18.dagger;

import org.pragyan.dalal18.fragment.CompanyFragment;
import org.pragyan.dalal18.fragment.DailyChallengesFragment;
import org.pragyan.dalal18.fragment.HomeFragment;
import org.pragyan.dalal18.fragment.NewsFragment;
import org.pragyan.dalal18.fragment.OrdersFragment;
import org.pragyan.dalal18.fragment.PortfolioFragment;
import org.pragyan.dalal18.fragment.ReferAndEarnFragment;
import org.pragyan.dalal18.fragment.SecretFragment;
import org.pragyan.dalal18.fragment.SingleDayChallengeFragment;
import org.pragyan.dalal18.fragment.StockExchangeFragment;
import org.pragyan.dalal18.fragment.TradeFragment;
import org.pragyan.dalal18.fragment.TransactionsFragment;
import org.pragyan.dalal18.fragment.adminPanel.AdminPanelDailyMarketFragment;
import org.pragyan.dalal18.fragment.adminPanel.AdminPanelStocksFragment;
import org.pragyan.dalal18.fragment.adminPanel.AdminPanelUserSpecificFragment;
import org.pragyan.dalal18.fragment.leaderboard.LeaderboardListFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthGraphFragment;
import org.pragyan.dalal18.fragment.marketDepth.DepthTableFragment;
import org.pragyan.dalal18.fragment.mortgage.MortgageFragment;
import org.pragyan.dalal18.fragment.mortgage.RetrieveFragment;
import org.pragyan.dalal18.fragment.smsVerification.AddPhoneFragment;
import org.pragyan.dalal18.fragment.smsVerification.OTPVerificationFragment;
import org.pragyan.dalal18.notifications.NotificationFragment;
import org.pragyan.dalal18.notifications.NotificationService;
import org.pragyan.dalal18.notifications.PushNotificationService;
import org.pragyan.dalal18.ui.LoginActivity;
import org.pragyan.dalal18.ui.MainActivity;
import org.pragyan.dalal18.ui.RegistrationActivity;
import org.pragyan.dalal18.ui.ResetPasswordActivity;
import org.pragyan.dalal18.ui.SplashActivity;
import org.pragyan.dalal18.ui.VerifyPhoneActivity;

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
    void inject(LeaderboardListFragment leaderboardListFragment);
    void inject(StockExchangeFragment stockExchangeFragment);
    void inject(TransactionsFragment transactionsFragment);
    void inject(OrdersFragment ordersFragment);
    void inject(PortfolioFragment portfolioFragment);
    void inject(NotificationFragment notificationFragment);
    void inject(CompanyFragment companyFragment);
    void inject(OTPVerificationFragment otpVerificationFragment);
    void inject(AddPhoneFragment addPhoneFragment);
    void inject(SecretFragment secretFragment);
    void inject(AdminPanelStocksFragment adminPanelStocksFragment);
    void inject(AdminPanelDailyMarketFragment adminPanelDailyMarketFragment);
    void inject(AdminPanelUserSpecificFragment adminPanelUserSpecificFragment);
    void inject(ReferAndEarnFragment referAndEarnFragment);
    void inject(DailyChallengesFragment dailyChallengesFragment);
    void inject(SingleDayChallengeFragment singleDayChallengeFragment);

    void inject(NotificationService notificationService);
    void inject(VerifyPhoneActivity verifyPhoneActivity);
    void inject(PushNotificationService pushNotificationService);

}
