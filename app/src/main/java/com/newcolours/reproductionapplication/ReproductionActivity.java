package com.newcolours.reproductionapplication;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.yalantis.ucrop.util.FastBitmapDrawable;
import com.yalantis.ucrop.view.GestureCropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReproductionActivity extends AppCompatActivity {

    @BindView(R.id.repro_image) GestureCropImageView mImageView;
    @BindView(R.id.repro_fab) FloatingActionButton mFab;

    private static final int UI_ANIMATION_DELAY = 300;
    private static final int DEFAULT_HIDE_TIME = 2500; //If I extend thi the 'pop' is delayed

    @SuppressLint("InlinedApi")
    private final Runnable mHideSystemRunnable = new Runnable() {
        @Override
        public void run() {
        /* Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices. */
            mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowSystemRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mFab.show();
            delayedHide(DEFAULT_HIDE_TIME);

        }
    };
    private final Runnable mHideUiRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    //NOTE: I've also tried setting up the handler in onCreate with it's own background HandlerThread
    private final Handler mHideHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repro);
        ButterKnife.bind(this);
        //Note: These commented lines are an attempt I made to move UI hiding to a different thread from Glide
        //mHandlerThread = new HandlerThread("UIHandlerThread");
        //mHandlerThread.start();
        //mHideHandler = new Handler(mHandlerThread.getLooper());
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!mFab.isShown()) {
                    mShowSystemRunnable.run();
                }
                return false;
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delayedHide(DEFAULT_HIDE_TIME);
                loadImageDirect();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hide(); //if i hide it before ever loading image, the image loads at correct size
        loadImageDirect();
        delayedHide(DEFAULT_HIDE_TIME / 2); //first, briefly hint at the UI controls, if I remove it the image stays small
    }

    private void loadImageDirect() {
        Glide.clear(mImageView); //this is there because of reuse
        Glide.with(this)
             .load("https://i.reddituploads.com/95453648ca5647e08421a52f44e6bf64?fit=max&h=1536&w=1536&s=2f91a0c0db8f4b4637e38d8ee0c421ac")
             .asBitmap()
             .into(new BitmapImageViewTarget(mImageView) {
                 @Override
                 protected void setResource(Bitmap resource) {
                     view.setImageDrawable(new FastBitmapDrawable(resource));
                     mImageView.setImageToWrapCropBounds(false); //fill screen - removing has no effect
                 }
             });
    }

//    private void loadImageAlternate() {
//        //This is another approach I tried (which made no difference)
//        mImageView.post(new Runnable() {
//            @Override
//            public void run() {
//                int width  = mImageView.getMeasuredWidth();
//                int height = mImageView.getMeasuredHeight();
//                new AsyncTask<Integer, Void, Bitmap>() {
//
//                    @Override
//                    protected Bitmap doInBackground(Integer... params) {
//                        Bitmap bmp = null;
//                        try {
//                            bmp = Glide.with(ReproductionActivity.this)
//                                    .load(...)
//                                    .asBitmap()
//                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                                    .into(params[0], params[1])
//                                    .get();
//                        } catch (Exception e) {
//                            Log.e(TAG, "Failed to handle background bitmapping", e);
//                        }
//                        return bmp;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Bitmap bitmap) {
//                        super.onPostExecute(bitmap);
//                        if (bitmap != null) {
//                            mImageView.setImageBitmap(bitmap);
//                            mLoader.setVisibility(View.GONE);
//                        }
//                    }
//
//                }.execute(width, height);
//    }

    @UiThread
    private void hide() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mFab.hide();
        mHideHandler.removeCallbacks(mShowSystemRunnable); //remove  status + nav bars after a delay
        mHideHandler.postDelayed(mHideSystemRunnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a hide of the UI, cancelling previous plans
     * thus consecutive calls to this method extend the overall
     * duration the ui is visible for
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideUiRunnable); //cancel any previously scheduled calls
        mHideHandler.postDelayed(mHideUiRunnable, delayMillis);
    }

}
