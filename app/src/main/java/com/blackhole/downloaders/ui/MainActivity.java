package com.blackhole.downloaders.ui;

import static com.blackhole.downloaders.utils.IntentUtils.openUrlInBrowser;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

public class MainActivity extends AppCompatActivity {

    private ImageView ivRound, ivInfo;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutTitle, layoutFollow;
    private TextView tvWait;

    private int originalImageResource = R.drawable.bt_in_no_back;
    private int hoverImageResource = R.drawable.btn_no_back;

    private String shareIntentText = "";
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseApiManager firebaseApiManager;

    private static final int PERMISSION_REQUEST_CODE = 123;
    public static boolean downloadFroze = false;

    private StartAppAd startAppAd ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.one_signal_app_id));

        startAppAd = new StartAppAd(this);
        // comment below line to disable test ads
//        StartAppSDK.setTestAdsEnabled(true);
        startAppAd.loadAd();

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
        shareIntentText = IntentUtils.extractSharedText(getIntent());
        if (shareIntentText != null && !shareIntentText.isEmpty()) {
            handleActionDown();
        }
    }

    private void setupTouchListener() {
        ivRound.setOnTouchListener((v, event) -> {
            if (!downloadFroze) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        shareIntentText = "";
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
                            startDownload();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        startDownload();
                    }
                });
    }

    private void startDownload() {

        startAppAd.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(Ad ad) {
                startAppAd.loadAd();
                downloadFroze = true;
                VideoUtils.fetchVideoData(MainActivity.this, progressBar, tvWait, shareIntentText);
            }

            @Override
            public void adDisplayed(Ad ad) {


            }

            @Override
            public void adClicked(Ad ad) {

            }

            @Override
            public void adNotDisplayed(Ad ad) {
                downloadFroze = true;
                VideoUtils.fetchVideoData(MainActivity.this, progressBar, tvWait, shareIntentText);
            }
        });

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
        startAppAd.onBackPressed();
        super.onBackPressed();
    }
}