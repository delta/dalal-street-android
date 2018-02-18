package org.pragyan.dalal18.notifications;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.adapter.NotificationRecyclerAdapter;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.data.Notification;
import org.pragyan.dalal18.loaders.NotificationLoader;
import org.pragyan.dalal18.utils.ConnectionUtils;
import org.pragyan.dalal18.utils.Constants;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetNotificationsResponse;

public class NotificationFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetNotificationsResponse> {

    private static final String LAST_NOTIFICATION_ID = "last_notification_id";

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    NotificationRecyclerAdapter notificationRecyclerAdapter;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.notifications_recyclerView)
    RecyclerView notificationsRecyclerView;

    @BindView(R.id.noNotification_textView)
    TextView noNotificationTextView;

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    private AlertDialog loadingDialog;

    boolean paginate = true;
    ArrayList<Notification> customNotificationList = new ArrayList<>();

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
            String tempString = "Getting notifications...";
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(tempString);
            loadingDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.notifications));
        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);

        notificationRecyclerAdapter = new NotificationRecyclerAdapter(getContext(), null);
        notificationsRecyclerView.setAdapter(notificationRecyclerAdapter);
        notificationsRecyclerView.setHasFixedSize(false);
        notificationsRecyclerView.addOnScrollListener(new CustomScrollListener());
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getActivity().getSupportLoaderManager().restartLoader(Constants.NOTIFICATION_LOADER_ID, null, this);
        loadingDialog.show();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferences.edit().remove(LAST_NOTIFICATION_ID).apply();
    }

    @Override
    public Loader<GetNotificationsResponse> onCreateLoader(int id, Bundle args) {

        if (getContext() != null) {
            int lastId = preferences.getInt(LAST_NOTIFICATION_ID, 0);
            return new NotificationLoader(getContext(), actionServiceBlockingStub, lastId);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<GetNotificationsResponse> loader, GetNotificationsResponse data) {

        loadingDialog.dismiss();

        if (data == null) {
            networkDownHandler.onNetworkDownError();
            return;
        }

        paginate = data.getNotificationsCount() == 10;

        if (data.getNotificationsList().size() > 0) {

            for (dalalstreet.api.models.Notification currentNotification : data.getNotificationsList()) {
                customNotificationList.add(new Notification(currentNotification.getText(), currentNotification.getCreatedAt()));
                preferences.edit()
                        .putInt(LAST_NOTIFICATION_ID, currentNotification.getId())
                        .apply();
            }

            notificationRecyclerAdapter.swapData(customNotificationList);
            noNotificationTextView.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);

        } else {
            noNotificationTextView.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<GetNotificationsResponse> loader) {
        // Do nothing
    }


    public class CustomScrollListener extends RecyclerView.OnScrollListener {

        CustomScrollListener() {
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
            int totalItemCount = recyclerView.getLayoutManager().getItemCount();
            int pastVisibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (pastVisibleItems + visibleItemCount >= totalItemCount) {

                if (paginate) {
                    if (getActivity() != null) {
                        getActivity().getSupportLoaderManager().restartLoader(Constants.NOTIFICATION_LOADER_ID, null, NotificationFragment.this);
                        paginate = false;
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.edit().remove(LAST_NOTIFICATION_ID).apply();
    }
}
