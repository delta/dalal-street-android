package org.pragyan.dalal18.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.CompanyTickerDetails;
import org.pragyan.dalal18.utils.Constants;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class CompanyTickerRecyclerAdapter extends RecyclerView.Adapter<CompanyTickerRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<CompanyTickerDetails> companyTickerDetailsList;
    private OnCompanyTickerClickListener listener;

    public interface OnCompanyTickerClickListener {
        void onCompanyTickerClick(View view, int position);
    }

    public CompanyTickerRecyclerAdapter(Context context, List<CompanyTickerDetails> companyTickerDetailsList, OnCompanyTickerClickListener listener) {
        this.context = context;
        this.companyTickerDetailsList = companyTickerDetailsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.company_ticker_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        position = position % companyTickerDetailsList.size();

        CompanyTickerDetails currentCompanyTickerDetails = companyTickerDetailsList.get(position);

        if (currentCompanyTickerDetails.getGivesDividend())
        holder.companyNameTextView.setText(currentCompanyTickerDetails.getFullName()+context.getResources().getString(R.string.dividendSuffix));
       else  if (currentCompanyTickerDetails.isBankrupt())
            holder.companyNameTextView.setText(currentCompanyTickerDetails.getFullName()+context.getResources().getString(R.string.bankruptSuffix));
        else{
            holder.companyNameTextView.setText(currentCompanyTickerDetails.getFullName());
        }
        new Handler().postDelayed(() -> holder.companyNameTextView.setSelected(true), 1000);

        holder.arrowImageView.setImageResource(currentCompanyTickerDetails.isUp()? R.drawable.arrow_up_green : R.drawable.arrow_down_red);


        String worthString = Constants.RUPEE_SYMBOL + String.valueOf(new DecimalFormat(Constants.PRICE_FORMAT).format(currentCompanyTickerDetails.getPreviousDayClose()));
        holder.previousDayCloseTextView.setText(worthString);

        if (currentCompanyTickerDetails.getImageUrl() != null) {
            Picasso.get().load(currentCompanyTickerDetails.getImageUrl()).placeholder(R.drawable.loading_placeholder)
                    .error(R.raw.connection_error).into(holder.companyImageView);
        } else {
            Picasso.get().load(R.raw.connection_error).into(holder.companyImageView);
        }
    }

    @Override
    public int getItemCount() {
        return (companyTickerDetailsList == null || companyTickerDetailsList.size() == 0)? 0 : companyTickerDetailsList.size();
    }

    public void swapData(List<CompanyTickerDetails> companyTickerDetailsList) {
        this.companyTickerDetailsList = companyTickerDetailsList;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView companyNameTextView, previousDayCloseTextView;
        ImageView companyImageView, arrowImageView;

        MyViewHolder(View view) {
            super(view);
            companyNameTextView = view.findViewById(R.id.companyNameTextView);
            previousDayCloseTextView = view.findViewById(R.id.lastDayClose_textView);
            companyImageView = view.findViewById(R.id.company_imageView);
            arrowImageView = view.findViewById(R.id.arrow_imageView);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onCompanyTickerClick(view, getAdapterPosition());
        }
    }
}
