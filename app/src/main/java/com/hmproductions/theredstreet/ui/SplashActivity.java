package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.GlobalStockDetails;
import com.hmproductions.theredstreet.data.StockDetails;
import com.hmproductions.theredstreet.loaders.LoginLoader;
import com.hmproductions.theredstreet.utils.MiscellaneousUtils;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.LoginRequest;
import dalalstreet.api.actions.LoginResponse;
import dalalstreet.api.models.Stock;
import io.grpc.ManagedChannel;

import static com.hmproductions.theredstreet.utils.Constants.LOGIN_LOADER_ID;

public class SplashActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<LoginResponse>{

    @Inject
    ManagedChannel channel;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.splash_text)
    TextView splashText;

    public static final String USERNAME_KEY = "username-key";
    public static final String EMAIL_KEY = "email-key";
    static final String PASSWORD_KEY = "password-key";
    private boolean threadRun = true;
    Thread splashTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        ButterKnife.bind(this);
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);


        String email = preferences.getString(EMAIL_KEY, null);
        String password = preferences.getString(PASSWORD_KEY, null);
        if (email != null && !email.equals("")) {

            Bundle bundle = new Bundle();
            bundle.putString(EMAIL_KEY, email);
            bundle.putString(PASSWORD_KEY, password);

            getSupportLoaderManager().restartLoader(LOGIN_LOADER_ID, bundle, this);
        }else {

            Intent intent=new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


        splashTimer = new Thread(){
            public void run(){
                try{
                    int splashTime = 0;
                    while(threadRun){

                        sleep(150);

                        if(splashTime % 10 < 3){
                            setText("Fetching Data.");
                        }
                        else if(splashTime % 10 >= 3 && splashTime % 10 < 7 ){
                            setText("Fetching Data..");
                        }else if (splashTime % 10 >= 7){
                            setText("Fetching Data...");
                        }
                        splashTime = splashTime + 1;
                    }

                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        splashTimer.start();

    }


    private void setText(final CharSequence text) {
        runOnUiThread(() -> splashText.setText(text));
    }


    @Override
    public Loader<LoginResponse> onCreateLoader(int id, Bundle args) {

        LoginRequest loginRequest = LoginRequest
                .newBuilder()
                .setEmail(args.getString(EMAIL_KEY))
                .setPassword(args.getString(PASSWORD_KEY))
                .build();

        DalalActionServiceGrpc.DalalActionServiceBlockingStub stub = DalalActionServiceGrpc.newBlockingStub(channel);

        return new LoginLoader(this, loginRequest, stub);
    }

    @Override
    public void onLoadFinished(Loader<LoginResponse> loader, LoginResponse loginResponse) {

        threadRun = false;
        try {
            splashTimer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (loginResponse.getStatusCode().getNumber() == 0) {

            MiscellaneousUtils.sessionId = loginResponse.getSessionId();

            // Adding user's stock details
            ArrayList<StockDetails> stocksOwnedList = new ArrayList<>();
            Map<Integer, Integer> stocksOwnedMap = loginResponse.getStocksOwnedMap();

            for (int i = 1; i <= 30; ++i) {
                if (stocksOwnedMap.containsKey(i)) {
                    stocksOwnedList.add(new StockDetails(i, stocksOwnedMap.get(i)));
                }
            }

            // Adding global stock details
            ArrayList<GlobalStockDetails> globalStockList = new ArrayList<>();
            Map<Integer, Stock> globalStockMap = loginResponse.getStockListMap();

            for (int q = 1; q <= globalStockMap.size(); ++q) {

                Stock currentStockDetails = globalStockMap.get(q);

                if (currentStockDetails != null) {
                    globalStockList.add(new GlobalStockDetails(
                            currentStockDetails.getFullName(),
                            currentStockDetails.getShortName(),
                            q,
                            currentStockDetails.getCurrentPrice(),
                            currentStockDetails.getStocksInMarket(),
                            currentStockDetails.getStocksInExchange(),
                            currentStockDetails.getPreviousDayClose(),
                            currentStockDetails.getUpOrDown() ? 1 : 0));
                }
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(USERNAME_KEY, loginResponse.getUser().getName());
            intent.putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.getUser().getCash());
            intent.putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.getUser().getTotal());

            intent.putParcelableArrayListExtra(MainActivity.STOCKS_OWNED_KEY, stocksOwnedList);
            intent.putParcelableArrayListExtra(MainActivity.GLOBAL_STOCKS_KEY, globalStockList);

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoginResponse> loader) {

    }
}