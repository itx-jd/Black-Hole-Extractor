package com.blackhole.downloaders.ui;

import static com.blackhole.downloaders.utils.IntentUtils.openUrlInBrowser;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.blackhole.downloaders.R;
import com.blackhole.downloaders.utils.AnimationUtils;
import com.blackhole.downloaders.utils.AppUtils;
import com.blackhole.downloaders.utils.ClipboardUtils;
import com.blackhole.downloaders.utils.DialogUtils;
import com.blackhole.downloaders.utils.FirebaseApiManager;
import com.blackhole.downloaders.utils.FirebaseUtils;
import com.blackhole.downloaders.utils.IntentUtils;
import com.blackhole.downloaders.utils.PermissionUtils;
import com.blackhole.downloaders.utils.UIUtils;
import com.blackhole.downloaders.utils.VideoUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class MainActivity extends AppCompatActivity implements IUnityAdsInitializationListener {

    private ImageView ivRound, ivInfo;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutTitle, layoutFollow;
    private TextView tvWait;

    private int originalImageResource = R.drawable.bt_in_no_back;
    private int hoverImageResource = R.drawable.btn_no_back;

    private String videoURL = "";
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseApiManager firebaseApiManager;

    private static final int PERMISSION_REQUEST_CODE = 123;
    public static boolean downloadFroze = false;
    private boolean isInterstitialLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.one_signal_app_id));

        UnityAds.initialize(MainActivity.this,getString(R.string.unity_game_id),false,this);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseApiManager = FirebaseApiManager.getInstance();

        // Load API keys when app starts
        loadApiKeys();

        initializeUI();
        handleSharedIntent();
        setupTouchListener();
        requestPermissionsIfNeeded();
    }

    private void initializeUI() {
        layoutTitle = findViewById(R.id.layout_title);
        layoutFollow = findViewById(R.id.layout_follow);
        ivRound = findViewById(R.id.iv_round);
        tvWait = findViewById(R.id.tv_wait);
        progressBar = findViewById(R.id.progressBar);
        ivInfo = findViewById(R.id.iv_info);

        ivRound.setImageResource(originalImageResource);
        UIUtils.delayedVisibility(layoutTitle, layoutFollow, ivInfo);
    }

    private void loadApiKeys() {
        firebaseApiManager.fetchApiKeys(new FirebaseApiManager.ApiKeyCallback() {
            @Override
            public void onApiKeysLoaded(String rapid_api,String terabox_api) {
                Log.d("MainActivity", "API keys loaded successfully");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this,
                        "Failed to load API keys: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSharedIntent() {
        String sharedURL = IntentUtils.extractSharedText(getIntent());
        if (sharedURL != null && !sharedURL.isEmpty()) {
            videoURL = sharedURL;
            if (!downloadFroze) { // Only proceed if not already downloading
                handleActionDown();
            }
            getIntent().removeExtra(Intent.EXTRA_TEXT); // Clear the intent to prevent reprocessing
        }
    }

    private void setupTouchListener() {
        ivRound.setOnTouchListener((v, event) -> {
            if (!downloadFroze) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        videoURL = "";
                        handleActionDown();
                        break;
                    case MotionEvent.ACTION_UP:
                        handleActionUp();
                        break;
                }
            }
            return true;
        });
    }

    private void requestPermissionsIfNeeded() {
        if (!PermissionUtils.hasRequiredPermissions(this)) {
            String[] permissions = getRequiredPermissions();
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return new String[]{Manifest.permission.POST_NOTIFICATIONS};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6-9
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        return new String[0];
    }

    private void handleActionDown() {

        tvWait.setText("Please wait...");

        if (PermissionUtils.hasRequiredPermissions(this)) {
            startAction();
        } else {
            requestPermissionsIfNeeded();
        }
    }

    private void startAction() {
        tvWait.setVisibility(View.GONE);
        ivRound.setImageResource(hoverImageResource);
        AnimationUtils.scaleImageView(ivRound, R.dimen.image_original_width, -20);

        if(AppUtils.isInternetAvailable()){
            checkForUpdate();
        }else{
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            videoURL = "";
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (PermissionUtils.hasRequiredPermissions(this)) {
                startAction();
            } else {
                Toast.makeText(this, "Permissions required for downloading to DCIM.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleActionUp() {
        ivRound.setImageResource(originalImageResource);
        AnimationUtils.scaleImageView(ivRound, R.dimen.image_original_width, +20);
    }

    private void checkForUpdate() {
        FirebaseUtils.getAppVersion(FirebaseDatabase.getInstance().getReference("app_version"),
                new FirebaseUtils.FirebaseCallback() {
                    @Override
                    public void onSuccess(String latestVersion) {
                        if (AppUtils.isVersionOutdated(MainActivity.this, latestVersion)) {
                            DialogUtils.showUpdateDialog(MainActivity.this);
                        } else {
                            preStartDownload();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        preStartDownload();
                    }
                });
    }

    boolean checkURLValidity(){

        if(videoURL.isEmpty() && ClipboardUtils.getClipBoardLink(this).isEmpty()){
            return  false;
        }

        if(!videoURL.isEmpty()){
            if(!ClipboardUtils.isURLInClipboard(videoURL)){
                return false;
            }
        }

        if(! ClipboardUtils.getClipBoardLink(this).isEmpty()){
            if(!ClipboardUtils.isURLInClipboard( ClipboardUtils.getClipBoardLink(this))){
                return false;
            }
        }

        return true;

    }

    String getVideoUrl(){
        if(!videoURL.isEmpty()){
            return videoURL;
        }else{
            return ClipboardUtils.getClipBoardLink(this);
        }
    }

    void showInterstitialAd(){
        UnityAds.show(MainActivity.this, "Interstitial_Android", new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.e("UnityAds", "Interstitial ad show failed: " + message);
                startDownload();
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.d("UnityAds", "Interstitial ad started");
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                Log.d("UnityAds", "Interstitial ad clicked");
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                Log.d("UnityAds", "Interstitial ad completed with state: " + state);
                startDownload();
            }
        });
    }

    private void preStartDownload() {

        // Check For URL Validity

        if(checkURLValidity()){
            videoURL = getVideoUrl();
        }else{
            Toast.makeText(this, "Copy Video URL First", Toast.LENGTH_SHORT).show();
            videoURL = "";
            return;
        }

//      Check if the interstitial ad is loaded

        if (isInterstitialLoaded) {
            showInterstitialAd();
        } else {
            startDownload();
        }
//        startDownload();
    }

    void startDownload(){
        downloadFroze = true;
        VideoUtils.fetchVideoData(MainActivity.this, progressBar, tvWait, videoURL);
        videoURL = "";
    }

    public void github(View view) {
        openUrlInBrowser(this, getString(R.string.github_com));
    }

    public void twitter(View view) {
        openUrlInBrowser(this, getString(R.string.x_com));
    }

    public void telegram(View view) {
        openUrlInBrowser(this, getString(R.string.telegram_com));
    }

    public void showInfoDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_platform, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_request_button).setOnClickListener(v -> {
            openUrlInBrowser(this, getString(R.string.platform_request_form));
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSharedIntent();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onInitializationComplete() {
        loadInterstitialAd();
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Toast.makeText(this, "Unity Initialized Failed", Toast.LENGTH_SHORT).show();

    }

    private void loadInterstitialAd() {

        isInterstitialLoaded = false;

        if (UnityAds.isInitialized()) {
            UnityAds.load(getString(R.string.unity_interstitial_id), new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    Log.d("UnityAds", "Interstitial ad loaded for placement: " + placementId);
                    isInterstitialLoaded = true;
                }

                @Override
                public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                    Log.e("UnityAds", "Interstitial ad failed to load: " + message);
                }
            });
        } else {
            Log.e("UnityAds", "Unity Ads not initialized yet");
        }
    }
}