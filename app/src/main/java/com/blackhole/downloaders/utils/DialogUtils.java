package com.blackhole.downloaders.utils;

import static com.blackhole.downloaders.utils.IntentUtils.openUrlInBrowser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.blackhole.downloaders.ui.APIKeyActivity;
import com.blackhole.downloaders.R;

public class DialogUtils {
    public static void showFailedDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_failed, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_restart_button).setOnClickListener(v -> {
            AppUtils.restartApp(context);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.dialog_api_button).setOnClickListener(v -> {
            context.startActivity(new Intent(context, APIKeyActivity.class));
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void showAPIDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_api, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_cancel_button).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.dialog_update_button).setOnClickListener(v -> {
            context.startActivity(new Intent(context, APIKeyActivity.class));
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void showInfoDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView appVersion = dialogView.findViewById(R.id.dialog_version);

        // Get version name from package info
        try {
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            appVersion.setText("Version: V" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appVersion.setText("Version: Unknown");
        }

        dialogView.findViewById(R.id.dialog_github_button).setOnClickListener(v -> {
            openUrlInBrowser(context, context.getString(R.string.github_com));
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.dialog_sponsor_button).setOnClickListener(v -> {
            openUrlInBrowser(context, context.getString(R.string.telegram_com));
            dialog.dismiss();
        });

        dialog.show();
    }

}
