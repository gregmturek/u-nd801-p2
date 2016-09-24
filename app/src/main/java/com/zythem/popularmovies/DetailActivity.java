package com.zythem.popularmovies;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailActivity extends AppCompatActivity implements Parcelable {
    String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
    }

    public DetailActivity() {
    }

    protected DetailActivity(Parcel in) {
        this.mId = in.readString();
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
