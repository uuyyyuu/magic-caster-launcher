package org.magiccaster.research.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.wbdl.elder.ui.HomeActivity;
import java.io.Serializable;
import java.security.MessageDigest;

/** A separate, permission-free launcher. No shell, root, private APIs or networking. */
public final class LauncherActivity extends Activity {
    private static final String TARGET = "com.wb.goog.magiccasterwand";
    private static final String ROUTING = "com.wbdl.elder.ui.splash.RoutingActivity";
    private static final String EXTRA = "extra_skip_to_home_with_destination";
    private static final String ORIGINAL_SIGNER =
            "e463d4d1c36dfd68e8b72882b862568420aafa3bee0798be4e08c87b1f552e42";
    private TextView status;
    private Button launch;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(16, 27, 49));
        getWindow().setNavigationBarColor(Color.rgb(16, 27, 49));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(16, 27, 49));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(28), dp(24), dp(28));
        scroll.addView(layout);
        // Accommodate Android 15 edge-to-edge and older devices without overlap.
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        addText(layout, "Caster Launcher", 28, Color.WHITE);
        addText(layout, "Unofficial · original Magic Caster 1.7.1 only", 15, Color.LTGRAY);
        status = addText(layout, "Checking installed app…", 17, Color.rgb(185, 182, 255));
        launch = new Button(this);
        launch.setText("Open Devices");
        launch.setAllCaps(false);
        launch.setTextSize(18);
        launch.setOnClickListener(view -> openDevices());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(58));
        buttonParams.setMargins(0, dp(12), 0, dp(18));
        layout.addView(launch, buttonParams);
        addText(layout, "If ‘Lost your way?’ still appears, select Devices, then tap Try again once.",
                16, Color.WHITE);
        addText(layout, "Then: Profile → XP bar → Current Progress → a spell.", 16, Color.WHITE);
        addText(layout, "This opens a fresh app screen stack. It does not replace the original app, "
                + "clear storage, or restore missing sounds/videos. Normal app actions can still save data.",
                14, Color.LTGRAY);
        addText(layout, "No root, ADB, proxy or special permissions are needed to use this launcher. "
                + "Keep your existing app and its data intact. Other Android versions still need testing.",
                14, Color.LTGRAY);
        setContentView(scroll);
    }

    @Override public void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        String error = compatibilityError();
        launch.setEnabled(error == null);
        status.setText(error == null ? "Original 1.7.1 detected · ready" : error);
    }

    private String compatibilityError() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(TARGET, PackageManager.GET_SIGNING_CERTIFICATES);
            if (info.getLongVersionCode() != 957 || !"1.7.1".equals(info.versionName)) {
                return "Unsupported app version: " + info.versionName
                        + ". This launcher is only for 1.7.1 (957). Do not replace your existing installation.";
            }
            Signature[] signers = info.signingInfo == null ? null : info.signingInfo.getApkContentsSigners();
            if (signers == null || signers.length != 1
                    || !ORIGINAL_SIGNER.equals(sha256(signers[0].toByteArray()))) {
                return "The installed app does not match the original signing certificate. No changes made.";
            }
            ActivityInfo routing = pm.getActivityInfo(new ComponentName(TARGET, ROUTING), 0);
            if (!routing.exported || !routing.enabled || !routing.applicationInfo.enabled
                    || routing.permission != null) {
                return "The original navigation entry is not available to this launcher. No changes made.";
            }
            return null;
        } catch (PackageManager.NameNotFoundException error) {
            return "Original Magic Caster 1.7.1 was not found in this Android profile. Keep any surviving installation intact.";
        } catch (Exception error) {
            return "Unable to check the installed app: " + error.getClass().getSimpleName();
        }
    }

    private void openDevices() {
        String error = compatibilityError();
        if (error != null) {
            status.setText(error);
            launch.setEnabled(false);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(TARGET, ROUTING));
        intent.putExtra(EXTRA, (Serializable) HomeActivity.c.Devices);
        // Close the old activity stack, NOT storage/data. Ensures RoutingActivity
        // receives onCreate instead of reusing a stuck startup screen.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            Log.i("CasterLauncher", "Requesting Devices from app uid=" + android.os.Process.myUid());
            startActivity(intent);
            finish();
        } catch (RuntimeException failure) {
            status.setText("Android blocked the launch: " + failure.getClass().getSimpleName()
                    + ". Stop here; do not clear data or reinstall the original app.");
        }
    }

    private static String sha256(byte[] value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
        StringBuilder text = new StringBuilder();
        for (byte b : digest) text.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        return text.toString();
    }

    private int dp(int pixels) { return Math.round(pixels * getResources().getDisplayMetrics().density); }

    private TextView addText(LinearLayout layout, String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(18));
        layout.addView(view, params);
        return view;
    }
}
