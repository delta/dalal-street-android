package org.pragyan.dalal18.ui

import android.animation.ValueAnimator
import android.content.*
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
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
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.DalalStreamServiceGrpc
import dalalstreet.api.actions.LogoutRequest
import dalalstreet.api.actions.LogoutResponse
import dalalstreet.api.datastreams.*
import dalalstreet.api.models.TransactionType
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.fragment.mortgage.MortgageFragment
import org.pragyan.dalal18.notifications.NotificationService
import org.pragyan.dalal18.utils.*
import org.pragyan.dalal18.utils.Constants.REFRESH_OWNED_STOCKS_ACTION
import org.pragyan.dalal18.utils.Constants.REFRESH_RESERVED_ASSETS_ACTION
import org.pragyan.dalal18.utils.MiscellaneousUtils.getNumberOfPlayersOnline
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

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

    private var notifIntent: Intent? = null
    private var handler: Handler? = null

    private var cashWorth: Long = 0
    private var stockWorth: Long = 0
    private var totalWorth: Long = 0
    private var lastOpenFragmentId = R.id.home_dest

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private val refreshCashStockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                REFRESH_WORTH_TEXTVIEW_ACTION -> {
                    // Passing future value of cash worth TextView beforehand as old cash
                    cashWorth += intent.getLongExtra(TOTAL_WORTH_KEY, 0)
                    updateStockWorthViaStreamUpdates()
                    changeTextViewValue(cashWorthTextView, cashIndicatorImageView, cashWorth)
                }

                REFRESH_CASH_AND_TOTAL_ACTION -> {
                    totalWorth += intent.getLongExtra(TOTAL_WORTH_KEY, 0)
                    changeTextViewValue(totalWorthTextView, totalIndicatorImageVIew, totalWorth)
                    cashWorth += intent.getLongExtra(TOTAL_WORTH_KEY, 0)
                    changeTextViewValue(cashWorthTextView, cashIndicatorImageView, cashWorth)
                }

                REFRESH_CASH_ACTION -> {
                    cashWorth += intent.getLongExtra(TOTAL_WORTH_KEY, 0)
                    changeTextViewValue(cashWorthTextView, cashIndicatorImageView, cashWorth)
                }

                UPDATE_WORTH_VIA_STREAM_ACTION ->
                    updateStockWorthViaStreamUpdates()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProviders.of(this).get(DalalViewModel::class.java)

        val tinyDB = TinyDB(this)
        tinyDB.remove(Constants.NOTIFICATION_SHARED_PREF)
        tinyDB.remove(Constants.NOTIFICATION_NEWS_SHARED_PREF)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.hamburger_icon))

        setupNavigationDrawer()

        model.ownedStockDetails = intent.getParcelableArrayListExtra(STOCKS_OWNED_KEY)
        model.globalStockDetails = intent.getParcelableArrayListExtra(GLOBAL_STOCKS_KEY)
        model.reservedStockDetails = intent.getParcelableArrayListExtra(RESERVED_STOCKS_KEY)
        model.reservedCash = intent.getLongExtra(RESERVED_CASH_KEY, 0)
        model.createCompanyArrayFromGlobalStockDetails()

        setupWorthTextViews()

        startMakingButtonsTransparent()

        createNetworkCallbackObject()

        if (!intent.getBooleanExtra(SplashActivity.MARKET_OPEN_KEY, false)) {
            AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("Market Closed")
                    .setMessage("Please check notifications for market opening time. Sorry for the inconvenience.")
                    .setCancelable(true)
                    .setPositiveButton("CLOSE") { dI, _ -> dI.dismiss() }
                    .show()
        }

        drawerEdgeButton.setOnClickListener { mainDrawerLayout.openDrawer(GravityCompat.START, true) }

        val navController = findNavController(R.id.main_host_fragment)
        val worthViewClickListener = View.OnClickListener {
            contentView?.hideKeyboard()
            navController.navigate(R.id.portfolio_dest, null, NavOptions.Builder().setPopUpTo(R.id.home_dest, false).build())
        }

        // Tried to use single view didn't work; It took up toolbar space also
        totalInHandTextView.setOnClickListener(worthViewClickListener)
        totalWorthTextView.setOnClickListener(worthViewClickListener)
        cashInHandTextView.setOnClickListener(worthViewClickListener)
        cashWorthTextView.setOnClickListener(worthViewClickListener)
        stocksInHandTextView.setOnClickListener(worthViewClickListener)
        stockWorthTextView.setOnClickListener(worthViewClickListener)
    }

    // Adding and setting up Navigation drawer
    private fun setupNavigationDrawer() {

        val host = supportFragmentManager.findFragmentById(R.id.main_host_fragment) as NavHostFragment
        navigationViewLeft.setupWithNavController(host.navController)

        MiscellaneousUtils.username = intent.getStringExtra(Constants.USERNAME_KEY)
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

        // Refresh every 1 min
        handler = Handler()
        handler?.post(object : Runnable {
            override fun run() {
                val tempString = "Players Online: " + getNumberOfPlayersOnline(System.currentTimeMillis(), 20, 24)
                header.find<TextView>(R.id.numberOfPlayersOnlineTextView).text = tempString
                handler?.postDelayed(this, 60000L)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        contentView?.hideKeyboard()

        when (id) {
            R.id.action_notifications -> {
                val navController = findNavController(R.id.main_host_fragment)
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

    fun logout() {

        unsubscribeFromAllStreams(false)

        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@MainActivity)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val logoutResponse = actionServiceBlockingStub.logout(LogoutRequest.newBuilder().build())

                    uiThread {
                        if (logoutResponse.statusCode == LogoutResponse.StatusCode.OK) {

                            val stopNotificationIntent = Intent(Constants.STOP_NOTIFICATION_ACTION)
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(stopNotificationIntent)

                            preferences.edit().putString(Constants.EMAIL_KEY, null).putString(Constants.PASSWORD_KEY, null).putString(Constants.SESSION_KEY, null).apply()
                        }

                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    uiThread { onNetworkDownError(resources.getString(R.string.error_server_down), R.id.home_dest) }
                }
            } else {
                uiThread { onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.home_dest) }
            }
        }
    }

    // Subscribes to transaction stream and gets updates (TESTED)
    private fun subscribeToTransactionsStream(transactionsSubscriptionId: SubscriptionId) {

        streamServiceStub.getTransactionUpdates(transactionsSubscriptionId,
                object : StreamObserver<TransactionUpdate> {
                    override fun onNext(value: TransactionUpdate) {

                        val transaction = value.transaction

                        when (transaction.type) {
                            TransactionType.DIVIDEND_TRANSACTION -> {
                                val intent = Intent(REFRESH_CASH_AND_TOTAL_ACTION)
                                intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                            }

                            TransactionType.ORDER_FILL_TRANSACTION -> { // transaction.total = -(stockTradePrice * stockTradeQty) + reservedCash
                                if (transaction.stockQuantity > 0) {  // Bid order transaction which means cash was reserved and user gains stocks
                                    model.updateStocksOwned(transaction.stockId, transaction.stockQuantity)

                                    // It will refresh all 3 worth TextViews
                                    val intent = Intent(REFRESH_WORTH_TEXTVIEW_ACTION)
                                    intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(Constants.REFRESH_OWNED_STOCKS_ACTION))

                                } else { // Ask order transaction which means stocks were reserved and OrderFill made user gain cash
                                    val intent = Intent(REFRESH_CASH_ACTION)
                                    intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                                }
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_RESERVED_ASSETS_ACTION))
                            }

                            TransactionType.FROM_EXCHANGE_TRANSACTION -> {

                                model.updateStocksOwned(transaction.stockId, transaction.stockQuantity)

                                val intent = Intent(REFRESH_WORTH_TEXTVIEW_ACTION)
                                intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                            }

                            TransactionType.MORTGAGE_TRANSACTION -> /* Meant for MORTGAGE TRANSACTION type; While retrieving stockQuantity is positive */ {

                                model.updateStocksOwned(transaction.stockId, transaction.stockQuantity)

                                var intent = Intent(REFRESH_WORTH_TEXTVIEW_ACTION)
                                intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)

                                intent = Intent(Constants.REFRESH_MORTGAGE_UPDATE_ACTION)
                                intent.putExtra(MortgageFragment.STOCKS_ID_KEY, transaction.stockId)
                                intent.putExtra(MortgageFragment.STOCKS_PRICE_KEY, transaction.price)
                                intent.putExtra(MortgageFragment.STOCKS_QUANTITY_KEY, transaction.stockQuantity)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                            }

                            TransactionType.TAX_TRANSACTION -> {
                                val intent = Intent(REFRESH_CASH_AND_TOTAL_ACTION)
                                intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                            }

                            TransactionType.ORDER_FEE_TRANSACTION -> {
                                val intent = Intent(REFRESH_CASH_AND_TOTAL_ACTION)
                                intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                            }

                            TransactionType.PLACE_ORDER_TRANSACTION -> {
                                if (transaction.total != 0L) { // Cash reserved; here transaction.total will be negative as reserveCash is taken from actual cash
                                    model.reservedCash += Math.abs(transaction.total)
                                    val intent = Intent(REFRESH_CASH_AND_TOTAL_ACTION) // Since now TotalWorth = CashWorth + StockWorth
                                    intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                                } else {
                                    model.updateReservedStocks(transaction.stockId, transaction.stockQuantity) // stockQuantity will be negative
                                    model.updateStocksOwned(transaction.stockId, transaction.stockQuantity)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_OWNED_STOCKS_ACTION))
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(UPDATE_WORTH_VIA_STREAM_ACTION))
                                }
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_RESERVED_ASSETS_ACTION))
                            }

                            TransactionType.CANCEL_ORDER_TRANSACTION -> {
                                if (transaction.total != 0L) { // Here transaction.total will be positive
                                    model.reservedCash -= Math.abs(transaction.total)
                                    val intent = Intent(REFRESH_CASH_AND_TOTAL_ACTION) //  Since now TotalWorth = CashWorth + StockWorth
                                    intent.putExtra(TOTAL_WORTH_KEY, transaction.total)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                                } else {
                                    model.updateReservedStocks(transaction.stockId, transaction.stockQuantity) // stockQuantity will be positive
                                    model.updateStocksOwned(transaction.stockId, transaction.stockQuantity)
                                    LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(UPDATE_WORTH_VIA_STREAM_ACTION))
                                }
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_RESERVED_ASSETS_ACTION))
                            }

                            TransactionType.RESERVE_UPDATE_TRANSACTION -> {
                                if (transaction.stockQuantity > 0) {
                                    model.updateReservedStocks(transaction.stockId, transaction.stockQuantity)
                                } else {
                                    model.reservedCash -= transaction.total
                                }
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(REFRESH_RESERVED_ASSETS_ACTION))
                            }

                            else -> {
                                // Do nothing
                            }
                        }
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
                        val refreshNewsIntent = Intent(Constants.REFRESH_NEWS_ACTION)
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
                        for (i in 1..Constants.NUMBER_OF_COMPANIES) {
                            if (value.pricesMap.containsKey(i)) {
                                model.updateGlobalStockPrice(i, value.pricesMap[i] ?: 0)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(Constants.REFRESH_PRICE_TICKER_ACTION))
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(Constants.REFRESH_STOCK_PRICES_ACTION))
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(UPDATE_WORTH_VIA_STREAM_ACTION))
                            }
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
                        val stockExchangeDataPointMap = value.stocksInExchangeMap
                        val tempGlobalStocks = model.globalStockDetails

                        for (x in 1..Constants.NUMBER_OF_COMPANIES) {
                            if (stockExchangeDataPointMap.containsKey(x)) {
                                val currentDataPoint = value.stocksInExchangeMap[x]

                                var position = -1
                                for (i in tempGlobalStocks.indices) {
                                    if (x == tempGlobalStocks[i].stockId) {
                                        position = i
                                        break
                                    }
                                }

                                model.updateGlobalStock(position, currentDataPoint!!.price, currentDataPoint.stocksInMarket, currentDataPoint.stocksInExchange)
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(Constants.REFRESH_STOCKS_EXCHANGE_ACTION))
                                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(Intent(UPDATE_WORTH_VIA_STREAM_ACTION))
                            }
                        }
                    }

                    override fun onError(t: Throwable) {

                    }

                    override fun onCompleted() {

                    }
                })
    }

    // Unsubscribes from all streams
    private fun unsubscribeFromAllStreams(shouldSubscribeAgain: Boolean) {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@MainActivity) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                for (subscriptionId in subscriptionIds) {
                    streamServiceBlockingStub.unsubscribe(UnsubscribeRequest.newBuilder().setSubscriptionId(subscriptionId).build())
                }

                subscriptionIds.clear()

                uiThread {
                    if (shouldSubscribeAgain) {
                        subscribeToStreamsAsynchronously()
                    }
                }
            }
        }
    }

    // Method is called when stock price update is received
    private fun updateStockWorthViaStreamUpdates() {
        var netStockWorth = 0L
        var rate = 0L
        for ((stockId, quantity) in model.ownedStockDetails) {
            for ((_, _, stockId1, _, price) in model.globalStockDetails) {
                if (stockId1 == stockId) {
                    rate = price
                    break
                }
            }
            netStockWorth += quantity * rate
        }
        stockWorth = netStockWorth

        var oldValue = stockWorthTextView.text.toString().replace(",", "").toLong()
        animateWorthChange(oldValue, netStockWorth, stockWorthTextView, stockIndicatorImageView)

        // We need to add reserved stocks worth to calculate total worth
        for ((stockId, quantity) in model.reservedStockDetails) {
            for ((_, _, stockId1, _, price) in model.globalStockDetails) {
                if (stockId1 == stockId) {
                    rate = price
                    break
                }
            }
            netStockWorth += quantity * rate
        }
        // Backend has it the way TotalWorth = CashWorth + StockWorth
        totalWorth = stockWorth + cashWorth

        oldValue = totalWorthTextView.text.toString().replace(",", "").toLong()
        animateWorthChange(oldValue, totalWorth, totalWorthTextView, totalIndicatorImageVIew)
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
                    if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                        uiThread {
                            unsubscribeFromAllStreams(true)
                            errorDialog?.dismiss()
                            navigateToLastOpenFragment()
                        }
                    } else {
                        uiThread { errorDialog?.setMessage(getString(R.string.error_server_down)) }
                    }
                }
            }

            override fun onLost(network: Network?) {
                super.onLost(network)
                toast(getString(R.string.internet_connection_lost))
                lastOpenFragmentId = findNavController(R.id.main_host_fragment).currentDestination?.id
                        ?: R.id.home_dest
            }
        }
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
                        Thread.sleep(175)
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

        val formatter = DecimalFormat(Constants.PRICE_FORMAT)
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

        subscribeToStreamsAsynchronously()

        val intentFilter = IntentFilter(REFRESH_CASH_AND_TOTAL_ACTION)
        intentFilter.addAction(REFRESH_WORTH_TEXTVIEW_ACTION)
        intentFilter.addAction(UPDATE_WORTH_VIA_STREAM_ACTION)
        intentFilter.addAction(REFRESH_CASH_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshCashStockReceiver, IntentFilter(intentFilter))

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        notifIntent = Intent(this, NotificationService::class.java)
        startService(notifIntent)
    }

    public override fun onPause() {
        super.onPause()

        unsubscribeFromAllStreams(false)
        lastOpenFragmentId = findNavController(R.id.main_host_fragment).currentDestination?.id
                ?: R.id.home_dest

        preferences.edit().remove(LAST_TRANSACTION_ID).remove(LAST_NOTIFICATION_ID).apply()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCashStockReceiver)
        helpDialog?.dismiss()
        logoutDialog?.dismiss()

        connectivityManager.unregisterNetworkCallback(networkCallback)

        stopService(notifIntent)
        preferences.edit().remove(LAST_TRANSACTION_ID).remove(LAST_NOTIFICATION_ID).apply()
    }

    private fun subscribeToStreamsAsynchronously() {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@MainActivity)) {

                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                    var response: SubscribeResponse = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_EXCHANGE).setDataStreamId("").build())
                    uiThread {
                        subscriptionIds.add(response.subscriptionId)
                        subscribeToStockExchangeStream(response.subscriptionId)
                    }

                    response = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.STOCK_PRICES).setDataStreamId("").build())
                    uiThread {
                        subscriptionIds.add(response.subscriptionId)
                        subscribeToStockPricesStream(response.subscriptionId)
                    }

                    response = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.MARKET_EVENTS).setDataStreamId("").build())
                    uiThread {
                        subscriptionIds.add(response.subscriptionId)
                        subscribeToMarketEventsUpdateStream(response.subscriptionId)
                    }

                    response = streamServiceBlockingStub.subscribe(SubscribeRequest.newBuilder().setDataStreamType(DataStreamType.TRANSACTIONS).setDataStreamId("").build())
                    uiThread {
                        subscriptionIds.add(response.subscriptionId)
                        subscribeToTransactionsStream(response.subscriptionId)
                    }

                } else {
                    onNetworkDownError(resources.getString(R.string.error_server_down), R.id.home_dest)
                }
            } else {
                onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.home_dest)
            }
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
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    uiThread {
                        unsubscribeFromAllStreams(true)
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

    companion object {

        private const val LAST_TRANSACTION_ID = "last_transaction_id"
        private const val LAST_NOTIFICATION_ID = "last_notification_id"

        const val CASH_WORTH_KEY = "cash-worth-key"
        const val TOTAL_WORTH_KEY = "total-worth-key"
        const val RESERVED_CASH_KEY = "reserved-cash-key"
        const val STOCKS_OWNED_KEY = "stocks-owned-key"
        const val GLOBAL_STOCKS_KEY = "global-stocks-key"
        const val RESERVED_STOCKS_KEY = "reserved-stocks-key"

        private const val REFRESH_WORTH_TEXTVIEW_ACTION = "refresh-cash-worth-textview"
        private const val REFRESH_CASH_ACTION = "refresh-hard-cash-action-textview"
        private const val REFRESH_CASH_AND_TOTAL_ACTION = "refresh-cash-and-total-worth-textview"
        private const val UPDATE_WORTH_VIA_STREAM_ACTION = "refresh-worth-via-stream-action"
    }
}