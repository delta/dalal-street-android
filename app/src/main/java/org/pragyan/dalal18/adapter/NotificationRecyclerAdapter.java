package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.Notification;

import java.util.List;

import static org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;

    public NotificationRecyclerAdapter(Context context, List<Notification> list) {
        this.context = context;
        notificationList = list;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.notification_list_item, parent, false);
        return new NotificationViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        holder.notificationTextTextView.setText(notificationList.get(position).getText());
        holder.createdAtTextView.setText(parseDate(notificationList.get(position).getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        if (notificationList == null || notificationList.size() <= 0) return 0;
        return notificationList.size();
    }

    public void setList(List<Notification> list) {
        notificationList = list;
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notificationTextTextView, createdAtTextView;

        NotificationViewHolder(View itemView) {
            super(itemView);

            notificationTextTextView = itemView.findViewById(R.id.notification_textView);
            createdAtTextView = itemView.findViewById(R.id.createdAt_textView);
        }
    }
}
