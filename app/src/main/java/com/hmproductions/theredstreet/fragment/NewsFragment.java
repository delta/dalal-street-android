package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.data.NewsDetails;
import com.hmproductions.theredstreet.R;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    RecyclerView recyclerView;
    NewsRecyclerAdapter adapter;
    List<NewsDetails> newsList;


    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView=inflater.inflate(R.layout.fragment_news, container, false);

        getActivity().setTitle("NewsFragment");

        recyclerView=(RecyclerView)rootView.findViewById(R.id.news_view);

        publish();

        return rootView;
    }

    public void prepareNews(){
        newsList=new ArrayList<>();  //todo : get from service
        newsList.clear();

        newsList.add(new NewsDetails("aaaaa","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")); //headlines,article
        newsList.add(new NewsDetails("bbbbb","bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));
        newsList.add(new NewsDetails("ccccc","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("ddddd","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("eeeee","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("fffff","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("ggggg","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("hhhhh","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("iiiii","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        newsList.add(new NewsDetails("jjjjj","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    public void setValues(){

        prepareNews();

    }

    public void publish(){
        setValues();

        adapter=new NewsRecyclerAdapter(newsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }

}
