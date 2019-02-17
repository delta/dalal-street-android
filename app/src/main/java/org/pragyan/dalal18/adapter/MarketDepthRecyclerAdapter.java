package org.pragyan.dalal18.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.MarketDepth;
import org.pragyan.dalal18.utils.Constants;

import java.text.DecimalFormat;
import java.util.List;

public class MarketDepthRecyclerAdapter extends RecyclerView.Adapter<MarketDepthRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<MarketDepth> marketDepthList;

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
        long price = marketDepthList.get(position).getPrice();
        if(price == 0L ||  price == Long.MAX_VALUE){
            holder.price.setText("M.O");
            holder.price.setTextColor(ContextCompat.getColor(context, R.color.neon_blue));
            holder.volume.setTextColor(ContextCompat.getColor(context, R.color.neon_blue));
        }else {
            holder.price.setText(fromHtml(String.valueOf(new DecimalFormat(Constants.PRICE_FORMAT).format(price))));
        }
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

        TextView price, volume;

        MyViewHolder(View view) {
            super(view);
            price = view.findViewById(R.id.market_depth_price);
            volume = view.findViewById(R.id.market_depth_volume);

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
