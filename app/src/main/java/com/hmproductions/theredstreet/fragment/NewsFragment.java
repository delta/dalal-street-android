package com.hmproductions.theredstreet.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hmproductions.theredstreet.R;
import com.hmproductions.theredstreet.adapter.NewsRecyclerAdapter;
import com.hmproductions.theredstreet.data.NewsDetails;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    RecyclerView newsRecyclerView;
    NewsRecyclerAdapter adapter;
    List<NewsDetails> newsList = new ArrayList<>();

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_news, container, false);

        if (getActivity() != null) getActivity().setTitle("NewsFragment");

        newsRecyclerView = rootView.findViewById(R.id.news_recyclerView);

        updateNews();

        adapter=new NewsRecyclerAdapter(getContext(), newsList);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsRecyclerView.setHasFixedSize(false);
        newsRecyclerView.setAdapter(adapter);

        return rootView;
    }

    public void updateNews(){

        //TODO : get from service
        newsList.clear();

        newsList.add(new NewsDetails("aaaaa","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
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
}