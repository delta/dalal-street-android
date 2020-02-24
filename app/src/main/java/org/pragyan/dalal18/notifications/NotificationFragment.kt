package org.pragyan.dalal18.notifications

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.DalalActionServiceGrpc
import dalalstreet.api.actions.GetNotificationsRequest
import kotlinx.android.synthetic.main.fragment_notification.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.pragyan.dalal18.R
import org.pragyan.dalal18.adapter.NotificationRecyclerAdapter
import org.pragyan.dalal18.dagger.ContextModule
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent
import org.pragyan.dalal18.data.Notification
import org.pragyan.dalal18.ui.MainActivity
import org.pragyan.dalal18.utils.ConnectionUtils
import org.pragyan.dalal18.utils.Constants
import java.util.*
import javax.inject.Inject

class NotificationFragment : Fragment() {

    @Inject
    lateinit var actionServiceBlockingStub: DalalActionServiceGrpc.DalalActionServiceBlockingStub

    @Inject
    lateinit var notificationRecyclerAdapter: NotificationRecyclerAdapter

    @Inject
    lateinit var preferences: SharedPreferences

    lateinit var networkDownHandler: ConnectionUtils.OnNetworkDownHandler
    private var loadingDialog: AlertDialog? = null
    private var paginate = true
    private var customNotificationList = ArrayList<Notification>()
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
        val rootView = inflater.inflate(R.layout.fragment_notification, container, false)
        DaggerDalalStreetApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.notifications)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val tempString = "Getting notifications..."
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).text = tempString
        loadingDialog = AlertDialog.Builder(context!!)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        notificationRecyclerAdapter = NotificationRecyclerAdapter(context, null)
        with(notifications_recyclerView) {
            adapter = notificationRecyclerAdapter
            setHasFixedSize(false)
            addOnScrollListener(CustomScrollListener())
            layoutManager = LinearLayoutManager(this@NotificationFragment.context)
        }


        preferences.edit().remove(Constants.LAST_NOTIFICATION_ID).apply()
        getNotificationsAsynchronously()
    }

    private fun getNotificationsAsynchronously() {

        loadingDialog?.show()
        lastId = preferences.getInt(Constants.LAST_NOTIFICATION_ID, 0)

        doAsync {
            if (ConnectionUtils.getConnectionInfo(context)) {
                if (ConnectionUtils.isReachableByTcp(Constants.HOST, Constants.PORT)) {
                    val notificationsResponse = actionServiceBlockingStub.getNotifications(
                            GetNotificationsRequest.newBuilder().setLastNotificationId(lastId).setCount(10).build())

                    uiThread {
                        paginate = notificationsResponse.notificationsCount == 10

                        if (notificationsResponse.notificationsList.size > 0) {

                            for (currentNotification in notificationsResponse.notificationsList) {
                                customNotificationList.add(Notification(currentNotification.text, currentNotification.createdAt))
                                preferences.edit()
                                        .putInt(Constants.LAST_NOTIFICATION_ID, currentNotification.id)
                                        .apply()
                            }

                            notificationRecyclerAdapter.swapData(customNotificationList)
                            noNotification_textView.visibility = View.GONE
                            notifications_recyclerView.visibility = View.VISIBLE

                        } else {
                            noNotification_textView.visibility = View.VISIBLE
                            notifications_recyclerView.visibility = View.GONE
                        }
                    }
                } else {
                    uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_server_down), R.id.notifications_dest) }
                }
            } else {
                uiThread { networkDownHandler.onNetworkDownError(resources.getString(R.string.error_check_internet), R.id.notifications_dest) }
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
                        getNotificationsAsynchronously()
                        paginate = false
                    }
                }
            }
        }
    }

    private val refreshNotifications = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            customNotificationList.add(Notification(intent.getStringExtra(TEXT_KEY), intent.getStringExtra(CREATED_AT_KEY)))
            customNotificationList.sortByDescending { it.createdAt }
            notificationRecyclerAdapter.swapData(customNotificationList)
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
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshNotifications, IntentFilter(intentFilter))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshNotifications)
        preferences.edit().remove(Constants.LAST_NOTIFICATION_ID).apply()
    }

    companion object {

        public const val TEXT_KEY = "text-key"
        public const val CREATED_AT_KEY = "createdat-key"
    }
}