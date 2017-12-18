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
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.company_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Company company=companyList.get(position);
        holder.name.setText(company.getCompany_name());
        holder.worth.setText("₹"+company.getCompany_value());


        Picasso.with(context).load(company.getCompany_image()).into(holder.company_image); //todo : change load to loadfromurl
        Picasso.with(context).load(company.getCompany_status()).into(holder.status);

    }

    @Override
    public int getItemCount() {
        return companyList.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name,worth;
        public ImageView company_image,status;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.company_name);
            worth = (TextView) view.findViewById(R.id.company_value);
            company_image = (ImageView) view.findViewById(R.id.company_image);
            status = (ImageView) view.findViewById(R.id.company_status);
        }
    }


}
