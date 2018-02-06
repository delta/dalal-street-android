package com.hmproductions.theredstreet.notifications;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.NotificationRecyclerAdapter;
import com.hmproductions.theredstreet.dagger.ContextModule;
import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
import com.hmproductions.theredstreet.data.Notification;
import com.hmproductions.theredstreet.loaders.NotificationLoader;
import com.hmproductions.theredstreet.utils.ConnectionUtils;
import com.hmproductions.theredstreet.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.GetNotificationsResponse;

public class NotificationFragment extends Fragment implements LoaderManager.LoaderCallbacks<GetNotificationsResponse> {

    @Inject
    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    NotificationRecyclerAdapter notificationRecyclerAdapter;

    @BindView(R.id.notifications_recyclerView)
    RecyclerView notificationsRecyclerView;

    @BindView(R.id.noNotification_textView)
    TextView noNotificationTextView;

    private ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    private AlertDialog loadingDialog;

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

        if (getContext() != null){
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

        notificationsRecyclerView.setAdapter(notificationRecyclerAdapter);
        notificationsRecyclerView.setHasFixedSize(false);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getActivity().getSupportLoaderManager().restartLoader(Constants.NOTIFICATION_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<GetNotificationsResponse> onCreateLoader(int id, Bundle args) {
        loadingDialog.show();

        if (getContext() != null) {
            return new NotificationLoader(getContext(), actionServiceBlockingStub);
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

        if (data.getNotificationsList().size() > 0) {

            List<Notification> customNotificationList = new ArrayList<>();
            customNotificationList.clear();

            for (dalalstreet.api.models.Notification currentNotification : data.getNotificationsList()) {
                customNotificationList.add(new Notification(currentNotification.getText(), currentNotification.getCreatedAt()));
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
}
