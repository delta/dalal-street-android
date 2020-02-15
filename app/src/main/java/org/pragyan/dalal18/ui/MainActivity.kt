package org.pragyan.dalal18.ui

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.actions.GetMortgageDetailsRequest
import dalalstreet.api.actions.LogoutRequest
import dalalstreet.api.actions.LogoutResponse
import dalalstreet.api.datastreams.*
import dalalstreet.api.models.GameStateUpdateType
import dalalstreet.api.models.TransactionType
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.data.GameStateDetails
<<<<<<< HEAD
import org.pragyan.dalal18.data.GlobalStockDetails
=======
import org.pragyan.dalal18.fragment.mortgage.MortgageFragment
import org.pragyan.dalal18.notifications.NotificationService
import org.pragyan.dalal18.notifications.PushNotificationService
>>>>>>> Add files for Notifications
import org.pragyan.dalal18.utils.*
import org.pragyan.dalal18.utils.Constants.*
import org.pragyan.dalal18.utils.CountDrawable.buildCounterDrawable
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/* Subscribes to Transactions, Exchange, StockPrices and MarketEvents stream*/
class MainActivity : AppCompatActivity(), ConnectionUtils.OnNetworkDownHandler {

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

    lateinit var model: DalalViewModel

    private val subscriptionIds = ArrayList<SubscriptionId>()

    private var helpDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null
    private var errorDialog: AlertDialog? = null

    private var cashWorth: Long = 0
    private var stockWorth: Long = 0
    private var totalWorth: Long = 0
    private var unreadNotificationsCount = 0
    private var lastOpenFragmentId = R.id.home_dest
    private var lostOnce = false

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private val refreshCashStockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                REFRESH_ALL_WORTH_ACTION -> {

                    cashWorth += intent.getLongExtra(TRANSACTION_TOTAL_KEY, 0)
                    updateStockWorthViaStreamUpdates()
                    changeTextViewValue(cashWorthTextView, cashIndicatorImageView, cashWorth)
                }

                REFRESH_UNREAD_NOTIFICATIONS_COUNT -> {
                    if (findNavController(R.id.main_host_fragment).currentDestination?.id == R.id.notifications_dest)
                        unreadNotificationsCount = 0
                    invalidateOptionsMenu()
                }

                GAME_STATE_UPDATE_ACTION -> {
                    val gameStateDetails = intent.getParcelableExtra<GameStateDetails>(GAME_STATE_KEY)

                    // TODO: Do something with update
                    if (gameStateDetails != null) when (gameStateDetails.gameStateUpdateType) {
                        GameStateUpdateType.MarketStateUpdate ->
                            displayMarketStatusAlert(gameStateDetails.isMarketOpen ?: true)
                        GameStateUpdateType.StockDividendStateUpdate ->
                            model.updateDividendState(gameStateDetails.dividendStockId, gameStateDetails.givesDividend)
                        GameStateUpdateType.StockBankruptStateUpdate ->
                            model.updateBankruptState(gameStateDetails.bankruptStockId, gameStateDetails.isBankrupt)
                        else ->
                            Log.v(MainActivity::class.java.simpleName, "Game state update unused: $gameStateDetails")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProvider(this).get(DalalViewModel::class.java)

        val tinyDB = TinyDB(this)
        tinyDB.remove(NOTIFICATION_SHARED_PREF)
        tinyDB.remove(NOTIFICATION_NEWS_SHARED_PREF)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        val mainToolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.mainToolbar)
        mainToolbar.inflateMenu(R.menu.main_menu)

        setSupportActionBar(mainToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.hamburger_icon))

        setupNavigationDrawer()

        model.ownedStockDetails = intent.getSerializableExtra(STOCKS_OWNED_KEY) as HashMap<Int, Long>
        model.globalStockDetails = intent.getSerializableExtra(GLOBAL_STOCKS_KEY) as HashMap<Int, GlobalStockDetails>
        model.reservedStockDetails = intent.getSerializableExtra(RESERVED_STOCKS_KEY) as HashMap<Int, Long>
        model.mortgageStockDetails = hashMapOf()
        model.reservedCash = intent.getLongExtra(RESERVED_CASH_KEY, 0)

        setupWorthTextViews()

        startMakingButtonsTransparent()

        createNetworkCallbackObject()

        getMortgageDetailsAsynchronously()

        if (!intent.getBooleanExtra(MARKET_OPEN_KEY, false))
            displayMarketStatusAlert(false)

        drawerEdgeButton.setOnClickListener { mainDrawerLayout.openDrawer(GravityCompat.START, true) }

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
        cashInHandTextView.setOnClickListener(worthViewClickListener)
        cashWorthTextView.setOnClickListener(worthViewClickListener)
        stocksInHandTextView.setOnClickListener(worthViewClickListener)
        stockWorthTextView.setOnClickListener(worthViewClickListener)
        totalInHandTextView.setOnClickListener(worthViewClickListener)
        totalWorthTextView.setOnClickListener(worthViewClickListener)
        createChannel()
        var notificationIntent = Intent(this.getBaseContext(), PushNotificationService::class.java)
        this.startService(notificationIntent)

    }


    // Adding and setting up Navigation drawer
    private fun setupNavigationDrawer() {

        val host = supportFragmentManager.findFragmentById(R.id.main_host_fragment) as NavHostFragment
        navigationViewLeft.setupWithNavController(host.navController)

        MiscellaneousUtils.username = intent.getStringExtra(USERNAME_KEY)
        val header = navigationViewLeft.getHeaderView(0)
        header.find<TextView>(R.id.usernameTextView).text = MiscellaneousUtils.username

        mainDrawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
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

        val id = item.itemId
        contentView?.hideKeyboard()

        when (id) {
            R.id.action_notifications -> {
                val navController = findNavController(R.id.main_host_fragment)
                unreadNotificationsCount = 0
                invalidateOptionsMenu()
                navController.navigate(R.id.notifications_dest, null, NavOptions.Builder().setPopUpTo(R.id.home_dest, false).build())
                return true
            }

            R.id.action_help -> {
                val navController = findNavController(R.id.main_host_fragment)
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
                mainDrawerLayout!!.openDrawer(GravityCompat.START)  // OPEN DRAWER
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun logout() = lifecycleScope.launch {
        var notificationIntent = Intent(baseContext, PushNotificationService::class.java)
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

    // Subscribes to s stream and gets updates (TESTED)
    private fun subscribeToNotificationsStream(notificationsSubscriptionId: SubscriptionId) {

        streamServiceStub.getNotificationUpdates(notificationsSubscriptionId,
                object : StreamObserver<NotificationUpdate> {
                    override fun onNext(value: NotificationUpdate?) {
                        unreadNotificationsCount++
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_UNREAD_NOTIFICATIONS_COUNT))
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
                                    stockBankruptState?.isBankrupt)

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
            netStockWorth += quantity * model.getGlobalStockPriceFromStockId(stockId)
        }
        stockWorth = netStockWorth

        changeTextViewValue(stockWorthTextView, stockIndicatorImageView, netStockWorth)

        // We need to add reserved stocks worth to calculate total worth
        for ((stockId, quantity) in model.reservedStockDetails) {
            netStockWorth += quantity * model.getGlobalStockPriceFromStockId(stockId)
        }
        // Backend has it the way TotalWorth = CashWorth + OwnedStockWorth + ReservedStockWorth
        totalWorth = netStockWorth + cashWorth + model.reservedCash

        changeTextViewValue(totalWorthTextView, totalIndicatorImageView, totalWorth)
    }

    // Initial setup, called in activity's onCreate()
    private fun setupWorthTextViews() {

        // Initial old value is 0; Zero is placeholder
        cashWorth = intent.getLongExtra(CASH_WORTH_KEY, -1)
        animateWorthChange(0, cashWorth, cashWorthTextView, cashIndicatorImageView)

        totalWorth = intent.getLongExtra(TOTAL_WORTH_KEY, -1)

        // Updates stockWorthTextView and totalWorthTextView
        updateStockWorthViaStreamUpdates()
    }

    // Creates a new networkCallback object
    private fun createNetworkCallbackObject() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                doAsync {
                    if (ConnectionUtils.isReachableByTcp(HOST, PORT)) {
                        uiThread {
                            if (lostOnce) {
                                navigateToLastOpenFragment()
                                subscribeToStreamsAsynchronously()
                            }

                            errorDialog?.dismiss()
                        }
                    } else {
                        uiThread { errorDialog?.setMessage(getString(R.string.error_server_down)) }
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                toast(getString(R.string.internet_connection_lost))
                lostOnce = true
                lastOpenFragmentId = findNavController(R.id.main_host_fragment).currentDestination?.id
                        ?: R.id.home_dest
            }
        }
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
    }

    // Increases/decreases text view value depending on input parameters
    private fun changeTextViewValue(textView: TextView, indicatorImageView: ImageView, newValue: Long) {
        val oldValue = textView.text.toString().replace(",", "").toLong()
        animateWorthChange(oldValue, newValue, textView, indicatorImageView)
    }

    // Starts making drawer button translucent
    private fun startMakingButtonsTransparent() {

        object : Thread() {
            override fun run() {

                while (drawerEdgeButton!!.alpha > 0.70) {
                    try {
                        sleep(175)
                        runOnUiThread { drawerEdgeButton!!.alpha = (drawerEdgeButton!!.alpha - 0.01).toFloat() }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    @Synchronized
    private fun animateWorthChange(oldValue: Long, newValue: Long, textView: TextView, indicatorImageView: ImageView) {

        if (newValue == oldValue) return

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

    override fun onBackPressed() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    public override fun onResume() {
        super.onResume()

        lostOnce = false
        subscribeToStreamsAsynchronously()

        val intentFilter = IntentFilter(REFRESH_ALL_WORTH_ACTION)
        intentFilter.addAction(REFRESH_UNREAD_NOTIFICATIONS_COUNT)
        intentFilter.addAction(GAME_STATE_UPDATE_ACTION)

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCashStockReceiver, IntentFilter(intentFilter))

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    public override fun onPause() {
        super.onPause()

        unsubscribeFromAllStreams()
        lastOpenFragmentId = findNavController(R.id.main_host_fragment).currentDestination?.id
                ?: R.id.home_dest

        preferences.edit().remove(LAST_TRANSACTION_ID).apply()

        helpDialog?.dismiss()
        logoutDialog?.dismiss()

        connectivityManager.unregisterNetworkCallback(networkCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCashStockReceiver)

        preferences.edit().remove(LAST_TRANSACTION_ID).apply()
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

        lastOpenFragmentId = fragment

        errorDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage(message)
                .setPositiveButton(getString(R.string.retry), null)
                .setTitle(getString(R.string.error))
                .setCancelable(false)
                .create()

        errorDialog?.setOnShowListener {
            val positiveButton = errorDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener {
                onRetryButtonDialogClick()
            }
        }
        errorDialog?.show()
        contentView?.hideKeyboard()
    }

    private fun onRetryButtonDialogClick() {
        errorDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@MainActivity)) {
                if (ConnectionUtils.isReachableByTcp(HOST, PORT)) {
                    uiThread {
                        errorDialog?.dismiss()
                        navigateToLastOpenFragment()
                    }
                } else {
                    uiThread { errorDialog?.setMessage(getString(R.string.error_server_down)) }
                }
            } else {
                uiThread { errorDialog?.setMessage(getString(R.string.error_check_internet)) }
            }
            uiThread { errorDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true }
        }
    }

    private fun navigateToLastOpenFragment() {
        val navController = findNavController(R.id.main_host_fragment)
        when (lastOpenFragmentId) { // Otherwise it crashes as these details fragment require arguments to be passed
            R.id.nav_news_details -> lastOpenFragmentId = R.id.news_dest
            R.id.company_description_dest -> lastOpenFragmentId = R.id.home_dest
        }
        navController.navigate(lastOpenFragmentId, null, NavOptions.Builder().setPopUpTo(R.id.home_dest, false).build())
    }

    private fun createChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            var nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var mChannel = NotificationChannel("dalal_notification_channel","dalal_notification_channel",NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.enableLights(true)
            mChannel.description = "gi"
            mChannel.setShowBadge(true)
            mChannel.setLightColor(Color.RED)
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
        private const val REFRESH_UNREAD_NOTIFICATIONS_COUNT = "refresh-unread-notifications-count"

        private const val GAME_STATE_UPDATE_ACTION = "game-state-update-action"
        private const val GAME_STATE_KEY = "game-state-key"
    }
}
