package com.aman.ferriswheel.flashlight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

public class MainActivity extends AppCompatActivity {

    Global global;
    boolean isTorchOn = false;
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
    private View relativeVwMain;
    private ColorSeekBar colorSeekBar;
    private View mControlsView;
    private final Handler mHideHandler = new Handler();

    ImageButton gridImageBtnVw;
    ImageButton rgbImageBtnVw;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar


        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            mControlsView.setVisibility(View.VISIBLE);
            relativeVwMain.setBackgroundResource(R.drawable.radial_background);
        }
    };
    private boolean mVisible;
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

    AdRequest adRequests;
    AdView mAdView;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVisible = true;
        global = new Global();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        MobileAds.initialize(this,
                "ca-app-pub-3136435737091654~7695867822");
        // Create ad request.
        adRequests = new AdRequest.Builder().build();


        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequests);
        interstitialAd = new InterstitialAd(MainActivity.this);

        interstitialAd.setAdUnitId("ca-app-pub-3136435737091654/7504296138");

        interstitialAd.loadAd(adRequests);

        mControlsView = findViewById(R.id.fullscreen_content_controls_main);
        relativeVwMain = findViewById(R.id.relLayMain);
        gridImageBtnVw = findViewById(R.id.gridButton);
        rgbImageBtnVw = findViewById(R.id.rgbButton);
        startService(new Intent(this, ShakeDetectService.class));

        colorSeekBar = findViewById(R.id.colorSlider);

        colorSeekBar.setVisibility(View.GONE);
        setSeekBar();
        relativeVwMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
                relativeVwMain.setBackgroundColor(color);
                //colorSeekBar.getAlphaValue();
            }
        });

        rgbImageBtnVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAds();
                colorSeekBar.setVisibility(View.VISIBLE);
            }
        });
        gridImageBtnVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAds();
                Intent intent = new Intent(getApplicationContext(), ScreenPlayActivity.class);
                startActivity(intent);
            }
        });
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


    public void toggle(View view) {

        ImageButton button = (ImageButton) view;
        if (button.getTag().equals("Switch Off")) {
            button.setTag("Switch On");

            button.setBackgroundResource(R.drawable.onbutton);
            torchToggle("on");
        } else {
            button.setTag("Switch Off");
            button.setBackgroundResource(R.drawable.offbutton);
            torchToggle("off");
        }
    }


    public void toggle2(View view) {
        startAds();
        Intent intent = new Intent(getApplicationContext(), SampleKotlinActivity.class);
        startActivity(intent);
    }

    private void torchToggle(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null; // Usually back camera is at 0 position.
            try {
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0];
                }
                if (camManager != null) {
                    if (command.equals("on")) {
                        camManager.setTorchMode(cameraId, true);   // Turn ON
                        isTorchOn = true;
                    } else {
                        camManager.setTorchMode(cameraId, false);  // Turn OFF
                        isTorchOn = false;
                    }
                }
            } catch (CameraAccessException e) {
                e.getMessage();
            }
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        colorSeekBar.setVisibility(View.GONE);
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
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

    public void goToSettings(View view) {

        startAds();
        startActivity(new Intent(this, SettingsActivity.class));
    }
    public void startAds(){
        interstitialAd.loadAd(adRequests);
        if(interstitialAd.isLoaded())
            interstitialAd.show();

    }
}
