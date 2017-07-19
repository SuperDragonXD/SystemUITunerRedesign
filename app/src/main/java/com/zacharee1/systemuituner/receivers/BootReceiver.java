package com.zacharee1.systemuituner.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.zacharee1.systemuituner.services.ShutdownService;
import com.zacharee1.systemuituner.utils.SettingsUtils;

public class BootReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (action != null &&
                sharedPreferences.getBoolean("safe_mode", false) && (
                action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(Intent.ACTION_REBOOT) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                action.equals("com.htc.intent.action.QUICKBOOT_POWERON")
        )) {
            String backupBL = Settings.Global.getString(context.getContentResolver(), "icon_blacklist_backup");
            SettingsUtils.writeSecure(context, "icon_blacklist", backupBL);
            SettingsUtils.writeGlobal(context, "system_booted", "1");

            context.startService(new Intent(context, ShutdownService.class));
        }
    }
}
