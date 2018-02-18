package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

import static org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.MyViewHolder>{

    private Context context;
    private List<NewsDetails> newsList = new ArrayList<>();
    OnClickListener onClickListener;

    public NewsRecyclerAdapter(Context context, List<NewsDetails> newsList) {
        this.newsList = newsList;
        this.context = context;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.news_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        NewsDetails currentNewsDetails = newsList.get(position);

        holder.headlinesTextView.setText(currentNewsDetails.getHeadlines());
        holder.createdAtTextView.setText(parseDate(currentNewsDetails.getCreatedAt()));
        holder.contentTextView.setText(currentNewsDetails.getContent());

        holder.newsLayout.setOnClickListener(view -> onClickListener.itemClicked(view,position));
    }

    @Override
    public int getItemCount() {
        if (newsList == null || newsList.size() == 0) return 0;
        return newsList.size();
    }

    public void swapData(List<NewsDetails> list) {
        newsList = list;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView headlinesTextView, contentTextView, createdAtTextView;
        RelativeLayout newsLayout;

        MyViewHolder(View view) {
            super(view);
            headlinesTextView = view.findViewById(R.id.news_head);
            contentTextView = view.findViewById(R.id.news_content);
            createdAtTextView = view.findViewById(R.id.createdAt_textView);
            newsLayout = view.findViewById(R.id.news_layout);
        }
    }

    public interface OnClickListener{
        void itemClicked(View view,int position);
    }
}