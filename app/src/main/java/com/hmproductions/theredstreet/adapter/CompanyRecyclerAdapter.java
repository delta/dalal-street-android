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
        View itemView = LayoutInflater.from(context).inflate(R.layout.company_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Company currentCompany = companyList.get(position);

        holder.name.setText(currentCompany.getName());
        Picasso.with(context).load(currentCompany.getImage()).into(holder.company_image); //todo : change load to loadfromurl
        Picasso.with(context).load(currentCompany.getStatus()).into(holder.status);

        String worthString = "₹" + currentCompany.getValue();
        holder.worth.setText(worthString);
    }

    @Override
    public int getItemCount() {
        if (companyList == null || companyList.size() == 0) return 0;
        return companyList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, worth;
        ImageView company_image, status;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.company_name);
            worth = view.findViewById(R.id.company_value);
            company_image = view.findViewById(R.id.company_image);
            status = view.findViewById(R.id.company_status);
        }
    }
}
