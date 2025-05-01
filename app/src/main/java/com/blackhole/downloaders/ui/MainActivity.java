package com.blackhole.downloaders.ui;

import static com.blackhole.downloaders.utils.IntentUtils.openUrlInBrowser;

import android.Manifest;
import android.content.Intent;
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

import com.blackhole.downloaders.R;
import com.blackhole.downloaders.utils.AnimationUtils;
import com.blackhole.downloaders.utils.AppUtils;
import com.blackhole.downloaders.utils.ClipboardUtils;
import com.blackhole.downloaders.utils.DialogUtils;
import com.blackhole.downloaders.utils.IntentUtils;
import com.blackhole.downloaders.utils.PermissionUtils;
import com.blackhole.downloaders.utils.SharedPrefsUtil;
import com.blackhole.downloaders.utils.UIUtils;
import com.blackhole.downloaders.utils.VideoUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;


public class MainActivity extends AppCompatActivity{

    private ImageView ivRound, ivInfo, ivSettings;
    private LinearProgressIndicator progressBar;
    private LinearLayout layoutTitle, layoutFollow;
    private TextView tvWait;

    private int originalImageResource = R.drawable.bt_in_no_back;
    private int hoverImageResource = R.drawable.btn_no_back;

    private String videoURL = "";

    private static final int PERMISSION_REQUEST_CODE = 123;
    public static boolean downloadFroze = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        // Load API KEY
        if(SharedPrefsUtil.isApiKeyPresent(this)){
            AppUtils.RAPID_API_KEY = SharedPrefsUtil.getApiKey(this);
        }

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
        ivSettings = findViewById(R.id.ivSettings);

        ivRound.setImageResource(originalImageResource);
        UIUtils.delayedVisibility(layoutTitle, layoutFollow, ivInfo,ivSettings);
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

        checkForAPIKEY();

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

    private void checkForAPIKEY() {

        if(AppUtils.RAPID_API_KEY.isEmpty()){
            DialogUtils.showAPIDialog(MainActivity.this);
        }else{
            preStartDownload();
        }
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

    private void preStartDownload() {

        // Check For URL Validity

        if(checkURLValidity()){
            videoURL = getVideoUrl();
        }else{
            Toast.makeText(this, "Copy Video URL First", Toast.LENGTH_SHORT).show();
            videoURL = "";
            return;
        }

        startDownload();
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
        DialogUtils.showInfoDialog(this);
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

    public void navigateSettings(View view) {
        startActivity(new Intent(this, APIKeyActivity.class));
    }
}