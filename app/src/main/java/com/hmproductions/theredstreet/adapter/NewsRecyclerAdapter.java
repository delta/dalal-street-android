package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.theredstreet.data.NewsDetails;
import com.hmproductions.theredstreet.R;

import java.util.List;

/**
 * Created by ravi on 30/1/17.
 */

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.MyViewHolder>{

    private Context context;
    private List<NewsDetails> newsList;

    public NewsRecyclerAdapter(Context context, List<NewsDetails> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        NewsDetails newsDetails=newsList.get(newsList.size()-position-1);

        holder.headlines.setText(newsDetails.getHeadlines());
        holder.content.setText(newsDetails.getContent());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView headlines,content;

        public MyViewHolder(View view) {
            super(view);
            headlines=(TextView)view.findViewById(R.id.news_head);
            content=(TextView)view.findViewById(R.id.news_content);
        }
    }
}
