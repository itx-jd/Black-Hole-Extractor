package com.blackhole.downloader.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import com.blackhole.downloader.R;
import com.blackhole.downloader.constants.Constant;

public class DialogUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showUpdateDialog(Context context, String latestVersion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        builder.setView(dialogView).setCancelable(false);

        dialogView.findViewById(R.id.dialog_update_button).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.getPackageManager().canRequestPackageInstalls()) {
                    Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(permissionIntent);
                    return;
                }
            }
            Constant.downloadAPK(context, context.getString(R.string.latest_apk_url) + latestVersion + ".apk",
                    "BlackHole++ v" + latestVersion);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
