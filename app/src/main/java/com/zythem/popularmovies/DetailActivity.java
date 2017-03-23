package com.zythem.popularmovies;

import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import org.parceler.Parcels;

import static com.zythem.popularmovies.R.layout.activity_detail;

public class DetailActivity extends AppCompatActivity {

    private NetworkChangeReceiver mReceiver;
    private boolean mIsConnected = true;

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

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new NetworkChangeReceiver();
        registerReceiver(mReceiver, filter);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            isNetworkAvailable(context);
        }

        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
                if (networkInfo != null) {
                    if (networkInfo.isConnectedOrConnecting()) {
                        if (!mIsConnected) {
                            mIsConnected = true;

                            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container);
                            if (detailFragment != null) {
                                detailFragment.reInit();
                            }
                        }
                        return true;
                    }
                }
            }
            View view = findViewById(R.id.fragment_container);
            if (view != null) {
                Snackbar.make(view, "No network connection!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            mIsConnected = false;
            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public Intent getParentActivityIntent() {
        Intent i = null;

        String parent = getIntent().getStringExtra("THE_PARENT");
        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
        if (parent != null && parent.equals("SEARCH")) {
            i = new Intent(this, SearchResultsActivity.class);
            // set any flags or extras that you need.
            // If you are reusing the previous Activity (i.e. bringing it to the top
            // without re-creating a new instance) set these flags:
            // GMT // Below is not needed because this activity is set to SINGLE_TOP in the manifest.
//            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // if you are re-using the parent Activity you may not need to set any extras
//            i.putExtra("someExtra", "whateverYouNeed");
            // GMT // Below is not needed because null defaults to what is specified in the manifest.
//        } else {
//            i = new Intent(this, MainActivity.class);
            // same comments as above
        }
        return i;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
                    // This activity is NOT part of this app's task,
                    // GMT // is the root of a task (initial launch of application
                    // GMT // or application was previously terminated through task manager),
                    // so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // GMT // This is not per guidelines but is the desired behavior.
    @Override
    public void onBackPressed() {
        String parent = getIntent().getStringExtra("THE_PARENT");
        if (parent != null && parent.equals("WIDGET")) {
            startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            super.onBackPressed();
        }
    }

}



