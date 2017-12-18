package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hmproductions.theredstreet.data.Orders;
import com.hmproductions.theredstreet.adapter.OrdersAdapter;
import com.hmproductions.theredstreet.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyOrders extends Fragment {

    RecyclerView orderView;
    OrdersAdapter adapter;


    ArrayList<Orders> orders;
    ArrayList<String[]> orderPrice;
    ArrayList<Integer[]> noOfOrders;



    public MyOrders() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_my_orders, container, false);
        getActivity().setTitle("My Orders");

        orderView=(RecyclerView)rootView.findViewById(R.id.orders_list);

        publish();

        return rootView;
    }

    public void prepareOrders(){
        orders=new ArrayList<>();
        orderPrice=new ArrayList<>();
        noOfOrders=new ArrayList<>();

        orders.clear();
        orderPrice.clear();
        noOfOrders.clear();
        //todo : get from service
        orderPrice.add(new String[]{"₹25", "₹24", "₹23", "₹22"});
        noOfOrders.add(new Integer[]{0,15,20,25});
        orders.add(new Orders(getActivity(),"Stoploss Order",false,25,"Intel","Completed",noOfOrders.get(noOfOrders.size()-1),orderPrice.get(orderPrice.size()-1)));

        orderPrice.add(new String[]{"₹80", "₹85", "₹90", "₹95","₹100"});
        noOfOrders.add(new Integer[]{100,150,200,250,50});
        orders.add(new Orders(getActivity(),"Market Order",true,100,"Github","Partially filled",noOfOrders.get(noOfOrders.size()-1),orderPrice.get(orderPrice.size()-1)));

        orderPrice.add(new String[]{"₹0"});
        noOfOrders.add(new Integer[]{50});
        orders.add(new Orders(getActivity(),"Limit Order",false,100,"Intel","Not filled",noOfOrders.get(noOfOrders.size()-1),orderPrice.get(orderPrice.size()-1)));

        orderPrice.add(new String[]{"₹25", "₹24", "₹23", "₹22"});
        noOfOrders.add(new Integer[]{0,15,20,25});
        orders.add(new Orders(getActivity(),"Stoploss Order",false,25,"Intel","Completed",noOfOrders.get(noOfOrders.size()-1),orderPrice.get(orderPrice.size()-1)));

        orderPrice.add(new String[]{"₹80", "₹85", "₹90", "₹95","₹100"});
        noOfOrders.add(new Integer[]{100,150,200,250,50});
        orders.add(new Orders(getActivity(),"Market Order",true,100,"Github","Partially filled",noOfOrders.get(noOfOrders.size()-1),orderPrice.get(orderPrice.size()-1)));

        orderPrice.add(new String[]{"₹0"});
        noOfOrders.add(new Integer[]{50});
        orders.add(new Orders(getActivity(),"Limit Order",false,100,"Intel","Not filled",noOfOrders.get(noOfOrders.size()-1),orderPrice.get(orderPrice.size()-1)));


    }

    public void setValues(){
        prepareOrders();
    }

    public void publish(){

        setValues();

        adapter=new OrdersAdapter(getActivity(),orders);
        orderView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        orderView.setItemAnimator(new DefaultItemAnimator());
        orderView.setAdapter(adapter);
    }

}
