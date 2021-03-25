package org.pragyan.dalal18.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
import javax.inject.Inject


class TransactionsFragment : Fragment() {

    private var binding by viewLifecycle<FragmentTransactionsBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var model: DalalViewModel

    private val transactionList = ArrayList<Transaction>()
    private lateinit var transactionsAdapter: TransactionRecyclerAdapter
    private lateinit var loadingDialog: AlertDialog

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler

    // Pagination
    private var firstFetch = true
    private var paginate = true
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

        buildRecyclerView()

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.loading_transaction)
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()
        preferences.edit().remove(Constants.LAST_TRANSACTION_ID).apply()
        getTransactionsAsynchronously()
    }

    private fun buildRecyclerView() {
        transactionsAdapter = TransactionRecyclerAdapter(context, null, model.globalStockDetails)
        binding.transactionsRecyclerView.apply {
            adapter = transactionsAdapter
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
        }

        binding.mainContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (paginate && activity != null) {
                    getTransactionsAsynchronously()
                    paginate = false
                }
            }
        })
    }

    private fun getTransactionsAsynchronously() {
        if (firstFetch)
            loadingDialog.show()
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
                        if (!paginate) {
                            // No more Transactions left to show for the user
                            binding.progressBar.visibility = View.GONE
                        }

                        val positionStart = transactionList.size
                        val newItemsCount = transactionsResponse.transactionsList.size

                        transactionList.addAll(transactionsResponse.transactionsList)

                        binding.apply {
                            if (firstFetch) {
                                if (transactionList.isEmpty()) {
                                    // No Transactions on the First Fetch itself
                                    mainContent.visibility = View.GONE
                                    noTransactionsTextView.visibility = View.VISIBLE
                                } else
                                    mainContent.visibility = View.VISIBLE

                                firstFetch = false
                            }
                            transactionsAdapter.setList(transactionList)
                            transactionsAdapter.notifyItemRangeInserted(positionStart, newItemsCount)

                            preferences.edit()
                                    .putInt(Constants.LAST_TRANSACTION_ID, transactionsResponse.transactionsList.last().id)
                                    .apply()
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.transactions_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.transactions_dest) }
            }
            if (firstFetch)
                uiThread { loadingDialog.dismiss() }
        }
    }

    override fun onPause() {
        super.onPause()
        preferences.edit().remove(Constants.LAST_TRANSACTION_ID).apply()
    }
}
