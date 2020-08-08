package com.aman.ferriswheel.flashlight;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ScreenPlayActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    ImageButton rgbImageBtnVw;

    int targetColor=Color.GREEN;
    private ColorSeekBar colorSeekBar;
    private LinearLayout linearLayoutMainVw;
    AdRequest adRequests;
    AdView mAdView;
    InterstitialAd interstitialAd;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            rgbImageBtnVw.setVisibility(View.VISIBLE);
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    Button backBtn;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screen_play);

        MobileAds.initialize(this,
                "ca-app-pub-3136435737091654~7695867822");
        // Create ad request.
        adRequests = new AdRequest.Builder().build();


        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequests);
        interstitialAd = new InterstitialAd(ScreenPlayActivity.this);

        interstitialAd.setAdUnitId("ca-app-pub-3136435737091654/7504296138");

        interstitialAd.loadAd(adRequests);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        linearLayoutMainVw = findViewById(R.id.linearMainVw);

        backBtn=findViewById(R.id.backBtn);
        rgbImageBtnVw = findViewById(R.id.rgbButton);

        // Set up the user interaction to manually show or hide the system UI.
        linearLayoutMainVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.backBtn).setOnTouchListener(mDelayHideTouchListener);

        myThread();
        colorSeekBar = findViewById(R.id.colorSlider);

        colorSeekBar.setVisibility(View.GONE);
        setSeekBar();
        linearLayoutMainVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
               // linearLayoutMainVw.setBackgroundColor(color);
                //colorSeekBar.getAlphaValue();
                targetColor=color;
            }
        });

        rgbImageBtnVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAds();
                colorSeekBar.setVisibility(View.VISIBLE);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    int i = 0;

    public void myThread() {
        Thread th = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(500);
                        ScreenPlayActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (i == 0) {
                                    linearLayoutMainVw.setBackgroundColor(targetColor);
                                    i = 1;
                                } else {
                                    linearLayoutMainVw.setBackgroundColor(Color.BLACK);
                                    i = 0;
                                }

                            }
                        });
                    }
                } catch (InterruptedException e) {
                    // TODO: handle exception
                }
            }
        };
        th.start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        rgbImageBtnVw.setVisibility(View.GONE);
        colorSeekBar.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        linearLayoutMainVw.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    private void setSeekBar() {
        colorSeekBar.setMaxPosition(100);
        colorSeekBar.setColorSeeds(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarPosition(10); //0 - maxValue
        colorSeekBar.setAlphaBarPosition(10); //0 - 255
        colorSeekBar.setShowAlphaBar(true);
        colorSeekBar.setBarHeight(15); //5dpi
        colorSeekBar.setThumbHeight(60); //30dpi
        colorSeekBar.setBarMargin(5); //set the margin between colorBar and alphaBar 10dpi
        colorSeekBar.setBarRadius(10);
    }
    public void startAds(){
        interstitialAd.loadAd(adRequests);
        if(interstitialAd.isLoaded())
            interstitialAd.show();

    }
}
