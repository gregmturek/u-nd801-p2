package com.zythem.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/*
 * Created by Greg M. Turek on 1/14/2017.
 */

public class YouTubePreview extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener{
    private int mIndex;
    private int mWidth;
    private int mHeight;
    private String mKey;
    private String mVideoTitle;
    private float mTextSize;
    private View mView;
    private ViewGroup mContainer;

    public YouTubePreview(Context context) {
        super(context);
        initializeViews(context);
    }

    public YouTubePreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        String key;
        String videoTitle;
        float textSize;

        TypedArray attributeValuesArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.YouTubePreview, 0, 0);
        try {
            key = attributeValuesArray.
                    getString(R.styleable.YouTubePreview_ytp_key);
            videoTitle = attributeValuesArray.
                    getString(R.styleable.YouTubePreview_ytp_video_title);
            textSize = attributeValuesArray.
                    getDimension(R.styleable.YouTubePreview_ytp_sp, TypedValue.COMPLEX_UNIT_SP);
        } finally {
            attributeValuesArray.recycle();
        }

        initializeViews(context);

        setContent(key, videoTitle, textSize);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.youtube_preview, this, true);

        mContainer = (ViewGroup) findViewById(R.id.youtube_preview);
        ViewTreeObserver viewTreeObserver = mContainer.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
    }

    public void setContent(final String key, String videoTitle, float textSize) {
        mKey = key;
        mVideoTitle = videoTitle;
        mTextSize = textSize;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        View v = this.findViewById(R.id.youtube_preview);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(new int[] {Color.parseColor("#dddddd"), Color.parseColor("#222222")} );
        v.setBackground(gradientDrawable);
    }

    @Override
    public void onGlobalLayout() {
        mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        TextView tv = (TextView) findViewById(R.id.youtube_preview_tv_movie_title);
        if (mTextSize > 0) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        tv.setText(mVideoTitle);

        int ivImageWidth = mView.getMeasuredWidth() - (int) getResources().getDimension(R.dimen.ytp_padding) * 2;
        int ivImageHeight = mView.getMeasuredHeight() - (int) getResources().getDimension(R.dimen.ytp_padding) * 2;
        if (ivImageHeight < Math.round(ivImageWidth * 3 / 4)) {
            ivImageHeight = Math.round(ivImageWidth * 3 / 4);
        }
        if (ivImageWidth < Math.round(ivImageHeight * 4 / 3)) {
            ivImageWidth = Math.round(ivImageHeight * 4 / 3);
        }

        mWidth = ivImageWidth;
        mHeight = ivImageHeight;

        ImageView ivThumb = (ImageView) findViewById(R.id.youtube_preview_iv_thumb);
        ivThumb.getLayoutParams().width = mWidth;
        ivThumb.getLayoutParams().height = mHeight;
        Picasso.with(getContext())
                .load("http://img.youtube.com/vi/" + mKey + "/0.jpg")
                .noFade()
                .into(ivThumb);

        View frameView = new FrameView(getContext());
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        frameView.draw(canvas);
        ImageView ivThumbOverlay = (ImageView) findViewById(R.id.youtube_preview_iv_thumb_overlay);
        ivThumbOverlay.getLayoutParams().width = mWidth;
        ivThumbOverlay.getLayoutParams().height = mHeight;
        ivThumbOverlay.setImageBitmap(bitmap);

        mWidth = ivImageWidth / 3;
        mHeight = ivImageHeight / 3;

        final ImageView iv1 = (ImageView) findViewById(R.id.youtube_preview_iv_1);
        iv1.getLayoutParams().width = mWidth;
        iv1.getLayoutParams().height = mHeight;
        Picasso.with(getContext())
                .load("http://img.youtube.com/vi/" + mKey + "/1.jpg")
                .noFade()
                .into(iv1);

        final ImageView iv3 = (ImageView) findViewById(R.id.youtube_preview_iv_3);
        iv3.getLayoutParams().width = mWidth;
        iv3.getLayoutParams().height = mHeight;
        Picasso.with(getContext())
                .load("http://img.youtube.com/vi/" + mKey + "/3.jpg")
                .noFade()
                .into(iv3);

        final ImageView iv2 = (ImageView) findViewById(R.id.youtube_preview_iv_2);
        iv2.getLayoutParams().width = mWidth;
        iv2.getLayoutParams().height = mHeight;
        Picasso.with(getContext())
                .load("http://img.youtube.com/vi/" + mKey + "/2.jpg")
                .noFade()
                .into(iv2);

        iv1.setVisibility(View.VISIBLE);
        iv2.setVisibility(View.INVISIBLE);
        iv3.setVisibility(View.INVISIBLE);

        View frameView2 = new FrameView(getContext());
        Bitmap bitmap2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap2);
        frameView2.draw(canvas2);
        ImageView ivThumbOverlay2 = (ImageView) findViewById(R.id.youtube_preview_iv_small_thumb_overlay);
        ivThumbOverlay2.getLayoutParams().width = mWidth;
        ivThumbOverlay2.getLayoutParams().height = mHeight;
        ivThumbOverlay2.setImageBitmap(bitmap2);

        mWidth = mHeight = ivImageWidth / 4;

        Button previousButton;
        Button nextButton;
        Button youtubeButton;

        youtubeButton = (Button) findViewById(R.id.youtube_preview_button_youtube);
        youtubeButton.getLayoutParams().width = mWidth;
        youtubeButton.getLayoutParams().height = mHeight;
        youtubeButton.setBackgroundResource(R.drawable.youtube);

        mWidth = mHeight = ivImageWidth / 9;

        previousButton = (Button) findViewById(R.id.youtube_preview_button_previous);
        previousButton.getLayoutParams().width = mWidth;
        previousButton.getLayoutParams().height = mHeight;
        previousButton.setBackgroundResource(R.drawable.previous);

        nextButton = (Button) findViewById(R.id.youtube_preview_button_next);
        nextButton.getLayoutParams().width = mWidth;
        nextButton.getLayoutParams().height = mHeight;
        nextButton.setBackgroundResource(R.drawable.next);

        mIndex = 1;

        previousButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                switch (mIndex) {
                    case 2:
                        mIndex = 1;
                        iv1.setVisibility(View.VISIBLE);
                        iv2.setVisibility(View.INVISIBLE);
                        break;
                    case 3:
                        mIndex = 2;
                        iv2.setVisibility(View.VISIBLE);
                        iv3.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });

        nextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                switch (mIndex) {
                    case 1:
                        mIndex = 2;
                        iv2.setVisibility(View.VISIBLE);
                        iv1.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        mIndex = 3;
                        iv3.setVisibility(View.VISIBLE);
                        iv2.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });

        youtubeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                youTube(mKey);
            }
        });

    }

    public class FrameView extends View {
        Paint mPaint = new Paint();

        public FrameView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.parseColor("#000000"));
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.ytp_stroke_width));
            canvas.drawRect(0, 0, mWidth-1, mHeight-1, mPaint);
        }
    }

    private void youTube(String key) {
        Uri uri = Uri.parse("vnd.youtube:" + key);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        getContext().startActivity(intent);
    }
}
