package com.blackhole.downloader.ui;

import static com.blackhole.downloader.utils.IntentUtils.openUrlInBrowser;

import android.os.Build;
import android.os.Bundle;
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

import com.blackhole.downloader.R;
import com.blackhole.downloader.utils.AnimationUtils;
import com.blackhole.downloader.utils.AppUtils;
import com.blackhole.downloader.utils.DialogUtils;
import com.blackhole.downloader.utils.FirebaseUtils;
import com.blackhole.downloader.utils.IntentUtils;
import com.blackhole.downloader.utils.PermissionUtils;
import com.blackhole.downloader.utils.UIUtils;
import com.blackhole.downloader.utils.VideoUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private ImageView ivRound, ivInfo;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutTitle, layoutFollow;
    private TextView tvWait;

    private int originalImageResource = R.drawable.bt_in_no_back;
    private int hoverImageResource = R.drawable.btn_no_back;

    private String shareIntentText = "";
    private FirebaseAnalytics firebaseAnalytics;

    private static final int PERMISSION_REQUEST_CODE = 123;

    public static boolean downloadFroze = false;

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

    /**
     * Initializes UI elements and sets their default states.
     */
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

    /**
     * Handles shared text from an intent and logs it to Firebase Analytics.
     */
    private void handleSharedIntent() {
        shareIntentText = IntentUtils.extractSharedText(getIntent());
        if (shareIntentText != null) {
            handleActionDown();
        }
    }

    /**
     * Sets up the touch listener for the round button.
     */
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

    /**
     * Handles the "Action Down" event, checking permissions and starting the action.
     */
    private void handleActionDown() {
        if (PermissionUtils.isPostNotificationsPermissionGranted(this)) {
            startAction();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * The main logic for the "Action Down" event.
     */
    private void startAction() {
        tvWait.setVisibility(View.GONE);
        ivRound.setImageResource(hoverImageResource);
        AnimationUtils.scaleImageView(ivRound, R.dimen.image_original_width, -20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkForUpdate();
        }
    }

    /**
     * Handles permission request results.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.isPostNotificationsPermissionGranted(this)) {
            startAction();
        } else {
            Toast.makeText(this, "Please Provide Notification Permission", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the "Action Up" event, resetting the button state.
     */
    private void handleActionUp() {
        ivRound.setImageResource(originalImageResource);
        AnimationUtils.scaleImageView(ivRound, R.dimen.image_original_width, +20);
    }

    /**
     * Checks for app updates using Firebase and starts the download if up-to-date.
     */
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

    /**
     * Starts the video download process.
     */
    private void startDownload() {
        downloadFroze = true;
        VideoUtils.fetchVideoData(this, progressBar, tvWait, shareIntentText);
    }

    public void github(View view) {
        // use url from resource string
        openUrlInBrowser(this,getString(R.string.github_com));
    }

    public void twitter(View view) {
        openUrlInBrowser(this,getString(R.string.x_com));
    }

    public void coffee(View view) {
        openUrlInBrowser(this,getString(R.string.buymeacoffee_com));
    }


    public void showInfoDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_platform, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_request_button).setOnClickListener(v -> {
            openUrlInBrowser(this,getString(R.string.platform_request_form));
            dialog.dismiss();
        });

        dialog.show();
    }
}
