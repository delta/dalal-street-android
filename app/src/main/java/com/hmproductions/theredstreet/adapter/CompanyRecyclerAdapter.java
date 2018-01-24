package com.hmproductions.theredstreet.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.data.Company;
import com.hmproductions.theredstreet.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CompanyRecyclerAdapter extends RecyclerView.Adapter<CompanyRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Company> companyList;

    public CompanyRecyclerAdapter(Context context, List<Company> companyList) {
        this.context = context;
        this.companyList = companyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.company_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Company currentCompany = companyList.get(position);
        int downArrowResourceId = R.drawable.down_arrow;
        int upArrowResourceId = R.drawable.up_arrow;

        holder.nameTextView.setText(currentCompany.getFullName());

        Picasso.with(context).load(currentCompany.isUp()? upArrowResourceId : downArrowResourceId).into(holder.arrowImageView);

        String worthString = Constants.RUPEE_SYMBOL + String.valueOf(currentCompany.getPreviousDayClose());
        holder.previousDayCloseTextView.setText(worthString);

        if (currentCompany.getImageUrl() != null) {
            Picasso.with(context).load(currentCompany.getImageUrl()).placeholder(R.drawable.rotate_drawable)
                    .error(R.raw.connection_error).into(holder.companyImageView);
        } else {
            Picasso.with(context).load(R.raw.connection_error).into(holder.companyImageView);
        }
    }

    @Override
    public int getItemCount() {
        if (companyList == null || companyList.size() == 0) return 0;
        return companyList.size();
    }

    public void swapData(List<Company> companyList) {
        this.companyList = companyList;
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
