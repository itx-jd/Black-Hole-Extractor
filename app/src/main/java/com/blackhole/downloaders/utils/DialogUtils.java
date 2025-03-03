package com.blackhole.downloaders.utils;

import static com.blackhole.downloaders.utils.IntentUtils.openUrlInBrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import com.blackhole.downloaders.R;

public class DialogUtils {

    public static void showUpdateDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        builder.setView(dialogView).setCancelable(false);

        dialogView.findViewById(R.id.dialog_update_button).setOnClickListener(v -> {
            openUrlInBrowser(context, context.getString(R.string.github_com));
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static void showFailedDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_failed, null);
        builder.setView(dialogView).setCancelable(false);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_update_button).setOnClickListener(v -> {
            AppUtils.restartApp(context);
            dialog.dismiss();
        });
        dialog.show();
    }
}
