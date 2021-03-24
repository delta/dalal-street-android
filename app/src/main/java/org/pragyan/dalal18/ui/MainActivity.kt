package org.pragyan.dalal18.ui

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.actions.GetMortgageDetailsRequest
import dalalstreet.api.actions.LogoutRequest
import dalalstreet.api.actions.LogoutResponse
import dalalstreet.api.datastreams.DataStreamType
import dalalstreet.api.datastreams.GameStateUpdate
import dalalstreet.api.datastreams.MarketEventUpdate
import dalalstreet.api.datastreams.NotificationUpdate
import dalalstreet.api.datastreams.StockExchangeUpdate
import dalalstreet.api.datastreams.StockPricesUpdate
import dalalstreet.api.datastreams.SubscribeRequest
import dalalstreet.api.datastreams.SubscribeResponse
import dalalstreet.api.datastreams.SubscriptionId
import dalalstreet.api.datastreams.TransactionUpdate
import dalalstreet.api.datastreams.UnsubscribeRequest
import dalalstreet.api.models.GameStateUpdateType
import dalalstreet.api.models.TransactionType
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.contentView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.GameStateDetails
import org.pragyan.dalal18.data.GlobalStockDetails
import org.pragyan.dalal18.databinding.ActivityMainBinding
import org.pragyan.dalal18.notifications.NotificationFragment
import org.pragyan.dalal18.notifications.PushNotificationService
import org.pragyan.dalal18.utils.*
import org.pragyan.dalal18.utils.Constants.EMAIL_KEY
import org.pragyan.dalal18.utils.Constants.HOST
import org.pragyan.dalal18.utils.Constants.MARKET_OPEN_KEY
import org.pragyan.dalal18.utils.Constants.NOTIFICATION_NEWS_SHARED_PREF
import org.pragyan.dalal18.utils.Constants.NOTIFICATION_SHARED_PREF
import org.pragyan.dalal18.utils.Constants.PASSWORD_KEY
import org.pragyan.dalal18.utils.Constants.PORT
import org.pragyan.dalal18.utils.Constants.PREF_COMP
import org.pragyan.dalal18.utils.Constants.PREF_MAIN
import org.pragyan.dalal18.utils.Constants.PRICE_FORMAT
import org.pragyan.dalal18.utils.Constants.REFRESH_MARKET_EVENTS_FOR_HOME_AND_NEWS
import org.pragyan.dalal18.utils.Constants.REFRESH_OWNED_STOCKS_FOR_ALL
import org.pragyan.dalal18.utils.Constants.REFRESH_PRICE_TICKER_FOR_HOME
import org.pragyan.dalal18.utils.Constants.REFRESH_STOCKS_EXCHANGE_FOR_COMPANY
import org.pragyan.dalal18.utils.Constants.REFRESH_STOCKS_FOR_MORTGAGE
import org.pragyan.dalal18.utils.Constants.REFRESH_STOCK_PRICES_FOR_ALL
import org.pragyan.dalal18.utils.Constants.SESSION_KEY
import org.pragyan.dalal18.utils.Constants.STOP_NOTIFICATION_ACTION
import org.pragyan.dalal18.utils.Constants.USERNAME_KEY
import org.pragyan.dalal18.utils.MiscellaneousUtils.buildCounterDrawable
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

/* Subscribes to Transactions, Exchange, StockPrices and MarketEvents stream*/
class MainActivity : AppCompatActivity(), ConnectionUtils.OnNetworkDownHandler {

    private val binding by viewLifecycle(ActivityMainBinding::inflate)

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var streamServiceStub: DalalStreamServiceGrpc.DalalStreamServiceStub

    @Inject
    lateinit var streamServiceBlockingStub: DalalStreamServiceGrpc.DalalStreamServiceBlockingStub

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var networkRequest: NetworkRequest

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var inAppUpdate: InAppUpdate

    lateinit var model: DalalViewModel

    private val subscriptionIds = ArrayList<SubscriptionId>()

    private var helpDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null

    private var cashWorth: Long = 0
    private var stockWorth: Long = 0
    private var totalWorth: Long = 0
    private var unreadNotificationsCount = 0

    val TAG = "MainActivity"

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private val worthNotificationGameStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "onReceived() called " + intent.action)
            when (intent.action) {
                REFRESH_ALL_WORTH_ACTION -> {
                    cashWorth += intent.getLongExtra(TRANSACTION_TOTAL_KEY, 0)
                    updateStockWorthViaStreamUpdates()
                    changeTextViewValue(binding.cashWorthTextView, binding.cashIndicatorImageView, cashWorth)
                }

                REFRESH_UNREAD_NOTIFICATIONS_COUNT -> {
                    if (findNavController(R.id.main_host_fragment).currentDestination?.id == R.id.notifications_dest)
                        unreadNotificationsCount = 0
                    invalidateOptionsMenu()
                }

                GAME_STATE_UPDATE_ACTION -> {
                    val gameStateDetails = intent.getParcelableExtra<GameStateDetails>(GAME_STATE_KEY)
                    if(gameStateDetails != null)
                        Log.i(TAG, "onReceived() called gameStateType " + gameStateDetails.gameStateUpdateType)
                    if (gameStateDetails != null) when (gameStateDetails.gameStateUpdateType) {
                        GameStateUpdateType.MarketStateUpdate ->
                            displayMarketStatusAlert(gameStateDetails.isMarketOpen ?: true)
                        GameStateUpdateType.StockDividendStateUpdate ->
                            model.updateDividendState(gameStateDetails.dividendStockId, gameStateDetails.givesDividend)
                        GameStateUpdateType.StockBankruptStateUpdate ->
                            model.updateBankruptState(gameStateDetails.bankruptStockId, gameStateDetails.isBankrupt)
                        GameStateUpdateType.UserBlockStateUpdate -> {
                            toast("Your account has been terminated")
                            logout()
                        }
                        GameStateUpdateType.UserReferredCreditUpdate -> {
                            toast("Reward claimed!")
                            val gameState = intent.getParcelableExtra<GameStateDetails>(GAME_STATE_KEY)
                            totalWorth += gameState.referredCashWorth - cashWorth
                            cashWorth = gameState.referredCashWorth
                            changeTextViewValue(binding.cashWorthTextView, binding.cashIndicatorImageView, cashWorth)
                            changeTextViewValue(binding.totalWorthTextView, binding.totalIndicatorImageView, totalWorth)
                        }
                        GameStateUpdateType.UserRewardCreditUpdate->{

                            toast("Reward claimed!")
                            val gameState = intent.getParcelableExtra<GameStateDetails>(GAME_STATE_KEY)
                            totalWorth += gameState.userRewardCash - cashWorth
                            cashWorth = gameState.userRewardCash
                            model.updateCashWorth(cashWorth)
                            model.updateNetWorth(totalWorth)
                            changeTextViewValue(binding.cashWorthTextView, binding.cashIndicatorImageView, cashWorth)
                            changeTextViewValue(binding.totalWorthTextView, binding.totalIndicatorImageView, totalWorth)
                        }
                        else ->
                            Log.v(MainActivity::class.java.simpleName, "Game state update unused: $gameStateDetails")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Log.d("INIT APPUPDATE", "APP UPDATE START")
        inAppUpdate = InAppUpdate(this@MainActivity)

        model = ViewModelProvider(this).get(DalalViewModel::class.java)

        val tinyDB = TinyDB(this)
        tinyDB.remove(NOTIFICATION_SHARED_PREF)
        tinyDB.remove(NOTIFICATION_NEWS_SHARED_PREF)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val mainToolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.mainToolbar)
        mainToolbar.inflateMenu(R.menu.main_menu)

        setSupportActionBar(mainToolbar)

        setupNavigationDrawer()

        model.ownedStockDetails = intent.getSerializableExtra(STOCKS_OWNED_KEY) as HashMap<Int, Long>
        model.globalStockDetails = intent.getSerializableExtra(GLOBAL_STOCKS_KEY) as HashMap<Int, GlobalStockDetails>
        model.reservedStockDetails = intent.getSerializableExtra(RESERVED_STOCKS_KEY) as HashMap<Int, Long>
        model.mortgageStockDetails = hashMapOf()
        model.reservedCash = intent.getLongExtra(RESERVED_CASH_KEY, 0)

        setupWorthTextViews()

        createNetworkCallbackObject()

        getMortgageDetailsAsynchronously()

        if (!intent.getBooleanExtra(MARKET_OPEN_KEY, false))
            displayMarketStatusAlert(false)

        if (preferences.getBoolean(PREF_MAIN, true)) {
            preferences.edit()
                    .putBoolean(PREF_MAIN, false)
                    .putBoolean(PREF_COMP, true)
                    .apply()
            DalalTourUtils.toolbarTour(mainToolbar, this, 40, getString(R.string.notification_tour))
        }

        val worthViewClickListener = View.OnClickListener {
            contentView?.hideKeyboard()
            findNavController(R.id.main_host_fragment).navigate(R.id.portfolio_dest, null, NavOptions.Builder().setPopUpTo(R.id.home_dest, false).build())
        }

        // Tried to use single view didn't work; It took up toolbar space also
        binding.apply {
            cashInHandTextView.setOnClickListener(worthViewClickListener)
            cashWorthTextView.setOnClickListener(worthViewClickListener)
            stocksInHandTextView.setOnClickListener(worthViewClickListener)
            stockWorthTextView.setOnClickListener(worthViewClickListener)
            totalInHandTextView.setOnClickListener(worthViewClickListener)
            totalWorthTextView.setOnClickListener(worthViewClickListener)
        }

        createChannel()
        this.startService(Intent(this.baseContext, PushNotificationService::class.java))

        binding.marketCloseIndicatorTextView.isSelected = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdate.onActivityResult(requestCode,resultCode, data)
    }

    // Adding and setting up Navigation drawer
    private fun setupNavigationDrawer() {

        val host = supportFragmentManager.findFragmentById(R.id.main_host_fragment) as NavHostFragment

        val appBarConfig = AppBarConfiguration.Builder(setOf(R.id.home_dest, R.id.companies_dest, R.id.portfolio_dest,
                R.id.exchange_dest, R.id.market_depth_dest, R.id.trade_dest, R.id.main_mortgage_dest, R.id.news_dest,
                R.id.leaderboard_dest, R.id.dailyChallenge_dest,R.id.open_orders_dest, R.id.transactions_dest, R.id.notifications_dest, R.id.refer_and_earn_dest))
                .setDrawerLayout(binding.mainDrawerLayout)
                .build()

        setupActionBarWithNavController(host.navController, appBarConfig)
        binding.navigationViewLeft.setupWithNavController(host.navController)

        MiscellaneousUtils.username = intent.getStringExtra(USERNAME_KEY)
        val header = binding.navigationViewLeft.getHeaderView(0)
        header.find<TextView>(R.id.usernameTextView).text = MiscellaneousUtils.username

        binding.mainDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerOpened(drawerView: View) {
                contentView?.hideKeyboard()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val menuItem = menu.findItem(R.id.action_notifications)
        val drawable: Drawable? = buildCounterDrawable(this, unreadNotificationsCount, R.drawable.notification_icon)
        if (drawable != null)
            menuItem.icon = drawable

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.main_host_fragment)

        val id = item.itemId
        contentView?.hideKeyboard()

        when (id) {
            R.id.action_notifications -> {
                unreadNotificationsCount = 0
                invalidateOptionsMenu()
                navController.navigate(R.id.notifications_dest, null, NavOptions.Builder().setPopUpTo(R.id.home_dest, false).build())
                return true
            }

            R.id.action_help -> {
                navController.navigate(R.id.help_dest, null, NavOptions.Builder().setPopUpTo(R.id.home_dest, false).build())
                return true
            }

            R.id.action_logout -> {
                val logOutBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)

                logOutBuilder
                        .setMessage("Do you want to logout?")
                        .setPositiveButton(getString(R.string.logout)) { _, _ -> logout() }
                        .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
                        .setTitle("Confirm Logout")
                        .setCancelable(true)
                        .show()
                logoutDialog = logOutBuilder.create()

                return true
            }

            android.R.id.home -> {
                if (navController.currentDestination?.id in listOf(R.id.company_description_dest, R.id.nav_news_details, R.id.help_dest)) {
                    onBackPressed()
                } else {
                    binding.mainDrawerLayout.openDrawer(GravityCompat.START)
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun logout() = lifecycleScope.launch {
        val notificationIntent = Intent(baseContext, PushNotificationService::class.java)
        this@MainActivity.stopService(notificationIntent)
        unsubscribeFromAllStreams()

        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@MainActivity) }) {
            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(HOST, PORT) }) {
                val logoutResponse = withContext(Dispatchers.IO) {
                    actionServiceBlockingStub.logout(LogoutRequest.newBuilder().build())
                }

                if (logoutResponse.statusCode == LogoutResponse.StatusCode.OK) {

                    val stopNotificationIntent = Intent(STOP_NOTIFICATION_ACTION)
                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(stopNotificationIntent)

                    preferences.edit().putString(EMAIL_KEY, null).putString(PASSWORD_KEY, null).putString(SESSION_KEY, null).apply()
                }

                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            } else {
                onNetworkDownError(resources.getString(R.string.error_server_down), R.id.home_dest)
            }
        } else {
            onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.home_dest)
        }
    }

    // Subscribes to transaction stream and gets updates (TESTED)
    private fun subscribeToTransactionsStream(transactionsSubscriptionId: SubscriptionId) {

        streamServiceStub.getTransactionUpdates(transactionsSubscriptionId,
                object : StreamObserver<TransactionUpdate> {
                    override fun onNext(value: TransactionUpdate) {

                        val transaction = value.transaction

                        model.updateStocksOwned(transaction.stockId, transaction.stockQuantity)
                        model.updateReservedStocks(transaction.stockId, transaction.reservedStockQuantity)
                        model.reservedCash += transaction.reservedCashTotal

                        if (transaction.type == TransactionType.MORTGAGE_TRANSACTION) {
                            // Incoming stock quantity will be negative when user mortgages
                            model.updateMortgagedStocks(transaction.stockId, -transaction.stockQuantity, transaction.price)
                        }

                        val intent = Intent(REFRESH_ALL_WORTH_ACTION)
                        intent.putExtra(TRANSACTION_TOTAL_KEY, transaction.total)
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)

                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_OWNED_STOCKS_FOR_ALL))
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_STOCKS_FOR_MORTGAGE))
                    }

                    override fun onError(t: Throwable) {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    // Subscribes to market events stream and gets updates (TESTED)
    private fun subscribeToMarketEventsUpdateStream(marketEventsSubscriptionId: SubscriptionId) {

        streamServiceStub.getMarketEventUpdates(marketEventsSubscriptionId,
                object : StreamObserver<MarketEventUpdate> {
                    override fun onNext(value: MarketEventUpdate) {
                        val refreshNewsIntent = Intent(REFRESH_MARKET_EVENTS_FOR_HOME_AND_NEWS)
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(refreshNewsIntent)
                    }

                    override fun onError(t: Throwable) {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    // Subscribes to stock prices stream and gets updates (TESTED)
    private fun subscribeToStockPricesStream(stockPricesSubscriptionId: SubscriptionId) {
        streamServiceStub.getStockPricesUpdates(stockPricesSubscriptionId,
                object : StreamObserver<StockPricesUpdate> {
                    override fun onNext(value: StockPricesUpdate) {
                        for ((stockId, price) in value.pricesMap) {
                            model.updateGlobalStockPrice(stockId, price)
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_PRICE_TICKER_FOR_HOME))
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_STOCK_PRICES_FOR_ALL))
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_ALL_WORTH_ACTION))
                        }
                    }

                    override fun onError(t: Throwable) {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    // Subscribes to stock exchange stream and gets updates globalStockDetails (TESTED)
    private fun subscribeToStockExchangeStream(stockExchangeSubscriptionId: SubscriptionId) {

        streamServiceStub.getStockExchangeUpdates(stockExchangeSubscriptionId,
                object : StreamObserver<StockExchangeUpdate> {
                    override fun onNext(value: StockExchangeUpdate) {
                        for ((stockId, currentDataPoint) in value.stocksInExchangeMap) {
                            model.updateGlobalStock(stockId, currentDataPoint.price, currentDataPoint.stocksInMarket, currentDataPoint.stocksInExchange)
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_STOCKS_EXCHANGE_FOR_COMPANY))
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_ALL_WORTH_ACTION))
                        }
                    }

                    override fun onError(t: Throwable) {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    // Subscribes to notifications stream and gets updates (TESTED)
    private fun subscribeToNotificationsStream(notificationsSubscriptionId: SubscriptionId) {

        streamServiceStub.getNotificationUpdates(notificationsSubscriptionId,
                object : StreamObserver<NotificationUpdate> {
                    override fun onNext(value: NotificationUpdate) {
                        val notification = value.notification
                        unreadNotificationsCount++
                        val notificationIntent = Intent(REFRESH_UNREAD_NOTIFICATIONS_COUNT)
                        notificationIntent.putExtra(NotificationFragment.TEXT_KEY, notification.text)
                        notificationIntent.putExtra(NotificationFragment.CREATED_AT_KEY, notification.createdAt)
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(notificationIntent))
                    }

                    override fun onError(t: Throwable?) {
                    }

                    override fun onCompleted() {
                    }

                })
    }

    private fun subscribeToGameStateStream(gameStateSubscriptionId: SubscriptionId) {

        streamServiceStub.getGameStateUpdates(gameStateSubscriptionId,
                object : StreamObserver<GameStateUpdate> {
                    override fun onNext(value: GameStateUpdate?) {
                        if (value?.gameState != null) with(value.gameState) {
                            val intent = Intent(GAME_STATE_UPDATE_ACTION)
                            val gameStateDetails = GameStateDetails(
                                    type,
                                    marketState?.isMarketOpen,
                                    otpVerifiedState?.isVerified,
                                    stockDividendState?.stockId,
                                    stockDividendState?.givesDividend,
                                    stockBankruptState?.stockId,
                                    stockBankruptState?.isBankrupt,
                                    userReferredCredit.cash,
                                    userRewardCredit.cash,
                                    dailyChallengeState.isDailyChallengeOpen)

                            intent.putExtra(GAME_STATE_KEY, gameStateDetails)
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                        }
                    }

                    override fun onError(t: Throwable?) {

                    }

                    override fun onCompleted() {

                    }

                })
    }

    // Unsubscribes from all streams
    private fun unsubscribeFromAllStreams() {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@MainActivity) && ConnectionUtils.isReachableByTcp(HOST, PORT)) {
                for (subscriptionId in subscriptionIds) {
                    val unsubscribeResponse =
                            streamServiceBlockingStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(subscriptionId).build())
                }

                subscriptionIds.clear()
            }
        }
    }

    // Method is called when stock price update is received, it changes stock worth and total worth textview
    private fun updateStockWorthViaStreamUpdates() {
        var netStockWorth = 0L
        for ((stockId, quantity) in model.ownedStockDetails) {
            netStockWorth += quantity * model.getPriceFromStockId(stockId)
        }
        stockWorth = netStockWorth
        model.updateStockWorth(stockWorth)
        changeTextViewValue(binding.stockWorthTextView, binding.stockIndicatorImageView, netStockWorth)

        // We need to add reserved stocks worth to calculate total worth
        for ((stockId, quantity) in model.reservedStockDetails) {
            netStockWorth += quantity * model.getPriceFromStockId(stockId)
        }
        // Backend has it the way TotalWorth = CashWorth + OwnedStockWorth + ReservedStockWorth
        totalWorth = netStockWorth + cashWorth + model.reservedCash
        model.updateNetWorth(totalWorth)
        model.updateCashWorth(cashWorth)

        changeTextViewValue(binding.totalWorthTextView, binding.totalIndicatorImageView, totalWorth)
    }

    // Initial setup, called in activity's onCreate()
    private fun setupWorthTextViews() {

        // Initial old value is 0; Zero is placeholder
        cashWorth = intent.getLongExtra(CASH_WORTH_KEY, -1)
        model.updateCashWorth(cashWorth)
        animateWorthChange(0, cashWorth, binding.cashWorthTextView, binding.cashIndicatorImageView)

        totalWorth = intent.getLongExtra(TOTAL_WORTH_KEY, -1)
        model.updateNetWorth(totalWorth)

        // Updates stockWorthTextView and totalWorthTextView
        updateStockWorthViaStreamUpdates()
    }

    private fun getMortgageDetailsAsynchronously() = lifecycleScope.launch {

        val dialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.progress_dialog, null)
        val tempString = "Getting stock details..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        val loadingDialog = AlertDialog.Builder(this@MainActivity).setView(dialogView).setCancelable(false).create()
        loadingDialog.show()

        withContext(Dispatchers.IO) {
            if (ConnectionUtils.getConnectionInfo(this@MainActivity) && ConnectionUtils.isReachableByTcp(HOST, PORT)) {
                val response = actionServiceBlockingStub.getMortgageDetails(GetMortgageDetailsRequest.newBuilder().build())

                for (currentMortgageStock in response.mortgageDetailsList) {
                    model.updateMortgagedStocks(currentMortgageStock.stockId, currentMortgageStock.stocksInBank, currentMortgageStock.mortgagePrice)
                }
            }
        }

        loadingDialog.dismiss()
    }

    private fun displayMarketStatusAlert(isMarketOpen: Boolean) {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle(if (isMarketOpen) "Market Open" else "Market Closed")
                .setMessage(getString(if (isMarketOpen) R.string.market_open_text else R.string.market_closed_text))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.close)) { dI, _ -> dI.dismiss() }
                .show()

        binding.marketCloseIndicatorTextView.visibility = if (!isMarketOpen) View.VISIBLE else View.GONE
    }

    // Increases/decreases text view value depending on input parameters
    private fun changeTextViewValue(textView: TextView, indicatorImageView: ImageView, newValue: Long) {
        val oldValue = textView.text.toString().replace(",", "").toLong()
        animateWorthChange(oldValue, newValue, textView, indicatorImageView)
    }

    @Synchronized
    private fun animateWorthChange(oldValue: Long, newValue: Long, textView: TextView, indicatorImageView: ImageView) {

        if (newValue == oldValue) return

        shortBurstVibrate()

        val formatter = DecimalFormat(PRICE_FORMAT)
        val valueAnimator = ValueAnimator.ofObject(LongEvaluator(), oldValue, newValue)
        valueAnimator.duration = 450
        valueAnimator.addUpdateListener {
            textView.text = formatter.format(it.animatedValue)
        }
        valueAnimator.start()

        indicatorImageView.setImageResource(if (oldValue > newValue) R.drawable.arrow_down_red else R.drawable.arrow_up_green)
        val alphaAnimator = ValueAnimator.ofInt(0, 255)
        alphaAnimator.duration = 350
        alphaAnimator.repeatCount = 5
        alphaAnimator.repeatMode = ValueAnimator.REVERSE
        alphaAnimator.addUpdateListener { indicatorImageView.imageAlpha = it.animatedValue as Int }
        alphaAnimator.start()
    }

    /* Total vibration duration is 400 ms */
    private fun shortBurstVibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 100, 100, 100, 100)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    public override fun onResume() {
        super.onResume()

        subscribeToStreamsAsynchronously()

        val intentFilter = IntentFilter(REFRESH_ALL_WORTH_ACTION)
        intentFilter.addAction(REFRESH_UNREAD_NOTIFICATIONS_COUNT)
        intentFilter.addAction(GAME_STATE_UPDATE_ACTION)

        LocalBroadcastManager.getInstance(this).registerReceiver(worthNotificationGameStateReceiver, IntentFilter(intentFilter))

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    public override fun onPause() {
        super.onPause()

        inAppUpdate.onResume()

        unsubscribeFromAllStreams()

        preferences.edit().remove(LAST_TRANSACTION_ID).apply()

        helpDialog?.dismiss()
        logoutDialog?.dismiss()

        connectivityManager.unregisterNetworkCallback(networkCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(worthNotificationGameStateReceiver)

        preferences.edit().remove(LAST_TRANSACTION_ID).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdate.onDestroy()
    }

    private fun subscribeToStreamsAsynchronously() = lifecycleScope.launch {
        if (withContext(Dispatchers.IO) { ConnectionUtils.getConnectionInfo(this@MainActivity) }) {

            if (withContext(Dispatchers.IO) { ConnectionUtils.isReachableByTcp(HOST, PORT) }) {

                val stockExchangeResponse: SubscribeResponse = withContext(Dispatchers.IO) {
                    streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_EXCHANGE).setDataStreamId("").build())
                }
                subscriptionIds.add(stockExchangeResponse.subscriptionId)
                subscribeToStockExchangeStream(stockExchangeResponse.subscriptionId)

                val stockPricesResponse = withContext(Dispatchers.IO) {
                    streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_PRICES).setDataStreamId("").build())
                }
                subscriptionIds.add(stockPricesResponse.subscriptionId)
                subscribeToStockPricesStream(stockPricesResponse.subscriptionId)

                val marketEventsResponse = withContext(Dispatchers.IO) {
                    streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MARKET_EVENTS).setDataStreamId("").build())
                }
                subscriptionIds.add(marketEventsResponse.subscriptionId)
                subscribeToMarketEventsUpdateStream(marketEventsResponse.subscriptionId)

                val notificationsResponse = withContext(Dispatchers.IO) {
                    streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.NOTIFICATIONS).setDataStreamId("").build())
                }
                subscriptionIds.add(notificationsResponse.subscriptionId)
                subscribeToNotificationsStream(notificationsResponse.subscriptionId)

                val transactionsResponse = withContext(Dispatchers.IO) {
                    streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.TRANSACTIONS).setDataStreamId("").build())
                }
                subscriptionIds.add(transactionsResponse.subscriptionId)
                subscribeToTransactionsStream(transactionsResponse.subscriptionId)

                val gameStateResponse = withContext(Dispatchers.IO) {
                    streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.GAME_STATE).setDataStreamId("").build())
                }
                subscriptionIds.add(gameStateResponse.subscriptionId)
                subscribeToGameStateStream(gameStateResponse.subscriptionId)

            } else {
                onNetworkDownError(resources.getString(R.string.error_server_down), R.id.home_dest)
            }
        } else {
            onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.home_dest)
        }
    }

    override fun onNetworkDownError(message: String, fragment: Int) {
        startActivity(Intent(this@MainActivity, SplashActivity::class.java))
        finish()
        contentView?.hideKeyboard()
    }

    // Creates a new networkCallback object
    private fun createNetworkCallbackObject() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                finish()
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_id), NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.enableLights(true)
            mChannel.description = getString(R.string.notification_channel_description)
            mChannel.setShowBadge(true)
            mChannel.lightColor = Color.RED
            nm.createNotificationChannel(mChannel)
        }
    }

    companion object {

        private const val LAST_TRANSACTION_ID = "last_transaction_id"
        private const val TRANSACTION_TOTAL_KEY = "transaction-total-key"

        const val CASH_WORTH_KEY = "cash-worth-key"
        const val TOTAL_WORTH_KEY = "total-worth-key"
        const val RESERVED_CASH_KEY = "reserved-cash-key"
        const val STOCKS_OWNED_KEY = "stocks-owned-key"
        const val GLOBAL_STOCKS_KEY = "global-stocks-key"
        const val RESERVED_STOCKS_KEY = "reserved-stocks-key"

        private const val REFRESH_ALL_WORTH_ACTION = "refresh-cash-worth-text-view"
        const val REFRESH_UNREAD_NOTIFICATIONS_COUNT = "refresh-unread-notifications-count"

        const val GAME_STATE_UPDATE_ACTION = "game-state-update-action"
        private const val GAME_STATE_KEY = "game-state-key"
    }
}
