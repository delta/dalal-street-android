package org.pragyan.dalal18.notifications

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetNotificationsRequest
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.NotificationRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.Notification
import org.pragyan.dalal18.databinding.FragmentNotificationBinding
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.viewLifecycle
import javax.inject.Inject

class NotificationFragment : Fragment() {

    private var binding by viewLifecycle<FragmentNotificationBinding>()

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var notificationRecyclerAdapter: NotificationRecyclerAdapter

    @Inject
    lateinit var preferences: SharedPreferences

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private lateinit var loadingDialog: AlertDialog

    private var customNotificationList = ArrayList<Notification>()

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
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Getting notifications..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        buildRecyclerView()

        preferences.edit().remove(Constants.LAST_NOTIFICATION_ID).apply()
        getNotificationsAsynchronously()
    }

    private fun buildRecyclerView() {
        notificationRecyclerAdapter = NotificationRecyclerAdapter(context, null)
        binding.notificationsRecyclerView.apply {
            adapter = notificationRecyclerAdapter
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@NotificationFragment.context)
        }

        binding.mainContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (paginate && activity != null) {
                    println("Bottom")
                    getNotificationsAsynchronously()
                    paginate = false
                }
            }
        })
    }

    private fun getNotificationsAsynchronously() {
        if (firstFetch)
            loadingDialog.show()
        lastId = preferences.getInt(Constants.LAST_NOTIFICATION_ID, 0)

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val notificationsResponse = actionServiceBlockingStub.getNotifications(GetNotificationsRequest
                            .newBuilder()
                            .setLastNotificationId(lastId)
                            .setCount(10)
                            .build())

                    uiThread {
                        paginate = notificationsResponse.notificationsCount == 10
                        if (!paginate) {
                            // No more Transactions left to show for the user
                            binding.progressBar.visibility = View.GONE
                        }

                        val positionStart = customNotificationList.size
                        val newItemsCount = notificationsResponse.notificationsList.size

                        customNotificationList.addAll(notificationsResponse.notificationsList.map {
                            Notification(it.text, it.createdAt)
                        })

                        binding.apply {
                            if (firstFetch) {
                                if (customNotificationList.isEmpty()) {
                                    // No Notifications on the First Fetch itself
                                    noNotificationTextView.visibility = View.VISIBLE
                                } else
                                    mainContent.visibility = View.VISIBLE

                                firstFetch = false
                            }
                        }
                        notificationRecyclerAdapter.setList(customNotificationList)
                        notificationRecyclerAdapter.notifyItemRangeInserted(positionStart, newItemsCount)

                        preferences.edit()
                                .putInt(Constants.LAST_NOTIFICATION_ID, notificationsResponse.notificationsList.last().id)
                                .apply()
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.notifications_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.notifications_dest) }
            }
            if (firstFetch)
                uiThread { loadingDialog.dismiss() }
        }
    }

    private val refreshNotifications = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            customNotificationList.add(Notification(intent.getStringExtra(TEXT_KEY), intent.getStringExtra(CREATED_AT_KEY)))
            customNotificationList.sortByDescending { it.createdAt }
            notificationRecyclerAdapter.setList(customNotificationList)
            notificationRecyclerAdapter.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshNotifications)
        preferences.edit().remove(Constants.LAST_NOTIFICATION_ID).apply()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(MainActivity.REFRESH_UNREAD_NOTIFICATIONS_COUNT)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshNotifications, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.edit().remove(Constants.LAST_NOTIFICATION_ID).apply()
    }

    companion object {
        const val TEXT_KEY = "text-key"
        const val CREATED_AT_KEY = "createdat-key"
    }
}
