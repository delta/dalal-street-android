package org.pragyan.dalal18.fragment

import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetTransactionsRequest
import kotlinx.android.synthetic.main.fragment_transactions.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.TransactionRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.Transaction
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import java.util.ArrayList
import javax.inject.Inject

@Suppress("DEPRECATION")
class TransactionsFragment : Fragment() {

    private val LAST_TRANSACTION_ID = "last_transaction_id"

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private val transactionList = ArrayList<Transaction>()
    private var transactionsAdapter: TransactionRecyclerAdapter? = null
    private var loadingDialog: AlertDialog? = null

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    internal var paginate = true

    private val connectionChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                networkDownHandler.onNetworkDownError()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_transactions, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionsAdapter = TransactionRecyclerAdapter(context, null)

        with(transactions_recyclerView) {
            adapter = transactionsAdapter
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(CustomScrollListener())
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.loading_transaction)
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        getTransactionsAsynchronously()
    }


    override fun onDestroy() {
        super.onDestroy()
        preferences
                .edit()
                .remove(LAST_TRANSACTION_ID)
                .apply()
    }

    private fun getTransactionsAsynchronously() {

        loadingDialog?.show()

        doAsync {

            val lastId = preferences.getInt(LAST_TRANSACTION_ID, 0)

            if (ConnectionUtils.getConnectionInfo(context) && ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                val transactionsResponse = actionServiceBlockingStub.getTransactions(GetTransactionsRequest
                        .newBuilder()
                        .setCount(0)
                        .setLastTransactionId(lastId)
                        .build())

                uiThread {

                    loadingDialog?.dismiss()
                    paginate = transactionsResponse.transactionsCount == 10

                    for (i in 0 until transactionsResponse.transactionsCount) {
                        val currentTransaction = transactionsResponse.getTransactions(i)
                        transactionList.add(Transaction(
                                currentTransaction.type.name,
                                currentTransaction.stockId,
                                currentTransaction.stockQuantity,
                                currentTransaction.price.toFloat(),
                                currentTransaction.createdAt,
                                currentTransaction.total.toFloat()
                        ))
                        preferences.edit()
                                .putInt(LAST_TRANSACTION_ID, currentTransaction.id)
                                .apply()
                    }

                    if (transactionList.size == 0) {
                        transactions_recyclerView.visibility = View.GONE
                        noTransactions_relativeLayout.visibility = View.VISIBLE
                    } else {
                        transactionsAdapter?.swapData(transactionList)
                        transactions_recyclerView.visibility = View.VISIBLE
                        noTransactions_relativeLayout.visibility = View.GONE
                    }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError() }
            }
        }
    }

    inner class CustomScrollListener internal constructor() : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val visibleItemCount = recyclerView.layoutManager!!.childCount
            val totalItemCount = recyclerView.layoutManager!!.itemCount
            val pastVisibleItems = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (pastVisibleItems + visibleItemCount >= totalItemCount) {

                if (paginate) {
                    if (activity != null) {
                        getTransactionsAsynchronously()
                        paginate = false
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(connectionChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    }

    override fun onPause() {
        super.onPause()
        preferences.edit().remove(LAST_TRANSACTION_ID).apply()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(connectionChangeReceiver)
    }
}
