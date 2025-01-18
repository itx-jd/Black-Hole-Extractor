package com.blackhole.downloader.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.blackhole.downloader.R;
import com.blackhole.downloader.constants.Constant;
import com.blackhole.downloader.utils.AnimationUtils;
import com.blackhole.downloader.utils.DialogUtils;
import com.blackhole.downloader.utils.FirebaseUtils;
import com.blackhole.downloader.utils.IntentUtils;
import com.blackhole.downloader.utils.UIUtils;
import com.blackhole.downloader.utils.VersionUtils;
import com.blackhole.downloader.utils.VideoUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.BuildConfig;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_round;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutTitle, layoutFollow;
    private TextView tv_wait;
    private int originalImageResource;
    private int hoverImageResource;

    public static boolean downloadFroze = false;

    String shareIntentText = "";

    FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initializeUI();
        handleSharedIntent();
        setupTouchListener();
    }

    private void initializeUI() {
        layoutTitle = findViewById(R.id.layout_title);
        layoutFollow = findViewById(R.id.layout_follow);
        iv_round = findViewById(R.id.iv_round);
        tv_wait = findViewById(R.id.tv_wait);
        progressBar = findViewById(R.id.progressBar);

        originalImageResource = R.drawable.bt_in_no_back;
        hoverImageResource = R.drawable.btn_no_back;
        iv_round.setImageResource(originalImageResource);

        UIUtils.delayedVisibility(layoutTitle, layoutFollow);
    }

    private void handleSharedIntent() {
        shareIntentText = IntentUtils.extractSharedText(getIntent());
        if (shareIntentText != null) {
            FirebaseUtils.logEvent(firebaseAnalytics, "shared_intent", "Shared Intent");
            handleActionDown();
        }
    }

    private void setupTouchListener() {
        iv_round.setOnTouchListener((v, event) -> {
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

    private void handleActionDown() {
        tv_wait.setText("Processing...");
        tv_wait.setVisibility(View.GONE);
        iv_round.setImageResource(hoverImageResource);
        AnimationUtils.scaleImageView(iv_round, R.dimen.image_original_width, -20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkForUpdate();
        }
    }

    private void handleActionUp() {
        iv_round.setImageResource(originalImageResource);
        AnimationUtils.scaleImageView(iv_round, R.dimen.image_original_width, +20);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkForUpdate() {
        FirebaseUtils.getAppVersion(FirebaseDatabase.getInstance().getReference("app_version"),
                new FirebaseUtils.FirebaseCallback() {
                    @Override
                    public void onSuccess(String latestVersion) {
                        if (VersionUtils.isVersionOutdated(MainActivity.this, latestVersion)) {
                            DialogUtils.showUpdateDialog(MainActivity.this, latestVersion);
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

    void startDownload() {
        FirebaseUtils.logEvent(firebaseAnalytics, "fetch_video", "Fetch Video");
        downloadFroze = true;
        VideoUtils.fetchVideoData(this, progressBar, tv_wait, shareIntentText);
    }

    public void github(View view) {
        // use url from resource string
        openUrl(getString(R.string.github_com));
    }

    public void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void twitter(View view) {
        openUrl(getString(R.string.x_com));
    }

    public void coffee(View view) {
        openUrl(getString(R.string.buymeacoffee_com));
    }

    public void getInfo(View view) {
        supported_platform_dialog();
    }

    public void supported_platform_dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_platform, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_request_button).setOnClickListener(v -> {
            openUrl(getString(R.string.platform_request_form));
            dialog.dismiss(); // Dismiss the dialog
        });

        dialog.show();

    }
}
