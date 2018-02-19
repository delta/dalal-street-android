package org.pragyan.dalal18.ui;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.data.NewsDetails;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate;

public class NewsDetailsActivity extends AppCompatActivity {

    public static final String NEWS_DETAILS_KEY = "news-details-key";

    @BindView(R.id.news_details_toolbar)
    Toolbar newsDetailsToolbar;

    @BindView(R.id.news_details_image)
    ImageView newsDetailsImage;

    @BindView(R.id.news_details_head)
    TextView newsDetailsHead;

    @BindView(R.id.news_details_content)
    TextView newsDetailsContent;

    @BindView(R.id.news_details_created_at)
    TextView newsDetailsCreatedAt;

    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        ButterKnife.bind(this);

        Intent i = getIntent();
        NewsDetails newsDetails = i.getParcelableExtra(NEWS_DETAILS_KEY);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null);
        ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_fresh_news);
        loadingDialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create();
        loadingDialog.show();

        setSupportActionBar(newsDetailsToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(getString(R.string.news_details));
        }

        newsDetailsHead.setText(newsDetails.getHeadlines());
        newsDetailsContent.setText(newsDetails.getContent());
        newsDetailsCreatedAt.setText(parseDate(newsDetails.getCreatedAt()));

        Picasso.with(this).load("https://dalal.pragyan.org/public/src/images/news/" + newsDetails.getImagePath()).
                into(newsDetailsImage);

        loadingDialog.dismiss();
        newsDetailsCreatedAt.setVisibility(View.VISIBLE);
        newsDetailsContent.setVisibility(View.VISIBLE);
        newsDetailsCreatedAt.setVisibility(View.VISIBLE);
        newsDetailsHead.setVisibility(View.VISIBLE);
        newsDetailsImage.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
