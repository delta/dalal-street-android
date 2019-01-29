package org.pragyan.dalal18.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.LoginRequest
import dalalstreet.api.actions.LoginResponse
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.GlobalStockDetails
import org.pragyan.dalal18.data.StockDetails
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils
import java.util.*
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    /* Not injecting stub directly into this context to prevent empty/null metadata attached to stub since user has not logged in. */
    @Inject
    lateinit var channel: ManagedChannel

    @Inject
    lateinit var preferences: SharedPreferences

    private var drawingThread: Thread? = null

    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            return resultCode == ConnectionResult.SUCCESS
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (isGooglePlayServicesAvailable) {
            DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)
            startLoginProcess(preferences.getString(EMAIL_KEY, null), preferences.getString(PASSWORD_KEY, null))

        } else {
            val playServicesBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            playServicesBuilder
                    .setMessage("Dalal Street requires latest version of google play services.")
                    .setPositiveButton("Close") { _, _ -> finish() }
                    .setTitle("Update PlayServices")
                    .setCancelable(true)
                    .show()
        }
    }

    override fun onStart() {
        super.onStart()
        drawingThread = Thread(graph_drawer)
        drawingThread?.start()
    }

    private fun startLoginProcess(email: String?, password: String?) {

        splashTextView.text = getString(R.string.signing_in)
        
        if (email != null && password != null && email != "") {
            loginAsynchronously(email, password)
        } else {
            startLoginActivity()
        }
    }
    
    private fun loginAsynchronously(email: String, password: String) {
        doAsync {
            if (ConnectionUtils.getConnectionInfo(this@SplashActivity)) {
                
                val sessionId = preferences.getString(Constants.SESSION_KEY, null)
                
                if(sessionId != null) {
                    
                    val customStub = MetadataUtils.attachHeaders(DalalActionServiceGrpc.newBlockingStub(channel), getStubMetadata(sessionId))
                    val loginResponse = customStub.login(LoginRequest.newBuilder().setEmail(email).setPassword(password).build())
                    
                    uiThread {

                        if (loginResponse.statusCode == LoginResponse.StatusCode.OK) {

                            MiscellaneousUtils.sessionId = loginResponse.sessionId

                            // Adding user's stock details
                            val stocksOwnedList = ArrayList<StockDetails>()
                            val stocksOwnedMap = loginResponse.stocksOwnedMap

                            for (i in 1..Constants.NUMBER_OF_COMPANIES) {
                                if (stocksOwnedMap.containsKey(i)) {
                                    stocksOwnedList.add(StockDetails(i, stocksOwnedMap[i]!!))
                                }
                            }

                            // Adding global stock details
                            val globalStockList = ArrayList<GlobalStockDetails>()
                            val globalStockMap = loginResponse.stockListMap

                            for (q in 1..globalStockMap.size) {

                                val currentStockDetails = globalStockMap[q]

                                if (currentStockDetails != null) {
                                    globalStockList.add(GlobalStockDetails(
                                            currentStockDetails.fullName,
                                            currentStockDetails.shortName,
                                            q,
                                            currentStockDetails.description,
                                            currentStockDetails.currentPrice,
                                            currentStockDetails.stocksInMarket,
                                            currentStockDetails.stocksInExchange,
                                            currentStockDetails.previousDayClose,
                                            if (currentStockDetails.upOrDown) 1 else 0,
                                            resources.getStringArray(R.array.image_links)[q-1]))
                                            //Constants.COMPANY_IMAGES_BASE_URL + currentStockDetails.shortName.toUpperCase() + ".png"))
                                }
                            }

                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            
                            with(intent) {
                                putExtra(USERNAME_KEY, loginResponse.user.name)
                                putExtra(MainActivity.CASH_WORTH_KEY, loginResponse.user.cash)
                                putExtra(MainActivity.TOTAL_WORTH_KEY, loginResponse.user.total)
                                putExtra(MARKET_OPEN_KEY, loginResponse.isMarketOpen)

                                putParcelableArrayListExtra(MainActivity.STOCKS_OWNED_KEY, stocksOwnedList)
                                putParcelableArrayListExtra(MainActivity.GLOBAL_STOCKS_KEY, globalStockList)
                            }

                            // Checking for constants
                            for ((key, value) in loginResponse.constantsMap) {
                                if (key == "MORTGAGE_DEPOSIT_RATE")
                                    Constants.MORTGAGE_DEPOSIT_RATE = value.toDouble()
                                else if (key == "MORTGAGE_RETRIEVE_RATE")
                                    Constants.MORTGAGE_RETRIEVE_RATE = value.toDouble()
                                else if (key == "ORDER_FEE_PERCENT")
                                    Constants.ORDER_FEE_RATE = (value.toDouble()/100)
                            }

                            preferences.edit().putString(Constants.MARKET_OPEN_TEXT_KEY, loginResponse.marketIsOpenHackyNotif)
                                    .putString(Constants.MARKET_CLOSED_TEXT_KEY, loginResponse.marketIsClosedHackyNotif).apply()

                            startActivity(intent)
                            finish()
                            
                        } else {
                            Toast.makeText(this@SplashActivity, "Please login again", Toast.LENGTH_SHORT).show()
                            preferences.edit().putString(EMAIL_KEY, null).putString(PASSWORD_KEY, null).apply()
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    uiThread { startLoginActivity() }
                }
                
            } else /* No internet available */ {
                uiThread {
                    Handler().postDelayed({
                        Snackbar.make(findViewById<View>(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY") {
                                    startLoginProcess(email, password)
                                    splashTextView.setText(R.string.error_signing_in)
                                }
                                .show()
                        splashTextView.setText(R.string.error_signing_in)
                    }, 500)
                }
            }
        }
    }
    
    private fun startLoginActivity() {
        preferences.edit().putString(EMAIL_KEY, null).putString(PASSWORD_KEY, null).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        if(drawingThread != null && drawingThread!!.isAlive)
            drawingThread?.interrupt()
    }

    private fun getStubMetadata(sessionId: String): Metadata {
        val metadata = Metadata()
        val metadataKey = Metadata.Key.of("sessionid", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(metadataKey, sessionId)
        return metadata
    }

    companion object {
        const val USERNAME_KEY = "username-key"
        const val EMAIL_KEY = "email-key"
        const val MARKET_OPEN_KEY = "market-open-key"
        internal const val PASSWORD_KEY = "password-key"
    }
}