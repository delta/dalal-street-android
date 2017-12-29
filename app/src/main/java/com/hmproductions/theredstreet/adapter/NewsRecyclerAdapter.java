package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.MyViewHolder>{

    private Context context;
    private List<NewsDetails> newsList = new ArrayList<>();

    public NewsRecyclerAdapter(Context context, List<NewsDetails> newsList) {
        this.newsList = newsList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.news_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.headlinesTextView.setText(newsList.get(position).getHeadlines());

        if (newsList.get(position).getContent() != null) {
            holder.contentTextView.setText(newsList.get(position).getContent());
            holder.contentTextView.setVisibility(View.VISIBLE);
        }
        else
            holder.contentTextView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (newsList == null || newsList.size() == 0) return 0;
        return newsList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView headlinesTextView, contentTextView;

        MyViewHolder(View view) {
            super(view);
            headlinesTextView= view.findViewById(R.id.news_head);
            contentTextView= view.findViewById(R.id.news_content);
        }
    }
}
