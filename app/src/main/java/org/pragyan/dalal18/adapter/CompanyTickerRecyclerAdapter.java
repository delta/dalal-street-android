package org.pragyan.dalal18.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.CompanyTickerDetails;
import org.pragyan.dalal18.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CompanyTickerRecyclerAdapter extends RecyclerView.Adapter<CompanyTickerRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<CompanyTickerDetails> companyTickerDetailsList;

    public CompanyTickerRecyclerAdapter(Context context, List<CompanyTickerDetails> companyTickerDetailsList) {
        this.context = context;
        this.companyTickerDetailsList = companyTickerDetailsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.company_ticker_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        position = position % companyTickerDetailsList.size();

        CompanyTickerDetails currentCompanyTickerDetails = companyTickerDetailsList.get(position);
        int downArrowResourceId = R.drawable.down_arrow;
        int upArrowResourceId = R.drawable.up_arrow;

        holder.nameTextView.setText(currentCompanyTickerDetails.getFullName());

        Picasso.with(context).load(currentCompanyTickerDetails.isUp()? upArrowResourceId : downArrowResourceId).into(holder.arrowImageView);

        String worthString = Constants.RUPEE_SYMBOL + String.valueOf(currentCompanyTickerDetails.getPreviousDayClose());
        holder.previousDayCloseTextView.setText(worthString);

        if (currentCompanyTickerDetails.getImageUrl() != null) {
            Picasso.with(context).load(currentCompanyTickerDetails.getImageUrl()).placeholder(R.drawable.loading_placeholder)
                    .error(R.raw.connection_error).into(holder.companyImageView);
        } else {
            Picasso.with(context).load(R.raw.connection_error).into(holder.companyImageView);
        }
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public void swapData(List<CompanyTickerDetails> companyTickerDetailsList) {
        this.companyTickerDetailsList = companyTickerDetailsList;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, previousDayCloseTextView;
        ImageView companyImageView, arrowImageView;

        MyViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.company_name);
            previousDayCloseTextView = view.findViewById(R.id.lastDayClose_textView);
            companyImageView = view.findViewById(R.id.company_imageView);
            arrowImageView = view.findViewById(R.id.arrow_imageView);
        }
    }
}
