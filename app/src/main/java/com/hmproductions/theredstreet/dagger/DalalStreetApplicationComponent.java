package com.hmproductions.theredstreet.dagger;

import com.hmproductions.theredstreet.fragment.BuySellFragment;
import com.hmproductions.theredstreet.fragment.CompanyProfileFragment;
import com.hmproductions.theredstreet.fragment.HomeFragment;
import com.hmproductions.theredstreet.fragment.NewsFragment;
import com.hmproductions.theredstreet.ui.HomeActivity;
import com.hmproductions.theredstreet.ui.LoginActivity;

import dagger.Component;

@DalalStreetApplicationScope
@Component(modules = { ChannelModule.class, ContextModule.class, StubModule.class, AdapterModule.class, SharedPreferencesModule.class } )

public interface DalalStreetApplicationComponent {

    void inject(LoginActivity loginActivity);
    void inject(HomeActivity homeActivity);

    void inject(CompanyProfileFragment companyProfileFragment);
    void inject(HomeFragment homeFragment);
    void inject(BuySellFragment buySellFragment);
    void inject(NewsFragment newsFragment);
}
