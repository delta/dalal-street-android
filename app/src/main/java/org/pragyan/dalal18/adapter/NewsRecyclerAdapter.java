package org.pragyan.dalal18.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.NewsDetails;

import java.util.List;

import static org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.MyViewHolder>{

    private Context context;
    private List<NewsDetails> newsList;
    private NewsItemClickListener onNewsClickListener;

    public interface NewsItemClickListener{
        void onNewsClicked(View view, int position, View view1, View view2, View view3);
    }

    public NewsRecyclerAdapter(Context context, List<NewsDetails> newsList,NewsItemClickListener newsItemClickListener) {
        this.newsList = newsList;
        this.context = context;
        this.onNewsClickListener = newsItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.news_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        NewsDetails currentNewsDetails = newsList.get(position);

        holder.headlinesTextView.setText(currentNewsDetails.getHeadlines());
        holder.createdAtTextView.setText(parseDate(currentNewsDetails.getCreatedAt()));
        holder.contentTextView.setText(currentNewsDetails.getContent());

        ViewCompat.setTransitionName(holder.headlinesTextView, "head" + String.valueOf(position));
        ViewCompat.setTransitionName(holder.contentTextView, "content" + String.valueOf(position));
        ViewCompat.setTransitionName(holder.createdAtTextView, "created" + String.valueOf(position));

        holder.newsLayout.setOnClickListener(v -> {
            onNewsClickListener.onNewsClicked(v,position,holder.headlinesTextView, holder.contentTextView, holder.createdAtTextView);
        });
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
}