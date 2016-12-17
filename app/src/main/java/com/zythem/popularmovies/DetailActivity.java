package com.zythem.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.parceler.Parcels;

import static com.zythem.popularmovies.R.layout.activity_detail;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_detail);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            final MovieDataToPass movieInfo = Parcels.unwrap(getIntent().getParcelableExtra("THE_DATA"));
            Bundle bundle = new Bundle();
            bundle.putParcelable("THE_DATA", Parcels.wrap(movieInfo));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, DetailFragment.newInstance(bundle, false))
                    .commit();
        }
    }
}



