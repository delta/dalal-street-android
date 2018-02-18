package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.MarketDepth;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MarketDepthRecyclerAdapter extends RecyclerView.Adapter<MarketDepthRecyclerAdapter.MyViewHolder> {

    Context context;
    List<MarketDepth> marketDepthList;

    public MarketDepthRecyclerAdapter(Context context, List<MarketDepth> marketDepthList) {
        this.context = context;
        this.marketDepthList = marketDepthList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.market_depth_list_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.price.setText(fromHtml(String.valueOf(marketDepthList.get(position).getPrice())));
        holder.volume.setText(String.valueOf(marketDepthList.get(position).getVolume()));
    }

    @Override
    public int getItemCount() {
        if (marketDepthList == null || marketDepthList.size() == 0){
            return 0;
        }
        return marketDepthList.size();

    }

    public void swapData(List<MarketDepth> list) {
        marketDepthList = list;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.market_depth_volume)
        TextView volume;

        @BindView(R.id.market_depth_price)
        TextView price;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml("&#8377;"+ html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml("&#8377;" + html);
        }
        return result;
    }
}
