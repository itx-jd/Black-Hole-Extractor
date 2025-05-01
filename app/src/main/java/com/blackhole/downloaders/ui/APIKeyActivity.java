package com.blackhole.downloaders.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blackhole.downloaders.R;
import com.blackhole.downloaders.utils.AppUtils;
import com.blackhole.downloaders.utils.SharedPrefsUtil;
import com.google.android.material.textfield.TextInputEditText;

public class APIKeyActivity extends AppCompatActivity {

    private TextInputEditText etApiKey;
    private Button btnSaveApiKey;
    private WebView webViewTutorial;
    private TextView tvContactSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apikey);

        // Initialize views
        etApiKey = findViewById(R.id.et_api_key);
        btnSaveApiKey = findViewById(R.id.btn_save_api_key);
        webViewTutorial = findViewById(R.id.webview_tutorial);
        tvContactSupport = findViewById(R.id.tv_contact_support);

        // Setup WebView
        setupWebView();

        // Check if API key exists
        if (SharedPrefsUtil.isApiKeyPresent(this)) {
            etApiKey.setText(SharedPrefsUtil.getApiKey(this));
        }

        // Save button click listener
        btnSaveApiKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = etApiKey.getText().toString().trim();

                if (apiKey.isEmpty()) {
                    Toast.makeText(APIKeyActivity.this,
                            "Please enter an API key", Toast.LENGTH_SHORT).show();
                    return;
                }

                AppUtils.RAPID_API_KEY = apiKey;

                // Save API key
                SharedPrefsUtil.saveApiKey(APIKeyActivity.this, apiKey);
                Toast.makeText(APIKeyActivity.this,
                        "API key saved successfully", Toast.LENGTH_SHORT).show();

                // Optionally navigate to next screen
                 startActivity(new Intent(APIKeyActivity.this, MainActivity.class));
            }
        });

        // Contact support click listener
        tvContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open support email
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:jawad2k01@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Black Hole Extractor Support");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(APIKeyActivity.this,
                            "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setupWebView() {

        String video ="<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/FvOzDM7_V2g?si=_8ileuTW_PYYBkcT\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>";
        webViewTutorial.loadData(video, "text/html","utf-8");
        webViewTutorial.getSettings().setJavaScriptEnabled(true);
        webViewTutorial.setWebChromeClient(new WebChromeClient());
    }
}