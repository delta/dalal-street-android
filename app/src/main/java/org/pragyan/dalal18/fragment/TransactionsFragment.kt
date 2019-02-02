package org.pragyan.dalal18.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import java.util.*
import javax.inject.Inject

class TransactionsFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private val transactionList = ArrayList<Transaction>()
    private var transactionsAdapter: TransactionRecyclerAdapter? = null
    private var loadingDialog: AlertDialog? = null

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    internal var paginate = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_transactions, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.transactions)
        transactionsAdapter = TransactionRecyclerAdapter(context, null)

        with(transactionsRecyclerView) {
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

    private fun getTransactionsAsynchronously() {

        loadingDialog?.show()

        doAsync {

            val lastId = preferences.getInt(Constants.LAST_TRANSACTION_ID, 0)

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
                                currentTransaction.price,
                                currentTransaction.createdAt,
                                currentTransaction.total
                        ))
                        preferences.edit()
                                .putInt(Constants.LAST_TRANSACTION_ID, currentTransaction.id)
                                .apply()
                    }

                    if (transactionList.size == 0) {
                        transactionsRecyclerView.visibility = View.GONE
                        noTransactionsTextView.visibility = View.VISIBLE
                    } else {
                        transactionsAdapter?.swapData(transactionList)
                        transactionsRecyclerView.visibility = View.VISIBLE
                        noTransactionsTextView.visibility = View.GONE
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

    override fun onPause() {
        super.onPause()
        preferences.edit().remove(Constants.LAST_TRANSACTION_ID).apply()
    }
}
