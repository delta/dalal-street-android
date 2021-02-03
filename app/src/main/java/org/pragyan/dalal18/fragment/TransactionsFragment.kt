package org.pragyan.dalal18.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetTransactionsRequest
import dalalstreet.api.models.Transaction
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.TransactionRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentTransactionsBinding
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import java.util.*
import javax.inject.Inject

class TransactionsFragment : Fragment() {

    private var binding by viewLifecycle<FragmentTransactionsBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var model: DalalViewModel

    private val transactionList = ArrayList<Transaction>()
    private var transactionsAdapter: TransactionRecyclerAdapter? = null
    private var loadingDialog: AlertDialog? = null

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    internal var paginate = true
    private var lastId = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            networkDownHandler = context as ConnectionUtils.OnNetworkDownHandler
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("$context must implement network down handler.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionsAdapter = TransactionRecyclerAdapter(context, null, model.globalStockDetails)

        with(binding.transactionsRecyclerView) {
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
        preferences.edit().remove(Constants.LAST_TRANSACTION_ID).apply()
        getTransactionsAsynchronously()
    }

    private fun getTransactionsAsynchronously() {

        loadingDialog?.show()

        doAsync {

            lastId = preferences.getInt(Constants.LAST_TRANSACTION_ID, 0)

            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {

                    val transactionsResponse = actionServiceBlockingStub.getTransactions(GetTransactionsRequest
                            .newBuilder()
                            .setCount(0)
                            .setLastTransactionId(lastId)
                            .build())

                    uiThread {

                        paginate = transactionsResponse.transactionsCount == 10

                        transactionList.addAll(transactionsResponse.transactionsList)

                        binding.apply {
                            if (transactionList.size == 0) {
                                transactionsRecyclerView.visibility = View.GONE
                                noTransactionsTextView.visibility = View.VISIBLE
                            } else {
                                transactionsAdapter?.swapData(transactionList)
                                transactionsRecyclerView.visibility = View.VISIBLE
                                noTransactionsTextView.visibility = View.GONE

                                preferences.edit()
                                        .putInt(Constants.LAST_TRANSACTION_ID, transactionsResponse.transactionsList.last().id)
                                        .apply()
                            }
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.transactions_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.transactions_dest) }
            }
            uiThread { loadingDialog?.dismiss() }
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
