package com.zythem.popularmovies;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.zythem.popularmovies.R.layout.activity_detail;

public class DetailActivity extends AppCompatActivity implements Parcelable {
    String mTitle;
    String mImagepath;
    String mDate;
    String mRating;
    String mId;
    String mOverview;
    String mImagepath2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int divisor;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            divisor = 2;
        }
        else{
            divisor = 1;
        }

        Configuration config = getResources().getConfiguration();
        int screenHeightDp = config.screenHeightDp;

        int appbarImageHeight = screenHeightDp / divisor;

        appbarImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, appbarImageHeight, getResources().getDisplayMetrics());

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.app_bar);
        appbar.getLayoutParams().height = appbarImageHeight;

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.mId.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DetailActivity movieInfo = getIntent().getParcelableExtra("THE_DATA");

        setTitle(movieInfo.mTitle);

/*
        TextView titleTextView = (TextView) findViewById(R.id.action_bar_title);
        titleTextView.setShadowLayer(1, 1, 1, 0);
*/

        ImageView ivImagepath2 = (ImageView) findViewById(R.id.detail_imagepath2);
        Picasso.with(DetailActivity.this).load(movieInfo.mImagepath2).into(ivImagepath2);

        ImageView ivImagepath = (ImageView) findViewById(R.id.detail_imagepath);
        float marginValue = getResources().getDimension(R.dimen.normal_layout_margin) / getResources().getDisplayMetrics().density;
        int ivImagepathHeight = (screenHeightDp / divisor) - (Math.round(marginValue) * 2);
        ivImagepathHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, ivImagepathHeight, getResources().getDisplayMetrics());
        if(divisor == 1) {
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{R.attr.actionBarSize}
            );
            int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            ivImagepathHeight -= actionBarHeight;
        }
        ivImagepath.getLayoutParams().width = (int) Math.round(ivImagepathHeight / 1.5);
        ivImagepath.getLayoutParams().height = ivImagepathHeight;
        Picasso.with(DetailActivity.this).load(movieInfo.mImagepath).into(ivImagepath);

        TextView tvDate = (TextView) findViewById(R.id.detail_date);
        tvDate.setText(movieInfo.mDate);

        TextView tvRating = (TextView) findViewById(R.id.detail_rating);
        tvRating.setText(movieInfo.mRating);

        TextView tvOverview = (TextView) findViewById(R.id.detail_overview);
        tvOverview.setText(movieInfo.mOverview);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mImagepath);
        dest.writeString(this.mDate);
        dest.writeString(this.mRating);
        dest.writeString(this.mId);
        dest.writeString(this.mOverview);
        dest.writeString(this.mImagepath2);
    }

    public DetailActivity() {
    }

    protected DetailActivity(Parcel in) {
        this.mTitle = in.readString();
        this.mImagepath = in.readString();
        this.mDate = in.readString();
        this.mRating = in.readString();
        this.mId = in.readString();
        this.mOverview = in.readString();
        this.mImagepath2 = in.readString();
    }

    public static final Parcelable.Creator<DetailActivity> CREATOR = new Parcelable.Creator<DetailActivity>() {
        @Override
        public DetailActivity createFromParcel(Parcel source) {
            return new DetailActivity(source);
        }

        @Override
        public DetailActivity[] newArray(int size) {
            return new DetailActivity[size];
        }
    };
}
