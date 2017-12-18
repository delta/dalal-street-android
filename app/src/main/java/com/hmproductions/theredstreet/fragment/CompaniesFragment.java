package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hmproductions.theredstreet.data.Company;
import com.hmproductions.theredstreet.adapter.CompanyRecyclerAdapter;
import com.hmproductions.theredstreet.R;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompaniesFragment extends Fragment {


    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ListView homeNews;
    CompanyRecyclerAdapter adapter;


    List<Company> companyList;
    ArrayList<String> news;

    Handler handler;



    public CompaniesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_companies, container, false);

        getActivity().setTitle("Home");



        recyclerView=(RecyclerView)rootView.findViewById(R.id.recycler_view);
        linearLayoutManager=new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        homeNews=(ListView)rootView.findViewById(R.id.home_news);

        publish();


       handler=new Handler();
        Runnable runnable=new Runnable() {
            int position =0;

            @Override
            public void run() {
                if(position < companyList.size()){
                    position=linearLayoutManager.findFirstVisibleItemPosition();
                    recyclerView.smoothScrollToPosition(++position);
                }
                else {
                    recyclerView.smoothScrollToPosition(0);
                    position=0;
                }
                handler.postDelayed(this,3000);
            }
        };
        handler.postDelayed(runnable,3000);



        return rootView;
    }

    public void setValues(){ //todo : get from service,checkout companyAdapter
        companyList=new ArrayList<>();
        companyList.clear();

        companyList.add(new Company("Github", String.valueOf(50),R.drawable.github2,R.drawable.down_arrow));
        companyList.add(new Company("Apple", String.valueOf(100),R.drawable.apple,R.drawable.up_arrow));
        companyList.add(new Company("Yahoo", String.valueOf(125),R.drawable.yahoo2,R.drawable.down_arrow));
        companyList.add(new Company("HDFC", String.valueOf(95),R.drawable.hdfc3,R.drawable.down_arrow));
        companyList.add(new Company("LG", String.valueOf(110),R.drawable.lg2,R.drawable.up_arrow));
        companyList.add(new Company("Sony", String.valueOf(50),R.drawable.sony,R.drawable.down_arrow));
        companyList.add(new Company("Infosys", String.valueOf(50),R.drawable.infosys,R.drawable.down_arrow));

        news=new ArrayList<>();
        news.clear();
        news.add("Github makes private repos free!");
        news.add("Apple revokes iphone 7 plus due to faulty cameras");
        news.add("Yahoo employees announce strike due to non payment of salary");
        news.add("Sony launches Xperia X conpact priced at 45,000");
        news.add("LG patents new refrigerant for its refrigerator products");

    }

    public void publish(){

     setValues();
        adapter= new CompanyRecyclerAdapter(getActivity(),companyList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,news);
        homeNews.setAdapter(arrayAdapter);

    }



}
